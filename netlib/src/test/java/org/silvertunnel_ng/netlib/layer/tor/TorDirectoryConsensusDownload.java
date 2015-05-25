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
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClientCompressed;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * This Test should not be executed every time as it tries to download the Tor
 * Directory consensus file from all known Directory servers.
 * <br>
 * This Unittest should only be used for debugging purposes only!
 *
 * @author Tobias Boese
 */
public final class TorDirectoryConsensusDownload {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TorDirectoryConsensusDownload.class);
    /**
     * Directory Router used for testcase.
     */
    private Router dirRouter;

    public TorDirectoryConsensusDownload(final Router routerToTest) {
        dirRouter = routerToTest;
    }

    /**
     * Fetch all directory Routers and decide which to use for Testing.
     *
     * @return a list of Routers
     * @throws Exception an exception (from Tor initialization process)
     */
    @DataProvider(name = "getRouters")
    public static Collection<Router[]> data() throws Exception {
        initializeTor();
        // first get all Routers
        Collection<Router> routers = torNetLayer.getValidTorRouters();
        Collection<Router[]> dirRouters = new ArrayList<Router[]>();
        Set<String> platforms = new HashSet<String>();
        for (Router router : routers) {
            if (router.isDirv2Authority() || router.isDirv2V2dir()) {
                // limit the number of test routers.
                String version = router.getPlatform().split(" ")[1];
                if (!platforms.contains(version)) {
                    platforms.add(version);
                    dirRouters.add(new Router[]{router});
                }
            }
        }
        return dirRouters;
    }

    @Test(timeOut = 60000, enabled = false)
    public void testDownloadConsensusOverTor() throws IOException, DataFormatException {

        LOG.info("fetch consensus document from router: "
                + dirRouter.getFingerprint()
                + " (" + dirRouter.getNickname() + ")"
                + " [" + dirRouter.getPlatform() + "]");
        // download network status from server
        final String path = "/tor/status-vote/current/consensus";
        final String newDirectoryConsensusStr = SimpleHttpClientCompressed
                .getInstance().get(torNetLayer, dirRouter.getDirAddress(), path);
        assertNotNull(newDirectoryConsensusStr);
        assertFalse(newDirectoryConsensusStr.isEmpty());
    }

    @Test(timeOut = 15000, enabled = false)
    public void testDownloadConsensusOverTcpIp() throws IOException, DataFormatException {

        LOG.info("fetch consensus document from router: "
                + dirRouter.getFingerprint()
                + " (" + dirRouter.getNickname() + ")"
                + " [" + dirRouter.getPlatform() + "]");
        // download network status from server
        final String path = "/tor/status-vote/current/consensus";
        final String newDirectoryConsensusStr = SimpleHttpClientCompressed
                .getInstance().get(loggingTcpipNetLayer, dirRouter.getDirAddress(), path);
        assertNotNull(newDirectoryConsensusStr);
        assertFalse(newDirectoryConsensusStr.isEmpty());
        LOG.info("successfully fetched directory consensus");
    }

    protected static NetLayer loggingTcpipNetLayer;
    protected static NetLayer loggingTlsNetLayer;
    protected static TorNetLayer torNetLayer;

    private static void initializeTor() throws Exception {
        // do it only once
        if (loggingTcpipNetLayer == null) {
            // create TCP/IP layer
            final NetLayer tcpipNetLayer = new TcpipNetLayer();
            loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer,
                    "upper tcpip  ");
            NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP,
                    loggingTcpipNetLayer);

            // create TLS/SSL over TCP/IP layer
            final TLSNetLayer tlsNetLayer = new TLSNetLayer(
                    loggingTcpipNetLayer);
            loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer,
                    "upper tls/ssl");
            NetFactory.getInstance().registerNetLayer(
                    NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);

            // create TCP/IP layer for directory access (use different layer
            // here to get different logging output)
            final NetLayer tcpipDirNetLayer = new TcpipNetLayer();

            // create Tor over TLS/SSL over TCP/IP layer
            torNetLayer = new TorNetLayer(loggingTlsNetLayer, /* loggingT */
                    tcpipDirNetLayer, TempfileStringStorage.getInstance());
            NetFactory.getInstance().registerNetLayer(
                    NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
            torNetLayer.waitUntilReady();
        }

        // refresh net layer registration
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP,
                loggingTcpipNetLayer);
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP,
                loggingTlsNetLayer);
        NetFactory.getInstance().registerNetLayer(
                NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
    }
}
