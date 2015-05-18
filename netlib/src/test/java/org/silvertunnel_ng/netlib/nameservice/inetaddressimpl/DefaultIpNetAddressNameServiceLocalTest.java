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

package org.silvertunnel_ng.netlib.nameservice.inetaddressimpl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of DefaultIpNetAddressNameService.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class DefaultIpNetAddressNameServiceLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DefaultIpNetAddressNameServiceLocalTest.class);

	private static final String TEST_HOSTNAME = "localhost";
	private static final IpNetAddress TEST_IP = new IpNetAddress("127.0.0.1");

	/**
	 * Test host name (localhost) -> IP (127.0.0.1) mapping.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLocalhostLookupAddress() throws Exception
	{
		final DefaultIpNetAddressNameService ns = new DefaultIpNetAddressNameService();
		final NetAddress[] resolvedIps = ns.getAddressesByName(TEST_HOSTNAME);

		// make the resolvedIps unique
		final Set<NetAddress> uniqueResolvedIps = new HashSet<NetAddress>(
				Arrays.asList(resolvedIps));

		assertEquals("wrong number of IPs found", 1, uniqueResolvedIps.size());
		assertEquals("wrong IP found", TEST_IP, resolvedIps[0]);
	}

	/**
	 * Test IP (127.0.0.1) -> host name (localhost) mapping.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = false)
	//@Ignore // Ignore this testcase as it doesnt retrieve the localhost anymore but instead the pc name + domain
	public void testLocalhostLookupName() throws Exception
	{
		final DefaultIpNetAddressNameService ns = new DefaultIpNetAddressNameService();
		final String[] resolvedNames = ns.getNamesByAddress(TEST_IP);
		assertEquals("wrong number of names found", 1, resolvedNames.length);
		assertEquals("wrong name found", TEST_HOSTNAME, resolvedNames[0]);
	}
}
