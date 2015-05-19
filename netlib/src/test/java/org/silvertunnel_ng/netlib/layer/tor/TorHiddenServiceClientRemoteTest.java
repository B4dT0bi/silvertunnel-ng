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
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.fail;

/**
 * Test Tor's hidden services (client implementation).
 *
 * @author hapke
 * @author Tobias Boese
 */
public final class TorHiddenServiceClientRemoteTest extends TorRemoteAbstractTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TorHiddenServiceClientRemoteTest.class);
    /**
     * onion address of Tor Directory.
     */
    private static final String TOR_DIRECTORY_HOSTNAME = "dppmfxaacucguzpc.onion";
    /**
     * onion address of Tor's example hidden service.
     */
    private static final String TOR_EXAMPLE_HOSTNAME = "duskgytldkxiuqc6.onion";
    /**
     * onion address of SilverTunnel-NG's test hidden service.
     */
    private static final String SILVERTUNNEL_HOSTNAME = "h6hk2h7fnr66d4o3.onion";
    /**
     * onion address of WikiLeaks.
     */
    private static final String WIKILEAKS_HOSTNAME = "gaddbiwdftapglkq.onion";

    private static final long TIMEOUT = 30000;

    /**
     * Setup SilverTunnel-NG Netlib.
     */
    @BeforeClass
    public void setUp() {
        // setting the route length to 2 (we do not need high security for our
        // tests.
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH, "2");
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH, "2");
        System.setProperty(TorConfig.SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS, "2");
        TorConfig.reloadConfigFromProperties();
    }

    @Override
    @Test(timeOut = 30000)
    public void initializeTor() throws Exception {
        // repeat method declaration here to be the first test method of the
        // class
        super.initializeTor();
    }

    /**
     * Open a connection to the given hidden service and read the whole content.
     *
     * @param onionAddress the *.onion address
     * @param port         the port
     * @return a string containing the http response body
     * @throws Exception in case of error
     */
    private String openHiddenService(final String onionAddress, final int port) throws Exception {
        final TcpipNetAddress netAddress = new TcpipNetAddress(onionAddress, port);
        String result = "";
        // create connection
        NetSocket topSocket = null;
        try {
            topSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP)
                    .createNetSocket(null, null, netAddress);

            HttpUtil.getInstance();
            // communicate with the remote side
            final byte[] httpResponse = HttpUtil.get(topSocket, netAddress, "/", TIMEOUT);
            String httpResponseStr = ByteArrayUtil.showAsString(httpResponse);
            LOG.info("http response body: " + httpResponseStr);

            // make the httpResponseStr readable in HTML reports
            result = removeHtmlTags(httpResponseStr);
        } finally {
            if (topSocket != null) {
                topSocket.close();
            }
        }
        return result;
    }

    /**
     * Access Tor's example hidden service.
     *
     * @throws Exception
     */
    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor"})
    public void testAccessToTorsExampleOnionDomain() throws Exception {
        String httpResponseStr = openHiddenService(TOR_EXAMPLE_HOSTNAME, 80);
        // check result
        if (!httpResponseStr.contains("This is the example page for Tor's")) {
            fail("did not get correct response of hidden service, response body=" + httpResponseStr);
        }
    }

    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor"})
    public void testAccessToSilvertunnelOnionDomain() throws Exception {
        String httpResponseStr = openHiddenService(SILVERTUNNEL_HOSTNAME, 80);
        // check result
        if (!httpResponseStr.contains("httptest works.")) {
            fail("did not get correct response of hidden service, response body=" + httpResponseStr);
        }
    }

    // @Ignore(value = "this service is not always on")
    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor"}, enabled = false)
    public void testAccessToWikileaksOnionDomain() throws Exception {
        String httpResponseStr = openHiddenService(WIKILEAKS_HOSTNAME, 80);
        // check result
        final String SUCCESS_STR = "Click here to make a secure submission";
        if (!httpResponseStr.contains(SUCCESS_STR)) {
            fail("did not get correct response of hidden service, response body=" + httpResponseStr);
        }
    }

    /**
     * Access Tor Directory via Hidden Service.
     * Test is currently disabled as service is currently not available.
     *
     * @throws Exception
     */
    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor"}, enabled = false)
    public void testAccessToTORDirectoryOnionDomain() throws Exception {
        String httpResponseStr = openHiddenService(TOR_DIRECTORY_HOSTNAME, 80);
        // check result
        if (!httpResponseStr.contains("TORDIR - Link List")) {
            fail("did not get correct response of hidden service, response body=" + httpResponseStr);
        }
    }
}
