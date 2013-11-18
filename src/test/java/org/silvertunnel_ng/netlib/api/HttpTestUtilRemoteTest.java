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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test HttpTestUtil.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class HttpTestUtilRemoteTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpTestUtilRemoteTest.class);

	@BeforeClass
	public void setUp()
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			
	}
	@Test
	public void testGetSourceIpNetAddress() throws Exception
	{		
		final NetLayer netLayer = NetFactory.getInstance().getNetLayerById(
				NetLayerIDs.TCPIP);

		// test 1
		final IpNetAddress ip1 = HttpTestUtil.getSourceIpNetAddress(netLayer);
		LOG.info("ip1=" + ip1);
		assertNotNull("wrong ip1", ip1);

		// test 2
		final IpNetAddress ip2 = HttpTestUtil.getSourceIpNetAddress(netLayer);
		LOG.info("ip2=" + ip2);
		assertNotNull("wrong ip2", ip2);

		// final check
		assertEquals("unexpected: ip1!=ip2", ip1, ip2);
	}
}
