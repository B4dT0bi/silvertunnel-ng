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

package org.silvertunnel_ng.netlib.layer.tor;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test that reading system properties in TorConfic static constructor works.
 * 
 * These tests are not executed by default.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class DynamicTorconfigRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DynamicTorconfigRemoteTest.class);
	// TODO : add more config-tests and enable them
	@Test(timeOut = 120000, enabled = false)
	public void testHttpGetWithAdapterURL() throws Exception
	{
		//
		// setting system properties
		//

		// reasonable values: 2, 3, 5
		// default: 5
		System.setProperty("torMinimumIdleCircuits", "2");

		// reasonable values: 30000, 20000, 10000
		// default: 10000
		System.setProperty("torMaxAllowedSetupDurationMs", "30000");

		//
		// execute the test
		LOG.info("Before generating lowerNetLayer");
		final NetLayer lowerNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
		LOG.info("Generated a lowerNetLayer");

		LOG.info("Before TOR is started up");
		((TorNetLayer) lowerNetLayer).waitUntilReady();
		LOG.info("Successfully started TOR");

		LOG.info("Before running NetlibURLStreamHandlerFactory");
		final NetlibURLStreamHandlerFactory factory = new NetlibURLStreamHandlerFactory(
				false);
		factory.setNetLayerForHttpHttpsFtp(lowerNetLayer);
		LOG.info("After running NetlibURLStreamHandlerFactory");

		LOG.info("Before assigning URL protocol");
		final String urlStr = "http://check.torproject.org";
		final URLStreamHandler handler = factory.createURLStreamHandler("http");
		final URL context = null;
		final URL url = new URL(context, urlStr, handler);
		LOG.info("After assigning URL protocol");

		LOG.info("Before openning URL connection");
		final URLConnection urlConnection = url.openConnection();
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(false);
		urlConnection.connect();
		LOG.info("After openning URL connection");
	}
}
