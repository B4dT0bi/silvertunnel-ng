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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author Tobias Boese
 * 
 */
public final class RouterDescriptorFormatKeysLocalTest
{
	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterDescriptorFormatKeys#getMin()}
	 * .
	 */
	@Test
	public void testGetMin()
	{
		assertEquals("should allow any number", 0, RouterDescriptorFormatKeys.ACCEPT.getMin());
		assertEquals("should allow any number", 0, RouterDescriptorFormatKeys.REJECT.getMin());
		assertEquals("should allow any number", 0, RouterDescriptorFormatKeys.OR_ADDRESS.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.ROUTER_INFO.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.BANDWIDTH.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.PLATFORM.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.PUBLISHED.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.FINGERPRINT.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.HIBERNATING.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.UPTIME.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.ONION_KEY.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.NTOR_ONION_KEY.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.SIGNING_KEY.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.IPV6_POLICY.getMin());
		assertEquals("should be allowed exactly once", 1, RouterDescriptorFormatKeys.ROUTER_SIGNATURE.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.CONTACT.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.FAMILY.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.CACHES_EXTRA_INFO.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.EXTRA_INFO_DIGEST.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.HIDDEN_SERVICE_DIR.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.PROTOCOLS.getMin());
		assertEquals("should be allowed at most once", 0, RouterDescriptorFormatKeys.ALLOW_SINGLE_HOP_EXITS.getMin());
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterDescriptorFormatKeys#getMax()}
	 * .
	 */
	@Test
	public void testGetMax()
	{
		assertEquals("should allow any number", Integer.MAX_VALUE, RouterDescriptorFormatKeys.ACCEPT.getMax());
		assertEquals("should allow any number", Integer.MAX_VALUE, RouterDescriptorFormatKeys.REJECT.getMax());
		assertEquals("should allow any number", Integer.MAX_VALUE, RouterDescriptorFormatKeys.OR_ADDRESS.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.ROUTER_INFO.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.BANDWIDTH.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.PLATFORM.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.PUBLISHED.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.FINGERPRINT.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.HIBERNATING.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.UPTIME.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.ONION_KEY.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.NTOR_ONION_KEY.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.SIGNING_KEY.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.IPV6_POLICY.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.ROUTER_SIGNATURE.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.CONTACT.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.FAMILY.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.CACHES_EXTRA_INFO.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.EXTRA_INFO_DIGEST.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.HIDDEN_SERVICE_DIR.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.PROTOCOLS.getMax());
		assertEquals("should be allowed only once", 1, RouterDescriptorFormatKeys.ALLOW_SINGLE_HOP_EXITS.getMax());
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterDescriptorFormatKeys#getAllKeysAsMap()}
	 * .
	 */
	@Test
	public void testGetAllKeysAsMap()
	{
		final Map<RouterDescriptorFormatKeys, Integer> allKeys = RouterDescriptorFormatKeys.getAllKeysAsMap();
		assertEquals("if this fails one or more keys are missing in getAllKeysAsMap method", allKeys.size(),
						RouterDescriptorFormatKeys.values().length);
	}

}
