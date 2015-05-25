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

package org.silvertunnel_ng.netlib.layer.tcpip;

import org.silvertunnel_ng.netlib.adapter.socket.SocketGlobalUtil;
import org.silvertunnel_ng.netlib.api.*;
import org.silvertunnel_ng.netlib.api.impl.ServerSocket2NetServerSocket;
import org.silvertunnel_ng.netlib.api.impl.Socket2NetSocket;
import org.silvertunnel_ng.netlib.api.service.NetlibVersion;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.nameservice.cache.CachingNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.silvertunnel_ng.netlib.util.PropertiesUtil;

import java.io.IOException;
import java.net.*;
import java.util.Map;

/**
 * Plain TCP/IP network layer - uses the JVM default SocketImpl implementation.
 * <br>
 * Property for createNetServerSocket(): TcpipNetLayer.backlog: integer the
 * maximum length of the server queue (int)
 *
 * @author hapke
 * @author Tobias Boese
 */
public class TcpipNetLayer implements NetLayer {
    public static final String BACKLOG = "TcpipNetLayer.backlog";
    public static final String TIMEOUT_IN_MS = "TcpipNetLayer.timeoutInMs";
    /**
     * default time to wait till timeout.
     */
    private static final int DEFAULT_TIMEOUT = 5000;

    static {
        // trigger silvertunnel-ng.org Netlib start logging
        // (we trigger it here because TcpipNetLayer is usually used very early)
        NetlibVersion.getInstance();
    }

    /**
     * the instance of NetAddressNameService; will be initialized during the
     * first call of getNetAddressNameService().
     */
    private NetAddressNameService netAddressNameService;

    public TcpipNetLayer() {
    }

    /**
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    @Override
    public NetSocket createNetSocket(final Map<String, Object> localProperties,
                                     final NetAddress localAddress,
                                     final NetAddress remoteAddress)
            throws IOException {
        final TcpipNetAddress r = (TcpipNetAddress) remoteAddress;

        // read (optional) properties
        final Integer timeoutInMs = PropertiesUtil.getAsInteger(localProperties, TIMEOUT_IN_MS, DEFAULT_TIMEOUT);

        // create connection and open socket
        final Socket socket = SocketGlobalUtil.createOriginalSocket();
        if (r.getIpaddress() != null) {
            // use IP address (preferred over host name)
            final InetAddress remoteInetAddress = InetAddress.getByAddress(r.getIpaddress());
            final InetSocketAddress remoteInetSocketAddress = new InetSocketAddress(remoteInetAddress, r.getPort());
            socket.connect(remoteInetSocketAddress, timeoutInMs);
        } else {
            // use host name
            final InetSocketAddress remoteInetSocketAddress = new InetSocketAddress(
                    r.getHostname(), r.getPort());
            if (remoteInetSocketAddress.getAddress() == null) {
                throw new UnknownHostException("hostlookup didnt worked. for Hostname : " + r.getHostname());
            }
            socket.connect(remoteInetSocketAddress, timeoutInMs);
        }

        // convert and return result
        return new Socket2NetSocket(socket);
    }

    /**
     * Simple version of this method.
     *
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    public NetSocket createNetSocket(final NetAddress localAddress,
                                     final NetAddress remoteAddress) throws IOException {
        final TcpipNetAddress r = (TcpipNetAddress) remoteAddress;

        // create connection and open socket
        Socket socket;
        if (r.getIpaddress() != null) {
            // use IP address (preferred over host name)
            final InetAddress inetAddress = InetAddress.getByAddress(r.getIpaddress());
            socket = new Socket(inetAddress, r.getPort());
        } else {
            // use host name
            socket = new Socket(r.getHostname(), r.getPort());
        }

        // convert and return result
        return new Socket2NetSocket(socket);
    }

    /**
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     */
    @Override
    public NetServerSocket createNetServerSocket(final Map<String, Object> properties,
                                                 final NetAddress localListenAddress)
            throws IOException {
        final TcpipNetAddress l = (TcpipNetAddress) localListenAddress;

        // read (optional) properties
        final Long backlogL = PropertiesUtil.getAsLong(properties, BACKLOG, null);
        final int backlog = (backlogL == null) ? 0 : backlogL.intValue();

        // open server socket
        final ServerSocket serverSocket = new ServerSocket(l.getPort(), backlog); // TODO : use local address,too

        // convert and return result
        return new ServerSocket2NetServerSocket(serverSocket);
    }

    /**
     * @see NetLayer#getStatus()
     */
    @Override
    public NetLayerStatus getStatus() {
        return NetLayerStatus.READY;
    }

    /**
     * @see NetLayer#waitUntilReady()
     */
    @Override
    public void waitUntilReady() {
        // nothing to do
    }

    /**
     * @see NetLayer#clear()
     */
    @Override
    public void clear() throws IOException {
        // nothing to do
    }

    /**
     * @see NetLayer#getNetAddressNameService
     */
    @Override
    public NetAddressNameService getNetAddressNameService() {
        if (netAddressNameService == null) {
            // create a new instance
            netAddressNameService = new CachingNetAddressNameService(new DefaultIpNetAddressNameService());
        }

        return netAddressNameService;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
