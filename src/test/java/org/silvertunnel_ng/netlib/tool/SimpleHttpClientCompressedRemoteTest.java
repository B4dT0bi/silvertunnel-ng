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

import static org.testng.AssertJUnit.assertNotNull;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of class HttpClient.
 * 
 * @author Tobias Boese
 */
public class SimpleHttpClientCompressedRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClientCompressedRemoteTest.class);

	private static final String PATH1 = "/tor/server/all";
	private static final String PATH2 = "/tor/status-vote/current/consensus";

	private static final String HTTPTEST_SERVER_NAME = "194.109.206.212";
	private static final int HTTPTEST_SERVER_PORT = 80;
	private static final TcpipNetAddress HTTPTEST_SERVER_NETADDRESS = new TcpipNetAddress(
			HTTPTEST_SERVER_NAME, HTTPTEST_SERVER_PORT);

	@Test(timeOut = 50000)
	public void testGetRequest() throws Exception
	{
		// communicate with the remote side
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
		final String httpResponse = SimpleHttpClientCompressed.getInstance().get(netLayer, HTTPTEST_SERVER_NETADDRESS, PATH1);
		assertNotNull(httpResponse);
		//LOG.info(httpResponse);
	}

	@Test(timeOut = 50000)
	public void testGetRequest2() throws Exception
	{
		// communicate with the remote side
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
		final String httpResponse = SimpleHttpClientCompressed.getInstance().get(netLayer, HTTPTEST_SERVER_NETADDRESS, PATH2);

		// check response
		assertNotNull(httpResponse);
		//LOG.info(httpResponse);
	}
}
