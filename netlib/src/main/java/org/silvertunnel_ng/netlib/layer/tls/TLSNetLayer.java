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

package org.silvertunnel_ng.netlib.layer.tls;

import org.silvertunnel_ng.netlib.api.*;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.util.PropertiesUtil;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TLS/SSL transport layer protocol implementation.
 * <br>
 * Supported localProperties:
 * TLSNetLayer.enabledCipherSuites=TLS_RSA_WITH_AES_128_CBC_SHA
 * ,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,...
 *
 * @author hapke
 */
public class TLSNetLayer implements NetLayer {
    public static final String ENABLES_CIPHER_SUITES = "TLSNetLayer.enabledCipherSuites";
    public static final String KEY_MANAGERS = "TLSNetLayer.KEYManagers";
    public static final String TRUST_MANAGERS = "TLSNetLayer.TrustManagers";

    private final NetLayer lowerNetLayer;

    public TLSNetLayer(final NetLayer lowerNetLayer) {
        this.lowerNetLayer = lowerNetLayer;
    }

    @Override
    public NetSocket createNetSocket(NetAddress remoteAddress) throws IOException {
        throw new IOException("not implemented");
    }

    /**
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    @Override
    public NetSocket createNetSocket(final Map<String, Object> localProperties,
                                     final NetAddress localAddress,
                                     final NetAddress remoteAddress)
            throws IOException {
        // create lower layer socket
        Map<String, Object> lowerLayerProperties = null;
        if (localProperties != null) {
            lowerLayerProperties = new HashMap<String, Object>(localProperties);
            lowerLayerProperties.remove(ENABLES_CIPHER_SUITES);
            lowerLayerProperties.remove(KEY_MANAGERS);
            lowerLayerProperties.remove(TRUST_MANAGERS);
        }
        final NetSocket lowerLayerSocket = lowerNetLayer.createNetSocket(
                lowerLayerProperties, localAddress, remoteAddress);

        // read (optional) properties
        final String[] enabledCipherSuites = PropertiesUtil.getAsStringArray(localProperties, ENABLES_CIPHER_SUITES, null);

        final Object keyManagersObj = PropertiesUtil.getAsObject(localProperties, KEY_MANAGERS, null);
        KeyManager[] keyManagers = null;
        if (keyManagersObj != null && keyManagersObj instanceof KeyManager[]) {
            keyManagers = (KeyManager[]) keyManagersObj;
        }

        final Object trustManagersObj = PropertiesUtil.getAsObject(localProperties, TRUST_MANAGERS, null);
        TrustManager[] trustManagers = null;
        if (trustManagersObj != null && trustManagersObj instanceof TrustManager[]) {
            trustManagers = (TrustManager[]) trustManagersObj;
        }

        // create TLS/SSL session
        TcpipNetAddress tcpidRemoteAddress = null;
        if ((remoteAddress != null) && (remoteAddress instanceof TcpipNetAddress)) {
            tcpidRemoteAddress = (TcpipNetAddress) remoteAddress;
        }
        final NetSocket higherLayerSocket = TLSNetSocketUtil.createTLSSocket(
                lowerLayerSocket,
                tcpidRemoteAddress,
                true, // auto close
                enabledCipherSuites,
                keyManagers,
                trustManagers);

        return higherLayerSocket;
    }

    /**
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     */
    @Override
    public NetServerSocket createNetServerSocket(final Map<String, Object> properties, final NetAddress localListenAddress) {
        throw new UnsupportedOperationException();
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
}
