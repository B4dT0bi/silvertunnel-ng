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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;

import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of the ability of TorNetLayer to handle connection setup even if many
 * TLSConnections and their circuits were interrupted.
 * 
 * Special test case(es) that use non-public methods of class Tor.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class TorLongTermRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorLongTermRemoteTest.class);

	/**
	 * Test that multiple/long term use of NetlibURLStream and HandlerFactory
	 * work correctly.
	 */
	@Test(timeOut = 900000)
	public void testLongTermUseOfNetlibURLStreamHandlerFactory()
			throws Exception
	{
		final int NUM_OF_DOWNLOADS = 10;

		final List<String> responses = new ArrayList<String>(NUM_OF_DOWNLOADS);

		try
		{
			// classic: TcpipNetLayer with NetLayerIDs.TCPIP (--> HTTP over
			// plain TCP/IP)
			// anonymous: TorNetLayer with NetLayerIDs.TOR (--> HTTP over TCP/IP
			// over Tor network)
			// NetLayer lowerNetLayer =
			// NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
			final NetLayer lowerNetLayer = NetFactory.getInstance()
					.getNetLayerById(NetLayerIDs.TOR);
			lowerNetLayer.waitUntilReady();

			// prepare URL handling on top of the lowerNetLayer
			final NetlibURLStreamHandlerFactory factory = new NetlibURLStreamHandlerFactory(
					false);
			// the following method could be called multiple times
			// to change layer used by the factory over the time:
			factory.setNetLayerForHttpHttpsFtp(lowerNetLayer);

			// create the suitable URL object
			final URLStreamHandler handler = factory
					.createURLStreamHandler("http");

			// communicate via HTTP multiple times
			for (int i = 1; i <= NUM_OF_DOWNLOADS; i++)
			{
				final String id = "NetlibHttpUsageExamplesLongTimeRemoteTest"
						+ i;
				final String urlStr = "http://httptest.silvertunnel-ng.org/httptest/bigtest.php?id=" + id;
				final URL context = null;
				final URL url = new URL(context, urlStr, handler);

				// send request without POSTing data
				final URLConnection urlConnection = url.openConnection();
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(false);
				urlConnection.connect();

				// receive and store the response as String
				final BufferedReader response = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream()));
				String line;
				final StringBuffer responseStrBuf = new StringBuffer();
				while ((line = response.readLine()) != null)
				{
					responseStrBuf.append(line);
					responseStrBuf.append("\n");
				}
				response.close();
				final String responseStr = responseStrBuf.toString();
				responses.add(responseStr);
				LOG.info("Response:\n" + responseStr + "\n");

				// check the response
				final String responseIdStr = "<id>" + id + "</id>";
				assertTrue("response does not contains expected id string=\""
						+ responseIdStr + "\":\n" + response,
						responseStr.contains(responseIdStr));

				if (i >= 2)
				{
					// close all TLS connections used by Tor
					// to simulate connection aborts and connection timeouts
					TLSConnectionAdmin.closeAllTlsConnections();
				}
			}
		}
		catch (final Exception e)
		{
			throw new Exception("Exception occured after reading "
					+ responses.size() + " responses", e);
		}
		finally
		{
			// log all responses
			LOG.info("************************************");
			LOG.info("************************************");
			LOG.info("************************************");
			LOG.info("************************************");
			LOG.info("************************************");
			LOG.info("Number of responses: " + responses.size());
			for (final String response : responses)
			{
				LOG.info("Response:\n" + response + "\n");
			}
		}
	}
}
