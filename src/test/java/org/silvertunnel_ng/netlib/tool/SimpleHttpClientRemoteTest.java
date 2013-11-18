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


package org.silvertunnel_ng.netlib.tool;

import static org.testng.AssertJUnit.assertEquals;

import java.net.URL;
import java.util.Map;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of class HttpClient.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SimpleHttpClientRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClientRemoteTest.class);

	private static final String POSTTEST_URL = "http://httptest.silvertunnel-ng.org/httptest/posttest.php";
	private static final String POSTTEST_URL2 = "http://109.123.119.163:9031/tor/rendezvous2/publish";

	@BeforeClass
	public void setUp()
	{
		NameServiceGlobalUtil.initNameService();
		NameServiceGlobalUtil.setIpNetAddressNameService(DefaultIpNetAddressNameService.getInstance());
	}
	
	@Test(timeOut = 15000)
	public void testGetRequest() throws Exception
	{
		// generate the id
		final int randomNo = (int) (1000000000 * Math.random());
		final String id = "testGetRequest" + randomNo;

		// communicate with the remote side
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(
				NetLayerIDs.TCPIP);
		final String path = "/httptest/smalltest.php?id=" + id;
		final String httpResponse = SimpleHttpClient.getInstance().get(
				netLayer, HttpUtil.HTTPTEST_SERVER_NETADDRESS, path);

		// check response
		final String expectedResponse = "<response><id>" + id
				+ "</id></response>\n";
		assertEquals("wrong response", expectedResponse, httpResponse);
	}

	@Test(timeOut = 15000)
	public void testPostRequest() throws Exception
	{
		final String DATA_TO_POST = "Das sind die\nPost\nDaten";
		final String EXPECTED_RESPONSE = "<postedData>" + DATA_TO_POST
				+ "</postedData>";
		final long TIMEOUT_MS = 3000;

		// prepare request
		final URL url = new URL(POSTTEST_URL);
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(
				NetLayerIDs.TCPIP);
		final int port = url.getPort();
		final TcpipNetAddress httpServerNetAddress = new TcpipNetAddress(
				url.getHost(), port < 0 ? 80 : port);
		final Map<String, Object> localProperties = null;
		String pathOnHttpServer = url.getPath();
		if (pathOnHttpServer == null || pathOnHttpServer.length() < 1)
		{
			pathOnHttpServer = "/";
		}
		LOG.info("pathOnHttpServer=" + pathOnHttpServer);

		// execute request and check response
		final NetSocket netSocket = netLayer.createNetSocket(localProperties, /* localAddress */
				null, httpServerNetAddress);
		final String response = new String(HttpUtil.getInstance().post(
				netSocket, httpServerNetAddress, pathOnHttpServer,
				DATA_TO_POST.getBytes(Util.UTF8), TIMEOUT_MS), Util.UTF8);
		if (!response.contains(EXPECTED_RESPONSE))
		{
			assertEquals("wrong result", EXPECTED_RESPONSE, response);
		}
	}
}
