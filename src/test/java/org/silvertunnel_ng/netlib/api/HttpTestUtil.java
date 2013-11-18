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

package org.silvertunnel_ng.netlib.api;

import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support Tests with HTTP.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class HttpTestUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpTestUtil.class);

	private static final Pattern CLIENT_IP_PATTERN = Pattern
			.compile("<client_host_or_ip>(.+?)</client_host_or_ip>");

	/**
	 * Try to execute the /httptest/smalltest.php over the provided net socket
	 * with a random id.
	 * 
	 * Closes the socket after the test.
	 * 
	 * @param lowerLayerNetSocket
	 *            this net socket will be closed inside the method
	 * @param idPrefix
	 *            only digits and ASCII letters, becomes part of the id sent to
	 *            the server
	 * @param timeoutInMs
	 * @throws IOException
	 */
	public static void executeSmallTest(NetSocket lowerLayerNetSocket,
			String idPrefix, long timeoutInMs) throws IOException
	{
		final boolean testOK = HttpUtil.getInstance().executeSmallTest(
				lowerLayerNetSocket, idPrefix, timeoutInMs);
		if (!testOK)
		{
			fail("wrong http response");
		}
	}

	/**
	 * Determine the source IP address visible to a public HTTP test server.
	 * 
	 * @param netLayer
	 *            used to create the connection
	 * @return the source IP address, not null
	 * @throws IOException
	 *             in the case of an error
	 */
	public static IpNetAddress getSourceIpNetAddress(NetLayer netLayer)
			throws IOException
	{
		return getSourceIpNetAddress(netLayer,
				HttpUtil.HTTPTEST_SERVER_NETADDRESS, "/httptest/bigtest.php");
	}

	/**
	 * Determine the source IP address visible to a public HTTP test server.
	 * 
	 * A HTTP connection will be established to a public test server that
	 * responses with something like
	 * "...<client_host_or_ip>1.2.3.4</client_host_or_ip>...".
	 * 
	 * @param netLayer
	 *            used to create the connection
	 * @param testAppNetAddress
	 *            address to reach the test web application, e.g.
	 *            HttpUtil.HTTPTEST_SERVER_NETADDRESS
	 * @param testAppUrlPath
	 *            path to reach the test web application, e.g.
	 *            "/httptest/bigtest.php"
	 * @return the source IP address, not null
	 * @throws IOException
	 *             in the case of an error
	 */
	public static IpNetAddress getSourceIpNetAddress(NetLayer netLayer,
			TcpipNetAddress testAppNetAddress, String testAppUrlPath)
			throws IOException
	{
		// create connection
		NetSocket netSocket = null;

		try
		{
			// create connection
			netSocket = netLayer.createNetSocket(null, null, testAppNetAddress);

			HttpUtil.getInstance();
			// communicate with the remote side
			final byte[] httpResponse = HttpUtil.get(netSocket,
					testAppNetAddress, testAppUrlPath, 10000);
			final String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
			LOG.debug("http response body: " + httpResponseStr);

			// analyze result
			if (httpResponseStr == null || httpResponseStr.length() < 1)
			{
				throw new IOException("got empty HTTP response");
			}
			final Matcher m = CLIENT_IP_PATTERN.matcher(httpResponseStr);
			IpNetAddress clientIp = null;
			if (m.find())
			{
				final String clientIpStr = m.group(1);
				try
				{
					clientIp = new IpNetAddress(clientIpStr);
				}
				catch (final Exception e)
				{
					throw new IOException("invalid source/client IP: "
							+ clientIpStr);
				}
			}
			if (clientIp == null)
			{
				throw new IOException("could not determine source/client IP");
			}
			return clientIp;
		}
		finally
		{
			if (netSocket != null)
			{
				netSocket.close();
			}
		}
	}

}
