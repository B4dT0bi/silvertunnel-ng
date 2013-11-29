/*
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel_ng.netlib.layer.tor.common.TorX509TrustManager;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.util.PrivateKeyHandler;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * functionality for the TLS connections bridging the gap to the first nodes in
 * the routes.
 * 
 * @author Lexi Pimenidis
 * @author Vinh Pham
 * @author hapke
 */
public class TLSConnection
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TLSConnection.class);

	private static final String enabledSuitesStr = "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA";

	/** pointer to the server/router. */
	private RouterImpl router;
	/** the physical connection (if any) to the node. */
	private final NetSocket tls;
	private boolean closed = false;
	private final TLSDispatcherThread dispatcher;
	private final DataOutputStream sout;
	/** key=circuit ID, value=circuit. */
	private final Map<Integer, Circuit> circuitMap = Collections
			.synchronizedMap(new HashMap<Integer, Circuit>());

	/**
	 * creates the TLS connection and installs a dispatcher for incoming data.
	 * 
	 * @param server
	 *            the server to connect to (e.g. a Tor Onion Router)
	 * @param lowerNetLayer
	 *            build TLS connection on this lower net layer
	 * @param pkh
	 *            handler to check server certs
	 * 
	 * @see TLSDispatcherThread
	 * @exception IOException
	 * @exception SSLPeerUnverifiedException
	 */
	TLSConnection(final RouterImpl server, final NetLayer lowerNetLayer,
			final PrivateKeyHandler pkh) throws IOException,
			SSLPeerUnverifiedException, SSLException
	{
		if (server == null)
		{
			throw new IOException("TLSConnection: server variable is NULL");
		}
		this.router = server;

		// create new certificates and use them ad-hoc
		final KeyManager[] kms = new KeyManager[1];

		// TODO: Leave out the PrivateKeyHandler, should be needed for
		// server operation and hidden services only
		// kms[0] = pkh;

		// use the keys and certs from above to connect to Tor-network
		// try {
		final TrustManager[] tms = { new TorX509TrustManager() };

		// new code:
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(TLSNetLayer.ENABLES_CIPHER_SUITES, enabledSuitesStr);
		props.put(TLSNetLayer.TRUST_MANAGERS, tms);
		final NetAddress remoteAddress = new TcpipNetAddress(
				server.getHostname(), server.getOrPort());
		final NetAddress localAddress = null;
		tls = lowerNetLayer.createNetSocket(props, localAddress, remoteAddress);

		// FIXME: check certificates received in TLS
		// (note: not an important security bug, since it only affects
		// hop2hop-encryption, real
		// data is encrypted anyway on top of TLS)

		/*
		 * // for debugging purposes javax.net.ssl.HandshakeCompletedListener
		 * hscl = new javax.net.ssl.HandshakeCompletedListener() { public void
		 * handshakeCompleted(HandshakeCompletedEvent e) { try {
		 * LOG.info("Cipher: "+e.getCipherSuite());
		 * java.security.cert.Certificate[] chain = e.getLocalCertificates();
		 * LOG.info("Send cert-chain of length "+chain.length); for(int
		 * i=0;i<chain.length;++i)
		 * LOG.info(" cert "+i+": "+chain[i].toString()); chain =
		 * e.getPeerCertificates(); LOG.info("Received cert-chain of length
		 * "+chain.length); for(int i=0;i<chain.length;++i)
		 * LOG.info(" cert "+i+": "+chain[i].toString()); } catch(Exception ex)
		 * {} } }; tls.addHandshakeCompletedListener(hscl);
		 */

		// create object to write data to stream
		sout = new DataOutputStream(tls.getOutputStream());
		// start listening for incoming data
		this.dispatcher = new TLSDispatcherThread(this, new DataInputStream(
				tls.getInputStream()));
	}

	/**
	 * converts a cell to bytes and transmits it over the line. received data is
	 * dispatched by the class TLSDispatcher
	 * 
	 * @param cell
	 *            the cell to send
	 * @exception IOException
	 * @see TLSDispatcherThread
	 */
	synchronized void sendCell(final Cell cell) throws IOException
	{
		try
		{
			sout.write(cell.toByteArray());
		}
		catch (final IOException exception)
		{
			LOG.debug("error while sending data Exception : {}", exception, exception);
			// force to close the connection
			close(true);
			// rethrow error
			throw exception;
		}
	}

	/**
	 * returns a free circID and save that it points to "c", save it to "c",
	 * too. Throws an exception, if no more free IDs are available, or the TLS
	 * connection is marked as closed.<br>
	 * FIXME: replace this code with something more beautiful
	 * 
	 * @param circuit
	 *            the circuit that is going to be build through this
	 *            TLS-Connection
	 * @return an identifier for this new circuit - this must be set by the
	 *         caller as id in the Circuit
	 * @exception TorException
	 */
	synchronized int assignCircuitId(final Circuit circuit) throws TorException
	{
		if (closed)
		{
			throw new TorException(
					"TLSConnection.assignCircuitId(): Connection to "
							+ router.getNickname()
							+ " is closed for new circuits");
		}
		// find a free number (other than zero)
		int ID;
		int j = 0;
		do
		{
			if (++j > 1000)
			{
				throw new TorException(
						"TLSConnection.assignCircuitId(): no more free IDs");
			}

			// Deprecated: 16 bit unsigned Integers with MSB set
			// ID = FirstNodeHandler.rnd.nextInt() & 0xffff | 0x8000;

			// XXX: Since the PrivateKeyHandler is gone, we don't need to
			// consider
			// the MSB as long as we are in client mode (see main-tor-spec.txt,
			// Section 5.1)
			ID = TLSConnectionAdmin.rnd.nextInt() & 0xffff; // & 0x7fff;

			if (circuitMap.containsKey(Integer.valueOf(ID)))
			{
				ID = 0;
			}
		}
		while (ID == 0);
		// memorize circuit
		circuitMap.put(Integer.valueOf(ID), circuit);
		return ID;
	}

	/**
	 * marks as closed. closes if no more data or forced closed on real close:
	 * kill dispatcher
	 * 
	 * @param force
	 *            set to TRUE if established circuits shall be cut and
	 *            terminated.
	 */
	void close(final boolean force)
	{
		LOG.debug("Closing TLS to {}", router.getNickname());

		closed = true;
		// FIXME: a problem with (!force) is, that circuits, that are currently
		// still build up
		// are not killed. their build-up should be stopped
		// close circuits, if forced
		Collection<Circuit> circuits;
		synchronized (circuitMap)
		{
			circuits = new ArrayList<Circuit>(circuitMap.values());
		}
		for (final Circuit circuit : circuits)
		{
			if (circuit.close(force))
			{
				removeCircuit(circuit.getId());
			}
		}

		LOG.debug("Fast exit while closing TLS to {}?", router.getNickname());
		if (!(force || circuitMap.isEmpty()))
		{
			LOG.debug("Fast exit while closing TLS to {}!", router.getNickname());
			return;
		}

		// kill dispatcher
		LOG.debug("Closing dispatcher of TLS to {}", router.getNickname());
		dispatcher.close();

		// close TLS connection
		LOG.debug("Closing TLS connection to {}", router.getNickname());
		try
		{
			sout.close();
			tls.close();
		}
		catch (final IOException e)
		{
			LOG.debug("got IOException : {}", e.getMessage(), e);
		}
		LOG.debug("Closing TLS to {} done", router.getNickname());
	}

	@Override
	public String toString()
	{
		return "TLS to " + router.getNickname();
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public RouterImpl getRouter()
	{
		return router;
	}

	public void setRouter(final RouterImpl router)
	{
		this.router = router;
	}

	public Collection<Circuit> getCircuits()
	{
		synchronized (circuitMap)
		{
			return new ArrayList<Circuit>(circuitMap.values());
		}
	}

	public Map<Integer, Circuit> getCircuitMap()
	{
		synchronized (circuitMap)
		{
			return new HashMap<Integer, Circuit>(circuitMap);
		}
	}

	public Circuit getCircuit(final Integer circuitId)
	{
		synchronized (circuitMap)
		{
			return circuitMap.get(circuitId);
		}
	}

	/**
	 * Remove
	 * 
	 * @param circuitId
	 * @return true=removed; false=not remove/did not exist
	 */
	public boolean removeCircuit(final Integer circuitId)
	{
		LOG.debug("remove circuit with circuitId={} from {}", circuitId, toString());

		// remove Circuit
		boolean result;
		boolean doClose;
		synchronized (circuitMap)
		{
			result = circuitMap.remove(circuitId) != null;
			doClose = circuitMap.size() == 0;
		}

		// last circuit of this TLSConnection removed: connection can be closed?
		if (doClose)
		{
			// yes
			if (LOG.isDebugEnabled())
			{
				LOG.debug("close TLSConnection from {} because last Circuit is removed", toString());
			}
			close(true);
		}
		else
		{
			// no
			synchronized (circuitMap)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("cannot close TLSConnection from " + toString()
						+ " because of additional circuits: " + circuitMap);
				}
			}
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("remove circuit from " + toString() + " done with result="
				+ result);
		}
		return result;
	}

	public boolean isClosed()
	{
		return closed;
	}
}
