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
package org.silvertunnel_ng.netlib.layer.socks;

import static org.silvertunnel_ng.netlib.util.ByteArrayUtil.getByteArray;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.ByteArrayTestUtil;
import org.silvertunnel_ng.netlib.api.HttpTestUtil;
import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** see http://de.wikipedia.org/wiki/SOCKS#Das_SOCKS-5-Protokoll */
public class Socks5RemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Socks5RemoteTest.class);

	@BeforeClass
	public void setUp() throws Exception
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			
		// create layer
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		final NetLayer socksProxyNetLayer = new SocksServerNetLayer(
				tcpipNetLayer);
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.SOCKS_OVER_TCPIP,
				socksProxyNetLayer);
	}

	@Test(timeOut = 10000)
	public void testSocks5EstablishClientConnection() throws Exception
	{
		// create connection
		final NetSocket socksSocket = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.SOCKS_OVER_TCPIP)
				.createNetSocket(null, (NetAddress) null, (NetAddress) null);

		// check socks negotiation
		final byte[] request1 = getByteArray(0x05, 0x01, /* auth method: */0x00);
		final byte[] expectedResponse1 = getByteArray(0x05, /* auth method: */
				0x00);
		socksSocket.getOutputStream().write(request1);
		socksSocket.getOutputStream().flush();
		ByteArrayTestUtil.assertByteArrayFromInputStream(LOG,
				"wrong response1", expectedResponse1,
				socksSocket.getInputStream());

		// check connection setup
		final byte[] request2 = getByteArray(0x05, /* TCP client: */0x01,
				0x00, /* with domain name: */0x03,
				/* domain name len: */0x13, 's', 'i', 'l', 'v', 'e', 'r', 't',
				'u', 'n', 'n', 'e', 'l', '-', 'n', 'g', '.', 'o', 'r', 'g',
				/* 2 bytes port */0x00, 80);
		final byte[] expectedResponse2 = getByteArray(0x05, /* reply code: */
				0x00, 0x00, /* with domain name: */0x03,
				/* domain name len: */0x13, 's', 'i', 'l', 'v', 'e', 'r', 't',
				'u', 'n', 'n', 'e', 'l', '-', 'n', 'g', '.', 'o', 'r', 'g',
				/* 2 bytes port */0x00, 80);
		socksSocket.getOutputStream().write(request2);
		socksSocket.getOutputStream().flush();
		ByteArrayTestUtil.assertByteArrayFromInputStream(LOG,
				"wrong response2", expectedResponse2,
				socksSocket.getInputStream());

		// check open connection
		HttpTestUtil.executeSmallTest(socksSocket,
				"testSocks5EstablishClientConnection", 2000);
	}
}
