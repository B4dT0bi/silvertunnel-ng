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
package org.silvertunnel_ng.netlib.adapter.java;

import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.adapter.socket.SocketGlobalUtil;
import org.silvertunnel_ng.netlib.adapter.url.URLGlobalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows JVM global redirection of everything, in detail: * (DNS)
 * name service requests * TCP/IP socket/connections * URL requests
 * 
 * Internally, all other org.silvertunnel_ng.netlib.adapter.adapter packages are
 * used.
 * 
 * Detailed description: TODO wiki page
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class JvmGlobalUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(JvmGlobalUtil.class);

	/**
	 * Initialize the redirection. Set lower services NopNetLayer and
	 * NopNetAddressNameService (similar to method setNopNet()). The lower
	 * services can be changed later.
	 * 
	 * This method must be called as early as possible after JVM startup. It is
	 * usually called from a static initializer of your startup class.
	 * 
	 * This method call influences the complete Java JVM. This method can be
	 * called multiple times without any problems.
	 * 
	 * @throws IllegalStateException
	 *             if the method call came too late after JVM start, i.e. class
	 *             java.net.InetAddress was already initialized before calling
	 *             this method.
	 * 
	 *             This method can be called multiple times without any
	 *             problems, but URL.setURLStreamHandlerFactory() may not be
	 *             called before the first call of this method to avoid
	 *             problems.
	 */
	public static synchronized void init() throws IllegalStateException
	{
		// do not log here to avoid to disturb initialization

		// initialize everything first (must be done as early as possible)
		IllegalStateException firstException = null;
		try
		{
			NameServiceGlobalUtil.initNameService();
		}
		catch (final IllegalStateException e)
		{
			firstException = e;
			LOG.error("initialization (1/3) failed", e);
		}

		try
		{
			SocketGlobalUtil.initSocketImplFactory();
		}
		catch (final IllegalStateException e)
		{
			if (firstException == null)
			{
				firstException = e;
			}
			LOG.error("initialization (2/3) failed", e);
		}
		try
		{
			URLGlobalUtil.initURLStreamHandlerFactory();
		}
		catch (final IllegalStateException e)
		{
			if (firstException == null)
			{
				firstException = e;
			}
			LOG.error("initialization (3/3) failed", e);
		}
		LOG.info("init() ongoing");

		/*
		 * set first services: is not needed because this is already done in the
		 * initXXX() methods: NetAddressNameService firstNetAddressNameService =
		 * new NopNetAddressNameService(); NetLayer firstNetLayer = new
		 * NopNetLayer();
		 * 
		 * NameServiceGlobalUtil.setIpNetAddressNameService(
		 * firstNetAddressNameService);
		 * SocketGlobalUtil.setNetLayerUsedBySocketImplFactory(firstNetLayer);
		 * URLGlobalUtil
		 * .setNetLayerUsedByURLStreamHandlerFactory(firstNetLayer);
		 */

		// the end
		if (firstException == null)
		{
			// normal end
			LOG.info("init() end");
		}
		else
		{
			// end with Exception
			LOG.info("init() end with exception");

			// throw delayed Exception now
			throw firstException;
		}
	}

	/**
	 * Set lower services to the specified new values.
	 * 
	 * @param nextNetLayer
	 *            if null, do not change the current NetLayer
	 * @param nextNetAddressNameService
	 *            if null, do not change the current NetAddressNameService
	 * @param waitUntilReady
	 *            true=wait (block the current thread) until the NetLayer and
	 *            NetAddressNameService instances are up and ready
	 * @throws IllegalStateException
	 *             if the initialization was omitted or failed before
	 */
	public static synchronized void setNetLayerAndNetAddressNameService(final NetLayer nextNetLayer,
	                                                                    final NetAddressNameService nextNetAddressNameService,
	                                                                    final boolean waitUntilReady) throws IllegalStateException
	{
		LOG.info("setNetLayerAndNetAddressNameService(nextNetLayer={}"
				+ ", nextNetAddressNameService={})", nextNetLayer, nextNetAddressNameService);

		// action
		if (nextNetAddressNameService != null)
		{
			NameServiceGlobalUtil.initNameService();
			NameServiceGlobalUtil.setIpNetAddressNameService(nextNetAddressNameService);
		}
		final long time1 = System.currentTimeMillis();
		if (nextNetLayer != null)
		{
			SocketGlobalUtil.setNetLayerUsedBySocketImplFactory(nextNetLayer);
			URLGlobalUtil.setNetLayerUsedByURLStreamHandlerFactory(nextNetLayer);
		}

		// wait a bit
		if (waitUntilReady)
		{
			// wait until layer is ready
			nextNetLayer.waitUntilReady();

			// wait until the name service cache is timed out (if necessary) // TODO : check if this is really needed
			final long time2 = System.currentTimeMillis();
			try
			{
				Thread.sleep(Math.max(
						0,
						NameServiceGlobalUtil.getCacheTimeoutMillis() - (time2 - time1)));
			}
			catch (final InterruptedException e)
			{
				// ignore exception
				LOG.debug("got InterruptedException : {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * Set lower services to nextNetLayer and
	 * nextNetLayer.getNetAddressNameService().
	 * 
	 * @param nextNetLayer
	 *            if null, do not change anything
	 * @param waitUntilReady
	 *            true=wait (block the current thread) until the NetLayer and
	 *            NetAddressNameService instances are up and ready
	 * @throws IllegalStateException
	 *             if the initialization was omitted or failed before
	 */
	public static synchronized void setNetLayerAndNetAddressNameService(final NetLayer nextNetLayer, final boolean waitUntilReady)
			throws IllegalStateException
	{
		LOG.info("setNetLayerAndNetAddressNameService(nextNetLayer={})", nextNetLayer);

		if (nextNetLayer != null)
		{
			setNetLayerAndNetAddressNameService(nextNetLayer, nextNetLayer.getNetAddressNameService(), waitUntilReady);
		}
	}

	/**
	 * Set lower services to {@link NopNetLayer} and
	 * {@link NopNetAddressNameService}.
	 * 
	 * @param waitUntilReady
	 *            true=wait (block the current thread) until the NetLayer and
	 *            NetAddressNameService instances are up and ready
	 * @return the lower {@link NetLayer} that is used now.
	 * @throws IllegalStateException
	 *             if the initialization was omitted or failed before
	 */
	// public static synchronized NetLayer setNopNet(boolean waitUntilReady)
	// throws IllegalStateException {
	// NetLayer nextNetLayer =
	// NetFactory.getInstance().getNetLayerById(NetLayerIDs.NOP);
	// setNetLayerAndNetAddressNameService(nextNetLayer, waitUntilReady);
	//
	// return nextNetLayer;
	// }

	/**
	 * Set lower services to {@link TcpipNetLayer} and
	 * {@link DefaultIpNetAddressNameService}.
	 * 
	 * @param waitUntilReady
	 *            true=wait (block the current thread) until the NetLayer and
	 *            NetAddressNameService instances are up and ready
	 * @return the lower {@link NetLayer} that is used now.
	 * @throws IllegalStateException
	 *             if the initialization was omitted or failed before
	 */
	// public static synchronized NetLayer setDefaultNet(boolean waitUntilReady)
	// throws IllegalStateException {
	// NetLayer nextNetLayer =
	// NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
	// setNetLayerAndNetAddressNameService(nextNetLayer, waitUntilReady);
	//
	// return nextNetLayer;
	// }

	/**
	 * Set lower services to {@link TorNetLayer} and
	 * {@link TorNetAddressNameService}.
	 * 
	 * @param waitUntilReady
	 *            true=wait (block the current thread) until the NetLayer and
	 *            NetAddressNameService instances are up and ready
	 * @return the lower {@link NetLayer} that is used now.
	 * @throws IllegalStateException
	 *             if the initialization was omitted or failed before
	 */
	// public static synchronized NetLayer setTorNet(boolean waitUntilReady)
	// throws IllegalStateException {
	// NetLayer nextNetLayer =
	// (ExtendedNetLayer)NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
	// setNetLayerAndNetAddressNameService(nextNetLayer, waitUntilReady);
	//
	// return nextNetLayer;
	// }
}
