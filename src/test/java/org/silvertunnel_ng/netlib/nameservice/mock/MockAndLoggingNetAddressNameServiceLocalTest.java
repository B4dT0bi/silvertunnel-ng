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

package org.silvertunnel_ng.netlib.nameservice.mock;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.nameservice.logger.LoggingNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of DefaultIpNetAddressNameService.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class MockAndLoggingNetAddressNameServiceLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(MockAndLoggingNetAddressNameServiceLocalTest.class);

	// the following host name - IP combinations should be different to real
	// live mappings
	private static final String TEST1_HOSTNAME = "dnstest.silvertunnel-ng.org";
	private static final IpNetAddress TEST1_IP = new IpNetAddress("11.22.33.44");

	private static final String TEST2_HOSTNAME = "silvertunnel-no-dns-entry.org";
	private static final IpNetAddress TEST2_IP = new IpNetAddress("55.66.77.88");

	private static final IpNetAddress TEST3_IP = new IpNetAddress("127.0.0.1");
	private static final String TEST3_HOSTNAME = "localhost-mock-test2.silvertunnel.org"; //TODO : change to silvertunnel-ng.org

	private static final String TEST_INVALID_HOSTNAME = "dnstest2.silvertunnel-ng.org";

	/** MockNetAddressNameService + LoggingNetAddressNameService */
	private NetAddressNameService ns;

	@BeforeClass
	public void setUp()
	{
		// prepare mock
		final Map<String, NetAddress> name2AddressMapping = new HashMap<String, NetAddress>();
		final Map<NetAddress, String> address2NameMapping = new HashMap<NetAddress, String>();
		name2AddressMapping.put(TEST1_HOSTNAME, TEST1_IP);
		name2AddressMapping.put(TEST2_HOSTNAME, TEST2_IP);
		name2AddressMapping.put(TEST3_HOSTNAME, TEST3_IP);
		address2NameMapping.put(TEST1_IP, TEST1_HOSTNAME);
		address2NameMapping.put(TEST2_IP, TEST2_HOSTNAME);
		address2NameMapping.put(TEST3_IP, TEST3_HOSTNAME);

		// create mock
		final NetAddressNameService mockNs = new MockNetAddressNameService(
				name2AddressMapping, address2NameMapping);

		// surround by a logger
		ns = new LoggingNetAddressNameService(mockNs, Level.INFO);
	}

	/**
	 * Test getAddressesByName().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLookupAddress1() throws Exception
	{
		final NetAddress[] resolvedIps = ns.getAddressesByName(TEST1_HOSTNAME);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST1_IP, resolvedIps[0]);
	}

	/**
	 * Test getAddressesByName().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLookupAddress2() throws Exception
	{
		final NetAddress[] resolvedIps = ns.getAddressesByName(TEST2_HOSTNAME);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST2_IP, resolvedIps[0]);
	}

	/**
	 * Test getNamesByAddress().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLookupName3() throws Exception
	{
		final String[] resolvedNames = ns.getNamesByAddress(TEST3_IP);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST3_HOSTNAME, resolvedNames[0]);
	}

	/**
	 * Test host name -> IP mapping for silvertunnel-no-dns-entry.org.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLookupAddressInvalid() throws Exception
	{
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
