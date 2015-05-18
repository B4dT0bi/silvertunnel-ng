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

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.ByteArrayTestUtil;
import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.TestUtil;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.layer.mock.MockNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author hapke
 * @author Tobias Boese
 *
 */
public class NetProxyLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NetProxyLocalTest.class);

	private byte[] USER_DATA_REQUEST;
	private byte[] USER_DATA_RESPONSE;

	private MockNetLayer mockNetLayer;
	private Thread netlibProxyThread;
	private static final int PROXY_SERVER_PORT = 11080;

	/**
	 * if NUM_OF_TEST_EXECUTIONS==1 then this test class behaves like an
	 * unparameterized one
	 */
	private static final int NUM_OF_TEST_EXECUTIONS = 1;

	@DataProvider(name = "multipleTestExecutions")
	public static Object[][] multipleTestExecutions()
	{
		return new Object[NUM_OF_TEST_EXECUTIONS][0];
	}

	public NetProxyLocalTest()
	{
		try
		{
			USER_DATA_REQUEST = ByteArrayUtil.getByteArray(
					"<request>Das ist mein Request", 2222, "</request>");
			USER_DATA_RESPONSE = ByteArrayUtil.getByteArray(
					"<response>Hier ist\n\nmeine Antwort\n fuer heute", 3333,
					"</response>");

		}
		catch (final Exception e)
		{
			LOG.error("unexpected during construction", e);
		}
	}

	@BeforeMethod
	public void setUp() throws Exception
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			
		NetFactory.getInstance().clearRegisteredNetLayers(); // do a cleanup
		// create layer that will always be used by the proxy for connection
		// connect to the proxy client
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP,
				tcpipNetLayer);

		// create layer that will be used to connect the proxy to
		final long WAIT_ENDLESS = -1;
		mockNetLayer = new MockNetLayer(USER_DATA_RESPONSE, false, WAIT_ENDLESS);
		final NetLayer loggingMockNetLayer = new LoggingNetLayer(mockNetLayer,
				NetLayerIDs.MOCK.getValue());
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.MOCK, loggingMockNetLayer);
	}

	@Test(timeOut = 30000, dataProvider = "multipleTestExecutions")
	public void testWithMock() throws Exception
	{
		// start and proxy
		netlibProxyThread = new Thread("NetProxy-main")
		{
			@Override
			public void run()
			{
				final String[] commanLineArgs = {
						"127.0.0.1:" + PROXY_SERVER_PORT, NetLayerIDs.MOCK.getValue() };
				NetlibProxy.start(commanLineArgs);
			}
		};
		netlibProxyThread.start();

		// wait until proxy startup is finished
		while (!NetlibProxy.isStarted())
		{
			// wait a bit
			Thread.sleep(1000);
		}

		// connect to the proxy
		LOG.info("connect to the proxy");
		final NetAddress proxyAddress = new TcpipNetAddress("localhost",
				PROXY_SERVER_PORT);
		final NetSocket socket = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.TCPIP)
				.createNetSocket(null, null, proxyAddress);

		// send user data to remote side
		LOG.info("send user data to remote side, i.e. to the mock");
		socket.getOutputStream().write(USER_DATA_REQUEST);
		socket.getOutputStream().flush();

		// receive and check user data from remote side
		LOG.info("receive and check user data from remote side, i.e. from the mock");
		ByteArrayTestUtil.assertByteArrayFromInputStream(null,
				"wrong user data response", USER_DATA_RESPONSE,
				socket.getInputStream());

		// check user data received by the remote side (mock)
		LOG.info("check user data received by the remote side (mock)");
		TestUtil.waitUntilMinimumNumberOfReceivedBytes(
				mockNetLayer.getFirstSessionHistory(), USER_DATA_REQUEST.length);
		socket.close();
		final NetAddress expectedNetAddress = null;
		TestUtil.assertMockNetLayerSavedData("wrong data received by mock",
				mockNetLayer.getFirstSessionHistory(), USER_DATA_REQUEST,
				expectedNetAddress);
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
		NetlibProxy.stop();
		netlibProxyThread.join();
	}
}
