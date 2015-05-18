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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test Tor's hidden services (server-sideTorHiddenServicePrivateNetAddress
 * netAddressWithoutPort = /service implementation).
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorHiddenServiceServerRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorHiddenServiceServerRemoteTest.class);

	private static final DateFormat DF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	protected static TorNetLayerUtil torNetLayerUtil = TorNetLayerUtil.getInstance();

	protected static TcpipNetAddress publicNewHiddenServiceTcpipNetAddress;
	protected static TcpipNetAddress publicOldHiddenServiceTcpipNetAddress;

	public static final String OLD_HIDDEN_SERVICE_PRIVATE_KEY_PEM_PATH = TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH;

	@Test
	public void trivialTest() throws Exception
	{
		// always successful
	}

	// /////////////////////////////////////////////////////
	// startup
	// /////////////////////////////////////////////////////

	/**
	 * Start TorNetLayer.
	 */
	@Override
	@Test(timeOut = 120000, dependsOnMethods = {"trivialTest" })
	public void initializeTor() throws Exception
	{
		// setting the route length to 2 (we do not need high security for our
		// tests.
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
		System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
		TorConfig.reloadConfigFromProperties();
		// repeat method declaration here to be the first test method of the class
		super.initializeTor();
	}

	// /////////////////////////////////////////////////////
	// provide hidden service(s)
	// /////////////////////////////////////////////////////

	/** do not use ports which could already run on a development machine. */
	private static final int PORT = 10080;

	/**
	 * 
	 * @param responseStr
	 *            will be part of each HTTP response of the server
	 * @param netAddressWithoutPort
	 *            hidden service private address
	 * @return hidden service public address
	 * @throws Exception
	 */
	private TcpipNetAddress provideHiddenService(final String responseStr,
	                                             final TorHiddenServicePrivateNetAddress netAddressWithoutPort)
	                                            		 throws Exception
	{
		// create net address inclusive port number
		final TorHiddenServicePortPrivateNetAddress netAddress = new TorHiddenServicePortPrivateNetAddress(netAddressWithoutPort, PORT);
		LOG.info("netAddress=" + netAddress);

		// establish the hidden service
		final TorNetServerSocket netServerSocket = (TorNetServerSocket) torNetLayer.createNetServerSocket(null, netAddress);
		// start a thread that waits for incoming connections
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					while (true)
					{
						// accept one new incoming connection per loop cycle
						LOG.info("TOR TEST: wait for accept");
						final NetSocket netSocket = netServerSocket.accept();
						LOG.info("TOR TEST: accept returned");

						// handle the new connection in an extra thread
						new Thread()
						{
							@Override
							public void run()
							{
								try
								{
									processOneServerSideConnection(netSocket, responseStr);
								}
								catch (final Exception e)
								{
									LOG.warn("exception while handling a server side connection", e);
								}
							}
						}.start();
					}
				}
				catch (final Exception e)
				{
					LOG.warn("exception while handling server side connections", e);
				}
			}
		}.start();

		// save public address of this service for later access of the client
		final TcpipNetAddress publicHiddenServiceTcpipNetAddress = netAddress.getPublicTcpipNetAddress();
		LOG.info("publicHiddenServiceTcpipNetAddress=" + publicHiddenServiceTcpipNetAddress);
		return publicHiddenServiceTcpipNetAddress;
	}

	/**
	 * Handle one server-side connection of the hidden service. Read the request
	 * and write a HTTP response.
	 * 
	 * @param netSocket
	 *            freshly opened connection to a (HTTP?) client
	 * @throws Exception
	 */
	private void processOneServerSideConnection(final NetSocket netSocket, final String responseStr) throws Exception
	{
		// read the first request line
		final BufferedReader reader = new BufferedReader(new InputStreamReader(netSocket.getInputStream()));
		LOG.info("TOR HIDDEN SERVICE - SERVER SIDE: wait for first line");
		final String firstLine = reader.readLine();
		LOG.info("TOR HIDDEN SERVICE - SERVER SIDE: firstLine=" + firstLine);

		// send response
		final String response = "HTTP/1.1 200 OK\n\r"
				+ "Content-Type: text/xml; charset=utf-8\n\r" + "\n\r"
				+ "<html><body>This is my response\nwith two lines\n"
				+ responseStr + "\ndate/time=" + getCurrentTime()
				+ "</body></html>";
		final Writer writer = new OutputStreamWriter(netSocket.getOutputStream());
		writer.append(response);
		writer.flush();
		LOG.info("TOR HIDDEN SERVICE - SERVER SIDE: send response=" + response);

		Thread.sleep(5000);

		writer.close();
		reader.close();
		netSocket.close();
	}

	/**
	 * Provide the NEW hidden service and establish a Thread that wait for
	 * incoming connections.
	 * 
	 * @throws Exception when an error occurs
	 */
	@Test(timeOut = 120000, dependsOnMethods = {"initializeTor" })
	public void testPhase1ProvideNewHiddenService() throws Exception
	{
		final TorHiddenServicePrivateNetAddress netAddressWithoutPort = torNetLayerUtil.createNewTorHiddenServicePrivateNetAddress();

		// in real life this netAddressWithoutPort should be saved on persistent media
		// for latter reuse, e.g.:
		// torNetLayerUtil.writeTorHiddenServicePrivateNetAddressToFiles(directory, netAddressWithoutPort);

		publicNewHiddenServiceTcpipNetAddress = provideHiddenService("NEW-SERVICE", netAddressWithoutPort);
	}

	/**
	 * Provide the OLD hidden service (based on existing private key) and
	 * establish a Thread that wait for incoming connections.
	 * 
	 * @throws Exception when an unexpected error occurs
	 */
	@Test(timeOut = 120000, dependsOnMethods = {"initializeTor" })
	public void testPhase2ProvideOldHiddenService() throws Exception
	{
		// read private key of OLD hidden service
		final String privateKeyPEMStr = FileUtil.readFileFromClasspath(OLD_HIDDEN_SERVICE_PRIVATE_KEY_PEM_PATH);
		final TorHiddenServicePrivateNetAddress netAddressWithoutPort = torNetLayerUtil
				.parseTorHiddenServicePrivateNetAddressFromStrings(privateKeyPEMStr, null, false);

		// start OLD hidden service
		publicOldHiddenServiceTcpipNetAddress = provideHiddenService("old-service", netAddressWithoutPort);
		assertNotNull(publicOldHiddenServiceTcpipNetAddress);
	}

	// /////////////////////////////////////////////////////
	// test access to the hidden service(s) with silvertunnel-ng.org Netlib
	// /////////////////////////////////////////////////////

	private void checkAccessProvidedHiddenService(final TcpipNetAddress publicHiddenServiceTcpipNetAddress,
	                                              final String expectedResponseStr) throws Exception
	{
		// pre-check
		assertNotNull("publicHiddenServiceTcpipNetAddress==null",
		              publicHiddenServiceTcpipNetAddress);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory
					.getInstance()
					.getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, publicHiddenServiceTcpipNetAddress);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket,
					publicHiddenServiceTcpipNetAddress,
					"/get/info/from/hidden/service/" + getCurrentTime(), 60000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			final String SUCCESS_STR1 = "This is my response";
			if (!httpResponseStr.contains(SUCCESS_STR1))
			{
				fail("did not get correct response of hidden service (1), response body="
						+ httpResponseStr);
			}
			final String SUCCESS_STR2 = expectedResponseStr;
			if (!httpResponseStr.contains(SUCCESS_STR2))
			{
				fail("did not get correct response of hidden service (2), response body="
						+ httpResponseStr);
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

	@Test(timeOut = 120000, dependsOnMethods = {"testPhase1ProvideNewHiddenService" })
	public void testPhase3AccessProvidedNewHiddenService() throws Exception
	{
		checkAccessProvidedHiddenService(publicNewHiddenServiceTcpipNetAddress, "NEW-SERVICE");
	}

	@Test(timeOut = 120000, dependsOnMethods = {"testPhase3AccessProvidedNewHiddenService" })
	public void testPhase3aAccessProvidedNewHiddenService_again()
			throws Exception
	{
		LOG.info("start to do it again");
		testPhase3AccessProvidedNewHiddenService();
	}

	@Test(timeOut = 120000, dependsOnMethods = {"testPhase2ProvideOldHiddenService" })
	public void testPhase4AccessProvidedOldHiddenService() throws Exception
	{
		checkAccessProvidedHiddenService(publicOldHiddenServiceTcpipNetAddress, "old-service");
	}

	@Test(timeOut = 110000, dependsOnMethods = {"testPhase4AccessProvidedOldHiddenService" })
	public void testPhase4aAccessProvidedOldHiddenService_again() throws Exception
	{
		LOG.info("start to do it again");
		testPhase4AccessProvidedOldHiddenService();
	}

	// /////////////////////////////////////////////////////
	// test access to the hidden service(s) with proxy (only for NEW hidden
	// service)
	// /////////////////////////////////////////////////////

	/**
	 * Test with tor2web proxy. Hint: this only works for hidden services listen
	 * on port 80.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 120000, dependsOnMethods = {"testPhase1ProvideNewHiddenService" }, enabled = false)
	public void testPhase5AccessProvidedNewHiddenServiceViaOriginalTorWithTor2webOrg()
			throws Exception
	{
		testPhase1ProvideNewHiddenService();
		LOG.info("use tor2web proxy now");
		// sleep be be able to manually connect to the tor2web proxy
		// Thread.sleep(240000);

		// create a URL of tor2web proxy, like
		// "https://4xuwatxuqzfnqjuz.tor2web.org/"
		final String path = "/bla/blub/" + getCurrentTime();
		final int PORT443 = 443;
		final TcpipNetAddress proxyTcpipNetAddress = new TcpipNetAddress(
				publicNewHiddenServiceTcpipNetAddress.getHostnameOrIpaddress()
						.replace("onion", "tor2web.org"), PORT443);
		final String url = "https://" + proxyTcpipNetAddress.getHostname()
				+ path;
		LOG.info("url=" + url);
		LOG.info("proxyTcpipNetAddress=" + proxyTcpipNetAddress);

		// create connection to tor2web proxy
		final NetSocket topSocket = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP)
				.createNetSocket(null, null, proxyTcpipNetAddress);

		HttpUtil.getInstance();
		// communicate with the remote side
		final byte[] httpResponse = HttpUtil.get(topSocket, proxyTcpipNetAddress, path, 150000);
		String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
		LOG.info("http response body: " + httpResponseStr);

		// make the httpResponseStr readable in HTML reports
		httpResponseStr = removeHtmlTags(httpResponseStr);

		// check result
		final String SUCCESS_STR = "This is my response";
		if (!httpResponseStr.contains(SUCCESS_STR))
		{
			fail("did not get correct response of hidden service, response body="
					+ httpResponseStr);
		}
	}

	// /////////////////////////////////////////////////////
	// internal helper methods (without business logic)
	// /////////////////////////////////////////////////////

	private String getCurrentTime()
	{
		return DF.format(new Date());
	}
}
