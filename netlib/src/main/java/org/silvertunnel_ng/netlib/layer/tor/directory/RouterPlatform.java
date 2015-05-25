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
package org.silvertunnel_ng.netlib.layer.tor.directory;

/**
 * The routerPlatform describes the Tor platform of the Router.
 * <br>
 * It can be sorted and compared.
 * <br>
 * It contains all information from https://gitweb.torproject.org/torspec.git/blob/HEAD:/version-spec.txt
 *
 * @author Tobias Boese
 */
public class RouterPlatform {
    /**
     * Is this platform an original Tor version?
     */
    private boolean originalTor = false;
    /**
     * Major version.
     */
    private int major = 0;
    /**
     * Minor version.
     */
    private int minor = 0;
    /**
     * Micro version.
     */
    private int micro = 0;
    /**
     * Patchlevel version.
     */
    private int patchlevel = 0;
    /**
     * Status tag. (rc, alpha, beta, etc)
     */
    private String statusTag = "";
    /**
     * Operating system. (Linux, Windows, etc)
     */
    private String os = "";
    /**
     * Operating system extra infos. (x86_64, i686, SP3, etc)
     */
    private String extraInfo = "";
    /**
     * development version.
     */
    private String dev = "";

    private String originalString;

    public RouterPlatform() {

    }

    public RouterPlatform(final String platform) {
        originalString = platform;
        // TODO : parse platform
    }
    @Override
    public String toString() {
        if (originalTor) {
            StringBuilder builder = new StringBuilder();
            builder.append("Tor ");
            builder.append(major).append('.');
            builder.append(minor).append('.');
            builder.append(micro);
            if (patchlevel > 0) {
                builder.append('.').append(major);
            }
            if (!statusTag.isEmpty()) {
                builder.append('-').append(statusTag);
            }
            if (!dev.isEmpty()) {
                builder.append(' ').append(dev);
            }
            if (!os.isEmpty()) {
                builder.append(" on ").append(os);
                if (!extraInfo.isEmpty()) {
                    builder.append(' ').append(extraInfo);
                }
            }
            return builder.toString();
        } else {
            return originalString;
        }
    }
}
