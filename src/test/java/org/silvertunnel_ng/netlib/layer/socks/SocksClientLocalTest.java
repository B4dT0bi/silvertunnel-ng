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

package org.silvertunnel_ng.netlib.layer.socks;

import org.silvertunnel_ng.netlib.api.ByteArrayTestUtil;
import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.TestUtil;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.mock.MockNetLayer;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test socks client+socks server.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SocksClientLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SocksClientLocalTest.class);

	private byte[] USER_DATA_REQUEST;
	private byte[] USER_DATA_RESPONSE;

	private MockNetLayer mockNetLayer;
	private NetLayer socksClientOverSocksServerOverMockNetLayer;

	public SocksClientLocalTest()
	{
		try
		{
			USER_DATA_REQUEST = ByteArrayUtil.getByteArray(
					"Das ist mein Request", 5000, "\u00e0.");
			USER_DATA_RESPONSE = ByteArrayUtil.getByteArray(
					"Hier ist\n\nmeine Antwort\n fuer heute", 3000, "\u00e0.");

		}
		catch (final Exception e)
		{
			LOG.error("unexpected during construction", e);
		}
	}

	@BeforeClass
	public void setUp() throws Exception
	{
		// create layer
		final long WAIT_ENDLESS = -1;
		mockNetLayer = new MockNetLayer(USER_DATA_RESPONSE, false, WAIT_ENDLESS);
		final NetLayer socksServerOverMockNetLayer = new SocksServerNetLayer(
				mockNetLayer);
		socksClientOverSocksServerOverMockNetLayer = new SocksClientNetLayer(
				socksServerOverMockNetLayer);
	}

	@Test(timeOut = 3000)
	public void testSocks5EstablishClientConnection() throws Exception
	{
		// create connection
		final NetAddress remoteAddress = new TcpipNetAddress("do.meins", 80);
		final NetSocket socksSocket = socksClientOverSocksServerOverMockNetLayer
				.createNetSocket(null, null, remoteAddress);

		// send user data to remote side
		socksSocket.getOutputStream().write(USER_DATA_REQUEST);
		socksSocket.getOutputStream().flush();

		// receive and check user data from remote side
		ByteArrayTestUtil.assertByteArrayFromInputStream(null,
				"wrong user data response", USER_DATA_RESPONSE,
				socksSocket.getInputStream());

		// check user data received by the remote side (mock)
		TestUtil.waitUntilMinimumNumberOfReceivedBytes(
				mockNetLayer.getFirstSessionHistory(), USER_DATA_REQUEST.length);
		socksSocket.close();
		final NetAddress expectedNetAddress = remoteAddress;
		TestUtil.assertMockNetLayerSavedData("wrong data received by mock",
				mockNetLayer.getFirstSessionHistory(), USER_DATA_REQUEST,
				expectedNetAddress);
	}
}
