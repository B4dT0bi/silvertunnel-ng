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

package org.silvertunnel_ng.netlib.adapter.nameservice;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.nameservice.mock.MockNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 * JUnit test cases to test NameServiceGlobalUtil and the NameService adapter.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class WithNetlibNameServiceLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(WithNetlibNameServiceLocalTest.class);

	private static final String MOCK_HOSTNAME1 = "dnstest.silvertunnel-ng.org";
	private static final IpNetAddress MOCK_IP1 = new IpNetAddress("44.11.33.22");

	private static final String MOCK_HOSTNAME2 = "dnstest2.silvertunnel-ng.org";
	private static final IpNetAddress MOCK_IP2 = new IpNetAddress("88.66.77.56");

	/**
	 * field to store a Throwable thrown in the static constructor to throw it
	 * later (in a test method).
	 */
	private static Throwable throwableOfStaticInitializer;

	/**
	 * Install NetlibNameService.
	 * 
	 * Do not use Junit's @org.junit.Before because this is too late.
	 */
	static
	{
		LOG.info("static init");
		try
		{
			NameServiceGlobalUtil.initNameService();
		}
		catch (final Throwable t)
		{
			throwableOfStaticInitializer = t;
		}
	}

	/**
	 * Check that the NopNetAddressNameService is really used after setup.
	 */
	@Test(timeOut = 5000)
	public void testWithNopNetAddressNameService() throws Throwable
	{
		LOG.info("testWithNopNetAddressNameService()");
		if (throwableOfStaticInitializer != null)
		{
			throw throwableOfStaticInitializer;
		}

		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress result1 = InetAddress
					.getAllByName(MOCK_HOSTNAME1)[0];
			// we should not reach this code because name resolution should fail
			fail(MOCK_HOSTNAME1
					+ " could be resolved to "
					+ result1
					+ " (was not expected) - this probably means that the NetlibNameService was not used but the Internet instead");

		}
		catch (final UnknownHostException e)
		{
			// this is expected
		}
	}

	/**
	 * Check that we can switch to an alternative NetAddressNameService (to the
	 * MockNetAddressNameService).
	 */
	@Test(timeOut = 15000, dependsOnMethods = {"testWithNopNetAddressNameService" })
	public void testWithMockNetAddressNameService() throws Throwable
	{
		LOG.info("testWithMockNetAddressNameService()");
		if (throwableOfStaticInitializer != null)
		{
			throw throwableOfStaticInitializer;
		}

		//
		// switch to MockNetAddressNameService
		//
		final Map<String, NetAddress> name2AddressMapping = new HashMap<String, NetAddress>();
		final Map<NetAddress, String> address2NameMapping = new HashMap<NetAddress, String>();
		name2AddressMapping.put(MOCK_HOSTNAME1, MOCK_IP1);
		final NetAddressNameService ns = new MockNetAddressNameService(
				name2AddressMapping, address2NameMapping);
		NameServiceGlobalUtil.setIpNetAddressNameService(ns);

		// circumvent caching
		Thread.sleep(NameServiceGlobalUtil.getCacheTimeoutMillis());

		//
		// check that dnstest.silvertunnel-ng.org can be resolved now
		// and that dnstest2.silvertunnel-ng.org cannot be resolved
		//
		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress result1 = InetAddress
					.getAllByName(MOCK_HOSTNAME1)[0];
			assertEquals(MOCK_HOSTNAME1 + " resolved to wrong address",
					MOCK_IP1, new IpNetAddress(result1));

		}
		catch (final UnknownHostException e)
		{
			fail("resolution of " + MOCK_HOSTNAME1
					+ " failed, but it should be resolved to " + MOCK_IP1);
		}

		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress result2 = InetAddress
					.getAllByName(MOCK_HOSTNAME2)[0];
			// we should not reach this code because name resolution should fail
			fail(MOCK_HOSTNAME2 + "resolved to " + result2
					+ " (was not expected)");

		}
		catch (final UnknownHostException e)
		{
			// this is expected
		}
	}

	/**
	 * Check that we can switch to an alternative NetAddressNameService (to the
	 * MockNetAddressNameService).
	 */
	@Test(timeOut = 15000, dependsOnMethods = {"testWithMockNetAddressNameService" })
	public void testWithMockNetAddressNameService2() throws Throwable
	{
		LOG.info("testWithMockNetAddressNameService2()");
		if (throwableOfStaticInitializer != null)
		{
			throw throwableOfStaticInitializer;
		}

		//
		// switch to 2nd MockNetAddressNameService
		//
		final Map<String, NetAddress> name2AddressMapping = new HashMap<String, NetAddress>();
		final Map<NetAddress, String> address2NameMapping = new HashMap<NetAddress, String>();
		name2AddressMapping.put(MOCK_HOSTNAME2, MOCK_IP2);
		final NetAddressNameService ns = new MockNetAddressNameService(
				name2AddressMapping, address2NameMapping);
		NameServiceGlobalUtil.setIpNetAddressNameService(ns);

		// circumvent caching
		Thread.sleep(NameServiceGlobalUtil.getCacheTimeoutMillis());

		//
		// check that dnstest.silvertunnel-ng.org can be resolved now
		// and that dnstest2.silvertunnel-ng.org cannot be resolved
		//
		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress result1 = InetAddress
					.getAllByName(MOCK_HOSTNAME1)[0];
			// we should not reach this code because name resolution should fail
			fail(MOCK_HOSTNAME1 + "resolved to " + result1
					+ " (was not expected)");

		}
		catch (final UnknownHostException e)
		{
			// this is expected
		}

		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress result2 = InetAddress
					.getAllByName(MOCK_HOSTNAME2)[0];
			assertEquals(MOCK_HOSTNAME2 + " resolved to wrong address",
					MOCK_IP2, new IpNetAddress(result2));

		}
		catch (final UnknownHostException e)
		{
			fail("resolution of " + MOCK_HOSTNAME2
					+ " failed, but it should be resolved to " + MOCK_IP2);
		}
	}
}
