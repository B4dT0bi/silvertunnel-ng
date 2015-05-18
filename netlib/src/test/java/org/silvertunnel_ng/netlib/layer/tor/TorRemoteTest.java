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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.HttpTestUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Tor's basic features.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorRemoteTest.class);

	@Override
	@Test(timeOut = 120000)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS, "2"); // just
																						// 2
																						// initial
																						// circuit
																						// for
																						// faster
																						// tests
		TorConfig.reloadConfigFromProperties();
		super.initializeTor();
	}

	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" })
	public void testWithHostname() throws Exception
	{
		// create connection
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, HttpUtil.HTTPTEST_SERVER_NETADDRESS);

		// use open socket to execute HTTP request and to check the response
		HttpTestUtil.executeSmallTest(topSocket, "testTorWithHostname", 10000);
	}

	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" })
	public void testWithHostname2() throws Exception
	{
		testWithHostname();
	}

	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" })
	public void testWithIPAddress() throws Exception
	{
		// create connection
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, new TcpipNetAddress(new byte[] { (byte) 188, (byte) 40, (byte) 59, (byte) 80 }, 80));

		// use open socket to execute HTTP request and to check the response
		HttpTestUtil.executeSmallTest(topSocket, "testTorWithIPAddress", 10000);
	}

	@BeforeClass
	public static void setUp()
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}
	}

	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" })
	public void testWithIPAddress2() throws Exception
	{
		testWithIPAddress();
	}

	private static final String BEGIN = "Begin";
	private static final String END = "enD";
	private static final byte[] HTTPTEST_TESTFILE_CONTENT_100_KBYTES = ByteArrayUtil.getByteArray(	BEGIN,
																											100000 - BEGIN.length() - END.length(),
																											END);
	private static final byte[] HTTPTEST_TESTFILE_CONTENT_1_MBYTE = ByteArrayUtil
			.concatByteArrays(	HTTPTEST_TESTFILE_CONTENT_100_KBYTES, HTTPTEST_TESTFILE_CONTENT_100_KBYTES,
								HTTPTEST_TESTFILE_CONTENT_100_KBYTES, HTTPTEST_TESTFILE_CONTENT_100_KBYTES,
								HTTPTEST_TESTFILE_CONTENT_100_KBYTES, HTTPTEST_TESTFILE_CONTENT_100_KBYTES,
								HTTPTEST_TESTFILE_CONTENT_100_KBYTES, HTTPTEST_TESTFILE_CONTENT_100_KBYTES,
								HTTPTEST_TESTFILE_CONTENT_100_KBYTES, HTTPTEST_TESTFILE_CONTENT_100_KBYTES);

	/**
	 * Download 100KB and check the speed.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 20000, dependsOnMethods = { "initializeTor" })
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
	 * Download 1MB and check the speed.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 100000, dependsOnMethods = { "initializeTor"})
	public void testDownloadPerformance2() throws Exception
	{
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, HttpUtil.HTTPTEST_SERVER_NETADDRESS);

		// use open socket for to execute HTTP request and to check the response
		// (download of file of size 1,000,000 bytes in max ?s = ?KByte/s)
		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, HttpUtil.HTTPTEST_SERVER_NETADDRESS, "/httptest/testfile1000000bytes.bin", 100000);
		AssertJUnit.assertArrayEquals(HTTPTEST_TESTFILE_CONTENT_1_MBYTE, httpResponse);
		topSocket.close();
	}

	// @Ignore(value = "this service is not always on")
	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" }, enabled = false)
	public void testThatRequestGoesThroughTorNetwork() throws Exception
	{
		final String TORCHECK_HOSTNAME = "torcheck.xenobite.eu";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 5000);
		String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
		LOG.info("http response body: " + httpResponseStr);

		// make the httpResponseStr readable in HTML reports
		httpResponseStr = removeHtmlTags(httpResponseStr);

		// trivial check
		final int MIN_RESONSE_LEN = 100;
		if (httpResponseStr == null || httpResponseStr.length() < MIN_RESONSE_LEN)
		{
			fail("invalid/short HTTP response body=" + httpResponseStr);
		}

		// check result
		final String SUCCESS_STR = "Your IP is identified to be a Tor-EXIT.";
		final String PROBABLY_SUCCESS_STR = "Your IP is identified to be a Tor-Node.";
		final String PROBABLY_SUCCESS_STR2 = "Congratulations. Your browser is configured to use Tor.";
		if (!httpResponseStr.contains(SUCCESS_STR) && !httpResponseStr.contains(PROBABLY_SUCCESS_STR)
				&& !httpResponseStr.contains(PROBABLY_SUCCESS_STR2))
		{
			fail("The request did NOT go through Tor network, see response body for details = " + httpResponseStr);
		}

		// /////////////////////////////////////////////////
		// crosscheck
		// /////////////////////////////////////////////////

		// create connection
		final NetSocket topSocket2 = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP).createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse2 = HttpUtil.get(topSocket2, TORCHECK_NETADDRESS, "/", 5000);
		final String httpResponseStr2 = ByteArrayUtil.showAsString(httpResponse2);
		LOG.info("http response body (crosscheck): " + httpResponseStr2);

		// check result
		if (httpResponseStr2.contains(SUCCESS_STR))
		{
			fail("crosscheck failed");
		}
	}

	// @Ignore(value = "this service is not always on")
	@Test(timeOut = 15000, dependsOnMethods = { "initializeTor" }, enabled = false)
	public void testThatRequestGoesThroughTorNetwork2() throws Exception
	{
		testThatRequestGoesThroughTorNetwork();
	}

	@Test(timeOut = 30000, dependsOnMethods = { "initializeTor" }, enabled = false) // doesnt work over HTTP anymore // TODO : change to HTTPS test
	public void testThatRequestGoesThroughTorNetworkVariantB() throws Exception
	{
		final String TORCHECK_HOSTNAME = "check.torproject.org";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

		// create connection
		final NetSocket topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
				.createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", 5000);
		String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
		LOG.info("http response body: " + httpResponseStr);

		// make the httpResponseStr readable in HTML reports
		httpResponseStr = removeHtmlTags(httpResponseStr);

		// trivial check
		final int MIN_RESONSE_LEN = 100;
		if (httpResponseStr == null || httpResponseStr.length() < MIN_RESONSE_LEN)
		{
			fail("invalid/short HTTP response body=" + httpResponseStr);
		}

		// check result
		final String SUCCESS_STR = "Congratulations. Your browser is configured to use Tor.";
		if (!httpResponseStr.contains(SUCCESS_STR))
		{
			fail("the request did NOT go through Tor network, response body=" + httpResponseStr);
		}

		// /////////////////////////////////////////////////
		// crosscheck
		// /////////////////////////////////////////////////
		NameServiceGlobalUtil.initNameService();
		NameServiceGlobalUtil.setIpNetAddressNameService(DefaultIpNetAddressNameService.getInstance());
		// create connection
		final NetSocket topSocket2 = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP).createNetSocket(null, null, TORCHECK_NETADDRESS);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse2 = HttpUtil.get(topSocket2, TORCHECK_NETADDRESS, "/", 5000);
		final String httpResponseStr2 = ByteArrayUtil.showAsString(httpResponse2);
		LOG.info("http response body (crosscheck): " + httpResponseStr2);

		// check result
		if (httpResponseStr2.contains(SUCCESS_STR))
		{
			fail("crosscheck failed");
		}
	}
	/**
	 * Check if we are really using the Tor network.
	 * 
	 * http://httptest.silvertunnel-ng.org/checktor.php will check the requester ip against a Tor exit node db.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 10000, dependsOnMethods = { "initializeTor" })
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

	@Test(timeOut = 1000, dependsOnMethods = { "initializeTor" })
	public void testGetValidTorRouters() throws Exception
	{
		// call API method
		final Collection<Router> routers = torNetLayer.getValidTorRouters();

		// check result
		final int MIN_NUM_OF_ROUTERS = 10;
		if (routers == null || routers.size() < MIN_NUM_OF_ROUTERS)
		{
			fail("invalid result of torNetLayer.getValidTorRouters()=" + routers);
		}

		// show one router
		LOG.info("one router=" + routers.iterator().next());
	}
}
