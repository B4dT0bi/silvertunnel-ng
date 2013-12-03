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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellCreate;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellCreateFast;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellDestroy;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellPadding;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayData;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayExtend;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayRendezvous1;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelaySendme;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEvent;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptor;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.TorNoAnswerException;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles the functionality of creating circuits, given a certain route and
 * buidling tcp-streams on top of them.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Koelsch
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */
public final class Circuit
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Circuit.class);

	/** Circuit Window receive standard value. (see tor-spec.txt 7.3) */
	private static final int CIRCUIT_LEVEL_FLOW_RECV = 1000; // TODO : extract circwindow from directory and use it here if it is set.
	/**
	 * current circuit receive window.
	 * 
	 * How many RELAY_DATA cells did we received? If this drops below a specific
	 * threshold we should send a RELAY_SENDME cell.
	 */
	private int circuitFlowRecv = CIRCUIT_LEVEL_FLOW_RECV;
	/**
	 * current circuit send window.
	 * 
	 * How many RELAY_DATA cells did we send? If this drops below a specific
	 * threshold we should wait for a RELAY_SENDME cell before sending new
	 * RELAY_DATA cells.
	 */
	private int circuitFlowSend = CIRCUIT_LEVEL_FLOW_RECV;
	/** Standard circuit receive. window increment value. */
	private static final int CIRCUIT_LEVEL_FLOW_RECV_INC = 100;

	/**  */
	public static volatile int numberOfCircuitsInConstructor = 0;
	/** a pointer to the TLS-layer. */
	private transient TLSConnection tls;
	/** stores the route. */
	private Node[] routeNodes;
	/** number of nodes in the route,og. where the keys have been established. */
	private int routeEstablished;
	/** used to receive incoming data. */
	private Queue queue;
	/** has this Circuit already been used for something? */
	private boolean unused = true;
	/** how many relayearly cells do we have ?*/
	private int relayEarlyCellsRemaining = 8;
	/**
	 * list of all TCP-streams relayed through this circuit.
	 * 
	 * key=stream ID, value=stream
	 */
	private final transient Map<Integer, Stream> streams = Collections.synchronizedMap(new HashMap<Integer, Stream>());
	/**
	 * contains URLs, InetAddresse or z-part of HS URL of hosts used to make
	 * contact to (or for DNS query) with this Circuit.
	 */
	private final transient Set<Object> streamHistory = new HashSet<Object>();
	/** counts the number of established streams. */
	private int establishedStreams = 0;
	/** service descriptor in case if used for rendezvous point. */
	private RendezvousServiceDescriptor serviceDescriptor;
	/** Circuit ID. */
	private transient int circuitId;
	/** set to true, if route is established. */
	private boolean established;
	/** set to true, if no new streams are allowed. */
	private boolean closed;
	/**
	 * set to true, if circuit is closed and inactive and may be removed from
	 * all sets.
	 */
	private boolean destruct;
	/**  */
	private long createdTime;
	/** last time, a cell was send that was not a padding cell. */
	private long lastAction;
	/** last time, a cell was send. */
	private long lastCell; //
	/** time in milliseconds it took to establish the circuit. */
	private int setupDurationMs;
	/** ranking index of the circuit. */
	private int ranking;
	/** duration of all streams' setup times. */
	private int sumStreamsSetupDelays;
	/** overall number of streams relayed through the circuit. */
	private int streamCounter;
	/** overall counter of failures in streams in this circuit. */
	private int streamFails;

	/** {@link Directory} which contains all TorServer information. */
	private Directory directory;
	private TLSConnectionAdmin tlsConnectionAdmin;
	private TorEventService torEventService;

	/** Close the circuit if last used stream has been closed? */
	private boolean closeCircuitIfLastStreamIsClosed;

	/**
	 * This circuit is used for (server side) hidden service introduction, this
	 * field saved the corresponding HiddenServiceInstance.
	 */
	private HiddenServiceInstance hiddenServiceInstanceForIntroduction;

	/**
	 * This circuit is used for (server side) hidden service rendezvous, this
	 * field saved the corresponding HiddenServiceInstance.
	 */
	private HiddenServiceInstance hiddenServiceInstanceForRendezvous;
	/** Saving the {@link TCPStreamProperties} for later usage. */
	private TCPStreamProperties streamProperties;
	/** save own information in {@link CircuitHistory}. */
	private CircuitHistory circuitHistory;
	/**
	 * initiates a circuit. tries to rebuild the circuit for a limited number of
	 * times, if first attempt fails.
	 * 
	 * @param fnh
	 *            a pointer to the TLS-Connection to the first node
	 * @param dir
	 *            a pointer to the directory, in case an alternative route is
	 *            necessary
	 * @param sp
	 *            some properties for the stream that is the reason for building
	 *            the circuit (needed if the circuit is needed to ask the
	 *            directory for a new route)
	 * @param torEventService
	 * @param circuitHistory {@link CircuitHistory} object where we will save this Stream information
	 * 						set to null if saving this info is not needed (eg for idle Circuits)
	 * @exception TorException
	 * @exception IOException
	 */
	public Circuit(final TLSConnectionAdmin fnh, 
	               final Directory dir, 
	               final TCPStreamProperties sp, 
	               final TorEventService torEventService,
	               final CircuitHistory circuitHistory)
															throws IOException,
															TorException,
															InterruptedException
	{
		numberOfCircuitsInConstructor++;
		boolean successful = false;
		try
		{
			// init variables
			this.directory = dir;
			this.tlsConnectionAdmin = fnh;
			this.torEventService = torEventService;
			this.circuitHistory = circuitHistory;
			streamProperties = sp;
			closed = false;
			established = false;
			destruct = false;
			sumStreamsSetupDelays = 0;
			streamCounter = 0;
			streamFails = 0;
			ranking = -1; // unused circs have highest priority for selection
			createdTime = System.currentTimeMillis();
			lastAction = createdTime;
			lastCell = createdTime;

			// save original Thread name
			final Thread currentThread = Thread.currentThread();
			final String originalThreadName = currentThread.getName();

			// get a new route
			RouterImpl[] routeServers = CircuitAdmin.createNewRoute(dir, sp);
			if (routeServers == null || routeServers.length < 1)
			{
				throw new TorException("Circuit: could not build route");
			}
			// try to build a circuit
			final long startSetupTime = System.currentTimeMillis();
			for (int misses = 1;; ++misses)
			{
				final long currentSetupDuration = System.currentTimeMillis() - startSetupTime;
				if (currentSetupDuration >= TorConfig.maxAllowedSetupDurationMs)
				{
					// stop here because it cannot be successful any more
					final String msg = "Circuit: close-during-create " + toString() + ", because current duration of " + currentSetupDuration
							+ " ms is already too long";
					LOG.info(msg);
					throw new IOException(msg);
				}

				// set thread name
				if (originalThreadName != null && originalThreadName.startsWith("Idle Thread"))
				{
					currentThread.setName(originalThreadName + " - Circuit to " + routeServers[routeServers.length - 1].getNickname());
				}

				if (Thread.interrupted())
				{
					throw new InterruptedException();
				}
				RouterImpl lastTarget = null;
				try
				{
					// attach circuit to TLS
					lastTarget = routeServers[0];
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: connecting to " + routeServers[0].getNickname() + " (" + routeServers[0].getCountryCode() + ")" + " ["
							+ routeServers[0].getPlatform() + "] over tls");
					}
					tls = fnh.getConnection(routeServers[0]);
					queue = new Queue(TorConfig.queueTimeoutCircuit);
					// attention: Addition to circuits-list is quite hidden
					// here.
					circuitId = tls.assignCircuitId(this);
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: assigned to tls " + routeServers[0].getNickname() + " (" + routeServers[0].getCountryCode() + ")" + " ["
							+ routeServers[0].getPlatform() + "]");
					}
					routeEstablished = 0;
					// connect to entry point = routeServers[0]
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: sending create cell to " + routeServers[0].getNickname());
					}
					routeNodes = new Node[routeServers.length];
					if (TorConfig.USE_CREATE_FAST_CELLS)
					{
						createFast(routeServers[0]);
					}
					else
					{
						create(routeServers[0]);
					}
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: connected to entry point " + routeServers[0].getNickname() + " (" + routeServers[0].getCountryCode() + ")"
							+ " [" + routeServers[0].getPlatform() + "]");
					}
					routeEstablished = 1;
					// extend route
					for (int i = 1; i < routeServers.length; ++i)
					{
						lastTarget = routeServers[i];
						extend(i, routeServers[i]);
						routeEstablished += 1;
					}
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: " + toString() + " successfully established");
					}
					// finished - success
					break;

				}
				catch (final Exception e)
				{
					// some error occurred during the creating of the circuit
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: " + toString() + " Exception " + misses + " :" + e, e);
					}
					if (lastTarget != null)
					{
						if (LOG.isDebugEnabled())
						{
							LOG.debug("Circuit: " + toString() + "\nlastTarget\n" + lastTarget.toLongString());
						}
					}
					// cleanup now
					if (circuitId != 0)
					{
						tls.removeCircuit(circuitId);
					}
					// error handling
					if (closed)
					{
						throw new IOException("Circuit: " + toString() + " closing during buildup");
					}
					if (misses >= TorConfig.reconnectCircuit)
					{
						// enough retries, exit
						if (e instanceof IOException)
						{
							throw (IOException) e;
						}
						else
						{
							throw new TorException(e.toString());
						}
					}
					// build a new route over the hosts that are known to be
					// working, punish failing host
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Circuit: " + toString() + " build a new route over the hosts that are known to be working, punish failing host");
					}
					routeServers = CircuitAdmin.restoreCircuit(dir, sp, routeServers, routeEstablished);
				}
			}
			setupDurationMs = (int) (System.currentTimeMillis() - startSetupTime);
			if (setupDurationMs < TorConfig.maxAllowedSetupDurationMs)
			{
				established = true;
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Circuit: " + toString() + " established within " + setupDurationMs + " ms - OK");
				}
				// fire event
				torEventService.fireEvent(new TorEvent(TorEvent.CIRCUIT_BUILD, this, "Circuit build " + toString()));
				successful = true;
			}
			else
			{
				if (LOG.isInfoEnabled())
				{
					LOG.info("Circuit: close-after-create " + toString() + ", because established within " + setupDurationMs + " ms was too long");
				}
				close(true);
			}
		}
		catch (Exception exception)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("got Exception while constructing circuit : " + exception, exception);
			}
		}
		finally
		{
			numberOfCircuitsInConstructor--;
			if (!successful)
			{
				this.circuitHistory = null;
				close(true);
			}
		}
	}

	/**
	 * CellRelayIntroduce2: From the Introduction Point to Bob's OP (section 1.9
	 * of Tor Rendezvous Specification)
	 * 
	 * We only support version 2 here.
	 * 
	 * does exactly that: - check introduce2 for validity and connect to
	 * rendezvous-point
	 */
	boolean handleIntroduce2(CellRelay cell) throws TorException, IOException
	{
		// parse introduce2-cell
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit.handleIntroduce2: received Intro2-Cell of length=" + cell.getLength());
		}
		if (cell.getLength() < 20)
		{
			throw new TorException("Circuit.handleIntroduce2: cannot parse content, cell is too short");
		}
		final byte[] identifier = new byte[20];
		System.arraycopy(cell.getData(), 0, identifier, 0, 20);
		final HiddenServiceProperties introProps = getHiddenServiceInstanceForIntroduction().getHiddenServiceProperties();
		if (!Arrays.equals(identifier, introProps.getPubKeyHash()))
		{
			throw new TorException("Circuit.handleIntroduce2: onion is for unknown key-pair");
		}
		final byte[] onionData = new byte[cell.getLength() - 20];
		System.arraycopy(cell.getData(), 20, onionData, 0, cell.getLength() - 20);

		final byte[] plainIntro2 = Encryption.asymDecrypt(introProps.getPrivateKey(), onionData);

		// TODO: deal with introduce2 version 1 - 3
		if (LOG.isDebugEnabled())
		{
			LOG.debug("   Intro2-Cell with plainIntro of lenght=" + plainIntro2.length);
		}

		// extract content from decoded Intro2 (v2 intro protocol)
		final byte[] version = new byte[1];
		final byte[] rendezvousPointAddress = new byte[4];
		// byte[] rendezvousPointPort = new byte[2]; (not needed because read
		// directly from big byte array)
		final byte[] rendezvousPointIdentityID = new byte[20];
		// byte[] rendezvousPointOnionKeyLen = new byte[2]; (not needed because
		// read directly from big byte array)
		byte[] rendezvousPointOnionKey;
		final byte[] cookie = new byte[20];
		final byte[] dhX = new byte[128];

		int i = 0;
		System.arraycopy(plainIntro2, i, version, 0, version.length);
		i += version.length;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("version=" + version[0]);
		}
		System.arraycopy(plainIntro2, i, rendezvousPointAddress, 0, rendezvousPointAddress.length);
		i += rendezvousPointAddress.length;
		final int rendezvousPointPort = Encoding.byteArrayToInt(plainIntro2, i, 2);
		i += 2;
		System.arraycopy(plainIntro2, i, rendezvousPointIdentityID, 0, rendezvousPointIdentityID.length);
		i += rendezvousPointIdentityID.length;
		final int rendezvousPointOnionKeyLength = Encoding.byteArrayToInt(plainIntro2, i, 2);
		i += 2;
		rendezvousPointOnionKey = new byte[rendezvousPointOnionKeyLength];
		System.arraycopy(plainIntro2, i, rendezvousPointOnionKey, 0, rendezvousPointOnionKey.length);
		i += rendezvousPointOnionKey.length;
		System.arraycopy(plainIntro2, i, cookie, 0, cookie.length);
		i += cookie.length;
		System.arraycopy(plainIntro2, i, dhX, 0, dhX.length);
		i += dhX.length;

		// determine rendezvous point router,
		// try both byte order variants - TODO: find the correct way
		final TcpipNetAddress rendezvousPointTcpipNetAddress1 = new TcpipNetAddress(rendezvousPointAddress, rendezvousPointPort);
		final RouterImpl rendezvousServer1 = directory.getValidRouterByIpAddressAndOnionPort(	rendezvousPointTcpipNetAddress1.getIpNetAddress(),
																								rendezvousPointTcpipNetAddress1.getPort());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("rendezvousServer1=" + rendezvousServer1);
		}
		// change byte order - TODO: find the correct way
		final byte[] rendezvousPointAddress2 = new byte[4];
		rendezvousPointAddress2[0] = rendezvousPointAddress[3];
		rendezvousPointAddress2[1] = rendezvousPointAddress[2];
		rendezvousPointAddress2[2] = rendezvousPointAddress[1];
		rendezvousPointAddress2[3] = rendezvousPointAddress[0];
		final TcpipNetAddress rendezvousPointTcpipNetAddress2 = new TcpipNetAddress(rendezvousPointAddress2, rendezvousPointPort);
		final RouterImpl rendezvousServer2 = directory.getValidRouterByIpAddressAndOnionPort(	rendezvousPointTcpipNetAddress2.getIpNetAddress(),
																								rendezvousPointTcpipNetAddress2.getPort());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("rendezvousServer2=" + rendezvousServer2);
		}
		// result
		final RouterImpl rendezvousServer = (rendezvousServer1 != null) ? rendezvousServer1 : rendezvousServer2;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("rendezvousServer=" + rendezvousServer);
		}

		// check version
		if (LOG.isDebugEnabled())
		{
			LOG.debug("received Introduce2 cell with rendevouz point server=" + rendezvousServer);
		}
		if (version[0] != 2)
		{
			if (LOG.isWarnEnabled())
			{
				LOG.warn("Intro2-Cell not supported with version=" + version[0]);
			}
			return false;
		}

		// do the rest in an extra thread/in background
		new Thread()
		{
			@Override
			public void run()
			{
				// build circuit to rendezvous
				final TCPStreamProperties sp = new TCPStreamProperties();
				sp.setExitPolicyRequired(false);
				sp.setCustomExitpoint(rendezvousServer.getFingerprint());

				// make new circuit where the last node is rendezvous point
				for (int j = 0; j < sp.getConnectRetries(); ++j)
				{
					try
					{
						final Circuit c2rendezvous = CircuitAdmin.provideSuitableNewCircuit(tlsConnectionAdmin, directory, sp, torEventService);
						if (c2rendezvous == null)
						{
							continue;
						}
						// send dhY
						final Node virtualNode = new Node(rendezvousServer, dhX);
						c2rendezvous.sendCell(new CellRelayRendezvous1(c2rendezvous, cookie, virtualNode.getDhYBytes(), virtualNode.getKeyHandshake()));
						if (LOG.isDebugEnabled())
						{
							LOG.debug("Circuit.handleIntroduce2: connected to rendezvous '" + rendezvousServer + "' over " + c2rendezvous.toString());
						}

						// extend circuit to 'virtual' next point AFTER doing
						// the rendezvous
						c2rendezvous.addNode(virtualNode);

						// connect - with empty address in begin cell set
						c2rendezvous.setHiddenServiceInstanceForRendezvous(hiddenServiceInstanceForIntroduction);

						break;

					}
					catch (final Exception e)
					{
						LOG.warn("Exception in handleIntroduce2", e);
					}
				}
			}
		}.start();
		/*
		 * https://gitweb.torproject.org/torspec.git/blob/HEAD:/rend-spec.txt -
		 * 1.10. Rendezvous
		 */
		return false;
	}

	/**
	 * CellRelayBegin received for hidden service.
	 * 
	 * @param cell
	 */
	void handleHiddenServiceStreamBegin(CellRelay cell, int streamId) throws TorException, IOException
	{
		// new stream requested on a circuit that was already established to the
		// rendezvous point
		LOG.info("new stream requested on a circuit that was already established to the rendezvous point");

		// determine requested port number (is between ':' and [00])
		final byte[] cellData = cell.getData();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("handleHiddenServiceStreamBegin with data=" + ByteArrayUtil.showAsStringDetails(cellData));
		}
		final int DEFAULT_PORT = -1;
		final int MAX_PORTSTR_LEN = 5;
		int port = DEFAULT_PORT;
		if (cellData[0] == ':')
		{
			// yes: ':' is at the first position
			final int startIndex = 1;
			int portNum = 0;
			for (int i = 0; i < MAX_PORTSTR_LEN; i++)
			{
				final char c = (char) cellData[startIndex + i];
				if (!Character.isDigit(c))
				{
					break;
				}
				portNum = 10 * portNum + (c - '0');
			}
			port = portNum;
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("new stream on port=" + port);
		}

		// add new TCPStream to NetServerSocket
		final HiddenServiceInstance hiddenServiceInstance = getHiddenServiceInstanceForRendezvous();
		final HiddenServicePortInstance hiddenServicePortInstance = hiddenServiceInstance.getHiddenServicePortInstance(port);
		if (hiddenServicePortInstance != null)
		{
			// accept stream
			hiddenServicePortInstance.createStream(this, streamId);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("added new TCPStream to NetServerSocket/hiddenServicePortInstance=" + hiddenServicePortInstance);
			}
		}
		else
		{
			// reject stream because nobody is listen to this port
			if (LOG.isDebugEnabled())
			{
				LOG.debug("rejected stream because nobody is listen on port=" + port + " of hiddenServiceInstance=" + hiddenServiceInstance);
			}
			// TODO: send cell to signal the rejection instead of letting stream
			// time out
		}
	}

	/**
	 * sends a cell on this circuit. Incoming data is received by the class
	 * TLSDispatcher and then put in the queue.
	 * 
	 * @param cell
	 *            the cell to send
	 * @exception IOException
	 * @see TLSDispatcherThread
	 */
	public final void sendCell(final Cell cell) throws IOException
	{
		// TODO : use also new protocol. (see tor-spec)
		// update 'action'-timestamp, if not padding cell
		lastCell = System.currentTimeMillis();
		if (!cell.isTypePadding())
		{
			lastAction = lastCell;
			if (cell.isTypeRelay() && cell instanceof CellRelayData)
			{
				circuitFlowSend--;
				LOG.debug("CIRCUIT_FLOW_CONTROL_SEND = {}", circuitFlowRecv);

				if (circuitFlowSend == 0)
				{
					LOG.debug("waiting for SENDME cell");
					try
					{
						waitForSendMe.wait();
					}
					catch (InterruptedException exception)
					{
						LOG.warn("got Exception while waiting for SENDME cell.", exception);
					}
				}
			}
		}
		// send cell
		try
		{
			tls.sendCell(cell);
		}
		catch (final IOException e)
		{
			LOG.debug("error in tls.sendCell Exception : {}", e, e);
			// if there's an error in sending it can only mean that the
			// circuit or the TLS-connection has severe problems. better close
			// it
			if (!closed)
			{
				close(false);
			}
			throw e;
		}
	}

	/** used for waiting for SENDME cell. */
	private final transient Object waitForSendMe = new Object();

	/** creates and send a padding-cell down the circuit. */
	public void sendKeepAlive()
	{
		try
		{
			sendCell(new CellPadding(this));
		}
		catch (final IOException e)
		{
			LOG.debug("got IOException : {}" + e.getMessage(), e);
		}
	}

	/**
	 * initiates circuit, sends CREATE-cell. throws an error, if something went
	 * wrong
	 */
	private void create(final RouterImpl init) throws IOException, TorException
	{
		// save starting point
		routeNodes[0] = new Node(init);
		// send create cell, set circID
		sendCell(new CellCreate(this));
		// wait for answer
		final Cell created = queue.receiveCell(Cell.CELL_CREATED);
		// finish DH-exchange
		routeNodes[0].finishDh(created.getPayload());
	}

	/**
	 * initiates circuit, sends CREATE_FAST-cell. throws an error, if something
	 * went wrong
	 */
	private void createFast(final RouterImpl init) throws IOException, TorException
	{
		LOG.debug("preparing CREATE-FAST cell");
		// save starting point
		routeNodes[0] = new Node(init, true);
		// send create cell, set circID
		LOG.debug("Sending CREATE-FAST cell");
		sendCell(new CellCreateFast(this));
		// wait for answer
		final Cell created_fast = queue.receiveCell(Cell.CELL_CREATED_FAST);
		// finish DH-exchange
		routeNodes[0].finishDh(created_fast.getPayload());
	}

	/**
	 * Extends the existing circuit one more hop. sends an EXTEND-cell.
	 */
	private void extend(final int i, final RouterImpl next) throws IOException, TorException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit: " + toString() + " extending to " + next.getNickname() + " ("
				+ next.getCountryCode() + ")" + " [" + next.getPlatform() + "]");
		}
		// save next node
		routeNodes[i] = new Node(next);
		// send extend cell
		sendCell(new CellRelayExtend(this, routeNodes[i]));
		// wait for extended-cell
		final CellRelay relay = queue.receiveRelayCell(CellRelay.RELAY_EXTENDED);
		// finish DH-exchange
		routeNodes[i].finishDh(relay.getData());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit: " + toString() + " successfully extended to " + next.getNickname() + " ("
				+ next.getCountryCode() + ")" + " [" + next.getPlatform() + "]");
		}
	}
	/**
	 * Extends the Circuit by another hop (from outside).
	 * 
	 * @param routerFingerprint the {@link Fingerprint} of the router to which the Circuit should be extended
	 * @throws TorException when Fingerprint is not found in {@link Directory} 
	 * 			or the Circuit cannot be extended to this router as this router already is in the nodes list
	 * @throws IOException when there is a problem extending the Circuit
	 */
	public void extend(final Fingerprint routerFingerprint) throws TorException, IOException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("extending Circuit with id {} to {}", new Object[]{getId(), routerFingerprint});
		}
		//check if we didnt have this Fingerprint already in our router list.
		for (Node node : routeNodes)
		{
			if (node.getRouter().getFingerprint() == routerFingerprint)
			{
				throw new TorException("Circuit cant be extended to given fingerprint as this router is already a node");
			}
		}
		RouterImpl router = directory.getValidRoutersByFingerprint().get(routerFingerprint);
		if (router == null)
		{
			throw new TorException("Router with fingerprint " + routerFingerprint + " not found.");
		}
		// create a new array for route that is one entry larger
		final Node[] newRoute = new Node[routeEstablished + 1];
		System.arraycopy(routeNodes, 0, newRoute, 0, routeEstablished);
		// route to set new array
		routeNodes = newRoute;		
		extend(routeEstablished, router);
	}
	/**
	 * adds node as the last one in the route.
	 * 
	 * @param n
	 *            new node that is appended to the existing route
	 */
	public void addNode(final Node n)
	{
		// create a new array for route that is one entry larger
		final Node[] newRoute = new Node[routeEstablished + 1];
		System.arraycopy(routeNodes, 0, newRoute, 0, routeEstablished);
		// add new node
		newRoute[routeEstablished] = n;
		++routeEstablished;
		// route to set new array
		routeNodes = newRoute;
	}

	/**
	 * used to report that this stream cause some trouble (either by itself, or
	 * the remote server, or what ever).
	 * 
	 * @param stream the stream which had the failure
	 */
	public void reportStreamFailure(final Stream stream)
	{
		++streamFails;
		// if it's just too much, 'soft'-close this circuit
		if ((streamFails > TorConfig.getCircuitClosesOnFailures()) && (streamFails > streamCounter * 3 / 2))
		{
			if (!closed)
			{
				LOG.info("Circuit.reportStreamFailure: closing due to failures {}", toString());
			}
			close(false);
		}
		// include in ranking
		updateRanking();
	}

	/**
	 * find a free stream ID, other than zero.
	 */
	private synchronized int getFreeStreamID() throws TorException
	{
		for (int nr = 1; nr < 0x10000; ++nr)
		{
			final int newId = (nr + streamCounter) & 0xffff;
			if (newId != 0)
			{
				if (!streams.containsKey(newId))
				{
					return newId;
				}
			}
		}
		throw new TorException("Circuit.getFreeStreamID: " + toString() + " has no free stream-IDs");
	}

	/**
	 * find a free stream-id, set it in the stream s.
	 * 
	 * @param stream the stream to be assigned to this Circuit
	 * @return the new stream id
	 * @throws TorException when stream id could not be set
	 */
	public int assignStreamId(final Stream stream) throws TorException
	{
		// assign stream ID and memorize stream
		final int streamId = getFreeStreamID();
		if (!assignStreamId(stream, streamId))
		{
			throw new TorException("streamId=" + streamId + " could not be set");
		}
		return streamId;
	}

	/**
	 * set the specified stream id to the stream.
	 * 
	 * @param stream the {@link Stream} to be assigned
	 * @param streamId the stream id to be assigned to the given stream
	 * @return true=success, false=stream id is already in use
	 * @throws TorException when Circuit is already closed
	 */
	public boolean assignStreamId(final Stream stream, final int streamId) throws TorException
	{
		if (closed)
		{
			throw new TorException("Circuit.assignStreamId: " + toString() + " is closed");
		}

		stream.setId(streamId);
		final Stream oldStream = streams.put(streamId, stream);
		if (oldStream == null)
		{
			// success
			return true;
		}
		else
		{
			// streamID was already used - rollback operation
			streams.put(streamId, oldStream);
			return false;
		}
	}

	/**
	 * registers a stream in the history to allow bundeling streams to the same
	 * connection in one circuit.
	 */
	final void registerStream(final TCPStreamProperties sp) throws TorException
	{
		++establishedStreams;
		if (sp.getAddr() != null)
		{
			streamHistory.add(sp.getAddr());
		}
		if (sp.getHostname() != null)
		{
			streamHistory.add(sp.getHostname());
		}
	}

	/**
	 * registers a stream in the history to allow bundling streams to the same
	 * connection in one circuit, wrapped for setting stream creation time.
	 */
	public void registerStream(final TCPStreamProperties sp, final long streamSetupDuration) throws TorException
	{

		sumStreamsSetupDelays += streamSetupDuration;
		streamCounter++;
		updateRanking();
		registerStream(sp);
	}

	/**
	 * updates the ranking of the circuit. takes into account: setup time of
	 * circuit and streams. but also number of stream-failures on this circuit;
	 * 
	 */
	private void updateRanking()
	{
		// do a weighted average of all setups. weighten the setup-time of the
		// circuit more
		// then those of the single streams. thus streams will be rather
		// unimportant at the
		// beginning, but play a more important role afterwards.
		ranking = (TorConfig.CIRCUIT_ESTABLISHMENT_TIME_IMPACT * setupDurationMs + sumStreamsSetupDelays)
				/ (streamCounter + TorConfig.CIRCUIT_ESTABLISHMENT_TIME_IMPACT);
		// take into account number of stream-failures on this circuit
		// DEPRECATED: just scale this up linearly
		// ranking *= 1 + streamFails;
		// NEW: be cruel! there should be something severe for 3 or 4 errors!
		ranking *= Math.exp(streamFails);
	}

	/**
	 * closes the circuit. either soft (remaining connections are kept, no new
	 * one allowed) or hard (everything is closed immediately, e.g. if a destroy
	 * cell is received)
	 */
	public boolean close(final boolean force)
	{
		if (!closed)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Circuit.close(): closing " + toString());
			}
			// remove servers from list of currently used nodes
			for (int i = 0; i < routeEstablished; ++i)
			{
				final Fingerprint f = routeNodes[i].getRouter().getFingerprint();
				Integer numberOfNodeOccurances = CircuitAdmin.getCurrentlyUsedNode(f);
				if (numberOfNodeOccurances != null)
				{
					// decrement the counter
					CircuitAdmin.putCurrentlyUsedNodeNumber(f, Math.max(0, --numberOfNodeOccurances));
				}
			}
		}
		torEventService.fireEvent(new TorEvent(TorEvent.CIRCUIT_CLOSED, this, "Circuit: closing " + toString()));

		// mark circuit closed. do nothing more, is soft close and streams are
		// left
		closed = true;
		established = false;
		// close all streams, removed closed streams
		for (final Stream stream : new ArrayList<Stream>(streams.values()))
		{
			try
			{
				// check if stream is still alive
				if (!stream.isClosed())
				{
					if (force)
					{
						stream.close(force);
					}
					else
					{
						// check if we can time-out the stream?
						if (System.currentTimeMillis() - stream.getLastCellSentDate() > 10 * TorConfig.queueTimeoutStreamBuildup * 1000)
						{
							// ok, fsck it!
							LOG.info("Circuit.close(): forcing timeout on stream");
							stream.close(true);
						}
						else
						{
							// no way...warning
							if (LOG.isDebugEnabled())
							{
								LOG.debug("Circuit.close(): can't close due to " + stream.toString());
							}
						}
					}
				}
				if (stream.isClosed())
				{
					streams.remove(stream.getId());
				}
			}
			catch (final Exception e)
			{
				LOG.warn("unexpected " + e, e);
			}
		}
		//
		if ((!force) && (!streams.isEmpty()))
		{
			return false;
		}
		// gracefully kill circuit with DESTROY-cell or so
		if (!force)
		{
			if (routeEstablished > 0)
			{
				// send a destroy-cell to the first hop in the circuit only
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Circuit.close(): destroying " + toString());
				}
				routeEstablished = 1;
				try
				{
					sendCell(new CellDestroy(this));
				}
				catch (final IOException e)
				{
					LOG.debug("Exception while destroying circuit: {}", e, e);
				}
			}
		}

		// close circuit (also removes handlers)
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit.close(): close queue? " + toString());
		}
		if (queue != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Circuit.close(): close queue! " + toString());
			}
			queue.close();
		}

		// cleanup and maybe close tls
		destruct = true;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit.close(): remove from tls? " + toString());
		}
		if (tls != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Circuit.close(): remove from tls! " + toString());
			}
			tls.removeCircuit(getId());
		}

		// closed
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Circuit.close(): done " + toString());
		}
		return true;
	}

	/**
	 * returns the route of the circuit. used to display route on a map or the
	 * like
	 */
	public RouterImpl[] getRoute()
	{
		final RouterImpl[] s = new RouterImpl[routeEstablished];
		for (int i = 0; i < routeEstablished; ++i)
		{
			s[i] = routeNodes[i].getRouter();
		}
		return s;
	}

	/** used for description. */
	@Override
	public String toString()
	{
		if (tls != null && tls.getRouter() != null)
		{
			final Router r1 = tls.getRouter();
			final StringBuffer sb = new StringBuffer(circuitId + " [" + r1.getNickname() + "/" + r1.getFingerprint() + " (" + r1.getCountryCode()
					+ ") (" + r1.getPlatform() + ")" + (isFast() ? "[fast]" : "") + (isStable() ? "[stable]" : ""));
			for (int i = 1; i < routeEstablished; ++i)
			{
				final Router r = routeNodes[i].getRouter();
				sb.append(" " + r.getNickname() + "/" + r.getHostname() + ":" + r.getOrPort() + "/" + r.getFingerprint() + " (" + r.getCountryCode()
						+ ") (" + r.getPlatform() + ")");
			}
			sb.append("]");
			if (closed)
			{
				sb.append(" (closed)");
			}
			else
			{
				if (!established)
				{
					sb.append(" (establishing)");
				}
			}
			return sb.toString();
		}
		else
		{
			return "<empty>";
		}
	}

	/**
	 * 
	 * @param streamId
	 * @return true=removed; false=could not remove/did not exist
	 */
	public boolean removeStream(final Integer streamId)
	{
		synchronized (streams)
		{
			final boolean result = streams.remove(streamId) != null;
			if (closeCircuitIfLastStreamIsClosed && streams.size() == 0)
			{
				close(true);
			}
			return result;
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public void setHiddenServiceInstanceForIntroduction(final HiddenServiceInstance hiddenServiceInstanceForIntroduction)
	{
		this.hiddenServiceInstanceForIntroduction = hiddenServiceInstanceForIntroduction;
	}

	HiddenServiceInstance getHiddenServiceInstanceForIntroduction()
	{
		return hiddenServiceInstanceForIntroduction;
	}

	public boolean isUsedByHiddenServiceToConnectToIntroductionPoint()
	{
		return hiddenServiceInstanceForIntroduction != null;
	}

	private void setHiddenServiceInstanceForRendezvous(final HiddenServiceInstance hiddenServiceInstanceForRendezvous)
	{
		this.hiddenServiceInstanceForRendezvous = hiddenServiceInstanceForRendezvous;
	}

	HiddenServiceInstance getHiddenServiceInstanceForRendezvous()
	{
		return hiddenServiceInstanceForRendezvous;
	}

	boolean isUsedByHiddenServiceToConnectToRendezvousPoint()
	{
		return hiddenServiceInstanceForRendezvous != null;
	}

	public TorEventService getTorEventService()
	{
		return torEventService;
	}

	public Node[] getRouteNodes()
	{
		return routeNodes;
	}

	public void setRouteNodes(final Node[] routeNodes)
	{
		this.routeNodes = routeNodes;
	}

	/**
	 * 
	 * @return number of nodes in the route, where the keys have been
	 *         established
	 */
	public int getRouteEstablished()
	{
		return routeEstablished;
	}

	public void setRouteEstablished(final int routeEstablished)
	{
		this.routeEstablished = routeEstablished;
	}

	public Map<Integer, Stream> getStreams()
	{
		synchronized (streams)
		{
			return new HashMap<Integer, Stream>(streams);
		}
	}

	/**
	 * Receive a special {@link CellRelay}.
	 * 
	 * @param type
	 *            the type of the RELAY-cell to be received
	 * @return a {@link CellRelay} if received correctly
	 * @throws TorNoAnswerException
	 * @throws IOException
	 * @throws TorException
	 */
	public final CellRelay receiveRelayCell(final int type) throws TorNoAnswerException, IOException, TorException
	{
		return queue.receiveRelayCell(type);
	}

	/**
	 * Handle the received cell.
	 * 
	 * @param cell
	 *            the {@link Cell}
	 * @throws TorException
	 *             will be thrown if there is a problem while sending a
	 *             RELAY_SENDME cell
	 */
	public final void processCell(final Cell cell) throws TorException
	{
		if (cell.isTypeRelay() && cell instanceof CellRelay)
		{
			CellRelay relay = (CellRelay) cell;
			if (relay.isTypeData())
			{
				reduceCircWindowRecv();
			}
			else if (relay.isTypeSendme())
			{
				circuitFlowSend += CIRCUIT_LEVEL_FLOW_RECV_INC;
				waitForSendMe.notifyAll();
				LOG.debug("got RELAY_SENDME cell, increasing circuit flow send window to {}", circuitFlowSend);
				// TODO : ignore SENDME cells from now on?
			}
		}
		queue.add(cell);
	}

	/**
	 * Reduce the circuit receive window. (see tor-spec.txt 7.3)
	 * 
	 * @throws TorException
	 *             will be thrown if there is a problem while sending a
	 *             RELAY_SENDME cell
	 */
	public final synchronized void reduceCircWindowRecv() throws TorException
	{
		circuitFlowRecv--;
		LOG.debug("CIRCUIT_FLOW_CONTROL_RECV = {}", circuitFlowRecv);
		if (circuitFlowRecv <= CIRCUIT_LEVEL_FLOW_RECV - CIRCUIT_LEVEL_FLOW_RECV_INC)
		{
			// send a RELAY_SENDME cell to the last router in the circuit
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("sending RELAY_SENDME cell to router {}", getRoute()[getRouteEstablished() - 1]);
				}
				sendCell(new CellRelaySendme(this, getRouteEstablished() - 1));
				circuitFlowRecv += CIRCUIT_LEVEL_FLOW_RECV_INC;
			}
			catch (IOException exception)
			{
				LOG.warn("problems with sending RELAY_SENDME cell to router {}", getRoute()[getRouteEstablished() - 1], exception);
				throw new TorException("problems with sending RELAY_SENDME cell to router " + getRoute()[getRouteEstablished() - 1], exception);
			}
		}
	}

	/**
	 * Get URLs, InetAddresses or z-part of HS URL of hosts used to make
	 * contact to (or for DNS query) with this Circuit.
	 * 
	 * @return a set of objects
	 */
	public final Set<Object> getStreamHistory()
	{
		return streamHistory;
	}

	public int getEstablishedStreams()
	{
		return establishedStreams;
	}

	public void setEstablishedStreams(final int establishedStreams)
	{
		this.establishedStreams = establishedStreams;
	}

	public int getId()
	{
		return circuitId;
	}

	public boolean isEstablished()
	{
		return established;
	}

	public void setEstablished(final boolean established)
	{
		this.established = established;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public boolean isDestruct()
	{
		return destruct;
	}

	public long getCreated()
	{
		return createdTime;
	}

	public void setCreated(final long created)
	{
		this.createdTime = created;
	}

	public long getLastAction()
	{
		return lastAction;
	}

	public void setLastAction(final long lastAction)
	{
		this.lastAction = lastAction;
	}

	public long getLastCell()
	{
		return lastCell;
	}

	public void setLastCell(final long lastCell)
	{
		this.lastCell = lastCell;
	}

	public int getSetupDurationMs()
	{
		return setupDurationMs;
	}

	public void setSetupDurationMs(final int setupDurationMs)
	{
		this.setupDurationMs = setupDurationMs;
	}

	public int getRanking()
	{
		return ranking;
	}

	public void setRanking(final int ranking)
	{
		this.ranking = ranking;
	}

	public int getSumStreamsSetupDelays()
	{
		return sumStreamsSetupDelays;
	}

	public void setSumStreamsSetupDelays(final int sumStreamsSetupDelays)
	{
		this.sumStreamsSetupDelays = sumStreamsSetupDelays;
	}

	public int getStreamCounter()
	{
		return streamCounter;
	}

	public void setStreamCounter(final int streamCounter)
	{
		this.streamCounter = streamCounter;
	}

	public int getStreamFails()
	{
		return streamFails;
	}

	public void setStreamFails(final int streamFails)
	{
		this.streamFails = streamFails;
	}

	public Directory getDirectory()
	{
		return directory;
	}

	public void setDirectory(final Directory directory)
	{
		this.directory = directory;
	}

	public TLSConnectionAdmin getTlsConnectionAdmin()
	{
		return tlsConnectionAdmin;
	}

	public void setTlsConnectionAdmin(final TLSConnectionAdmin tlsConnectionAdmin)
	{
		this.tlsConnectionAdmin = tlsConnectionAdmin;
	}

	public RendezvousServiceDescriptor getServiceDescriptor()
	{
		return serviceDescriptor;
	}

	public void setServiceDescriptor(final RendezvousServiceDescriptor serviceDescriptor)
	{
		this.serviceDescriptor = serviceDescriptor;
	}
	/**
	 * @return Should we close the circuit if the last used stream has been closed?
	 */
	public boolean isCloseCircuitIfLastStreamIsClosed()
	{
		return closeCircuitIfLastStreamIsClosed;
	}
	/**
	 * Should the circuit be closed when the last used stream has been closed?
	 * @param closeCircuitIfLastStreamIsClosed true if Circuit should be closed
	 */
	public void setCloseCircuitIfLastStreamIsClosed(final boolean closeCircuitIfLastStreamIsClosed)
	{
		this.closeCircuitIfLastStreamIsClosed = closeCircuitIfLastStreamIsClosed;
	}

	/** caching the result of isFast method for performance reasons. */
	private Boolean isFast = null;

	/**
	 * @return true if this Circuit only contains Routers with flag fast.
	 */
	public synchronized boolean isFast()
	{
		boolean circuitComplete = true;
		boolean tmpValue = true;
		if (isFast == null)
		{
			if (routeNodes == null)
			{
				return false;
			}
			for (Node routerNode : routeNodes)
			{
				if (routerNode == null)
				{
					circuitComplete = false;
					return false;
				}
				else
				{
					if (!routerNode.getRouter().isDirv2Fast())
					{
						tmpValue = false;
					}
				}
			}
			if (circuitComplete)
			{
				isFast = tmpValue;
			}
		}
		return isFast;
	}

	/** caching the result of isStable method for performance reasons. */
	private Boolean isStable = null;

	/**
	 * @return true if this Circuit only contains Routers with flag stable.
	 */
	public synchronized boolean isStable()
	{
		boolean circuitComplete = true;
		boolean tmpValue = true;
		if (isStable == null)
		{
			if (routeNodes == null)
			{
				return false;
			}
			for (Node routerNode : routeNodes)
			{
				if (routerNode == null)
				{
					circuitComplete = false;
					return false;
				}
				else
				{
					if (!routerNode.getRouter().isDirv2Stable())
					{
						tmpValue = false;
					}
				}
			}
			if (circuitComplete)
			{
				isStable = tmpValue;
			}
		}
		return isStable;
	}

	/**
	 * @return the relayEarlyCellsRemaining
	 */
	public int getRelayEarlyCellsRemaining() 
	{
		return relayEarlyCellsRemaining;
	}

	/**
	 * Decrease the amount of relay early cells remaining.
	 */
	public void decrementRelayEarlyCellsRemaining() 
	{
		this.relayEarlyCellsRemaining--;
	}
	/**
	 * Get the {@link TCPStreamProperties} which have been used for creating this circuit.
	 * 
	 * @return the {@link TCPStreamProperties} object.
	 */
	public TCPStreamProperties getTcpStreamProperties()
	{
		return streamProperties;
	}

	/**
	 * @return the unused
	 */
	public boolean isUnused()
	{
		return unused 
			&& establishedStreams == 0 
		    && !isUsedByHiddenServiceToConnectToIntroductionPoint() 
		    && !isUsedByHiddenServiceToConnectToRendezvousPoint();
	}

	/**
	 * @param unused the unused to set
	 */
	public void setUnused(final boolean unused)
	{
		this.unused = unused;
	}
}
