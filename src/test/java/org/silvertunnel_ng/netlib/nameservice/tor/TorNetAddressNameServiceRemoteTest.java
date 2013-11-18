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

package org.silvertunnel_ng.netlib.nameservice.tor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorRemoteAbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test TorNetAddressNameService's basic features.
 * 
 * Similar test cases:
 * 
 * @see org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameServiceRemoteTest
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class TorNetAddressNameServiceRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorNetAddressNameServiceRemoteTest.class);

	private static final String TEST1_HOSTNAME = "dnstest.silvertunnel-ng.org";
	private static final IpNetAddress TEST1_IP = new IpNetAddress("1.2.3.4");
	private static final String TEST3_HOSTNAME = "google-public-dns-a.google.com";
	private static final IpNetAddress TEST3_IP = new IpNetAddress("8.8.8.8");
	private static final String TEST_INVALID_HOSTNAME = "silvertunnel-no-dns-entry.org";

	@Override
	@Test(timeOut = 600000)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		super.initializeTor();
	}

	/**
	 * Test host name -> IP mapping for dnstest.silvertunnel.org.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 30000, dependsOnMethods = {"initializeTor" })
	public void testGetAddressesByName() throws Exception
	{
		final NetAddressNameService ns = torNetLayer.getNetAddressNameService();
		final NetAddress[] resolvedIps = ns.getAddressesByName(TEST1_HOSTNAME);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST1_IP, resolvedIps[0]);
	}

	/**
	 * Test IP -> host name mapping for 8.8.8.8.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 30000, dependsOnMethods = {"initializeTor" })
	public void testGetNamesByAddress() throws Exception
	{
		final NetAddressNameService ns = torNetLayer.getNetAddressNameService();
		final String[] resolvedNames = ns.getNamesByAddress(TEST3_IP);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST3_HOSTNAME, resolvedNames[0]);
	}

	/**
	 * Test host name -> IP mapping for silvertunnel-no-dns-entry.org.
	 * 
	 * Some ISPs resolve every name to an IP address (to redirect web traffic to
	 * their servers) - when the exit node is connected to such a ISP then this
	 * test will fail.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 90000, dependsOnMethods = {"initializeTor" })
	public void testGetAddressesByNameInvalid() throws Exception
	{
		final NetAddressNameService ns = torNetLayer.getNetAddressNameService();
		try
		{
			final NetAddress[] resolvedIps = ns
					.getAddressesByName(TEST_INVALID_HOSTNAME);

			// this should not be executed:
			final String resolvedIpsStr = (resolvedIps == null) ? null : Arrays
					.toString(resolvedIps);
			fail("expected UnknownHostException not thrown, resolvedIps="
					+ resolvedIpsStr);

		}
		catch (final UnknownHostException e)
		{
			// expected
		}
	}
}
