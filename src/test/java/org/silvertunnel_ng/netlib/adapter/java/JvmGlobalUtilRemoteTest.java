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

package org.silvertunnel_ng.netlib.adapter.java;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * JUnit test cases to test NameServiceGlobalUtil and the NameService adapter.
 * 
 * Hint: Because of initialization issue, these test cases do not work in
 * Eclipse - you have to start them (with ant) from command line!
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class JvmGlobalUtilRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(JvmGlobalUtilRemoteTest.class);

	private static final String SOCKETTEST_HOSTNAME = "httptest.silvertunnel-ng.org";
	private static final int SOCKETTEST_PORT = 80;

	private static final String DNSTEST_HOSTNAME = "dnstest.silvertunnel-ng.org";
	private static final IpNetAddress DNSTEST_IP = new IpNetAddress("1.2.3.4");

	/**
	 * Do initial setup.
	 */
	@BeforeClass
	public void setUp()
	{
		JvmGlobalUtil.init();
	}
	/**
	 * Check that the NopNet is really used after setup with
	 * JvmGlobalUtil.init().
	 */
	@Test(timeOut = 1000)
	public void testAfterInit() throws Throwable
	{
		LOG.info("testAfterInit()");
		// check functionality
		checkNopNet();
	}

	private void checkNopNet()
	{
		// test sockets
		try
		{
			// try to use Java standard way of TCP/IP communication
			new Socket(SOCKETTEST_HOSTNAME, SOCKETTEST_PORT).close();
			// we should not reach this code because socket creation should fail
			fail("Connection to " + SOCKETTEST_HOSTNAME + ":" + SOCKETTEST_PORT
					+ " was established (but not expected)");

		}
		catch (final IOException e)
		{
			// this is expected
		}

		// test name service
		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress[] result = InetAddress.getAllByName(DNSTEST_HOSTNAME);
			// we should not reach this code because name resolution should fail
			fail(DNSTEST_HOSTNAME
					+ " could be resolved to "
					+ Arrays.toString(result)
					+ " (was not expected) - this probably means that the NetlibNameService was not used but the Internet instead");

		}
		catch (final UnknownHostException e)
		{
			// this is expected
		}
	}

	/**
	 * Check that the "DefaultNet" is used after JvmGlobalUtil.setDefaultNet().
	 */
	@Test(timeOut = 20000, dependsOnMethods = {"testAfterInit" })
	public void testDefaultNet() throws Throwable
	{
		LOG.info("testDefaultNet()");
		// switch lower services and wait until ready
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
		JvmGlobalUtil.setNetLayerAndNetAddressNameService(netLayer, true);

		// check functionality
		checkDefaultNet();
	}

	private void checkDefaultNet() throws IOException
	{
		// test sockets
		{
			// try to use Java standard way of TCP/IP communication
			new Socket(SOCKETTEST_HOSTNAME, SOCKETTEST_PORT).close();
		}

		// test name service
		{
			// try to use Java standard way of DNS resolution
			final InetAddress[] result = InetAddress.getAllByName(DNSTEST_HOSTNAME);
			assertEquals("wrong name resolution result", DNSTEST_IP,
					new IpNetAddress(result[0]));
		}
	}

	/**
	 * Check using of TorNetLayer.
	 */
	@Test(timeOut = 120000, dependsOnMethods = {"testAfterInit", "testDefaultNet" })
	public void testTorNet() throws Throwable
	{
		LOG.info("testTorNet()");

		// switch lower services and wait until ready
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
		JvmGlobalUtil.setNetLayerAndNetAddressNameService(netLayer, true);

		// check functionality
		checkTorNet();
	}

	private void checkTorNet() throws IOException
	{
		// test sockets
		{
			// try to use Java standard way of TCP/IP communication
			new Socket(SOCKETTEST_HOSTNAME, SOCKETTEST_PORT).close();
		}

		// test name service
		{
			// try to use Java standard way of DNS resolution
			final InetAddress[] result = InetAddress.getAllByName(DNSTEST_HOSTNAME);
			assertEquals("wrong name resolution result", DNSTEST_IP,
					new IpNetAddress(result[0]));
		}

	}

	/**
	 * Check that the "NopNet" is used after JvmGlobalUtil.setNopNet();
	 */
	@Test(timeOut = 20000, dependsOnMethods = {"testAfterInit" })
	public void testNopNet() throws Throwable
	{
		LOG.info("testNopNet()");

		// switch lower services and wait until ready
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.NOP);
		JvmGlobalUtil.setNetLayerAndNetAddressNameService(netLayer, true);
		
		// check functionality
		checkNopNet();
	}

	/**
	 * Check using of TorNetLayer (special tests);
	 */
	@Test(timeOut = 120000, dependsOnMethods = {"testAfterInit", "testTorNet" })
	public void testTorNetAdvanced() throws Throwable
	{
		LOG.info("testTorNet2()");

		// switch lower services and wait until ready
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
		JvmGlobalUtil.setNetLayerAndNetAddressNameService(netLayer, true);

		// check functionality
		checkTorNetAdvanced();
	}

	private void checkTorNetAdvanced() throws IOException
	{
		// test sockets
		{
			// try to use Java standard way of TCP/IP communication
			final Socket s = new Socket(SOCKETTEST_HOSTNAME, SOCKETTEST_PORT);
			final BufferedReader in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			final OutputStream os = s.getOutputStream();
			final PrintStream out = new PrintStream(new DataOutputStream(os));

			// send message - without explicit flush!
			out.print("GET / HTTP/1.0\n");	
			out.println("Host: " + SOCKETTEST_HOSTNAME + "\n\n");

			// wait for server response
			final String firstLine = in.readLine();
			assertEquals("wrong first line of response", "HTTP/1.1 200 OK", firstLine);
			s.close();
		}
	}
}
