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
package org.silvertunnel_ng.netlib.layer.tor.util;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;

/**
 * Exception used when a specific Tor server could not be found.
 *
 * @author Tobias Boese
 */
public class TorServerNotFoundException extends TorException {
    /**
     * A TorServerNotFoundException.
     */
    public TorServerNotFoundException()
    {
        super();
    }

    /**
     * TorServerNotFoundException with detail Message.
     * @param message the detail message
     */
    public TorServerNotFoundException(final String message)
    {
        super(message);
    }

    /**
     * TorServerNotFoundException with Router information.
     * @param fingerprint the Fingerprint of the router which was not found
     * @param pos the position in the circuit
     * @param nodeType the Type of the Node inside the Circuit
     */
    public TorServerNotFoundException(final Fingerprint fingerprint, final int pos, NodeType nodeType)
    {
        super("couldn't find server " + fingerprint + " for position " + pos);
        this.fingerprint = fingerprint;
        this.pos = pos;
        this.nodeType = nodeType;
    }

    /**
     * TorServerNotFoundException with root cause.
     * @param cause the root cause
     */
    public TorServerNotFoundException(final Throwable cause)
    {
        super(cause);
    }
    /**
     * TorException with detail message and root cause.
     * @param message the detail message
     * @param cause the root cause
     */
    public TorServerNotFoundException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
    private int pos;
    private NodeType nodeType;
    private Fingerprint fingerprint;

    public int getPos() {
        return pos;
    }
    public Fingerprint getFingerprint() {
        return fingerprint;
    }
    public NodeType getNodeType() {
        return nodeType;
    }
}
