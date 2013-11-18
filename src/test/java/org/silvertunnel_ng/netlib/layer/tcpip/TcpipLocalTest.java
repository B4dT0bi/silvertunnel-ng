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

package org.silvertunnel_ng.netlib.layer.tcpip;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of TcpipNetLayer.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TcpipLocalTest
{
	/** */
	protected static final Logger LOG = LoggerFactory.getLogger(TcpipLocalTest.class);

	protected static final int TEST_SERVER_PORT = 9999;

	protected static final byte[] DATA_CLIENT_TO_SERVER = ByteArrayUtil.getByteArray(
			"Send me to the server...", 2000, "...!!!");
	protected static final byte[] DATA_SERVER_TO_CLIENT = ByteArrayUtil.getByteArray(
			"The server speaks...", 3000, "...OK");

	protected static volatile byte[] dataReceivedByServer;
	private static volatile byte[] dataReceivedByClient;

	@BeforeClass
	public void setUp() throws Exception
	{
		// create layer for modify_over_tcpip
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		final NetLayer loggedTcpiNetLayer = new LoggingNetLayer(tcpipNetLayer,
				"upper tcp");
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP,
				loggedTcpiNetLayer);
	}

	@Test(timeOut = 40000)
	public void testServerClientConnection() throws Exception
	{
		// start server
		new TcpipLocalTestServerThread().start();

		LOG.info("(client) connect client to server");
		final TcpipNetAddress remoteAddress = new TcpipNetAddress("localhost", TEST_SERVER_PORT);
		final NetSocket client = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.TCPIP)
				.createNetSocket(null, null, remoteAddress);

		LOG.info("(client) send data client->server");
		client.getOutputStream().write(DATA_CLIENT_TO_SERVER);
		client.getOutputStream().flush();

		LOG.info("(client) read data from server");
		dataReceivedByClient = ByteArrayUtil.readDataFromInputStream(
				DATA_SERVER_TO_CLIENT.length, client.getInputStream());

		LOG.info("(client) finish connection");
		client.close();

		LOG.info("(client) wait for end");
		while (dataReceivedByServer == null)
		{
			Thread.sleep(100);
		}

		// check result
		assertEquals("wrong dataReceivedByServer",
				Arrays.toString(DATA_CLIENT_TO_SERVER),
				Arrays.toString(dataReceivedByServer));
		assertEquals("wrong dataReceivedByClient",
				Arrays.toString(DATA_SERVER_TO_CLIENT),
				Arrays.toString(dataReceivedByClient));
	}
}

class TcpipLocalTestServerThread extends Thread
{
	private NetServerSocket server;

	public TcpipLocalTestServerThread()
	{
		try
		{
			// setDaemon(true);
			TcpipLocalTest.LOG.info("(server) create server socket");
			final TcpipNetAddress localListenAddress = new TcpipNetAddress(
					"0.0.0.0", TcpipLocalTest.TEST_SERVER_PORT);
			server = NetFactory.getInstance()
					.getNetLayerById(NetLayerIDs.TCPIP)
					.createNetServerSocket(null, localListenAddress);

		}
		catch (final IOException e)
		{
			TcpipLocalTest.LOG.warn("exception", e);
		}
	}

	@Override
	public void run()
	{
		try
		{
			TcpipLocalTest.LOG.info("(server) wait for one connection");
			final NetSocket s = server.accept();
			server.close();

			TcpipLocalTest.LOG.info("(server) send data from server->client");
			s.getOutputStream().write(TcpipLocalTest.DATA_SERVER_TO_CLIENT);
			s.getOutputStream().flush();

			TcpipLocalTest.LOG.info("(server) read data from client");
			TcpipLocalTest.dataReceivedByServer = ByteArrayUtil
					.readDataFromInputStream(
							TcpipLocalTest.DATA_CLIENT_TO_SERVER.length,
							s.getInputStream());
			TcpipLocalTest.LOG.info("(server) reading finished");

		}
		catch (final IOException e)
		{
			TcpipLocalTest.LOG.warn("exception", e);
		}
	}
}
