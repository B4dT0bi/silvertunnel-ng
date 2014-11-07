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

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.*;

/**
 * maintains the list of active TLS-connections to Tor nodes (direct connections
 * to neighbor nodes).
 * 
 * Hint: previous class name = FirstNodeHandler
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author hapke
 * @author Tobias Boese
 */
public final class TLSConnectionAdmin
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TLSConnectionAdmin.class);

	protected static final SecureRandom RANDOM = new SecureRandom();

	/** key=fingerprint, value=connection to this router */
	private final Map<Fingerprint, WeakReference<TLSConnection>> connectionMap = Collections
			.synchronizedMap(new HashMap<Fingerprint, WeakReference<TLSConnection>>());

	/**
	 * key=fingerprint, value=connection to this router. 
	 * contains all connections of all TLSConnectionAdmin instances, used by some test cases
	 */
	private static Map<Fingerprint, WeakReference<TLSConnection>> connectionMapAll = Collections
			.synchronizedMap(new HashMap<Fingerprint, WeakReference<TLSConnection>>());

	/**
	 * lower layer network layer, e.g. TLS over TCP/IP to connect to TOR onion
	 * routers
	 */
	private final NetLayer lowerTlsConnectionNetLayer;

	/**
	 * initialize Handler of TLSConnections.
	 */
	public TLSConnectionAdmin(final NetLayer lowerTlsConnectionNetLayer) throws IOException
	{
		this.lowerTlsConnectionNetLayer = lowerTlsConnectionNetLayer;
	}

	/**
	 * return a pointer to a direct TLS-connection to a certain node. if there
	 * is none, it is created and returned.
	 * 
	 * @param router
	 *            the node to connect to
	 * @return the TLS connection
	 */
	TLSConnection getConnection(final Router router) throws IOException,
																TorException
	{
		if (router == null)
		{
			throw new TorException("TLSConnectionAdmin: server is NULL");
		}
		// check if TLS-connections to node established
		WeakReference<TLSConnection> weakConn = connectionMap.get(router.getFingerprint());
		TLSConnection conn = null;
		if (weakConn != null)
		{
			conn = weakConn.get();
		}
		if (conn == null)
		{
			// not in cache: build new TLS connection
			LOG.debug("TLSConnectionAdmin: TLS connection to {}", router.getNickname());
			conn = new TLSConnection(router, lowerTlsConnectionNetLayer);
			weakConn = new WeakReference<TLSConnection>(conn);
			connectionMap.put(router.getFingerprint(), weakConn);
			connectionMapAll.put(router.getFingerprint(), weakConn);
		}
		return conn;
	}

	/**
	 * Remove TLSConnection if it was closed.
	 * 
	 * @param conn
	 */
	public void removeConnection(final TLSConnection conn)
	{
		connectionMap.remove(conn.getRouter().getFingerprint());
	}

	/**
	 * Closes all TLS connections.
	 * 
	 * This method is used by some test cases and it it not intended to make
	 * this method public.
	 */
	static void closeAllTlsConnections()
	{
		synchronized (connectionMapAll)
		{
			for (final WeakReference<TLSConnection> w : connectionMapAll.values())
			{
				final TLSConnection t = w.get();
				if (t != null)
				{
					t.close(true);
				}
			}
			connectionMapAll.clear();
		}
	}

	/**
	 * closes all TLS connections.
	 * 
	 * @param force
	 *            set to false, if circuits shall be terminated gracefully
	 */
	public void close(final boolean force)
	{
		synchronized (connectionMap)
		{
			for (final WeakReference<TLSConnection> w : connectionMap.values())
			{
				final TLSConnection t = w.get();
				if (t != null)
				{
					t.close(force);
				}
			}
			connectionMap.clear();
		}
	}
	/**
	 * Get a collection of all valid {@link TLSConnection}s.
	 * 
	 * @return a {@link Collection} with valid {@link TLSConnection}s
	 */
	public Collection<TLSConnection> getConnections()
	{
		// create new Collection to avoid concurrent modifications,
		// use the iteration to remove weak references that lost it object
		final Collection<Fingerprint> entriesToRemove = new ArrayList<Fingerprint>(connectionMap.size());
		final Collection<TLSConnection> result = new ArrayList<TLSConnection>(connectionMap.size());
		synchronized (connectionMap)
		{
			for (final Fingerprint fingerprint : connectionMap.keySet())
			{
				final WeakReference<TLSConnection> weakReference = connectionMap.get(fingerprint);
				if (weakReference != null)
				{
					final TLSConnection tlsConnection = weakReference.get();
					if (tlsConnection != null)
					{
						// valid TLSConnection found
						result.add(tlsConnection);
					}
					else
					{
						// entry with lost reference found
						entriesToRemove.add(fingerprint);
					}
				}
			}
			// cleanup (part 1)
			for (final Fingerprint f : entriesToRemove)
			{
				connectionMap.remove(f);
			}
		}
		synchronized (connectionMapAll)
		{
			// cleanup (part 2)
			for (final Fingerprint f : entriesToRemove)
			{
				connectionMapAll.remove(f);
			}
		}
		return result;
	}
}
