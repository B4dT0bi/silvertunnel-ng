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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitAdmin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.HiddenServiceInstance;
import org.silvertunnel_ng.netlib.layer.tor.circuit.HiddenServicePortInstance;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnectionAdmin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayEstablishIntro;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptorService;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptorUtil;
import org.silvertunnel_ng.netlib.layer.tor.directory.SDIntroductionPoint;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a hidden service.
 * 
 * @author hapke
 */
public class HiddenServiceServer
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HiddenServiceServer.class);

	/**
	 * all NetServerSockets... key=hiddenServicePermanentIdBase32 (z part of
	 * public key)
	 */
	private static Map<String, HiddenServiceInstance> allHiddenServices = new HashMap<String, HiddenServiceInstance>();

	private static HiddenServiceServer instance = new HiddenServiceServer();

	public static HiddenServiceServer getInstance()
	{
		return instance;
	}

	/**
	 * Establish a hidden service (server-side).
	 * 
	 * @param directory
	 * @param torEventService
	 * @param tlsConnectionAdmin
	 * @param torNetLayerToConnectToDirectoryService
	 * @param hiddenServiceProps
	 * @throws IOException
	 * @throws TorException
	 */
	public void provideHiddenService(final Directory directory,
	                                 final TorEventService torEventService,
	                                 final TLSConnectionAdmin tlsConnectionAdmin,
	                                 NetLayer torNetLayerToConnectToDirectoryService,
	                                 final HiddenServiceProperties hiddenServiceProps,
	                                 HiddenServicePortInstance hiddenServicePortInstance) throws IOException, TorException
	{
		// check whether this service was already published
		HiddenServiceInstance hiddenServiceInstance = null;
		final String hiddenServicePermanentIdBase32 = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(hiddenServiceProps.getPublicKey());
		synchronized (allHiddenServices)
		{
			hiddenServiceInstance = allHiddenServices.get(hiddenServicePermanentIdBase32);
			if (hiddenServiceInstance == null)
			{
				// new hidden service
				hiddenServiceInstance = new HiddenServiceInstance(hiddenServiceProps);
				allHiddenServices.put(hiddenServicePermanentIdBase32, hiddenServiceInstance);
				// add new port
				hiddenServiceInstance.putHiddenServicePortInstance(hiddenServicePortInstance);
			}
			else
			{
				// running hidden service
				// add new port (if it is still free)
				hiddenServiceInstance.putHiddenServicePortInstance(hiddenServicePortInstance);
				// further hidden service establishing is not necessary
			}
		}

		//
		// establish circuits to (randomly chosen) introduction points
		// - in parallel (as good as possible)
		//

		final ExecutorService executor = Executors.newCachedThreadPool();
		while (hiddenServiceProps.getNumberOfIntroPoints() < hiddenServiceProps.getMinimumNumberOfIntroPoints())
		{
			LOG.debug("establish circuits to (randomly chosen) introduction points for {}", hiddenServicePortInstance);

			// define the tasks for later parallel execution
			final Collection<Callable<Circuit>> allTasks = new ArrayList<Callable<Circuit>>();
			final int TRY_MORE_NUMBER_OF_INTRO_POINTS = 2;
			for (int i = hiddenServiceProps.getNumberOfIntroPoints(); i < hiddenServiceProps.getMinimumNumberOfIntroPoints()
					+ TRY_MORE_NUMBER_OF_INTRO_POINTS; i++)
			{
				final HiddenServiceInstance hiddenServiceInstanceFinal = hiddenServiceInstance;
				final Callable<Circuit> callable = new Callable<Circuit>()
				{
					/** establish Circuit to one introduction point */
					@Override
					public Circuit call() throws Exception
					{
						LOG.debug("Callable Started..");
						final TCPStreamProperties spIntro = new TCPStreamProperties();
						spIntro.setExitPolicyRequired(false);
						// spIntro.setCustomExitpoint(new
						// FingerprintImpl(Encoding.parseHex("F9B29AC7C015DE52419D7754A4A9E2F823A34771")));
						// // FreedomFries/98.157.178.36:443
						// spIntro.setCustomExitpoint(new
						// FingerprintImpl(Encoding.parseHex("F5A78ED829191D76C7399B86E4429F8F663E0C02")));
						// // bach/212.42.236.140:443
						Circuit result = null;
                        try {
                            result = establishIntroductionPoint(directory,
                                                                              torEventService,
                                                                              tlsConnectionAdmin,
                                                                              hiddenServiceProps,
                                                                              spIntro,
                                                                              hiddenServiceInstanceFinal);
                        } catch (Throwable throwable) {
                            LOG.warn("got Exception", throwable);
                        }
                        LOG.debug("Callable Finished!");
						return result;
					}
				};
				allTasks.add(callable);
			}

			// execute the tasks in parallel
			LOG.debug("start to execute the tasks in parallel");
			final int TIMEOUT_SECONDS = 120;
			Collection<Future<Circuit>> allTaskResults = null;
			try
			{
				allTaskResults = executor.invokeAll(allTasks, TIMEOUT_SECONDS, TimeUnit.SECONDS);
			}
			catch (final Exception e)
			{
				LOG.info("Exception in background task", e);
			}

			// check the results
			for (final Future<Circuit> taskResult : allTaskResults)
			{
				try
				{
					LOG.debug("analyse taskResult={}", taskResult);
					final Circuit c = taskResult.get();
					if (c != null)
					{
						final Router introPointRouter = c.getRouteNodes()[c.getRouteEstablished() - 1].getRouter();
						LOG.info("Tor.provideHiddenService: establish introduction point at " + introPointRouter.getNickname());
						hiddenServiceProps.addIntroPoint(new SDIntroductionPoint(Encoding.toBase32(introPointRouter.getFingerprint().getBytes()),
						                                                         new TcpipNetAddress(introPointRouter.getAddress().getAddress(),
						                                                                             introPointRouter.getOrPort()),
						                                                         introPointRouter.getOnionKey(),
						                                                         hiddenServiceProps.getPublicKey() // TODO: introPointRouter.getSigningKey() OR use intro-point specific key
								));
					}
				}
				catch (final InterruptedException e)
				{
					LOG.debug("task interruped");
				}
				catch (final Exception e)
				{
					LOG.info("in background task", e);
				}
			}
			LOG.info("(server side) circuit(s) to hidden service introduction point(s)==" + hiddenServiceProps.getIntroPoints() + " established for "
					+ hiddenServicePortInstance);
		}
		executor.shutdown();
		LOG.debug("establish circuits finished introduction points for {}", hiddenServicePortInstance);

		//
		// advertise introduction points/service descriptor
		// TODO : the descriptor should be renewed every hour but at least every 18 hours (see rend-spec.txt 1.4)
		//
		RendezvousServiceDescriptorService.getInstance().putRendezvousServiceDescriptorToDirectory(directory,
																								   torNetLayerToConnectToDirectoryService,
																								   hiddenServiceProps);
	}

	/**
	 * Establish an introduction point, inclusive Circuit to this introduction
	 * point.
	 * 
	 * @param service
	 * @param spIntro
	 * @return
	 */
	private Circuit establishIntroductionPoint(final Directory directory,
											   final TorEventService torEventService,
											   final TLSConnectionAdmin tlsConnectionAdmin,
											   final HiddenServiceProperties service,
											   final TCPStreamProperties spIntro,
											   final HiddenServiceInstance hiddenServiceInstance) throws Throwable {
		Circuit circuit = null;
		for (int i = 0; i < spIntro.getConnectRetries(); ++i)
		{
			try
			{
				// use circuit
				circuit = CircuitAdmin.provideSuitableExclusiveCircuit(tlsConnectionAdmin, directory, spIntro, torEventService);
				if (circuit == null || !circuit.isEstablished())
				{
					LOG.warn("could not establish Circuit to introduction point with spIntro=" + spIntro);
					Thread.sleep(5000); // TODO : check if this timeout is really needed
					continue;
				}
				// mark circuit as "used by hidden service to connect to
				// introduction point
				circuit.setHiddenServiceInstanceForIntroduction(hiddenServiceInstance);

				LOG.debug("Tor.provideHiddenService: send relay_establish_intro-Cell over {}", circuit.toString());
				circuit.sendCell(new CellRelayEstablishIntro(circuit, service));
				circuit.receiveRelayCell(CellRelay.RELAY_INTRO_ESTABLISHED);
				return circuit;

			}
			catch (final Exception e)
			{
				LOG.warn("Tor.provideHiddenService: " + e.getMessage(), e);
				if (circuit != null)
				{
					circuit.close(true);
				}
			}
		}
		return null;
	}
}
