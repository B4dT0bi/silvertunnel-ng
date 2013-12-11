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
import java.security.SecureRandom;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitAdmin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Node;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnectionAdmin;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayEstablishRendezvous;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayIntroduce1;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptor;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptorService;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.directory.SDIntroductionPoint;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceDescriptorCache;
import org.silvertunnel_ng.netlib.layer.tor.stream.TCPStream;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Hidden Service Client.
 * Used for connecting to a Tor Hidden Service through Tor network.
 * 
 * @author hapke
 * @author Tobias Boese
 *
 */
public final class HiddenServiceClient
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HiddenServiceClient.class);

	/** a {@link RendezvousServiceDescriptorService} instance. */
	private static RendezvousServiceDescriptorService rendezvousServiceDescriptorService = RendezvousServiceDescriptorService.getInstance();

	/**
	 * makes a connection to a hidden service.
	 * 
	 * @param directory
	 *            tor environment
	 * @param torEventService
	 *            tor environment
	 * @param tlsConnectionAdmin
	 *            tor environment
	 * @param torNetLayer
	 *            tor environment
	 * @param spo
	 *            connection destination
	 * @return
	 * @throws IOException
	 */
	static TCPStream connectToHiddenService(final Directory directory,
											final TorEventService torEventService,
											final TLSConnectionAdmin tlsConnectionAdmin,
											final NetLayer torNetLayer,
											final TCPStreamProperties spo) throws IOException
	{
		// String address, x, y;
		final String z = Encoding.parseHiddenAddress(spo.getHostname()).get("z");

		//
		// get a copy from the rendezvous service descriptor
		//
		RendezvousServiceDescriptor sd = HiddenServiceDescriptorCache.getInstance().get(z);
		if (sd == null || (!sd.isPublicationTimeValid()))
		{
			// no valid entry in cache: retrieve a fresh one
			sd = rendezvousServiceDescriptorService.loadRendezvousServiceDescriptorFromDirectory(z, directory, torNetLayer);
			// cache it
			HiddenServiceDescriptorCache.getInstance().put(z, sd);
		}
		if (sd == null)
		{
			throw new IOException("connectToHiddenService(): couldn't retrieve RendezvousServiceDescriptor for z=" + z);
		}
		LOG.info("connectToHiddenService(): use RendezvousServiceDescriptor=" + sd);

		//
		// action
		//
		boolean establishedRendezvousPoint = false;
		boolean connectedToIntroPoint = false;
		boolean didRendezvous = false;
		for (int attempts = 0; attempts < spo.getConnectRetries(); attempts++)
		{
			Circuit rendezvousPointCircuit = null;
			try
			{
				//
				// establish a rendezvous point (section 1.7 of Tor Rendezvous
				// Specification)
				//
				RendezvousPointData rendezvousPointData = null;
				rendezvousPointData = createRendezvousPoint(directory, torEventService, tlsConnectionAdmin, z);
				rendezvousPointCircuit = rendezvousPointData.getMyRendezvousCirc();
				rendezvousPointCircuit.setServiceDescriptor(sd);
				establishedRendezvousPoint = true;
				LOG.info("connectToHiddenService(): use circuit to rendezvous point=" + rendezvousPointData.getMyRendezvousCirc());

				//
				// Introduction: from Alice's OP to Introduction Point (section
				// 1.8 & 1.9 of Tor Rendezvous Specification)
				//
				for (final SDIntroductionPoint introPoint : sd.getIntroductionPoints())
				{
					try
					{
						final Node introPointServicePublicKeyNode = sendIntroduction1Cell(directory, 
						                                                                  torEventService,
						                                                                  tlsConnectionAdmin,
						                                                                  rendezvousPointData, 
						                                                                  introPoint, 
						                                                                  z);
						connectedToIntroPoint = true;

						//
						// Rendezvous (section 1.10 of Tor Rendezvous
						// Specification)
						//
						doRendezvous(rendezvousPointCircuit, introPointServicePublicKeyNode, z);
						didRendezvous = true;

						//
						// Creating stream(s) (section 1.11 of Tor Rendezvous
						// Specification)
						//

						// connect - with empty address in begin cell set
						final String hiddenServiceExternalAddress = "";
						final TCPStreamProperties tcpProps = new TCPStreamProperties(hiddenServiceExternalAddress, spo.getPort());
						return new TCPStream(rendezvousPointCircuit, tcpProps);
					}
					catch (TorException exception)
					{
						LOG.debug("got Exception while rendezvous", exception);
					}
				}
			}
			catch (final Exception e)
			{
				LOG.info("got Exception", e);
				// release resources
				if (rendezvousPointCircuit != null)
				{
					rendezvousPointCircuit.close(true);
					rendezvousPointCircuit = null;
				}
			}
			finally
			{
				// set flag for later release of resources
				if (rendezvousPointCircuit != null)
				{
					rendezvousPointCircuit.setCloseCircuitIfLastStreamIsClosed(true);
				}
			}
		}

		//
		// error occurred - send suitable error messages
		//
		String msg;
		if (!establishedRendezvousPoint)
		{
			msg = "connectToHiddenService(): coudn't establishing rendezvous point for " + z;
		}
		else if (!connectedToIntroPoint)
		{
			msg = "connectToHiddenService(): couldn't connect to an introduction point of " + z;
		}
		else if (!didRendezvous)
		{
			msg = "connectToHiddenService(): coudn't make a rendezvous for " + z;
		}
		else
		{
			msg = "connectToHiddenService(): couldn't connect to remote server of " + z;
		}
		LOG.warn(msg);
		throw new IOException(msg);
	}

	/**
	 * Establish a circuit to a new rendezvous point.
	 * 
	 * "establish a rendezvous point (section 1.7 of Tor Rendezvous Specification)"
	 * 
	 * @param directory
	 * @param torEventService
	 * @param tlsConnectionAdmin
	 * @param z
	 * @return the rendezvous point; not null
	 * @throws IOException
	 * @throws TorException
	 */
	private static RendezvousPointData createRendezvousPoint(final Directory directory,
																final TorEventService torEventService,
																final TLSConnectionAdmin tlsConnectionAdmin,
																final String z) throws IOException, TorException
	{
		Circuit myRendezvousCirc = null;
		try
		{
			TCPStreamProperties streamProperties = new TCPStreamProperties();
			streamProperties.setFastRoute(true);
			streamProperties.setStableRoute(true);
			streamProperties.setExitPolicyRequired(false);
			myRendezvousCirc = CircuitAdmin.provideSuitableExclusiveCircuit(tlsConnectionAdmin, directory, streamProperties, torEventService);
			if (myRendezvousCirc == null || !myRendezvousCirc.isEstablished())
			{
				throw new TorException("getNewRendezvousPoint(): couldnt establish rendezvous point for " + z + " - at the moment");
			}
			final RouterImpl rendezvousPointRouter = myRendezvousCirc.getRouteNodes()[myRendezvousCirc.getRouteEstablished() - 1].getRouter();

			LOG.info("getNewRendezvousPoint(): establishing rendezvous point for " + z + " at " + rendezvousPointRouter);
			final SecureRandom rnd = new SecureRandom();
			final byte[] rendezvousCookie = new byte[20];
			rnd.nextBytes(rendezvousCookie);

			myRendezvousCirc.sendCell(new CellRelayEstablishRendezvous(myRendezvousCirc, rendezvousCookie));
			// TODO: not needed?
			// myRendezvousCirc.getStreamHistory().add(spo.getHostname());

			// wait for answer
			final CellRelay rendezvousACK = myRendezvousCirc.receiveRelayCell(CellRelay.RELAY_RENDEZVOUS_ESTABLISHED);
			if (rendezvousACK.getLength() > 0)
			{
				throw new TorException("connectToHiddenService(): Got NACK from RENDEZVOUS Point");
			}

			// success
			LOG.info("getNewRendezvousPoint(): establishing rendezvous point for " + z + " at " + rendezvousPointRouter);
			return new RendezvousPointData(rendezvousCookie, rendezvousPointRouter, myRendezvousCirc);
		}
		catch (final IOException e)
		{
			if (myRendezvousCirc != null)
			{
				myRendezvousCirc.close(true);
			}
			throw e;
		}
		catch (final TorException e)
		{
			if (myRendezvousCirc != null)
			{
				myRendezvousCirc.close(true);
			}
			throw e;
		}
	}

	/**
	 * Send introduction1 cell.
	 * 
	 * "Introduction: from Alice's OP to Introduction Point (section 1.8 of Tor Rendezvous Specification)"
	 * 
	 * @param directory
	 * @param torEventService
	 * @param tlsConnectionAdmin
	 * @param rendezvousPointData
	 * @param introPoint
	 *            send the introduction1 cell to this introPoint
	 * @param z
	 * @return introPointServicePublicKeyNode; not null
	 * @throws IOException
	 * @throws TorException
	 * @throws InterruptedException
	 */
	private static Node sendIntroduction1Cell(final Directory directory,
	                                          final TorEventService torEventService,
	                                          final TLSConnectionAdmin tlsConnectionAdmin,
	                                          final RendezvousPointData rendezvousPointData,
	                                          final SDIntroductionPoint introPoint,
	                                          final String z) throws IOException, TorException, InterruptedException
	{

		final Fingerprint introPointFingerprint = introPoint.getIdentifierAsFingerprint();
		LOG.info("sendIntroduction1Cell(): contacting introduction point=" + introPointFingerprint + " for " + z);

		// build new circuit where the last node is introduction point
		final TCPStreamProperties spIntro = new TCPStreamProperties();
		spIntro.setExitPolicyRequired(false);
		spIntro.setCustomExitpoint(introPointFingerprint);
		Circuit myIntroCirc = null;
		try
		{
//			myIntroCirc = new Circuit(tlsConnectionAdmin, directory, spIntro, torEventService);
			myIntroCirc = CircuitAdmin.provideSuitableExclusiveCircuit(tlsConnectionAdmin, directory, spIntro, torEventService);

			if (!myIntroCirc.isEstablished())
			{
				LOG.debug("Circuit to Introductionpoint not successful.");
				throw new TorException("Circuit to Introductionpoint " + introPointFingerprint + " not successful.");
			}
			LOG.info("sendIntroduction1Cell(): use Circuit to introduction point=" + myIntroCirc);

			// send CellIntro1 data encrypted with PK of the introPoint
			final RouterImpl introPointServicePublicKey = new RouterImpl(introPoint.getServicePublicKey());
			final Node introPointServicePublicKeyNode = new Node(introPointServicePublicKey);
			myIntroCirc.sendCell(new CellRelayIntroduce1(myIntroCirc,
															rendezvousPointData.getRendezvousCookie(),
															introPoint,
															introPointServicePublicKeyNode,
															rendezvousPointData.getRendezvousPointRouter()));

			// wait for ack
			final CellRelay introACK = myIntroCirc.receiveRelayCell(CellRelay.RELAY_COMMAND_INTRODUCE_ACK);
			if (introACK.getLength() > 0)
			{
				throw new TorException("sendIntroduction1Cell(): Got NACK from Introduction Point introACK=" + introACK);
			}
			// introduce ACK is received
			LOG.info("sendIntroduction1Cell(): Got ACK from Intro Point");

			return introPointServicePublicKeyNode;
		}
		finally
		{
			// close the circuit: not needed anymore
			if (myIntroCirc != null)
			{
				myIntroCirc.close(true);
			}
		}

	}

	/**
	 * Implementation of the rendezvous.
	 * 
	 * "Rendezvous (section 1.10 of Tor Rendezvous Specification)"
	 * 
	 * @param myRendezvousCircuit
	 *            try to rendezvous here
	 * @param introPointServicePublicKeyNode
	 * @param z
	 * @throws TorException
	 * @throws IOException
	 */
	private static void doRendezvous(final Circuit myRendezvousCircuit, 
	                                 final Node introPointServicePublicKeyNode, 
	                                 final String z)
														throws TorException,
														       IOException
	{
		// wait for answer from the hidden service (RENDEZVOUS2)
		// TODO : check if it still works!!! (tobi)
//		final int oldTimeout = myRendezvousCircuit.getQueue().getTimeoutMs();
//		if (oldTimeout < 120 * 1000) // TODO : 2 minutes timeout ? really?
//		{
//			myRendezvousCircuit.getQueue().setTimeoutMs(120 * 1000);
//		}
		final CellRelay r2Relay = myRendezvousCircuit.receiveRelayCell(CellRelay.RELAY_RENDEZVOUS2);
//		myRendezvousCircuit.getQueue().setTimeoutMs(oldTimeout);
		// finish Diffie-Hellman
		final byte[] dhGy = new byte[148];
		System.arraycopy(r2Relay.getData(), 0, dhGy, 0, 148);
		introPointServicePublicKeyNode.finishDh(dhGy);

		myRendezvousCircuit.addNode(introPointServicePublicKeyNode);

		LOG.info("doRendezvous(): succesfully established rendezvous with " + z);
	}
}
