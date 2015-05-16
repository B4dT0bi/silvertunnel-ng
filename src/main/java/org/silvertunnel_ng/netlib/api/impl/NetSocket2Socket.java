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

package org.silvertunnel_ng.netlib.api.impl;

import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

/**
 * Wrapper NetSocket -&gt; Socket.
 *
 * @author hapke
 */
public class NetSocket2Socket extends Socket {

    private final static Logger LOG = LoggerFactory.getLogger(NetSocket2Socket.class);

    private final NetSocket2SocketImpl netSocket2SocketImpl;

    private final Object connectLock = new Object();

    public NetSocket2Socket(final NetSocket netSocket) throws IOException {
        this(new NetSocket2SocketImpl(netSocket));

        // TODO: connect with dummy address
        final int IP4SIZE = 4;
        final InetAddress dummyInetAddress = InetAddress.getByAddress(
                "NetSocket-dummy-host", new byte[IP4SIZE]);
        final int dummyPort = 0;
        connect(new InetSocketAddress(dummyInetAddress, dummyPort));
    }

    private NetSocket2Socket(final NetSocket2SocketImpl netSocket2SocketImpl) throws IOException {
        super(netSocket2SocketImpl);
        this.netSocket2SocketImpl = netSocket2SocketImpl;
    }

    /**
     * Change the NetSocket used by this object.
     *
     * @param newNetSocket
     */
    public void setNetSocket(final NetSocket newNetSocket) {
        netSocket2SocketImpl.setNetSocket(newNetSocket);
    }

    /**
     * Connects this socket to the given remote host address and port specified
     * by the SocketAddress {@code remoteAddr} with the specified timeout. The
     * connecting method will block until the connection is established or an
     * error occurred.
     *
     * @param remoteAddr the address and port of the remote host to connect to.
     * @param timeout    the timeout value in milliseconds or {@code 0} for an infinite
     *                   timeout.
     * @throws IllegalArgumentException if the given SocketAddress is invalid or not supported or the
     *                                  timeout value is negative.
     * @throws IOException              if the socket is already connected or an error occurs while
     *                                  connecting.
     */
    public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
        if (JavaVersion.getJavaVersion() != JavaVersion.ANDROID) {
            super.connect(remoteAddr, timeout);
            return;
        }

        // Copied from Android SDK

        try {
            Class<?> clazz = getClass().getSuperclass();
            Method method = clazz.getDeclaredMethod("checkOpenAndCreate", boolean.class);
            method.setAccessible(true);
            method.invoke(this, true);
            //checkOpenAndCreate(true);
        } catch (Exception e) {
            LOG.warn("failed to call Android Socket.checkOpenAndCreate method", e);
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        if (isConnected()) {
            throw new SocketException("Already connected");
        }
        if (remoteAddr == null) {
            throw new IllegalArgumentException("remoteAddr == null");
        }

        if (!(remoteAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Remote address not an InetSocketAddress: " +
                    remoteAddr.getClass());
        }
        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        InetAddress addr;
        if ((addr = inetAddr.getAddress()) == null) {
            throw new UnknownHostException("Host is unresolved: " + inetAddr.getHostName());
        }
        int port = inetAddr.getPort();

        checkDestination(addr, port);
        synchronized (connectLock) {
            try {
                if (!isBound()) {
                    // socket already created at this point by earlier call or
                    // checkOpenAndCreate this caused us to lose socket
                    // options on create
                    // impl.create(true);
                    //if (!usingSocks()) {
                    //    netSocket2SocketImpl.bind(Inet4Address.ANY, 0);
                    //}
                    try {
                        Class<?> clazz = getClass().getSuperclass();
                        Field field = clazz.getDeclaredField("isBound");
                        field.setAccessible(true);
                        field.setBoolean(this, true);
                        //isBound = true;
                    } catch (Exception e) {
                        LOG.warn("failed to set Android Socket.isBound field", e);
                    }
                }
                netSocket2SocketImpl.connect(remoteAddr, timeout);
                try {
                    Class<?> clazz = getClass().getSuperclass();
                    Field field = clazz.getDeclaredField("isConnected");
                    field.setAccessible(true);
                    field.setBoolean(this, true);
                    //isConnected = true;
                } catch (Exception e) {
                    LOG.warn("failed to set Android Socket.isConnected field", e);
                }
                //cacheLocalAddress();
            } catch (IOException e) {
                netSocket2SocketImpl.close();
                throw e;
            }
        }
    }

    /**
     * Checks whether the connection destination satisfies the security policy
     * and the validity of the port range.
     *
     * @param destAddr the destination host address.
     * @param dstPort  the port on the destination host.
     */
    private void checkDestination(InetAddress destAddr, int dstPort) {
        if (dstPort < 0 || dstPort > 65535) {
            throw new IllegalArgumentException("Port out of range: " + dstPort);
        }
    }

}
