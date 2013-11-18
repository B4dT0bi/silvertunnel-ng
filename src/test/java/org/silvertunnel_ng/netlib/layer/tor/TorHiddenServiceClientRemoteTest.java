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

import static org.testng.AssertJUnit.fail;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Tor's hidden services (client implementation).
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorHiddenServiceClientRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorHiddenServiceClientRemoteTest.class);

	@BeforeClass
	public void setUp()
	{
		// setting the route length to 2 (we do not need high security for our
		// tests.
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS, "1"); // just
																						// 2
																						// initial
																						// circuit
																						// for
																						// faster
																						// tests
		TorConfig.reloadConfigFromProperties();
	}

	@Override
	@Test(timeOut = 60000)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		super.initializeTor();
	}

	@Test(timeOut = 120000, dependsOnMethods = { "initializeTor" })
	public void testAccessToTorsExampleOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "duskgytldkxiuqc6.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);
		// final String TORCHECK_HOSTNAME = "pev3fvr4qjgh63go.onion";
		// final TcpipNetAddress TORCHECK_NETADDRESS = new
		// TcpipNetAddress(TORCHECK_HOSTNAME, 2203);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			if (!httpResponseStr.contains("This is the example page for Tor's"))
			{
				fail("did not get correct response of hidden service, response body=" + httpResponseStr);
			}
		}
		finally
		{
			if (topSocket != null)
			{
				topSocket.close();
			}
		}
	}

	@Test(timeOut = 120000, dependsOnMethods = { "initializeTor" })
	public void testAccessToSilvertunnelOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "h6hk2h7fnr66d4o3.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			if (!httpResponseStr.contains("httptest works."))
			{
				fail("did not get correct response of hidden service, response body=" + httpResponseStr);
			}
		}
		finally
		{
			if (topSocket != null)
			{
				topSocket.close();
			}
		}
	}

	// @Ignore(value = "this service is not always on")
	@Test(timeOut = 60000, dependsOnMethods = { "initializeTor" }, enabled = false)
	public void testAccessToWikileaksOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "gaddbiwdftapglkq.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			final String SUCCESS_STR = "Click here to make a secure submission";
			if (!httpResponseStr.contains(SUCCESS_STR))
			{
				fail("did not get correct response of hidden service, response body=" + httpResponseStr);
			}
		}
		finally
		{
			if (topSocket != null)
			{
				topSocket.close();
			}
		}
	}

	@Test(timeOut = 120000, dependsOnMethods = { "initializeTor" })
	public void testAccessToTORDirectoryOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "dppmfxaacucguzpc.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			final String SUCCESS_STR = "TORDIR - Link List";
			if (!httpResponseStr.contains(SUCCESS_STR))
			{
				fail("did not get correct response of hidden service, response body=" + httpResponseStr);
			}
		}
		finally
		{
			if (topSocket != null)
			{
				topSocket.close();
			}
		}
	}

	// @Ignore(value =
	// "this service is just a test service and only temporarily available")
	@Test(timeOut = 60000, dependsOnMethods = { "initializeTor" }, enabled = false)
	public void testAccessToSTHiddenServiceOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "4xuwatxuqzfnqjuz.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			final String SUCCESS_STR = "Speziell f&uuml;r kleine und mittlere Unternehmen sowie f&uuml;r Privatpersonen";
			if (!httpResponseStr.contains(SUCCESS_STR))
			{
				fail("did not get correct response of hidden service, response body=" + httpResponseStr);
			}
		}
		finally
		{
			if (topSocket != null)
			{
				topSocket.close();
			}
		}
	}
}
