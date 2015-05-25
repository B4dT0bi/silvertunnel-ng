/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2015 silvertunnel-ng.org
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
package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;

import java.net.InetAddress;

/**
 * Bridge object containing the information about a Bridge.
 */
public class Bridge extends RouterImpl {
    private String pluggableTransport;

    //public Bridge(final String bridgeString) {

    //}

    protected Bridge(final String nickname, final InetAddress inetAddress, final int port, final Fingerprint fingerprint) {
        super(nickname, inetAddress, port, 0, fingerprint, fingerprint);
    }

    public Bridge(final String hostname, final int port) {
        this(hostname, port, null);
    }

    public Bridge(final String hostname, final int port, final String fingerprint) {
        this("Bridge-" + hostname, new TcpipNetAddress(hostname).getIpaddressAsInetAddress(), port, fingerprint == null ? null : new FingerprintImpl(fingerprint));
    }

    /**
     * @return the PluggableTransport type of this Bridge.
     * check https://www.torproject.org/docs/pluggable-transports.html.en
     */
    public String getPluggableTransport() {
        return pluggableTransport;
    }

    public void setPluggableTransport(final String pluggableTransport) {
        this.pluggableTransport = pluggableTransport;
    }

    @Override
    public String toString() {
        return "Bridge{" +
                "pluggableTransport='" + pluggableTransport + '\'' +
                "} " + super.toString();
    }
}
