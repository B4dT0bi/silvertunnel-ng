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

import static org.testng.AssertJUnit.*;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test many connection through Tor.
 * 
 * @author Tobias Boese
 */
public final class TorConnectionsRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorConnectionsRemoteTest.class);

	/**
	 * if NUM_OF_TEST_EXECUTIONS==1 then this test class behaves like an
	 * unparameterized one.
	 */
	private static final int NUM_OF_TEST_EXECUTIONS = 200;

	/** 
	 * Parametrized testcase.
	 * @return empty Objects with size of NUM_OF_TEST_EXECUTIONS
	 */
	@DataProvider(name = "multipleTestExecutions")
	public static Object[][] multipleTestExecutions()
	{
		return new Object[NUM_OF_TEST_EXECUTIONS][0];
	}
	/**
	 * Modify the standard tor settings.
	 */
	@BeforeClass
	public void setUp()
	{
		// setting the route length to 2 (we do not need high security for our
		// tests.
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS, "1"); // just
																						// 1
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

	private static final String BEGIN = "Begin";
	private static final String END = "enD";
	private static final byte[] HTTPTEST_TESTFILE_CONTENT_100_KBYTES = ByteArrayUtil.getByteArray(BEGIN,
																											100000 - BEGIN.length() - END.length(),
																											END);

	/**
	 * Download 100KB and check the speed.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 20000, dependsOnMethods = { "initializeTor" }, dataProvider = "multipleTestExecutions", enabled = false)
	public void testDownloadPerformance() throws Exception
	{
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, HttpUtil.HTTPTEST_SERVER_NETADDRESS);

		// use open socket for to execute HTTP request and to check the response
		// (download of file of size 100,000 bytes in max 20s = 5KByte/s)
		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, HttpUtil.HTTPTEST_SERVER_NETADDRESS, "/httptest/testfile100000bytes.bin", 20000);
		AssertJUnit.assertArrayEquals(HTTPTEST_TESTFILE_CONTENT_100_KBYTES, httpResponse);
		topSocket.close();
	}

	/**
	 * Executes getip.php for fetching our ip address.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 20000, dependsOnMethods = { "initializeTor" }, dataProvider = "multipleTestExecutions")
	public void testGetIP() throws Exception
	{
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, HttpUtil.HTTPTEST_SERVER_NETADDRESS);

		// use open socket for to execute HTTP request and to check the response
		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, HttpUtil.HTTPTEST_SERVER_NETADDRESS, "/httptest/getip.php", 20000);
		assertNotNull(httpResponse);
		assertTrue(httpResponse.length > 0);
		topSocket.close();
		LOG.info("Our IP : " + new String(httpResponse));
	}

	/**
	 * Check if we are really using the Tor network.
	 * 
	 * http://httptest.silvertunnel-ng.org/checktor.php will check the requester ip against a Tor exit node db.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 20000, dependsOnMethods = { "initializeTor" }, dataProvider = "multipleTestExecutions", enabled = false)
	public void testThatRequestGoesThroughTorNetworkVariantOwn() throws Exception
	{
		final String TORCHECK_HOSTNAME = "httptest.silvertunnel-ng.org";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/checktor.php", 5000);
		String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
		LOG.info("http response body: " + httpResponseStr);

		assertEquals("Congratulations. Your browser is configured to use Tor.", httpResponseStr);
		// /////////////////////////////////////////////////
		// crosscheck
		// /////////////////////////////////////////////////
		NameServiceGlobalUtil.initNameService();
		NameServiceGlobalUtil.setIpNetAddressNameService(DefaultIpNetAddressNameService.getInstance());
		// create connection
		final NetSocket topSocket2 = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP).createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse2 = HttpUtil.get(topSocket2, TORCHECK_NETADDRESS, "/checktor.php", 5000);
		final String httpResponseStr2 = ByteArrayUtil.showAsString(httpResponse2);
		LOG.info("http response body (crosscheck): " + httpResponseStr2);

		assertEquals("Sorry. You are not using Tor.", httpResponseStr2);
	}
}
