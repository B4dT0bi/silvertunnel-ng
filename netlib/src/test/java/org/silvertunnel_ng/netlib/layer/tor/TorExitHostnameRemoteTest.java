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
import org.silvertunnel_ng.netlib.layer.tor.util.TorServerNotFoundException;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

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

	private static final String OUR_EXITNODE_HEX_DIGEST[] = {"AB176BD65735A99DCCB7889184E62EF0B2E35751", "48B1B47BD189B86EFD67D93AB6904DAEFFE81B82", "9BDF3EEA1D33AA58A2EEA9E6CA58FB8A667288FC"};
	private static final String OUR_EXITNODE_IP[] = {"77.244.254.228", "31.172.30.1", "77.244.254.227"};
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
        for (int i = 0; i < OUR_EXITNODE_HEX_DIGEST.length; i++) {
            try {
                final String HOSTNAME = HttpUtil.HTTPTEST_SERVER_NAME + "." + OUR_EXITNODE_HEX_DIGEST[i] + ".exit";
                final TcpipNetAddress NETADDRESS = new TcpipNetAddress(HOSTNAME, HttpUtil.HTTPTEST_SERVER_PORT);

                // determine exit node id
                final IpNetAddress exitNodeIp = HttpTestUtil.getSourceIpNetAddress(
                        torNetLayer, NETADDRESS, "/httptest/bigtest.php");

                // check result
                assertEquals("wrong exit node IP determined", new IpNetAddress(
                        OUR_EXITNODE_IP[i]), exitNodeIp);
            } catch (IOException e) {
                // did we got a TorServerNotFound Exception? If yes try the next exit node
                if (!(e.getCause() instanceof TorServerNotFoundException)) {
                    throw e; // else rethrow the exception
                }
                LOG.info("skipping exit with fingerprint {} due to exception", OUR_EXITNODE_HEX_DIGEST[i]);
            }
        }
	}
}
