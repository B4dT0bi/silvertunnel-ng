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
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * NetLayer that transparently forwards all traffic to a lower NetLayer -
 * depending on the used addresses.
 * <br>
 * The decision, when to use which lower layer, depends on the configuration
 * provided to the constructor.
 *
 * @author hapke
 */
public class ConditionalNetLayer implements NetLayer {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(ConditionalNetLayer.class);

    /**
     * patterns and its assigned lower NetLayers
     */
    private List<Condition> conditions = new ArrayList<Condition>();
    /**
     * use this NetLayer if no condition matches
     */
    private final NetLayer defaultLowerNetLayer;

    /**
     * Start with the provided lowerNetLayer. The lowerNetLayer can be exchanged
     * later by calling the method setLowerNetLayer().
     *
     * @param conditions
     * @param defaultLowerNetLayer
     */
    public ConditionalNetLayer(List<Condition> conditions,
                               NetLayer defaultLowerNetLayer) {
        this.conditions = Collections.synchronizedList(conditions);
        this.defaultLowerNetLayer = defaultLowerNetLayer;
    }

    /**
     * Find entry in conditions.
     *
     * @param netAddress
     * @return matching entry from conditions; defaultLowerNetLayer if no
     * condition matches
     */
    protected NetLayer getMatchingNetLayer(NetAddress netAddress) {
        NetLayer result = getMatchingNetLayerOrNull(netAddress);
        if (result == null) {
            result = defaultLowerNetLayer;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("netAddress={} matches with lowerNetLayer={}", netAddress, result);
        }
        return result;

    }

    /**
     * Find entry in conditions.
     *
     * @param netAddress
     * @return matching entry from conditions; defaultLowerNetLayer if no
     * condition matches
     */
    private NetLayer getMatchingNetLayerOrNull(NetAddress netAddress) {
        // synchronized here to keep the result consistent if conditions will be
        // modified in parallel
        synchronized (conditions) {
            if (netAddress == null) {
                return defaultLowerNetLayer;
            } else if (netAddress instanceof TcpipNetAddress) {
                final TcpipNetAddress tcpipNetAddress = (TcpipNetAddress) netAddress;

                // check hostname+port
                String s = tcpipNetAddress.getHostnameAndPort();
                NetLayer result = getMatchingNetLayerOrNull(s);
                if (result != null) {
                    return result;
                }
                // check IP address+port
                s = tcpipNetAddress.getIpaddressAndPort();
                result = getMatchingNetLayerOrNull(s);
                if (result != null) {
                    return result;
                }
            } else if (netAddress instanceof IpNetAddress) {
                final IpNetAddress ipNetAddress = (IpNetAddress) netAddress;

                // check hostname+port
                final String s = ipNetAddress.getIpaddressAsString();
                final NetLayer result = getMatchingNetLayerOrNull(s);
                if (result != null) {
                    return result;
                }
            } else {
                // check via toString()
                final String s = netAddress.toString();
                final NetLayer result = getMatchingNetLayerOrNull(s);
                if (result != null) {
                    return result;
                }
            }
        }

        // no condition matched
        return defaultLowerNetLayer;
    }

    /**
     * Find entry in conditions.
     *
     * @param addressName
     * @return matching entry from conditions; defaultLowerNetLayer if no
     * condition matches
     */
    protected NetLayer getMatchingNetLayer(String addressName) {
        NetLayer result = getMatchingNetLayerOrNull(addressName);
        if (result == null) {
            result = defaultLowerNetLayer;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("addressName={} matches with lowerNetLayer={}", addressName, result);
        }
        return result;
    }

    /**
     * Find entry in conditions.
     *
     * @param s
     * @return matching entry from conditions; null if no condition matches
     */
    private NetLayer getMatchingNetLayerOrNull(final String s) {
        synchronized (conditions) {
            for (final Condition condition : conditions) {
                if (condition.getPattern().matcher(s).matches()) {
                    // found result
                    return condition.getNetLayer();
                }
            }
        }
        // nothing found
        return null;
    }

    /**
     * @return all possible lower NetLayers.
     */
    private Set<NetLayer> getAllLowerNetLayers() {
        final Set<NetLayer> result = new HashSet<NetLayer>(
                conditions.size() + 1);
        synchronized (conditions) {
            for (final Condition condition : conditions) {
                result.add(condition.getNetLayer());
            }
        }
        result.add(defaultLowerNetLayer);
        return result;
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
        final NetLayer lowerNetLayer = getMatchingNetLayer(remoteAddress);
        return lowerNetLayer.createNetSocket(localProperties, localAddress,
                remoteAddress);
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
        final NetLayer lowerNetLayer = getMatchingNetLayer(localListenAddress);
        return lowerNetLayer.createNetServerSocket(properties,
                localListenAddress);
    }

    /**
     * @return the lowest status of all lower NetLayers.
     * @see NetLayer#getStatus()
     */
    @Override
    public NetLayerStatus getStatus() {
        // get status of all possible lower net layers
        final Collection<NetLayerStatus> statuses = new ArrayList<NetLayerStatus>();
        for (final NetLayer lowerNetLayer : getAllLowerNetLayers()) {
            statuses.add(lowerNetLayer.getStatus());
        }

        // take the lowest status
        return Collections.max(statuses);
    }

    /**
     * Wait until all lower NetLayers are ready.
     *
     * @see NetLayer#waitUntilReady()
     */
    @Override
    public void waitUntilReady() {
        for (final NetLayer lowerNetLayer : getAllLowerNetLayers()) {
            lowerNetLayer.waitUntilReady();
        }
    }

    /**
     * Clear all lower NetLayers.
     *
     * @see NetLayer#clear()
     */
    @Override
    public void clear() throws IOException {
        for (final NetLayer lowerNetLayer : getAllLowerNetLayers()) {
            lowerNetLayer.clear();
        }
    }

    /**
     * @see NetLayer#getNetAddressNameService()
     */
    @Override
    public NetAddressNameService getNetAddressNameService() {
        return new ConditionalNetAddressNameService(this);
    }

    @Override
    public void close() {

    }
}
