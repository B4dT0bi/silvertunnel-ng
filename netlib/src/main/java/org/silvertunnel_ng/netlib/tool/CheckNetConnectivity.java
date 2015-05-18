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
package org.silvertunnel_ng.netlib.tool;

import org.silvertunnel_ng.netlib.adapter.socket.ExtendedSocket;
import org.silvertunnel_ng.netlib.adapter.socket.SocketGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.JavaVersion;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line tool or test method that checks whether Java and Netlib can
 * access to the Internet (which is the precondition for the most Netlib
 * features).
 * 
 * @author hapke
 */
public class CheckNetConnectivity
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(CheckNetConnectivity.class);

	private static final long timeoutInMs = 5000;

	/**
	 * Command line tool.
	 * 
	 * @param argv
	 */
	public static void main(String[] argv)
	{
		if (argv.length > 0)
		{
			LOG.error("CheckNetConnectivity: no command line arguments are supported");
			System.exit(1);
			return;
		}

		// action
		executeCheck(true);
	}

	/**
	 * Before calling this method the caller should execute
	 * System.setProperty(NetLayerBootstrap.SKIP_TOR, "true"); If not then the
	 * semantic of tests1+2 could be different. In any case all tests should be
	 * successful.
	 * 
	 * @param checkWithSocketGlobalUtil
	 *            false=check without SocketGlobalUtil.initSocketImplFactory();
	 *            true=check everything
	 * @return true=all tests were successful; false=at least one test failed
	 */
	public static boolean executeCheck(final boolean checkWithSocketGlobalUtil)
	{
		LOG.info("CheckNetConnectivity.executeCheck()");

		// execute test 1
		boolean test1Result = false;
		try
		{
			final ExtendedSocket socket = new ExtendedSocket(
					HttpUtil.HTTPTEST_SERVER_NAME,
					HttpUtil.HTTPTEST_SERVER_PORT);
			test1Result = HttpUtil.getInstance().executeSmallTest(socket,
					"test1", timeoutInMs);
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while test1", e);
		}

		// execute test 2
		boolean test2Result = false;
		try
		{
			final NetSocket netSocket = NetFactory
					.getInstance()
					.getNetLayerById(NetLayerIDs.TCPIP)
					.createNetSocket(null, null,
							HttpUtil.HTTPTEST_SERVER_NETADDRESS);
			test2Result = HttpUtil.getInstance().executeSmallTest(netSocket,
					"test2", timeoutInMs);
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while test2", e);
		}

		boolean test3Result;
		boolean test4Result;
		if (checkWithSocketGlobalUtil)
		{
			// initialize and set SocketImplFactory
			SocketGlobalUtil.initSocketImplFactory();
			final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(
					NetLayerIDs.TCPIP);
			SocketGlobalUtil.setNetLayerUsedBySocketImplFactory(netLayer);

			// execute test 3
			test3Result = false;
			try
			{
				final ExtendedSocket socket = new ExtendedSocket(
						HttpUtil.HTTPTEST_SERVER_NAME,
						HttpUtil.HTTPTEST_SERVER_PORT);
				test3Result = HttpUtil.getInstance().executeSmallTest(socket,
						"test3", timeoutInMs);
			}
			catch (final Exception e)
			{
				LOG.warn("Exception while test3", e);
			}

			// execute test 4
			test4Result = false;
			try
			{
				final NetSocket netSocket = NetFactory
						.getInstance()
						.getNetLayerById(NetLayerIDs.TCPIP)
						.createNetSocket(null, null,
								HttpUtil.HTTPTEST_SERVER_NETADDRESS);
				test4Result = HttpUtil.getInstance().executeSmallTest(
						netSocket, "test4", timeoutInMs);
			}
			catch (final Exception e)
			{
				LOG.warn("Exception while test4", e);
			}

		}
		else
		{
			test3Result = true;
			test4Result = true;
		}

		// show test results
		print("===================================================");
		print("=== Test Results ==================================");
		print("===================================================");
		print("JavaVersion: " + JavaVersion.getJavaVersion());
		print("test1 (Socket    before initSocketImplFactory): "
				+ (test1Result ? "OK" : "FAILED"));
		print("test2 (NetSocket before initSocketImplFactory): "
				+ (test2Result ? "OK" : "FAILED"));
		if (checkWithSocketGlobalUtil)
		{
			print("test3 (Socket    after initSocketImplFactory):  "
					+ (test3Result ? "OK" : "FAILED"));
			print("test4 (NetSocket after initSocketImplFactory):  "
					+ (test4Result ? "OK" : "FAILED"));
		}

		// result
		return test1Result && test2Result && test3Result && test4Result;
	}

	private static void print(final String s)
	{
		System.out.println(s);
	}
}
