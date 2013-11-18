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

package org.silvertunnel_ng.netlib.layer.tor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import org.silvertunnel_ng.netlib.api.HttpTestUtil;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test the support of .exit host names to specify Tor exit nodes.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorCleanRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorCleanRemoteTest.class);

	@Override
	@Test(timeOut = 600000)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		super.initializeTor();
	}

	@Test(timeOut = 15000, dependsOnMethods = {"initializeTor" })
	public void testTwoConnectionsWithoutReset() throws Exception
	{
		// determine 1st exit node id
		final IpNetAddress exitNodeIp1 = HttpTestUtil
				.getSourceIpNetAddress(torNetLayer);
		LOG.info("exitNodeIp1=" + exitNodeIp1);

		// determine 2nd exit node id
		final IpNetAddress exitNodeIp2 = HttpTestUtil
				.getSourceIpNetAddress(torNetLayer);
		LOG.info("exitNodeIp2=" + exitNodeIp2);
		LOG.info("exitNodeIp1=" + exitNodeIp1);

		assertEquals("exitNodeIp1!=exitNodeIp2 (but not expected)",
				exitNodeIp1, exitNodeIp2);
	}

	@Test(timeOut = 30000, dependsOnMethods = {"initializeTor" })
	public void testTwoConnectionsWithReset() throws Exception
	{
		// determine 1st exit node id
		final IpNetAddress exitNodeIp1 = HttpTestUtil
				.getSourceIpNetAddress(torNetLayer);
		LOG.info("exitNodeIp1=" + exitNodeIp1);
		assertNotNull("exitNodeIp1==null", exitNodeIp1);

		// reset ToNetLayer state
		torNetLayer.clear();

		// determine 2nd exit node id
		final IpNetAddress exitNodeIp2 = HttpTestUtil
				.getSourceIpNetAddress(torNetLayer);
		LOG.info("exitNodeIp2=" + exitNodeIp2);
		assertNotNull("exitNodeIp1==null", exitNodeIp1);

		// log again to simplify log file reading
		LOG.info("exitNodeIp1=" + exitNodeIp1);

		// check
		if (exitNodeIp1.equals(exitNodeIp2))
		{
			fail("exitNodeIp1==exitNodeIp2 (but not expected)");
		}
	}
	/*
	 * @Test(timeout=15000)
	 * 
	 * @Given("#initializeTor") public void testWithHostname2() throws Exception
	 * { testWithHostname(); }
	 */
}
