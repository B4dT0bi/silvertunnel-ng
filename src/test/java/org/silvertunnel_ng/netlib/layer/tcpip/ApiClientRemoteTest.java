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

import static org.silvertunnel_ng.netlib.util.HttpUtil.HTTPTEST_SERVER_NETADDRESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.net.Socket;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.ApiClientLocalTest;
import org.silvertunnel_ng.netlib.api.HttpTestUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.NetSocket2Socket;
import org.silvertunnel_ng.netlib.layer.modification.AddModificator;
import org.silvertunnel_ng.netlib.layer.modification.ModificatorNetLayer;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author hapke
 * @author Tobias Boese
 *
 */
public final class ApiClientRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ApiClientLocalTest.class);


	@BeforeClass
	public void setUp() throws Exception
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			

		// create layer for modify_over_tcpip
		final NetLayer tcpipNetLayer = new TcpipNetLayer();
		final NetLayer modificatorLayer2 = new ModificatorNetLayer(
				tcpipNetLayer, new AddModificator(1 - 1), new AddModificator(0));
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.MODIFY_OVER_TCPIP, modificatorLayer2);
	}

	@Test(timeOut = 15000)
	public void testLayerModifyOverTcpipWithOldSocket() throws Exception
	{
		// create connection
		final NetSocket topSocket = NetFactory
				.getInstance()
				.getNetLayerById(NetLayerIDs.MODIFY_OVER_TCPIP)
				.createNetSocket(null, null,
						HttpUtil.HTTPTEST_SERVER_NETADDRESS);
		final Socket top = new NetSocket2Socket(topSocket);

		// write data
		final String dataToSend = "GET /httptest/smalltest.php?id=APIHelloWorld HTTP/1.0\n" 
								+ "Host: " + HttpUtil.HTTPTEST_SERVER_NAME + "\n\n";
		top.getOutputStream().write(dataToSend.getBytes());
		top.getOutputStream().flush();

		// read (result) data
		final byte[] resultBuffer = new byte[10000];
        int len = top.getInputStream().read(resultBuffer);
		String result = new String(resultBuffer).substring(0, len);
        if (!result.contains("<response>")) {
            len = top.getInputStream().read(resultBuffer);
            result = new String(resultBuffer).substring(0, len);
        }
        assertTrue("result does not contain <response> tag\n" + result, result.contains("<response>"));
		result = result.substring(result.indexOf("<response>"));
		// close connection
		top.close();

		// show and check result data
		LOG.info("result=\"" + result + "\"");
		assertEquals("got wrong result",
				"<response><id>APIHelloWorld</id></response>\n", result);
	}

	@Test(timeOut = 15000)
	public void testLayerModifyOverTcpip() throws Exception
	{
		// create connection
		final NetSocket topSocket = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.MODIFY_OVER_TCPIP)
				.createNetSocket(null, null, HTTPTEST_SERVER_NETADDRESS);

		// use open socket to execute HTTP request and to check the response
		HttpTestUtil.executeSmallTest(topSocket, "testLayerModifyOverTcpip",
				2000);
	}
}
