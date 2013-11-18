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

package org.silvertunnel_ng.netlib.layer.tls;

import static org.testng.AssertJUnit.fail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author hapke
 * @author Tobias Boese
 *
 */
public final class TLSRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TLSRemoteTest.class);

	public static final String WEBPAGE_GMX_NET_CONTENT_SNIPPET = "<title>GMX: Email-Adresse, FreeMail, De-Mail &amp; Nachrichten";

	@BeforeClass
	public void setUp() throws Exception
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			

		// create lower layer
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		final NetLayer loggingTcpipNetLayer = new LoggingNetLayer(
				tcpipNetLayer, "upper tcpip  ");
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP,
				loggingTcpipNetLayer);

		// create TLS/SSL layer
		final TLSNetLayer tlsNetLayer = new TLSNetLayer(loggingTcpipNetLayer);
		final NetLayer loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer,
				"upper tls/ssl");
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP,
				loggingTlsNetLayer);

		LOG.info("------------------------------------------------------------------------------------------------");
	}

	@Test
	public void testHttpRequest() throws Exception
	{
		// create connection
		final NetSocket tcpipSocket = NetFactory
				.getInstance()
				.getNetLayerById(NetLayerIDs.TCPIP)
				.createNetSocket(null, null,
						new TcpipNetAddress("www.gmx.net", 80));
		completeHttpRequestResponse(tcpipSocket);
	}

	@Test
	public void testHttpsRequest() throws Exception
	{
		// create connection
		final NetSocket tlsSocket = NetFactory
				.getInstance()
				.getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP)
				.createNetSocket(null, null,
						new TcpipNetAddress("www.gmx.net", 443));
		completeHttpRequestResponse(tlsSocket);
	}

	@Test
	public void testHttpsRequestWithLimitedCiphers() throws Exception
	{
		// create connection
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(TLSNetLayer.ENABLES_CIPHER_SUITES,
				"TLS_RSA_WITH_AES_128_CBC_SHA");
		final NetSocket tlsSocket = NetFactory
				.getInstance()
				.getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP)
				.createNetSocket(props, null,
						new TcpipNetAddress("www.gmx.net", 443));
		completeHttpRequestResponse(tlsSocket);
	}

	@Test
	public void testHttpsRequestWithInvalidCiphers() throws Exception
	{
		// create connection
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(TLSNetLayer.ENABLES_CIPHER_SUITES, "MICH_GIBTS_NICHT");
		try
		{
			final NetSocket tlsSocket = NetFactory
					.getInstance()
					.getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP)
					.createNetSocket(props, null,
							new TcpipNetAddress("www.gmx.net", 443));
			completeHttpRequestResponse(tlsSocket);
			fail("expected exception not thrown");
		}
		catch (final Exception e)
		{
			LOG.info("expected exception catched: " + e);
		}
	}

	// /////////////////////////////////////////////////////
	// helper methods
	// /////////////////////////////////////////////////////
	private static final int MAX_BUFFER_SIZE = 100000;

	private void completeHttpRequestResponse(final NetSocket netSocket)
			throws Exception
	{
		// write data
		final String dataToSend = "GET / HTTP/1.1\nHost: www.gmx.net\n\n";
		netSocket.getOutputStream().write(dataToSend.getBytes());
		netSocket.getOutputStream().flush();

		// read (result) data
		final InputStream is = netSocket.getInputStream();
		final byte[] resultBuffer = ByteArrayUtil.readDataFromInputStream(
				MAX_BUFFER_SIZE, is);
		if (resultBuffer.length >= MAX_BUFFER_SIZE)
		{
			LOG.info("result buffer is full");
		}
		else
		{
			LOG.info("end of result stream");
		}
		final String result = new String(resultBuffer);

		// close connection
		netSocket.close();

		// show and check result data
		LOG.debug("result=\"" + result + "\"");
		if (!result.contains(WEBPAGE_GMX_NET_CONTENT_SNIPPET))
		{
			fail("wrong result=\"" + result + "\"");
		}
	}
}
