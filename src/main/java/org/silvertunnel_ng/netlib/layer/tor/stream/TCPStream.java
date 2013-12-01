/**
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
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2013 silvertunnel-ng.org
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

package org.silvertunnel_ng.netlib.layer.tor.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Queue;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Stream;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayBegin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayBeginDir;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayConnected;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayData;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayDrop;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayEnd;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelaySendme;
import org.silvertunnel_ng.netlib.layer.tor.clientimpl.Tor;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEvent;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.TorNoAnswerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles the features of single TCP streams on top of circuits through the tor
 * network. provides functionality to send and receive data by this streams and
 * is publicly visible.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Koelsch
 * @author Michael Koellejan
 * @author Tobias Boese
 */
public class TCPStream implements Stream, NetSocket
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TCPStream.class);

	/** used for stream level flow control. (as described in tor-spec.txt 7.4)*/
	private static final int STREAM_LEVEL_FLOW_WINDOW = 500;
	/** increment used for stream level flow control. (as described in tor-spec.txt 7.4)*/
	private static final int STREAM_LEVEL_FLOW_INCREMENT = 50;
	/** 
	 * current receive window.
	 * 
	 * How many RELAY_DATA cells did we received?
	 */
	private int streamLevelFlowControlRecv = STREAM_LEVEL_FLOW_WINDOW;
	/** 
	 * current send window.
	 * 
	 * How many RELAY_DATA cells did we send?
	 */
	private int streamLevelFlowControlSend = STREAM_LEVEL_FLOW_WINDOW;
	/** wait x seconds for answer. */
	private final int queueTimeout = TorConfig.queueTimeoutStreamBuildup;
	// TODO: do we need this?
	public static final int QUEUE_TIMEOUNT2 = 20;

	/** the {@link Circuit} which this {@link Stream} is attached to. */
	protected transient Circuit circuit;
	/** stream ID. */
	protected int streamId;
	/** receives incoming data. */
	protected Queue queue;
	private InetAddress resolvedAddress;
	// TODO: private TCPStreamProperties sp;
	private boolean established;
	private boolean closed;

	/** set by CellRelay. descriptive Strings are in CellRelay.REASON_TO_STRING */
	private int closedForReason;

	private QueueTor2JavaHandler qhT2J;
	private TCPStreamOutputStream outputStream;

	/** timestamp when this {@link Stream} was created. */
	private long created;

	/** last time, a cell was send that was not a padding cell. */
	private long lastAction;

	/** last time, a cell was send. */
	private long lastCellSentDate;

	/**
	 * creates a stream on top of a existing circuit. users and programmers
	 * should never call this function, but Tor.connect() instead.
	 * 
	 * @param circuit
	 *            the circuit to build the stream through
	 * @param sp
	 *            the host etc. to connect to
	 * @see Tor
	 * @see Circuit
	 * @see TCPStreamProperties
	 */
	public TCPStream(final Circuit circuit, final TCPStreamProperties sp) throws IOException,
																		   TorException, 
																		   TorNoAnswerException
	{
		// TODO: this.sp = sp;
		established = false;
		created = System.currentTimeMillis();
		lastAction = created;
		lastCellSentDate = created;
		// stream establishment duration
		int setupDuration;
		long startSetupTime;

		// attach stream to circuit
		this.circuit = circuit;
		circuit.assignStreamId(this);
		queue = new Queue(queueTimeout);
		closed = false;
		closedForReason = 0;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("TCPStream: building new stream " + toString());
		}

		startSetupTime = System.currentTimeMillis();
		if (sp.isConnectToDirServer())
		{
			// connected to a dir server
			sendCell(new CellRelayBeginDir(this));
		}
		else
		{
			// send RELAY-BEGIN
			sendCell(new CellRelayBegin(this, sp));
		}
		// wait for RELAY_CONNECTED
		CellRelay relay = null;
		try
		{
			LOG.debug("TCPStream: Waiting for Relay-Connected Cell...");
			relay = queue.receiveRelayCell(CellRelay.RELAY_CONNECTED);
			LOG.debug("TCPStream: Got Relay-Connected Cell");
		}
		catch (final TorException e)
		{
			if (!closed)
			{
				// only msg, if closing was unintentionally
				LOG.warn("TCPStream: Closed: " + toString()
						+ " due to TorException:" + e.getMessage());
			}
			closed = true;

			// MRK: when the circuit does not work at this point: close it
			// Lexi: please do it soft! there might be other streams
			// working on this circuit...
			// c.close(false);
			// Lexi: even better: increase only a counter for this circuit
			// otherwise circuits will close on an average after 3 or 4
			// streams. this is nothing we'd like to happen
			circuit.reportStreamFailure(this);

			throw e;
		}
		catch (final IOException e)
		{
			closed = true;
			LOG.warn("TCPStream: Closed:" + toString()
					+ " due to IOException:" + e.getMessage());
			throw e;
		}

		setupDuration = (int) (System.currentTimeMillis() - startSetupTime);

		// store resolved IP in TCPStreamProperties
		switch (relay.getLength())
		{
			case 4 + 4:
				// IPv4 address
				final byte[] ip = new byte[4];
				System.arraycopy(relay.getData(), 0, ip, 0, ip.length);
				try
				{
					resolvedAddress = InetAddress.getByAddress(ip);
					sp.setAddr(resolvedAddress);
					sp.setAddrResolved(true);
					if (LOG.isDebugEnabled())
					{
						LOG.debug("TCPStream: storing resolved IP "
							+ resolvedAddress.toString());
					}
				}
				catch (final IOException e)
				{
					LOG.info("unexpected for resolved ip={}", Arrays.toString(ip), e);
				}
				break;
			case 4 + 1 + 16 + 4:
				// IPv6 address
				// TODO: not yet implemented
				break;
			default:
				LOG.error("this should not happen");
				break;
		}

		// create reading threads to relay between user-side and tor-side
		// tor2java = new TCPStreamThreadTor2Java(this);
		// java2tor = new TCPStreamThreadJava2Tor(this);
		qhT2J = new QueueTor2JavaHandler(this);
		this.queue.addHandler(qhT2J);
		outputStream = new TCPStreamOutputStream(this);

		LOG.info("TCPStream: build stream " + toString() + " within " + setupDuration + " ms");
		// attach stream to history
		circuit.registerStream(sp, setupDuration);
		established = true;
		// Tor.lastSuccessfulConnection = new Date(System.currentTimeMillis());
		circuit.getTorEventService().fireEvent(
				new TorEvent(TorEvent.STREAM_BUILD, this, "Stream build: "
						+ toString()));
	}

	/**
	 * creates a stream on top of a existing circuit. users and programmers
	 * should never call this function, but Tor.connect() instead.
	 * 
	 * TODO: hidden-server-side.
	 * 
	 * Called after RELAY_BEGIN was received.
	 * 
	 * @param circuit
	 *            the circuit to build the stream through
	 * @param sp
	 *            the host etc. to connect to
	 * @see Tor
	 * @see Circuit
	 * @see TCPStreamProperties
	 */
	public TCPStream(final Circuit circuit, final int streamId) throws IOException, TorException,
			TorNoAnswerException
	{
		// TODO: this.sp = sp;
		established = false;
		created = System.currentTimeMillis();
		lastAction = created;
		lastCellSentDate = created;
		// stream establishment duration
		int setupDuration;
		long startSetupTime;

		// attach stream to circuit
		this.circuit = circuit;
		circuit.assignStreamId(this, streamId);
		queue = new Queue(QUEUE_TIMEOUNT2);
		closed = false;
		closedForReason = 0;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("TCPStream(2): building new stream " + toString());
		}

		startSetupTime = System.currentTimeMillis();
		/*
		 * TODO remove because not needed any more? while (true) { // wait for
		 * RELAY-BEGIN CellRelay relay = null; try {
		 * LOG.info("TCPStream(2): Waiting for Relay-Begin Cell..."); relay =
		 * queue.receiveRelayCell(CellRelay.RELAY_BEGIN);
		 * LOG.info("TCPStream(2): Got Relay-Begin Cell"); } catch (TorException
		 * e) { // only msg, if closing was unintentionally
		 * LOG.warn("TCPStream(2): Closed: " + toString() +
		 * " due to TorException:" + e.getMessage()); //TODO: continue; throw e;
		 * } catch (IOException e) { LOG.warn("TCPStream(2): Closed:" +
		 * toString() + " due to IOException:" + e.getMessage()); //TODO:
		 * continue; throw e; } break; }
		 */

		// send RELAY_CONNECTED
		sendCell(new CellRelayConnected(this));

		setupDuration = (int) (System.currentTimeMillis() - startSetupTime);

		// create reading threads to relay between user-side and tor-side
		// tor2java = new TCPStreamThreadTor2Java(this);
		// java2tor = new TCPStreamThreadJava2Tor(this);
		qhT2J = new QueueTor2JavaHandler(this);
		this.queue.addHandler(qhT2J);
		outputStream = new TCPStreamOutputStream(this);

		LOG.info("TCPStream: build stream " + toString() + " within "
				+ setupDuration + " ms");
		// attach stream to history
		final TCPStreamProperties sp = new TCPStreamProperties();
		circuit.registerStream(sp, setupDuration);
		established = true;
		// Tor.lastSuccessfulConnection = new Date(System.currentTimeMillis());
		circuit.getTorEventService().fireEvent(
				new TorEvent(TorEvent.STREAM_BUILD, this, "Stream build: "
						+ toString()));
	}

	/** called from derived ResolveStream. */
	protected TCPStream(final Circuit circuit)
	{
		this.circuit = circuit;
	}
	/** used for waiting for sendme cell. */
	private final transient Object waitForSendme = new Object();
	@Override
	public void sendCell(final Cell cell) throws TorException
	{
		// update 'action'-timestamp, if not padding cell
		lastCellSentDate = System.currentTimeMillis();
		if (!cell.isTypePadding())
		{
			lastAction = lastCellSentDate;
			if (cell.isTypeRelay() && cell instanceof CellRelayData)
			{
				streamLevelFlowControlSend--;
				LOG.debug("STREAM_FLOW_CONTROL_SEND = {}", streamLevelFlowControlSend);
				if (streamLevelFlowControlSend == 0)
				{
					LOG.debug("waiting for SENDME cell");
					try
					{
						waitForSendme.wait();
					}
					catch (InterruptedException exception)
					{
						throw new TorException("interrupted while trying to wait for SENDME cell", exception);
					}
				}
			}
		}
		// send cell
		try
		{
			circuit.sendCell(cell);
		}
		catch (final IOException e)
		{
			// if there's an error in sending a cell, close this stream
			this.circuit.reportStreamFailure(this);
			close(false);
			throw new TorException(e);
		}
	}

	/** send a stream-layer dummy. */
	public void sendKeepAlive()
	{
		try
		{
			sendCell(new CellRelayDrop(this));
		}
		catch (final TorException e)
		{
			LOG.debug("got TorException while trying to send a keep alive", e);
		}
	}

	/** for application interaction. */
	@Override
	public void close()
	{
		// gracefully close stream
		close(false);
		// remove from circuit
		if (LOG.isDebugEnabled())
		{
			LOG.debug("TCPStream.close(): removing stream " + toString());
		}
		circuit.removeStream(streamId);
	}

	/**
	 * for internal usage.
	 * 
	 * @param force
	 *            if set to true, just destroy the object, without sending
	 *            END-CELLs and stuff
	 */
	@Override
	public void close(final boolean force)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("TCPStream.close(): closing stream " + toString());
		}
		circuit.getTorEventService().fireEvent(
				new TorEvent(TorEvent.STREAM_CLOSED, this, "Stream closed: "
						+ toString()));

		// if stream is not closed, send a RELAY-END-CELL
		if (!(closed || force))
		{
			try
			{
				sendCell(new CellRelayEnd(this, (byte) 6)); // send cell with
															// 'DONE'
			}
			catch (final TorException e)
			{
				LOG.debug("got TorException while trying to close the stream", e);
			}
		}
		// terminate threads gracefully
		closed = true;
		/*
		 * if (!force) { try { this.wait(3); } catch (Exception e) { } }
		 */
		// terminate threads if they are still alive
		if (outputStream != null)
		{
			try
			{
				outputStream.close();
			}
			catch (final Exception e)
			{
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}
		// close queue (also removes handlers)
		queue.close();
		// remove from circuit
		circuit.removeStream(streamId);
	}

	/**
	 * use this to receive data by the anonymous data stream.
	 * 
	 * @return a standard Java-Inputstream
	 */
	@Override
	public InputStream getInputStream()
	{
		return qhT2J.getInputStream();
	}

	/**
	 * use this to transmit data through the Tor-network
	 * 
	 * @return a standard Java-Outputstream
	 */
	@Override
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	/** used for proxy and UI */
	public String getRoute()
	{
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < circuit.getRouteEstablished(); ++i)
		{
			final RouterImpl r = circuit.getRouteNodes()[i].getRouter();
			sb.append(", ");
			sb.append(r.getNickname() + " (" + r.getCountryCode() + ")");
		}
		return sb.toString();
	}

	/** for debugging. */
	@Override
	public String toString()
	{
		/*
		 * TODO: if (sp == null) { return id + " on circuit " +
		 * circuit.toString() + " to nowhere"; } else { if (closed) { return id
		 * + " on circuit " + circuit.toString() + " to " + sp.getHostname() +
		 * ":" + sp.getPort() + " (closed)"; } else { return id + " on circuit "
		 * + circuit.toString() + " to " + sp.getHostname() + ":" +
		 * sp.getPort(); } }
		 */
		if (closed)
		{
			return streamId + " on circuit " + circuit.toString() + " to ??? (closed)";
		}
		else
		{
			return streamId + " on circuit " + circuit.toString() + " to ???";
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	@Override
	public void setId(final int id)
	{
		if (this.streamId == 0)
		{
			// set initial ID
			this.streamId = id;
		}
		else
		{
			// replace id
			this.streamId = id;
			LOG.warn("replaced TCPStream.ID " + this.streamId + " by " + id);
		}
	}

	@Override
	public int getId()
	{
		return streamId;
	}

	@Override
	public long getLastCellSentDate()
	{
		return lastCellSentDate;
	}

	@Override
	public boolean isClosed()
	{
		return closed;
	}

	void setClosed(final boolean closed)
	{
		this.closed = closed;
	}

	@Override
	public Circuit getCircuit()
	{
		return circuit;
	}

	@Override
	public void processCell(final Cell cell) throws TorException
	{
		if (cell.isTypeRelay() && cell instanceof CellRelay)
		{
			CellRelay relay = (CellRelay) cell;
			if (relay.isTypeData())
			{
				streamLevelFlowControlRecv--;
				LOG.debug("STREAM_FLOW_CONTROL_RECV = {}", streamLevelFlowControlRecv);
				circuit.reduceCircWindowRecv(); // also reduce the circuits receive window.
				if (streamLevelFlowControlRecv <= STREAM_LEVEL_FLOW_WINDOW - STREAM_LEVEL_FLOW_INCREMENT)
				{
					// send a RELAY_SENDME cell to the edge node
						try
						{
							sendCell(new CellRelaySendme(this));
							streamLevelFlowControlRecv += STREAM_LEVEL_FLOW_INCREMENT;
						}
						catch (TorException exception)
						{
							LOG.warn("problems with sending RELAY_SENDME for stream {}", getId(), exception);
							throw new TorException("problems with sending RELAY_SENDME for stream " + getId(), exception);
						}
				}
			}
			else if (relay.isTypeSendme())
			{
				streamLevelFlowControlSend += STREAM_LEVEL_FLOW_INCREMENT;
				waitForSendme.notifyAll();
				LOG.debug("got RELAY_SENDME cell, increasing stream {} flow send window to {}", getId(), streamLevelFlowControlRecv);
				// TODO : ignore SENDME cells from now on?
			}
		}
		queue.add(cell);
	}

	public int getQueueTimeout()
	{
		return queueTimeout;
	}

	public InetAddress getResolvedAddress()
	{
		return resolvedAddress;
	}

	/*
	 * TODO public TCPStreamProperties getSp() { return sp; }
	 */

	public boolean isEstablished()
	{
		return established;
	}

	public int getClosedForReason()
	{
		return closedForReason;
	}

	public void setClosedForReason(int closedForReason)
	{
		this.closedForReason = closedForReason;
	}

	/**
	 * @return timestamp when this {@link Stream} was created.
	 */
	public long getCreated()
	{
		return created;
	}

	public long getLastAction()
	{
		return lastAction;
	}
}
