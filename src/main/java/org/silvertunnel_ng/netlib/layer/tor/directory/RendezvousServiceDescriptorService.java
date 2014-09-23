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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClient;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logic to handle Service Descriptors of hidden services.
 * 
 * see https
 *      ://www.torproject.org/doc/design-paper/tor-design.html#sec:rendezvous
 * see http
 *      ://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend
 *      -spec.txt
 * 
 * @author Andriy
 * @author Lexi
 * @author hapke
 * @author Tobias Boese
 */
public class RendezvousServiceDescriptorService
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(RendezvousServiceDescriptorService.class);

	private static RendezvousServiceDescriptorService instance = new RendezvousServiceDescriptorService();

	/** service dependency. */
	private final HttpUtil httpUtil = HttpUtil.getInstance();

	/**
	 * Number of non-consecutive replicas (i.e. distributed somewhere in the
	 * ring) for a descriptor.
	 */
	private static final int RENDEZVOUS_NUMBER_OF_NON_CONSECUTIVE_REPLICAS = 2;

	public static RendezvousServiceDescriptorService getInstance()
	{
		return instance;
	}

	/**
	 * Loads a RendezvousServiceDescriptor from the network.
	 * 
	 * @param z
	 *            the z-part of the address/domain name = rendezvous descriptor
	 *            service ID
	 * @param directory
	 * @param torNetLayer
	 *            NetLayer to establish stream that goes through Tor network -
	 *            used to load rendezvous ServiceDescriptor
	 */
	public RendezvousServiceDescriptor loadRendezvousServiceDescriptorFromDirectory(String z,
																					Directory directory,
																					NetLayer torNetLayer) throws IOException
	{
		final String hiddenServicePermanentIdBase32 = z;
		final Long now = System.currentTimeMillis();
		final String PRE = "loadRendezvousServiceDescriptorFromDirectory(): ";

		int attempts = TorConfig.getRetriesConnect();
		while (attempts > 0)
		{
			for (int replica = 0; replica < RENDEZVOUS_NUMBER_OF_NON_CONSECUTIVE_REPLICAS; replica++)
			{
				final byte[] descriptorId = RendezvousServiceDescriptorUtil.getRendezvousDescriptorId(hiddenServicePermanentIdBase32, replica, now)
						.getDescriptorId();
				final String descriptorIdBase32 = Encoding.toBase32(descriptorId);
				final String descriptorIdHex = Encoding.toHexStringNoColon(descriptorId);
				final Fingerprint descriptorIdAsFingerprint = new FingerprintImpl(descriptorId);

				// try the routers/hidden service directory servers that are
				// responsible for the descriptorId
				final Collection<RouterImpl> routers = directory.getThreeHiddenDirectoryServersWithFingerprintGreaterThan(descriptorIdAsFingerprint);
				for (final RouterImpl r : routers)
				{
					TcpipNetAddress dirAddress = r.getDirAddress();
					dirAddress = new TcpipNetAddress(dirAddress.getHostnameOrIpaddress() + ":" + dirAddress.getPort());
					LOG.info(PRE + "try fetching service descriptor for " + z + " with descriptorID base32/hex=" + descriptorIdBase32 + "/"
							+ descriptorIdHex + " (with replica=" + replica + ") from " + r);

					// try to load from one router/hidden service directory
					// server
					String response = null;
					try
					{
						response = retrieveServiceDescriptor(torNetLayer, dirAddress, descriptorIdBase32);
					}
					catch (final Exception e)
					{
						LOG.warn("unable to connect to or to load data from directory server " + r + "(" + e.getMessage() + ")", e);
						continue;
					}

					// response: OK
					if (LOG.isDebugEnabled())
					{
						LOG.debug(PRE + "found descriptorIdBase32=" + descriptorIdBase32 + " with result(plain)=" + response);
					}
					try
					{
						final RendezvousServiceDescriptor result = new RendezvousServiceDescriptor(response, System.currentTimeMillis());
						return result;

					}
					catch (final TorException e)
					{
						LOG.info(PRE + "problem parsing Service Descriptor for " + z, e);
						continue;
					}
				}
				--attempts;
			}
		}
		LOG.warn(PRE + "unable to fetch service descriptor for " + z);
		throw new IOException("unable to fetch service descriptor for " + z);
	}

	/**
	 * Save a RendezvousServiceDescriptor in the network, i.e. advertise
	 * introduction points of a hidden service.
	 * 
	 * @param directory
	 * @param torNetLayerToConnectToDirectoryService
	 *            NetLayer to establish stream that goes through Tor network -
	 *            used to save rendezvous ServiceDescriptor
	 * @param hiddenServiceProps
	 * @throws IOException
	 * @throws TorException
	 */
	public void putRendezvousServiceDescriptorToDirectory(final Directory directory,
	                                                      final NetLayer torNetLayerToConnectToDirectoryService,
	                                                      final HiddenServiceProperties hiddenServiceProps) throws IOException, TorException
	{
		// get the the z-part of the address/domain name
		final String hiddenServicePermanentIdBase32 = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(hiddenServiceProps.getPublicKey());
		final Long now = System.currentTimeMillis();
		final String PRE = "putRendezvousServiceDescriptorToDirectory(): ";

		// try to post the descriptors
		final AtomicInteger advertiseSuccess = new AtomicInteger(0);
		for (int replica = 0; replica < RENDEZVOUS_NUMBER_OF_NON_CONSECUTIVE_REPLICAS; replica++)
		{
			try
			{
				final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(hiddenServicePermanentIdBase32,
				                                                                       replica,
				                                                                       now,
				                                                                       hiddenServiceProps.getPublicKey(),
				                                                                       hiddenServiceProps.getPrivateKey(),
				                                                                       hiddenServiceProps.getIntroPoints());
				final byte[] descriptorId = sd.getDescriptorId();
				final String descriptorIdBase32 = Encoding.toBase32(descriptorId);
				final String descriptorIdHex = Encoding.toHexStringNoColon(descriptorId);
				final Fingerprint descriptorIdAsFingerprint = new FingerprintImpl(descriptorId);
				final int replicaFinal = replica;

				// try to post the descriptor to hidden service directory
				// servers that are responsible for the descriptorId -
				// do it in parallel
				final Collection<RouterImpl> routers = directory.getThreeHiddenDirectoryServersWithFingerprintGreaterThan(descriptorIdAsFingerprint);
				for (final RouterImpl ro : routers)
				{
					final RouterImpl r = ro;
					new Thread()
					{
						@Override
						public void run()
						{
							TcpipNetAddress dirAddress = r.getDirAddress();
							dirAddress = new TcpipNetAddress(dirAddress.getHostnameOrIpaddress() + ":" + dirAddress.getPort());
							LOG.info(PRE + "try putting service descriptor for " + hiddenServicePermanentIdBase32 + " with descriptorID base32/hex="
									+ descriptorIdBase32 + "/" + descriptorIdHex + " (with replica=" + replicaFinal + ") from " + r);

							// try to post
							for (int attempts = 0; attempts < TorConfig.getRetriesConnect(); attempts++)
							{
								try
								{
									postServiceDescriptor(torNetLayerToConnectToDirectoryService, dirAddress, sd);
									advertiseSuccess.addAndGet(1);
									// finish thread
									return;
								}
								catch (final Exception e)
								{
									LOG.warn(PRE + "unable to connect to directory server " + dirAddress + "(" + e.getMessage() + ")");
									continue;
								}
							}
						}
					}.start();
				}
			}
			catch (final TorException e1)
			{
				LOG.warn("unexpected exception", e1);
			}
		}

		// wait until timeout or at least one descriptor is posted
		final int TIMEOUT_SECONDS = 120;
		final int MIN_NUMBER_OF_ADVERTISEMENTS = 1;
		for (int seconds = 0; seconds < TIMEOUT_SECONDS && advertiseSuccess.get() < MIN_NUMBER_OF_ADVERTISEMENTS; seconds++)
		{
			// wait a second
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{ /* do nothing */
				LOG.debug("got IterruptedException : {}", e.getMessage(), e);
			}
		}

		// at least one advertisement?
		if (advertiseSuccess.get() < MIN_NUMBER_OF_ADVERTISEMENTS)
		{
			throw new TorException("RendezvousServiceDescriptorService: no successful hidden service descriptor advertisement");
		}
	}

	/**
	 * Retrieve a service descriptor from a directory server via Tor.
	 * 
	 * @param torNetLayer
	 * @param dirNetAddress
	 *            address of the directory server
	 * @param descriptorIdBase32
	 * @return the service descriptor as String
	 * @throws Exception
	 */
	private String retrieveServiceDescriptor(final NetLayer torNetLayer, 
	                                         final TcpipNetAddress dirNetAddress, 
	                                         final String descriptorIdBase32)
	                                        		 throws Exception
	{
		// download descriptor
		try
		{
			LOG.debug("retrieveServiceDescriptor() from {}", dirNetAddress);
			LOG.debug("descriptorId : {}", descriptorIdBase32);
			final String path = "/tor/rendezvous2/" + descriptorIdBase32;

			return SimpleHttpClient.getInstance().get(torNetLayer, dirNetAddress, path);
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("retrieveServiceDescriptor() from {} failed", dirNetAddress, e);
			}
			throw e;
		}
	}

	/**
	 * Send a service descriptor to a directory server via Tor.
	 * 
	 * @param torNetLayerToConnectToDirectoryService
	 * @param dirNetAddress
	 *            address of the directory server
	 * @param sd
	 *            the service descriptor to send
	 * @throws IOException
	 * @throws TorException
	 */
	private void postServiceDescriptor(final NetLayer torNetLayerToConnectToDirectoryService, 
	                                   final TcpipNetAddress dirNetAddress, 
	                                   final RendezvousServiceDescriptor sd)
	                                		   throws IOException,
													  TorException
	{
		final String pathOnHttpServer = "/tor/rendezvous2/publish";
		final long timeoutInMs = 60000;

		// send post request and ignore the response:
		final NetSocket netSocket = torNetLayerToConnectToDirectoryService.createNetSocket(null, null, dirNetAddress);
		httpUtil.post(netSocket, dirNetAddress, pathOnHttpServer, sd.toByteArray(), timeoutInMs);
	}
}
