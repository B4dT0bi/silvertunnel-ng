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

package org.silvertunnel_ng.netlib.layer.redirect;

import org.silvertunnel_ng.netlib.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * NetLayer that transparently forwards all traffic to switchable/exchangeable
 * lower NetLayer.
 *
 * @author hapke
 */
public class SwitchingNetLayer implements NetLayer {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(SwitchingNetLayer.class);

    /**
     * beginning from this number of open (server)sockets log warn messages.
     */
    private static int OPEN_SOCKETS_WARN_THRESHOLD = 100;

    /**
     * Currently used lower NetLayer.
     */
    private volatile NetLayer lowerNetLayer;

    /**
     * NetSocket instances created for the currently used lower NetLayer. Always
     * use the addToLayer()/removeFromLayer() methods to modify the content!
     */
    private final Collection<SwitchingNetSocket> switchingNetSockets = new ArrayList<SwitchingNetSocket>();
    /**
     * NetServerSocket instances created for the currently used lower NetLayer.
     * Always use the addToLayer()/removeFromLayer() methods to modify the
     * content!
     */
    private final Collection<SwitchingNetServerSocket> switchingNetServerSockets = new ArrayList<SwitchingNetServerSocket>();

    /**
     * Start with the provided lowerNetLayer. The lowerNetLayer can be exchanged
     * later by calling the method setLowerNetLayer().
     *
     * @param lowerNetLayer
     */
    public SwitchingNetLayer(NetLayer lowerNetLayer) {
        this.lowerNetLayer = lowerNetLayer;
    }

    /**
     * Exchange the lower layer.
     *
     * @param lowerNetLayer                      new lower layer
     * @param closeAllOpenConnectionsImmediately if true then sockets are close; independently of this flag,
     *                                           server sockets are always closed
     */
    public synchronized void setLowerNetLayer(NetLayer lowerNetLayer,
                                              boolean closeAllOpenConnectionsImmediately) {
        // close all server sockets (always)
        for (final SwitchingNetServerSocket serverSocket : switchingNetServerSockets) {
            try {
                serverSocket.closeLowerLayer();
            } catch (final Exception e) {
                LOG.info("setLowerNetLayer(): exception while closing lower server socket: "
                        + e);
            }
        }
        switchingNetServerSockets.clear();

        // close all sockets (if requested)
        if (closeAllOpenConnectionsImmediately) {
            for (final SwitchingNetSocket socket : switchingNetSockets) {
                try {
                    socket.closeLowerLayer();
                } catch (final Exception e) {
                    LOG.info("setLowerNetLayer(): exception while closing lower socket: "
                            + e);
                }
            }
        }
        switchingNetSockets.clear();

        // set new lower layer
        this.lowerNetLayer = lowerNetLayer;
    }

    /**
     * Create a connection using the lower layer with its predefined address.
     *
     * @param localProperties will be ignored
     * @param localAddress    will be ignored
     * @param remoteAddress   will be ignored
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    @Override
    public NetSocket createNetSocket(Map<String, Object> localProperties,
                                     NetAddress localAddress, NetAddress remoteAddress)
            throws IOException {
        final SwitchingNetSocket result = new SwitchingNetSocket(this,
                lowerNetLayer.createNetSocket(localProperties, localAddress,
                        remoteAddress));
        addToLayer(result);

        return result;
    }

    /**
     * Create a server connection.
     *
     * @param properties         e.g. property "backlog"; can also be used to handle a
     *                           "security profile"; is optional and can be null
     * @param localListenAddress usually one NetAddress, but can be null for layers without
     *                           address
     * @return a new NetServerSocket, not null
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     */
    @Override
    public NetServerSocket createNetServerSocket(
            Map<String, Object> properties, NetAddress localListenAddress)
            throws IOException {
        final SwitchingNetServerSocket result = new SwitchingNetServerSocket(
                this, lowerNetLayer.createNetServerSocket(properties,
                localListenAddress));
        addToLayer(result);

        return result;
    }

    /**
     * @see NetLayer#getStatus()
     */
    @Override
    public NetLayerStatus getStatus() {
        return lowerNetLayer.getStatus();
    }

    /**
     * @see NetLayer#waitUntilReady()
     */
    @Override
    public void waitUntilReady() {
        lowerNetLayer.waitUntilReady();
    }

    /**
     * @see NetLayer#clear()
     */
    @Override
    public void clear() throws IOException {
        lowerNetLayer.clear();
    }

    /**
     * @see NetLayer#getNetAddressNameService()
     */
    @Override
    public NetAddressNameService getNetAddressNameService() {
        return lowerNetLayer.getNetAddressNameService();
    }

    @Override
    public void close() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////
    // methods called by SwitchingNet(Server)Socket
    // /////////////////////////////////////////////////////

    protected synchronized void addToLayer(SwitchingNetSocket switchingNetSocket) {
        switchingNetSockets.add(switchingNetSocket);

        // resource and memory leak warning
        if (switchingNetSockets.size() >= OPEN_SOCKETS_WARN_THRESHOLD) {
            final String msg = "SwitchingNetLayer: "
                    + switchingNetSockets.size()
                    + " open sockets - this could be a resource and memory leak";
            if (switchingNetSockets.size() == OPEN_SOCKETS_WARN_THRESHOLD) {
                // first (and maybe further) message: log with tread dump
                LOG.warn(msg,
                        new Throwable(
                                "use thread dump to localize potential resource and memory leak"));
            } else {
                // log normally
                LOG.warn(msg);
            }
        }
    }

    private synchronized void addToLayer(
            SwitchingNetServerSocket switchingNetServerSocket) {
        switchingNetServerSockets.add(switchingNetServerSocket);

        // resource and memory leak warning
        if (switchingNetServerSockets.size() >= OPEN_SOCKETS_WARN_THRESHOLD) {
            final String msg = "SwitchingNetLayer: "
                    + switchingNetServerSockets.size()
                    + " open server sockets - this could be a resource and memory leak";
            if (switchingNetServerSockets.size() == OPEN_SOCKETS_WARN_THRESHOLD) {
                // first (and maybe further) message: log with tread dump
                LOG.warn(
                        msg,
                        new Throwable(
                                "use thread dump to localize potential resource and memory leak"));
            } else {
                // log normally
                LOG.warn(msg);
            }
        }
    }

    protected synchronized void removeFromLayer(
            SwitchingNetSocket switchingNetSocket) {
        switchingNetSockets.remove(switchingNetSocket);
    }

    protected synchronized void removeFromLayer(
            SwitchingNetServerSocket switchingNetServerSocket) {
        switchingNetServerSockets.remove(switchingNetServerSocket);
    }
}
