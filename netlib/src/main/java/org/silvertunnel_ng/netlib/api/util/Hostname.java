/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2014 silvertunnel-ng.org
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

package org.silvertunnel_ng.netlib.api.util;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;

import java.io.UnsupportedEncodingException;

/**
 * Convenient class for Hostnames.
 *
 * @author Tobias Boese
 */
public class Hostname implements NetAddress {
    public Hostname(final String hostname) {
        this.hostname = hostname;
    }

    public Hostname(final byte[] array) throws UnsupportedEncodingException {
        hostname = new String(array, Util.UTF8);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    private String hostname;
}
