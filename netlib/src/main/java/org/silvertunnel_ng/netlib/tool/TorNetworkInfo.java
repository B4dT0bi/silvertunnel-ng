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

package org.silvertunnel_ng.netlib.tool;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayer;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Command line tool that connects to the Tor Network and shows live information about the Status of the network.
 *
 * @author Tobias Boese
 */
public class TorNetworkInfo {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TorNetworkInfo.class);

    /**
     * Start the program from command line.
     *
     * @param argv
     */
    public static void main(final String[] argv) throws Exception {
        TorNetworkInfo torNetworkInfo = new TorNetworkInfo();
        torNetworkInfo.initializeTor();
        Collection<Router> routers = torNetworkInfo.torNetLayer.getValidTorRouters();
        System.out.println("Number of routers : " + routers.size());
        torNetworkInfo.processPlatforms(routers);
        torNetworkInfo.torNetLayer.close();
    }

    private void processPlatforms(final Collection<Router> routers) {
        Map<String, Integer> mapPlatforms = new HashMap<String, Integer>();
        for (Router router : routers) {
            Integer value = mapPlatforms.get(router.getPlatform());
            if (value == null) {
                value = 0;
            }
            value = value + 1;
            mapPlatforms.put(router.getPlatform(), value);
        }
        System.out.println("Platforms : " + mapPlatforms);
    }

    protected TorNetLayer torNetLayer;

    protected void initializeTor() throws Exception {
        // create TCP/IP layer
        final NetLayer tcpipNetLayer = new TcpipNetLayer();
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, tcpipNetLayer);

        // create TLS/SSL over TCP/IP layer
        final TLSNetLayer tlsNetLayer = new TLSNetLayer(tcpipNetLayer);
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, tlsNetLayer);

        // create Tor over TLS/SSL over TCP/IP layer
        torNetLayer = new TorNetLayer(tlsNetLayer, tcpipNetLayer, TempfileStringStorage.getInstance());
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
        torNetLayer.waitUntilReady();
    }
}
