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

import static org.testng.AssertJUnit.fail;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test Tor's hidden services (client implementation) - to find the memory leak.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorMemRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorMemRemoteTest.class);
	public static void main(String[] argv) throws Exception
	{
		final TorMemRemoteTest test = new TorMemRemoteTest();
		test.initializeTor();
		test.testAccessToTorsExampleOnionDomain();
		test.testLongTerm();
	}

	@Override
	@Test(timeOut = 600000, enabled = false)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		super.initializeTor();
	}

	@Test(timeOut = 120000, dependsOnMethods = {"initializeTor" }, enabled = false)
	public void testAccessToTorsExampleOnionDomain() throws Exception
	{
		final String TORCHECK_HOSTNAME = "duskgytldkxiuqc6.onion";
		final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(
				TORCHECK_HOSTNAME, 80);

		// create connection
		NetSocket topSocket = null;
		try
		{
			topSocket = NetFactory.getInstance()
					.getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
					.createNetSocket(null, null, TORCHECK_NETADDRESS);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(topSocket,
					TORCHECK_NETADDRESS, "/", 10000);
			String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.info("http response body: " + httpResponseStr);

			// make the httpResponseStr readable in HTML reports
			httpResponseStr = removeHtmlTags(httpResponseStr);

			// check result
			final String SUCCESS_STR = "This is the example page for Tor's";
			if (!httpResponseStr.contains(SUCCESS_STR))
			{
				fail("did not get correct response of hidden service, response body="
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

	@Test(timeOut = 120000, dependsOnMethods = {"initializeTor" }, enabled = false)
	public void testAccessToTorsExampleOnionDomain2() throws Exception
	{
		testAccessToTorsExampleOnionDomain();
	}

	private static final int ATTEMPTS = 10000;
	@Test(timeOut = 120000, dependsOnMethods = {"testAccessToTorsExampleOnionDomain" }, enabled = false)
	public void testLongTerm() throws Exception
	{
		for (int i = 1; i <= ATTEMPTS; i++)
		{
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			LOG.info("**************************************************** attempt="
					+ i);
			try
			{
				testAccessToTorsExampleOnionDomain();
			}
			catch (final Throwable t)
			{
				LOG.error(
						"single testAccessToTorsExampleOnionDomain() failed", t);
			}
			LOG.info("**************************************************** attempt="
					+ i);
		}
	}
}
