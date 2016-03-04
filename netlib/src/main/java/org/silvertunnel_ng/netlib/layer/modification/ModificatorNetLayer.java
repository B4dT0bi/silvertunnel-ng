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

package org.silvertunnel_ng.netlib.layer.modification;

import org.silvertunnel_ng.netlib.api.*;

import java.io.IOException;
import java.util.Map;

/**
 * Bytewise modification of the input and output stream.
 *
 * @author hapke
 */
public class ModificatorNetLayer implements NetLayer {
    private final NetLayer lowerNetLayer;
    private final ByteModificator inByteModificator;
    private final ByteModificator outByteModificator;

    public ModificatorNetLayer(NetLayer lowerNetLayer,
                               ByteModificator inByteModificator,
                               ByteModificator outByteModificator) {
        this.lowerNetLayer = lowerNetLayer;
        this.inByteModificator = inByteModificator;
        this.outByteModificator = outByteModificator;
    }

    @Override
    public NetSocket createNetSocket(NetAddress remoteAddress) throws IOException {
        final NetSocket lowerLayerSocket = lowerNetLayer.createNetSocket(remoteAddress);
        return new ModificatorNetSocket(lowerLayerSocket, inByteModificator,
                outByteModificator);
    }

    /**
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    @Override
    public NetSocket createNetSocket(Map<String, Object> localProperties,
                                     NetAddress localAddress, NetAddress remoteAddress)
            throws IOException {
        final NetSocket lowerLayerSocket = lowerNetLayer.createNetSocket(
                localProperties, localAddress, remoteAddress);
        return new ModificatorNetSocket(lowerLayerSocket, inByteModificator,
                outByteModificator);
    }

    /**
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     */
    @Override
    public NetServerSocket createNetServerSocket(
            Map<String, Object> properties, NetAddress localListenAddress) {
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
