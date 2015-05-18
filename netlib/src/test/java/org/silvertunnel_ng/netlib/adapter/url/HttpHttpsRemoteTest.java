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

package org.silvertunnel_ng.netlib.adapter.url;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSRemoteTest;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HttpHttpsRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpHttpsRemoteTest.class);

	private LoggingNetLayer loggingTcpipNetLayer;
	private LoggingNetLayer loggingTlsNetLayer;

	/**
	 * Initialize the URLStreamHandlerFactory.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			

		// enable redirection
		URLGlobalUtil.initURLStreamHandlerFactory();

		// create lower layer (TCP/IP)
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer, "upper tcpip  ");

		// create lower layer (TLS)
		final NetLayer tcpipNetLayer2 = new TcpipNetLayer();
		final NetLayer loggingTcpipNetLayer2 = new LoggingNetLayer(tcpipNetLayer2, "upper tcpip(tls)  ");
		final NetLayer tlsNetLayer = new TLSNetLayer(loggingTcpipNetLayer2);
		loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer, "upper tls  ");

		// select the NetSocket implementation
		URLGlobalUtil.setNetLayerUsedByURLStreamHandlerFactory(loggingTcpipNetLayer, loggingTlsNetLayer);
		LOG.info("------------------------------------------------------------------------------------------------");
	}

	/**
	 * After test execution: Let the URLStreamHandlerFactory behave as normal as
	 * possible.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public void tearDown() throws Exception
	{
		// select the NetSocket implementation
		final NetLayer tcpipNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
		final NetLayer tlsNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP);
		URLGlobalUtil.setNetLayerUsedByURLStreamHandlerFactory(tcpipNetLayer, tlsNetLayer);
	}

	@Test(timeOut = 100009999)
	public void testHttp() throws Exception
	{
		// action
		final long connectionCountStart = loggingTcpipNetLayer.getConnectionEstablisedCounter();
		final String urlStr = "http://www.gmx.net/";
		final URL url = new URL(urlStr);
		final URLConnection urlConnection = url.openConnection();

		// receive and check HTTP response
		final InputStream responseIs = urlConnection.getInputStream();
		checkResponse(responseIs);
		final long connectionCount = loggingTcpipNetLayer.getConnectionEstablisedCounter() - connectionCountStart;
		assertEquals(
				"wrong number of established connections (via logging layer) during test",
				1, connectionCount);
	}

	@Test(timeOut = 10000, enabled = false)
	//@Ignore(value = "only used for manual tests")
	public void testClientHttpHeaders() throws Exception
	{
		// action
		final long connectionCountStart = loggingTcpipNetLayer.getConnectionEstablisedCounter();
		final String urlStr = "http://www.xhaus.com/headers";
		final URL url = new URL(urlStr);
		final URLConnection urlConnection = url.openConnection();

		// receive and check HTTP response
		final InputStream responseIs = urlConnection.getInputStream();
		checkResponse(responseIs);
		final long connectionCount = loggingTcpipNetLayer.getConnectionEstablisedCounter() - connectionCountStart;
		assertEquals(
				"wrong number of established connections (via logging layer) during test",
				1, connectionCount);
	}

	@Test(timeOut = 10000)
	public void testHttps() throws Exception
	{
		// action
		final long connectionCountStart = loggingTlsNetLayer.getConnectionEstablisedCounter();
		final String urlStr = "https://www.gmx.net/";
		final URL url = new URL(urlStr);
		final URLConnection urlConnection = url.openConnection();

		// receive and check HTTP response
		final InputStream responseIs = urlConnection.getInputStream();
		checkResponse(responseIs);
		final long connectionCount = loggingTlsNetLayer
				.getConnectionEstablisedCounter() - connectionCountStart;
		assertEquals(
				"wrong number of established connections (via logging layer) during test",
				1, connectionCount);
	}

	/**
	 * Execute test_http again but with a lower timeout.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 10000)
	public void testHttp2() throws Exception
	{
		testHttp();
	}

	// /////////////////////////////////////////////////////
	// helper methods
	// /////////////////////////////////////////////////////

	private void checkResponse(final InputStream is) throws Exception
	{
		// read result/response data
		final int MAX_BUFFER_SIZE = 100000;
		final byte[] resultBuffer = ByteArrayUtil.readDataFromInputStream(MAX_BUFFER_SIZE, is);
		if (resultBuffer.length >= MAX_BUFFER_SIZE)
		{
			LOG.info("result buffer is full");
		}
		else
		{
			LOG.info("end of result stream");
		}
		final String result = new String(resultBuffer);

		// show and check result data
		LOG.debug("result=\"" + result + "\"");
		if (!result.contains(TLSRemoteTest.WEBPAGE_GMX_NET_CONTENT_SNIPPET))
		{
			fail("wrong result=\"" + result + "\"");
		}
	}
}
