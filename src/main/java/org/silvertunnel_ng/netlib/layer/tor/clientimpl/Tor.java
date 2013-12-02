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

package org.silvertunnel_ng.netlib.layer.tor.clientimpl;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.api.TorNetLayerStatus;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitAdmin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitsStatus;
import org.silvertunnel_ng.netlib.layer.tor.circuit.HiddenServicePortInstance;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnection;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnectionAdmin;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.stream.ClosingThread;
import org.silvertunnel_ng.netlib.layer.tor.stream.ResolveStream;
import org.silvertunnel_ng.netlib.layer.tor.stream.StreamThread;
import org.silvertunnel_ng.netlib.layer.tor.stream.TCPStream;
import org.silvertunnel_ng.netlib.layer.tor.util.NetLayerStatusAdmin;
import org.silvertunnel_ng.netlib.layer.tor.util.PrivateKeyHandler;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.TorNoAnswerException;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAIN CLASS. keeps track of circuits, tls-connections and the status of
 * servers. Provides high level access to all needed functionality, i.e.
 * connecting to some remote service via Tor.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Koelsch
 * @author Vinh Pham
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */

public class Tor implements NetLayerStatusAdmin
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Tor.class);

	private static final int TOR_CONNECT_MAX_RETRIES = 10;
	private static final long TOR_CONNECT_MILLICESCONDS_BETWEEN_RETRIES = 10;
	/**
	 * List of "long-lived" ports listed in path-spec 2.2. a circuit needs to be
	 * "stable" for these ports. 
	 */
	public static final int[] LONG_LIVED_PORTS = { 21, 22, 706, 1863, 5050, 5190, 5222, 5223, 6667, 6697, 8300 };
	private Directory directory;
	private TLSConnectionAdmin tlsConnectionAdmin;
	private TorBackgroundMgmtThread torBackgroundMgmtThread;
	private final TorConfig torConfig;
	private PrivateKeyHandler privateKeyHandler;
	/**
	 * Absolute time in milliseconds: until this date/time the init is in
	 * progress.
	 * 
	 * Used to delay connects until Tor has some time to build up circuits and
	 * stuff.
	 */
	private long startupPhaseWithoutConnects;

	/**
	 * lower layer network layer, e.g. TLS over TCP/IP to connect to TOR onion
	 * routers
	 */
	private final NetLayer lowerTlsConnectionNetLayer;
	/** lower layer network layer, e.g. TCP/IP to connect to directory servers */
	private final NetLayer lowerDirConnectionNetLayer;
	/** storage that can be used, e.g. to cache directory information */
	private final StringStorage stringStorage;
	private final TorEventService torEventService = new TorEventService();

	private boolean gaveMessage = false;
	private boolean startUpInProgress = true;

	private NetLayerStatus status = TorNetLayerStatus.NEW;

	/**
	 * Initialize Tor with all defaults.
	 * 
	 * @exception IOException
	 */
	public Tor(final NetLayer lowerTlsConnectionNetLayer, final NetLayer lowerDirConnectionNetLayer, StringStorage stringStorage) throws IOException
	{
		this.lowerTlsConnectionNetLayer = lowerTlsConnectionNetLayer;
		this.lowerDirConnectionNetLayer = lowerDirConnectionNetLayer;
		this.stringStorage = stringStorage;
		// TODO webstart: config = new TorConfig(true);
		torConfig = TorConfig.getInstance();
		initLocalSystem(false);
		initDirectory();
		initRemoteAccess();
	}

	private void initLocalSystem(final boolean noLocalFileSystemAccess) throws IOException
	{
		// install BC, if not already done
		if (Security.getProvider("BC") == null)
		{
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			// Security.insertProviderAt(new
			// org.bouncycastle.jce.provider.BouncyCastleProvider(),2);
		}
		// logger and config
		LOG.info("Tor implementation of silvertunnel-ng.org is starting up");
		// create identity
		privateKeyHandler = new PrivateKeyHandler();
		// determine end of startup-Phase
		startupPhaseWithoutConnects = System.currentTimeMillis() + TorConfig.getStartupDelay() * 1000L;
		// init event-handler
	}

	private void initDirectory() throws IOException
	{
		directory = new Directory(torConfig, stringStorage, lowerDirConnectionNetLayer, privateKeyHandler.getIdentity(), this);
	}

	private void initRemoteAccess() throws IOException
	{
		// establish handler for TLS connections
		tlsConnectionAdmin = new TLSConnectionAdmin(lowerTlsConnectionNetLayer, privateKeyHandler);
		// initialize thread to renew every now and then
		torBackgroundMgmtThread = new TorBackgroundMgmtThread(this, TorConfig.getMinimumIdleCircuits());
	}

	/**
	 * @return read-only view of the currently valid Tor routers
	 */
	public Collection<Router> getValidTorRouters()
	{
		final Collection<RouterImpl> resultBase = directory.getValidRoutersByFingerprint().values();
		final Collection<Router> result = new ArrayList<Router>(resultBase.size());

		// copy all routers to the result collection
		for (final RouterImpl r : resultBase)
		{
			result.add(r.cloneReliable());
		}

		return result;
	}

	/**
	 * makes a connection to a remote service.
	 * 
	 * @param sp
	 *            hostname, port to connect to and other stuff
	 * @param torNetLayer
	 * @return some socket-thing
	 */
	public TCPStream connect(final TCPStreamProperties sp, final NetLayer torNetLayer) throws IOException
	{
		if (sp.getHostname() == null && sp.getAddr() == null)
		{
			throw new IOException("Tor: no hostname and no address provided");
		}

		// check, if tor is still in startup-phase
		checkStartup();

		// check whether the address is hidden
		if (sp.getHostname() != null && sp.getHostname().endsWith(".onion"))
		{
			return HiddenServiceClient.connectToHiddenService(torConfig, directory, torEventService, tlsConnectionAdmin, torNetLayer, sp);
		}

		// connect to exit server
		int retry = 0;
		String hostnameAddress = null;
		final int minIdleCircuits = Math.min(2, TorConfig.getMinimumIdleCircuits());
		for (; retry <= TOR_CONNECT_MAX_RETRIES; retry++)
		{
			// check precondition
			waitForIdleCircuits(minIdleCircuits);

			// action
			final Circuit[] circuits = CircuitAdmin.provideSuitableCircuits(tlsConnectionAdmin, directory, sp, torEventService, false);
			if (circuits == null || circuits.length < 1)
			{
				LOG.debug("no valid circuit found: wait for new one created by the TorBackgroundMgmtThread");
				try
				{
					Thread.sleep(TorBackgroundMgmtThread.INTERVAL_S * 1000L);
				}
				catch (final InterruptedException e)
				{
					LOG.debug("got IterruptedException : {}", e.getMessage(), e);
				}
				continue;
			}
			if (TorConfig.isVeryAggressiveStreamBuilding())
			{

				for (int j = 0; j < circuits.length; ++j)
				{
					// start N asynchronous stream building threads
					try
					{
						final StreamThread[] streamThreads = new StreamThread[circuits.length];
						for (int i = 0; i < circuits.length; ++i)
						{
							streamThreads[i] = new StreamThread(circuits[i], sp);
						}
						// wait for the first stream to return
						int chosenStream = -1;
						int waitingCounter = TorConfig.queueTimeoutStreamBuildup * 1000 / 10;
						while ((chosenStream < 0) && (waitingCounter >= 0))
						{
							boolean atLeastOneAlive = false;
							for (int i = 0; (i < circuits.length) && (chosenStream < 0); ++i)
							{
								if (!streamThreads[i].isAlive())
								{
									if ((streamThreads[i].getStream() != null) && (streamThreads[i].getStream().isEstablished()))
									{
										chosenStream = i;
									}
								}
								else
								{
									atLeastOneAlive = true;
								}
							}
							if (!atLeastOneAlive)
							{
								break;
							}

							final long SLEEPING_MS = 10;
							try
							{
								Thread.sleep(SLEEPING_MS);
							}
							catch (final InterruptedException e)
							{
								LOG.debug("got IterruptedException : {}", e.getMessage(), e);
							}

							--waitingCounter;
						}
						// return one and close others
						if (chosenStream >= 0)
						{
							final TCPStream returnValue = streamThreads[chosenStream].getStream();
							new ClosingThread(streamThreads, chosenStream);
							return returnValue;
						}
					}
					catch (final Exception e)
					{
						LOG.warn("Tor.connect(): " + e.getMessage());
						return null;
					}
				}

			}
			else
			{
				// build serial N streams, stop if successful
				for (int i = 0; i < circuits.length; ++i)
				{
					try
					{
						return new TCPStream(circuits[i], sp);
					}
					catch (final TorNoAnswerException e)
					{
						LOG.warn("Tor.connect: Timeout on circuit:" + e.getMessage());
					}
					catch (final TorException e)
					{
						LOG.warn("Tor.connect: TorException trying to reuse existing circuit:" + e.getMessage());
					}
					catch (final IOException e)
					{
						LOG.warn("Tor.connect: IOException " + e.getMessage());
					}
				}
			}

			hostnameAddress = (sp.getAddr() != null) ? "" + sp.getAddr() : sp.getHostname();
			LOG.info("Tor.connect: not (yet) connected to " + hostnameAddress + ":" + sp.getPort() + ", full retry count=" + retry);
			try
			{
				Thread.sleep(TOR_CONNECT_MILLICESCONDS_BETWEEN_RETRIES);
			}
			catch (final InterruptedException e)
			{
				LOG.debug("got IterruptedException : {}", e.getMessage(), e);
			}
		}
		hostnameAddress = (sp.getAddr() != null) ? "" + sp.getAddr() : sp.getHostname();
		throw new IOException("Tor.connect: unable to connect to " + hostnameAddress + ":" + sp.getPort() + " after " + retry + " full retries with "
				+ sp.getConnectRetries() + " sub retries");
	}

	/**
	 * initializes a new hidden service.
	 * 
	 * @param service
	 *            all data needed to init the things
	 */
	public void provideHiddenService(final NetLayer torNetLayerToConnectToDirectoryService,
									 final HiddenServiceProperties service,
									 final HiddenServicePortInstance hiddenServicePortInstance) throws IOException, TorException
	{
		// check, if tor is still in startup-phase
		checkStartup();

		// action
		HiddenServiceServer.getInstance().provideHiddenService(torConfig, directory, torEventService, tlsConnectionAdmin,
																torNetLayerToConnectToDirectoryService, service, hiddenServicePortInstance);
	}

	/**
	 * shut down everything.
	 * 
	 * @param force
	 *            set to true, if everything shall go fast. For graceful end,
	 *            set to false
	 */
	public void close(final boolean force)
	{
		LOG.info("TorJava ist closing down");
		// shutdown mgmt
		torBackgroundMgmtThread.close();
		// shut down connections
		tlsConnectionAdmin.close(force);
		// shutdown directory
		directory.close();
		// write config file
		torConfig.close();
		// close hidden services
		// TODO close hidden services
		// kill logger
		LOG.info("Tor.close(): CLOSED");
	}

	/** synonym for close(false). */
	public void close()
	{
		close(false);
	}

	/**
	 * Anonymously resolve a host name.
	 * 
	 * @param hostname
	 *            the host name
	 * @return the resolved IP; null if no mapping found
	 */
	public IpNetAddress resolve(final String hostname) throws IOException
	{
		final Object o = resolveInternal(hostname);
		if (o instanceof IpNetAddress)
		{
			return (IpNetAddress) o;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Anonymously do a reverse look-up.
	 * 
	 * @param addr
	 *            the IP address to be resolved
	 * @return the host name; null if no mapping found
	 */
	public String resolve(final IpNetAddress addr) throws IOException
	{
		// build address (works only for IPv4!)
		final byte[] a = addr.getIpaddress();
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; ++i)
		{
			sb.append((a[3 - i]) & 0xff);
			sb.append('.');
		}
		sb.append("in-addr.arpa");
		// resolve address
		final Object o = resolveInternal(sb.toString());
		if (o instanceof String)
		{
			return (String) o;
		}
		else
		{
			return null;
		}
	}

	/**
	 * internal function to use the tor-resolve-functionality.
	 * 
	 * @param query
	 *            a hostname to be resolved, or for a reverse lookup:
	 *            A.B.C.D.in-addr.arpa
	 * @return either an IpNetAddress (normal query), or a String
	 *         (reverse-DNS-lookup)
	 */
	private Object resolveInternal(final String query) throws IOException
	{
		try
		{
			// check, if tor is still in startup-phase
			checkStartup();
			// try to resolve query over all existing circuits
			// so iterate over all TLS-Connections
			for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
			{
				// and over all circuits in each TLS-Connection
				for (final Circuit circuit : tls.getCircuits())
				{
					try
					{
						if (circuit.isEstablished())
						{
							// if an answer is given, we're satisfied
							final ResolveStream rs = new ResolveStream(circuit);
							final Object o = rs.resolve(query);
							rs.close();
							return o;
						}
					}
					catch (final Exception e)
					{
						// in case of error, do nothing, but retry with the next
						// circuit
						LOG.debug("got Exception : {}", e.getMessage(), e);
					}
				}
			}
			// if no circuit could give an answer (possibly there was no
			// established circuit?)
			// build a new circuit and ask this one to resolve the query
//			final ResolveStream rs = new ResolveStream(new Circuit(tlsConnectionAdmin, directory, new TCPStreamProperties(), torEventService));
			final TCPStreamProperties streamProperties = new TCPStreamProperties();
			streamProperties.setConnectToTorIntern(true);
			final Circuit [] rsCircuit = CircuitAdmin.provideSuitableCircuits(tlsConnectionAdmin, 
																			  directory, 
																			  streamProperties, 
																			  torEventService, 
																			  false);
			final ResolveStream rs = new ResolveStream(rsCircuit[0]);
			final Object o = rs.resolve(query);
			rs.close();
			return o;
		}
		catch (final TorException e)
		{
			throw new IOException("Error in Tor: " + e.getMessage());
		}
	}

	@Override
	public void setStatus(final NetLayerStatus newStatus)
	{
		LOG.debug("TorNetLayer old status: {}", status);
		status = newStatus;
		LOG.info("TorNetLayer new status: {}", status);
	}

	/**
	 * Set the new status, but only, if the new readyIndicator is higher than
	 * the current one.
	 * 
	 * @param newStatus
	 */
	@Override
	public void updateStatus(final NetLayerStatus newStatus)
	{
		if (getStatus().getReadyIndicator() < newStatus.getReadyIndicator())
		{
			setStatus(newStatus);
		}
	}

	@Override
	public NetLayerStatus getStatus()
	{
		return status;
	}

	/**
	 * make sure that tor had some time to read the directory and build up some
	 * circuits.
	 */
	public void checkStartup()
	{
		// start up is proved to be over
		if (!startUpInProgress)
		{
			return;
		}

		// check if startup is over
		final long now = System.currentTimeMillis();
		if (now >= startupPhaseWithoutConnects)
		{
			startUpInProgress = false;
			return;
		}

		// wait for startup to be over
		final long sleep = startupPhaseWithoutConnects - System.currentTimeMillis();
		if (!gaveMessage)
		{
			gaveMessage = true;
			LOG.debug("Tor.checkStartup(): Tor is still in startup phase, sleeping for max. {} seconds",  (sleep / 1000L));
			LOG.debug("Tor not yet started - wait until torServers available");
		}
		// try { Thread.sleep(sleep); }
		// catch(Exception e) {}

		// wait until server info and established circuits are available
		waitForIdleCircuits(TorConfig.getMinimumIdleCircuits());
		try
		{
			Thread.sleep(500);
		}
		catch (final Exception e)
		{ /* ignore it */
			LOG.debug("got Exception : {}", e.getMessage(), e);
		}
		LOG.info("Tor start completed!!!");
		startUpInProgress = false;
	}

	/**
	 * Wait until Tor has at least minExpectedIdleCircuits idle circuits.
	 * 
	 * @param minExpectedIdleCircuits
	 *            minimum expected idling circuits
	 */
	private void waitForIdleCircuits(final int minExpectedIdleCircuits)
	{
		// wait until server info and established circuits are available
		while (!directory.isDirectoryReady() || getCircuitsStatus().getCircuitsEstablished() < minExpectedIdleCircuits)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (final Exception e)
			{ /* ignore it */
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * returns a set of current established circuits (only used by
	 * TorJava.Proxy.MainWindow to get a list of circuits to display).
	 * 
	 */
	public HashSet<Circuit> getCurrentCircuits()
	{

		final HashSet<Circuit> allCircs = new HashSet<Circuit>();
		for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
		{
			for (final Circuit circuit : tls.getCircuits())
			{
				// if (circuit.established && (!circuit.closed)){
				allCircs.add(circuit);
				// }
			}
		}
		return allCircs;
	}

	/**
	 * @return status summary of the Ciruits
	 */
	public CircuitsStatus getCircuitsStatus()
	{
		// count circuits
		int circuitsTotal = 0; // all circuits
		int circuitsAlive = 0; // circuits that are building up, or that are
								// established
		int circuitsEstablished = 0; // established, but not already closed
		int circuitsClosed = 0; // closing down

		for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
		{
			for (final Circuit c : tls.getCircuits())
			{
				String flag = "";
				++circuitsTotal;
				if (c.isClosed())
				{
					flag = "C";
					++circuitsClosed;
				}
				else
				{
					flag = "B";
					++circuitsAlive;
					if (c.isEstablished())
					{
						flag = "E";
						++circuitsEstablished;
					}
				}
//				if (LOG.isDebugEnabled())
//				{
//					LOG.debug("Tor.getCircuitsStatus(): " + flag + " rank " + c.getRanking() + " fails " + c.getStreamFails() + " of "
//							+ c.getStreamCounter() + " TLS " + tls.getRouter().getNickname() + "/" + c.toString());
//				}
			}
		}

		final CircuitsStatus result = new CircuitsStatus();
		result.setCircuitsTotal(circuitsTotal);
		result.setCircuitsAlive(circuitsAlive);
		result.setCircuitsEstablished(circuitsEstablished);
		result.setCircuitsClosed(circuitsClosed);

		return result;
	}

	/**
	 * Remove the current history. Close all circuits that were already be used.
	 */
	public void clear()
	{
		CircuitAdmin.clear(tlsConnectionAdmin);
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public TorEventService getTorEventService()
	{
		return torEventService;
	}

	/**
	 * @return the {@link Directory} of all known Routers.
	 */
	public Directory getDirectory()
	{
		return directory;
	}

	public TLSConnectionAdmin getTlsConnectionAdmin()
	{
		return tlsConnectionAdmin;
	}

	public TorConfig getTorConfig()
	{
		return torConfig;
	}

	public NetLayer getLowerTlsConnectionNetLayer()
	{
		return lowerTlsConnectionNetLayer;
	}

	public NetLayer getLowerDirConnectionNetLayer()
	{
		return lowerDirConnectionNetLayer;
	}

	public PrivateKeyHandler getPrivateKeyHandler()
	{
		return privateKeyHandler;
	}
}
