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

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.fail;

/**
 * Test Tor's hidden services (client implementation).
 * Open's and closes man connections for profiling.
 *
 * @author Tobias Boese
 *         <p/>
 *         TestNG arguments : -agentpath:"C:\Program Files (x86)\YourKit Java Profiler 2013 build 13062\bin\win64\yjpagent.dll=tracing"
 */
public final class TorHiddenServiceClientRemotePerformanceTest extends TorRemoteAbstractTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TorHiddenServiceClientRemotePerformanceTest.class);

    private static final long TIMEOUT = 30000;
    /**
     * if NUM_OF_TEST_EXECUTIONS==1 then this test class behaves like an
     * unparameterized one.
     */
    private static final int NUM_OF_TEST_EXECUTIONS = 10;

    /**
     * Parametrized testcase.
     *
     * @return empty Objects with size of NUM_OF_TEST_EXECUTIONS
     */
    @DataProvider(name = "multipleTestExecutions")
    public static Object[][] multipleTestExecutions() {
        return new Object[NUM_OF_TEST_EXECUTIONS][0];
    }

    /**
     * Modify the standard tor settings.
     */
    @BeforeClass
    public void setUp() {
        // setting the route length to 2 (we do not need high security for our
        // tests.
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS, "1");
        TorConfig.reloadConfigFromProperties();
    }

    @Override
    @Test(timeOut = 1200000)
    public void initializeTor() throws Exception {
        // repeat method declaration here to be the first test method of the
        // class
        super.initializeTor();
    }

    /**
     * onion address of silvertunnel server.
     */
    private static final String TORCHECK_HOSTNAME = "h6hk2h7fnr66d4o3.onion";
    private static final TcpipNetAddress TORCHECK_NETADDRESS = new TcpipNetAddress(TORCHECK_HOSTNAME, 80);

    /**
     * Access silvertunnel-ng's hidden service.
     *
     * @throws Exception
     */
    @Test(timeOut = 120000, dependsOnMethods = {"initializeTor"}, dataProvider = "multipleTestExecutions")
    public void testAccessToSilvertunnelOnionDomain() throws Exception {
        LOG.info("testAccessToSilvertunnelOnionDomain started");
        // create connection
        NetSocket topSocket = null;
        try {
            topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR).createNetSocket(null, null, TORCHECK_NETADDRESS);

            HttpUtil.getInstance();
            // communicate with the remote side
            final byte[] httpResponse = HttpUtil.get(topSocket, TORCHECK_NETADDRESS, "/", TIMEOUT);
            String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
            LOG.info("http response body: " + httpResponseStr);

            // make the httpResponseStr readable in HTML reports
            httpResponseStr = removeHtmlTags(httpResponseStr);

            // check result
            if (!httpResponseStr.contains("httptest works.")) {
                fail("did not get correct response of hidden service, response body=" + httpResponseStr);
            }
        } finally {
            if (topSocket != null) {
                topSocket.close();
            }
        }
        LOG.info("testAccessToSilvertunnelOnionDomain ended");
    }
}
