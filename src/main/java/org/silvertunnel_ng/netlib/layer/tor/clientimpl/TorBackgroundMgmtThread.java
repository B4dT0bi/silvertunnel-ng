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

package org.silvertunnel_ng.netlib.layer.tor.clientimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.layer.tor.api.TorNetLayerStatus;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitsStatus;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Stream;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnection;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.directory.DirectoryManagerThread;
import org.silvertunnel_ng.netlib.layer.tor.stream.TCPStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management thread.
 * 
 * @author Lexi Pimenidis
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */
class TorBackgroundMgmtThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorBackgroundMgmtThread.class);

	/** general factor seconds:milliseconds. */
	private static final int MILLISEC = 1000;
	/** time to sleep until first actions in seconds. */
	private static final int INITIAL_INTERVAL_S = 3;
	/** time to wait between working loads in seconds. */
	protected static final int INTERVAL_S = 3;
	/** interval of padding messages on circuits in seconds. */
	private static final int CIRCUITS_KEEP_ALIVE_INTERVAL_S = 30;
	/** interval of padding messages on streams in seconds. */
	private static final int STREAMS_KEEP_ALIVE_INTERVAL_S = 30;

	private static long idleThreadCounter = 0;

	/** reference to main class. */
	private final Tor tor;
	/** store the current time. */
	private long currentTimeMillis;
	/** List of background threads (for graceful close). */
	private final List<Thread> backgroundThreads;
	/** As stop() is depreciated we follow the Sun recommendation. */
	private boolean stopped = false;
	private final DirectoryManagerThread directoryManagerThread;

	/**
	 * Run the {@link TorBackgroundMgmtThread}.
	 * @param tor current {@link Tor} object
	 */
	TorBackgroundMgmtThread(final Tor tor)
	{
		this.backgroundThreads = new ArrayList<Thread>(TorConfig.getMinimumIdleCircuits());
		this.tor = tor;
		currentTimeMillis = System.currentTimeMillis();
		spawnIdleCircuits(TorConfig.getMinimumIdleCircuits());
		this.directoryManagerThread = new DirectoryManagerThread(tor.getDirectory());
		setName(getClass().getName());
		setDaemon(true);
		start();
	}

	/** create some empty circuits to have at hand - does so in the background. */
	private void spawnIdleCircuits(final int amount)
	{
		// Don't create circuits until not at least a certain fraction of the
		// routers is known
		if (tor.getDirectory().isDirectoryReady())
		{
			if (amount > 0)
			{
				LOG.info("TorBackgroundMgmtThread.spawnIdleCircuits: Spawn {} new circuits", amount);
			}
		}
		else
		{
			LOG.debug("Not yet spawning circuits (too few routers known until now)");
			return;
		}

		// Cleanup our background thread list
		final ListIterator<Thread> brtIterator = backgroundThreads.listIterator();
		while (brtIterator.hasNext())
		{
			final Thread brt = brtIterator.next();
			if (!brt.isAlive())
			{
				brtIterator.remove();
			}
		}

		// Spawn new background threads
		if (amount > 0)
		{
			tor.updateStatus(TorNetLayerStatus.INITIAL_CIRCUITES_ESTABLISHING);
		}
		for (int i = 0; i < amount; ++i)
		{
			final Thread brt = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						// TODO : implement circuit predictor here
						// idle threads should at least allow using port 80
						final TCPStreamProperties sp = new TCPStreamProperties();
						sp.setFastRoute(true);
						sp.setPort(80);
						new Circuit(tor.getTlsConnectionAdmin(),
								tor.getDirectory(), sp,
								tor.getTorEventService(), null);
					}
					catch (final Exception e)
					{
						LOG.debug("TorBackgroundMgmtThread.spawnIdleCircuits got Exception: {}"	, e.getMessage(), e);
					}
				}
			};
			LOG.debug("TorBackgroundMgmtThread.spawnIdleCircuits: Circuit-Spawning thread started.");
			brt.setName("Idle Thread " + idleThreadCounter++);
			brt.start();
			backgroundThreads.add(brt);
		}
	}

	/**
	 * sends keep-alive data on circuits.
	 */
	private void sendKeepAlivePackets()
	{
		for (final TLSConnection tls : tor.getTlsConnectionAdmin().getConnections())
		{
			for (final Circuit c : tls.getCircuits())
			{
				// check if this circuit needs a keep-alive-packet
				if ((c.isEstablished())	&& (currentTimeMillis - c.getLastCell() > CIRCUITS_KEEP_ALIVE_INTERVAL_S * MILLISEC))
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("TorBackgroundMgmtThread.sendKeepAlivePackets(): Circuit " + c.toString());
					}
					c.sendKeepAlive();
				}
				// check streams in circuit
				for (final Stream streamX : c.getStreams().values())
				{
					final TCPStream stream = (TCPStream) streamX;
					if ((stream.isEstablished())
							&& (!stream.isClosed())
							&& (currentTimeMillis
									- stream.getLastCellSentDate() > STREAMS_KEEP_ALIVE_INTERVAL_S
									* MILLISEC))
					{
						if (LOG.isDebugEnabled())
						{
							LOG.debug("TorBackgroundMgmt.sendKeepAlivePackets(): Stream "
									+ stream.toString());
						}
						stream.sendKeepAlive();
					}
				}
			}
		}
	}

	/**
	 * used to determine which (old) circuits can be torn down because there are
	 * enough new circuits. or builds up new circuits, if there are not enough.
	 */
	private void manageIdleCircuits()
	{
		final CircuitsStatus circuitsStatus = tor.getCircuitsStatus();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("TorBackgroundMgmt.manageIdleCircuits(): circuit counts: "
				+ (circuitsStatus.getCircuitsAlive() - circuitsStatus
						.getCircuitsEstablished()) + " building, "
				+ circuitsStatus.getCircuitsEstablished() + " established + "
				+ circuitsStatus.getCircuitsClosed() + " closed = "
				+ circuitsStatus.getCircuitsTotal());
		}
		// check if enough 'alive' circuits are there
		if (circuitsStatus.getCircuitsAlive() + Circuit.numberOfCircuitsInConstructor < TorConfig.getMinimumIdleCircuits())
		{
			spawnIdleCircuits((TorConfig.getMinimumIdleCircuits() - circuitsStatus.getCircuitsAlive()) * 3 / 2);
		}
		else if (circuitsStatus.getCircuitsEstablished() > TorConfig.getMinimumIdleCircuits()	+ TorConfig.circuitsMaximumNumber)
		{
			// TODO: if for some reason there are too many established circuits. close the oldest ones
			if (LOG.isDebugEnabled())
			{
				LOG.debug("TorBackgroundMgmtThread.manageIdleCircuits(): kill "
					+ (TorConfig.getMinimumIdleCircuits() + TorConfig.circuitsMaximumNumber - circuitsStatus
							.getCircuitsAlive()) + "new circuits (FIXME)");
			}
		}
	}

	/**
	 * used to close circuits that are marked for closing, but are still alive.
	 * They are closed, if no more streams are contained.
	 */
	private void tearDownClosedCircuits()
	{
		for (final TLSConnection tls : tor.getTlsConnectionAdmin().getConnections())
		{
			LOG.debug("check tls={}", tls);
			if (tls.isClosed())
			{
				LOG.debug("remove tls={}", tls);
				tor.getTlsConnectionAdmin().removeConnection(tls);
			}
			for (final Circuit c : tls.getCircuitMap().values())
			{
				// check if stream is establishing but doesn't had any action
				// for a longer period of time
				for (final Stream streamX : c.getStreams().values())
				{
					final TCPStream s = (TCPStream) streamX;
					final long diff = (currentTimeMillis - s.getLastAction()) / MILLISEC;
					if ((!s.isEstablished()) || s.isClosed())
					{
						if (diff > (2 * TorConfig.queueTimeoutStreamBuildup))
						{
							// LOG.info("close "+diff+" "+s.print());
							LOG.debug("TorBackgroundMgmtThread.tearDownClosedCircuits(): closing stream (too long building) "
									+ s.toString());
							s.close(true);
						}
						else
						{
							LOG.debug("Checked {} {}", diff, s.getRoute());
						}
					}
					else
					{
						LOG.debug("OK {} {}", diff, s.getRoute());
					}
				}
				// check if circuit is establishing but doesn't had any action
				// for a longer period of time
				if ((!c.isEstablished()) && (!c.isClosed()))
				{
					if ((currentTimeMillis - c.getLastAction()) / MILLISEC > (2 * TorConfig.queueTimeoutCircuit))
					{
						LOG.debug("TorBackgroundMgmtThread.tearDownClosedCircuits(): closing (too long building) "
								+ c.toString());
						c.close(false);
					}
				}
				// check if this circuit should not accept more streams
				if (c.getEstablishedStreams() > TorConfig.getStreamsPerCircuit())
				{
					LOG.debug("TorBackgroundMgmtThread.tearDownClosedCircuits(): closing (maximum streams) "
							+ c.toString());
					c.close(false);
				}
				// if closed, recall close() again and again to do garbage
				// collection and stuff
				if (c.isClosed())
				{
					c.close(false);
				}
				// check if this circuit can be removed from the set of circuits
				if (c.isDestruct())
				{
					LOG.debug("TorBackgroundMgmtThread.tearDownClosedCircuits(): destructing circuit " + c.toString());
					tls.removeCircuit(c.getId());
				}
			}
		}
	}

	public void close()
	{
		// stop sub-thread
		directoryManagerThread.setStopped(true);
		directoryManagerThread.interrupt();
		// stop this thread
		this.stopped = true;
		this.interrupt();
	}

	public void cleanup()
	{
		final ListIterator<Thread> brtIterator = backgroundThreads.listIterator();
		while (brtIterator.hasNext())
		{
			final Thread brt = brtIterator.next();
			if (brt.isAlive())
			{
				brt.interrupt();
			}
			brtIterator.remove();
		}
	}

	@Override
	public void run()
	{
		try
		{
			sleep(INITIAL_INTERVAL_S * MILLISEC);
		}
		catch (final InterruptedException e)
		{
			LOG.debug("got IterruptedException : {}", e.getMessage(), e);
		}
		// run until killed
		outerWhile: while (!stopped)
		{
			try
			{
				currentTimeMillis = System.currentTimeMillis();
				// do work
				manageIdleCircuits();
				tearDownClosedCircuits();
				sendKeepAlivePackets();
				// update final state
				if (tor.getCircuitsStatus().getCircuitsEstablished() >= TorConfig.getMinimumIdleCircuits())
				{
					tor.updateStatus(NetLayerStatus.READY);
				}
				// wait
				sleep(INTERVAL_S * MILLISEC);
			}
			catch (final InterruptedException e)
			{
				LOG.error("stop thread1", e);
				break outerWhile;
			}
			catch (final Exception e)
			{
				LOG.error("stop thread2", e);
				break outerWhile;
			}
		}
		cleanup();
	}
}
