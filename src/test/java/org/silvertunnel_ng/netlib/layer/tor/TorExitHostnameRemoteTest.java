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

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.api.HttpTestUtil;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the support of .exit host names to specify Tor exit nodes.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorExitHostnameRemoteTest extends TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorExitHostnameRemoteTest.class);

	// our exit node: chaoscomputerclub10
	// private static final String OUR_EXITNODE_HEX_DIGEST =
	// "11A0239FC6668705F68842811318B669C636F86E";
	// private static final String OUR_EXITNODE_IP = "62.113.219.3";

	// our exit node: chaoscomputerclub30
	// parameters found with: grep -A 9 chaoscomputerclub30
	// /tmp/st-directory-cached-router-descriptors.txt
	//TODO : this test fails sometimes (maybe exitnode is under heavy load/ressource limit/hibernation/etc) try to make this test more stable
	private static final String OUR_EXITNODE_HEX_DIGEST = "0936672091AD02665560F0C8B09B890A98590BE2";
	private static final String OUR_EXITNODE_IP = "77.244.254.230";
	@BeforeClass
	public static void setUp()
	{
		if (!NameServiceGlobalUtil.isDefaultIpNetAddressNameServiceActive())
		{
			NameServiceGlobalUtil.activateDefaultIpNetAddressNameService();
		}			
	}
	@Override
	@Test(timeOut = 600000)
	public void initializeTor() throws Exception
	{
		// repeat method declaration here to be the first test method of the
		// class
		super.initializeTor();
	}

	@Test(timeOut = 15000, dependsOnMethods = {"initializeTor" })
	public void testWithHostname() throws Exception
	{
		final String HOSTNAME = HttpUtil.HTTPTEST_SERVER_NAME + "." + OUR_EXITNODE_HEX_DIGEST + ".exit";
		final TcpipNetAddress NETADDRESS = new TcpipNetAddress(HOSTNAME, HttpUtil.HTTPTEST_SERVER_PORT);

		// determine exit node id
		final IpNetAddress exitNodeIp = HttpTestUtil.getSourceIpNetAddress(
				torNetLayer, NETADDRESS, "/httptest/bigtest.php");

		// check result
		assertEquals("wrong exit node IP determined", new IpNetAddress(
				OUR_EXITNODE_IP), exitNodeIp);
	}
}
