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

package org.silvertunnel_ng.netlib.nameservice.redirect;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.nameservice.cache.CachingNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.mock.MockNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test of DefaultIpNetAddressNameService.
 * 
 * Test idea: 1) resolve a name1 (or address1), result comes from 1st mock
 * instance 2) switch to the 2nd lower mock instance 3) resolve a name1 (or
 * address1) again, result comes from the cache (cannot come from 2nd mock
 * instance) 4) wait until time-to-live end, i.e. cache is empty then 5) resolve
 * a name1 (or address1) again, this fails now because it cannot come from 2nd
 * mock instance) 6) resolve a name2 (or address2), result comes from 2nd mock
 * instance
 * 
 * Setup: SwitchingNetAddressNameService(2xMock) + on-top
 * CachingNetAddressNameService:
 * 
 * <pre>
 *   +-------------------------------------------------+
 *   |          CachingNetAddressNameService           |
 *   +-------------------------------------------------+
 *   |         SwitchingNetAddressNameService          |
 *   +-------------------------------------------------+
 *   | MockNetAddressNameService (1st or 2nd instance) |
 *   +-------------------------------------------------+
 * </pre>
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SwitchingAndCachingNetAddressNameServiceLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SwitchingAndCachingNetAddressNameServiceLocalTest.class);

	// the following host name - IP combinations should be different to real
	// live mappings
	private static final String TEST_HOSTNAME1 = "dnstest.silvertunnel-ng.org";
	private static final IpNetAddress TEST_IP1 = new IpNetAddress("11.22.33.44");

	private static final String TEST_HOSTNAME2 = "silvertunnel-no-dns-entry.org";
	private static final IpNetAddress TEST_IP2 = new IpNetAddress("55.66.77.88");

	private static final int MAX_ELEMENTS_IN_CACHE = 100;
	private static final boolean IS_NAME_CASE_SENSITIVE = false;
	private static final int CACHE_TTL_SECONDS = 2;
	private static final int CACHE_NEGATIVE_TTL_SECONDS = 2;

	/**
	 * SwitchingNetAddressNameService(2xMock) + on-top
	 * CachingNetAddressNameService.
	 */
	private NetAddressNameService ns;
	/** 2nd mock: we will switch to it during the test. */
	private NetAddressNameService mock2Ns;
	/** SwitchingNetAddressNameService. */
	private SwitchingNetAddressNameService switchingNs;

	@BeforeMethod
	public void setUp()
	{
		// prepare and mock1
		final Map<String, NetAddress> name2AddressMapping1 = new HashMap<String, NetAddress>();
		final Map<NetAddress, String> address2NameMapping1 = new HashMap<NetAddress, String>();
		name2AddressMapping1.put(TEST_HOSTNAME1, TEST_IP1);
		address2NameMapping1.put(TEST_IP1, TEST_HOSTNAME1);
		final NetAddressNameService mock1Ns = new MockNetAddressNameService(
				name2AddressMapping1, address2NameMapping1);

		// prepare and mock2
		final Map<String, NetAddress> name2AddressMapping2 = new HashMap<String, NetAddress>();
		final Map<NetAddress, String> address2NameMapping2 = new HashMap<NetAddress, String>();
		name2AddressMapping2.put(TEST_HOSTNAME2, TEST_IP2);
		address2NameMapping2.put(TEST_IP2, TEST_HOSTNAME2);
		mock2Ns = new MockNetAddressNameService(name2AddressMapping2,
				address2NameMapping2);

		// SwitchingNetAddressNameService on top
		switchingNs = new SwitchingNetAddressNameService(mock1Ns);

		// CachingNetAddressNameService on top
		ns = new CachingNetAddressNameService(switchingNs,
				MAX_ELEMENTS_IN_CACHE, IS_NAME_CASE_SENSITIVE,
				CACHE_TTL_SECONDS, CACHE_NEGATIVE_TTL_SECONDS);
	}

	@Test
	public void testGetAddressesByName() throws Exception
	{
		// 1) resolve a name1 (or address1), result comes from 1st mock instance
		NetAddress[] resolvedIps = ns.getAddressesByName(TEST_HOSTNAME1);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST_IP1, resolvedIps[0]);

		// 1a) resolve a name2 (or address2), negative result comes from 1st
		// mock instance
		try
		{
			resolvedIps = ns.getAddressesByName(TEST_HOSTNAME2);
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

		// 2) switch to the 2nd lower mock instance
		switchingNs.setLowerNetAddressNameService(mock2Ns);

		// 3) resolve a name1 (or address1) again, result comes from the
		// positive cache (cannot come from 2nd mock instance)
		resolvedIps = ns.getAddressesByName(TEST_HOSTNAME1);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST_IP1, resolvedIps[0]);

		// 3a) resolve a name2 (or address2), negative comes from the positive
		// cache (cannot come from 2nd mock instance)
		try
		{
			resolvedIps = ns.getAddressesByName(TEST_HOSTNAME2);
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

		// 4) wait until time-to-live end, i.e. cache is empty then
		Thread.sleep((1000L * (Math.max(CACHE_TTL_SECONDS,
				CACHE_NEGATIVE_TTL_SECONDS))) + 100L);

		// 5) resolve a name1 (or address1) again, this fails now because it
		// cannot come from 2nd mock instance)
		try
		{
			resolvedIps = ns.getAddressesByName(TEST_HOSTNAME1);
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

		// 6) resolve a name2 (or address2), result comes from 2nd mock instance
		resolvedIps = ns.getAddressesByName(TEST_HOSTNAME2);
		assertEquals("wrong number of IPs found", 1, resolvedIps.length);
		assertEquals("wrong IP found", TEST_IP2, resolvedIps[0]);
	}

	@Test
	public void testGetNamesByAddress() throws Exception
	{
		// 1) resolve a name1 (or address1), result comes from 1st mock instance
		String[] resolvedNames = ns.getNamesByAddress(TEST_IP1);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST_HOSTNAME1, resolvedNames[0]);

		// 1a) resolve a name2 (or address2), negative result comes from 1st
		// mock instance
		try
		{
			resolvedNames = ns.getNamesByAddress(TEST_IP2);
			// this should not be executed:
			final String resolvedNamesStr = (resolvedNames == null) ? null
					: Arrays.toString(resolvedNames);
			fail("expected UnknownHostException not thrown, resolvedIps="
					+ resolvedNamesStr);
		}
		catch (final UnknownHostException e)
		{
			// expected
		}

		// 2) switch to the 2nd lower mock instance
		switchingNs.setLowerNetAddressNameService(mock2Ns);

		// 3) resolve a name1 (or address1) again, result comes from the
		// positive cache (cannot come from 2nd mock instance)
		resolvedNames = ns.getNamesByAddress(TEST_IP1);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST_HOSTNAME1, resolvedNames[0]);

		// 3a) resolve a name2 (or address2), negative comes from the positive
		// cache (cannot come from 2nd mock instance)
		try
		{
			resolvedNames = ns.getNamesByAddress(TEST_IP2);
			// this should not be executed:
			final String resolvedNamesStr = (resolvedNames == null) ? null
					: Arrays.toString(resolvedNames);
			fail("expected UnknownHostException not thrown, resolvedIps="
					+ resolvedNamesStr);
		}
		catch (final UnknownHostException e)
		{
			// expected
		}

		// 4) wait until time-to-live end, i.e. cache is empty then
		Thread.sleep((1000L * (Math.max(CACHE_TTL_SECONDS,
				CACHE_NEGATIVE_TTL_SECONDS))) + 100L);

		// 5) resolve a name1 (or address1) again, this fails now because it
		// cannot come from 2nd mock instance)
		try
		{
			resolvedNames = ns.getNamesByAddress(TEST_IP1);
			// this should not be executed:
			final String resolvedNamesStr = (resolvedNames == null) ? null
					: Arrays.toString(resolvedNames);
			fail("expected UnknownHostException not thrown, resolvedIps="
					+ resolvedNamesStr);
		}
		catch (final UnknownHostException e)
		{
			// expected
		}

		// 6) resolve a name2 (or address2), result comes from 2nd mock instance
		resolvedNames = ns.getNamesByAddress(TEST_IP2);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST_HOSTNAME2, resolvedNames[0]);
	}
}
