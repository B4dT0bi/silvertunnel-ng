/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * <br>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel_ng.netlib.layer.tor.stream;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.util.Hostname;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Queue;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayResolve;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.DynByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * used to anonymously resolve hostnames.
 *
 * @author Lexi Pimenidis
 */
public final class ResolveStream extends TCPStream {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(ResolveStream.class);

    /**
     * wait x seconds for answer.
     */
    private final int queueTimeoutS = TorConfig.queueTimeoutResolve;

    public ResolveStream(final Circuit c) {
        super(c);
    }

    /**
     * creates a new stream and does an anonymous DNS-Lookup. <br>
     *
     * @param hostname a host name to be resolved, or for a reverse lookup:
     *                 A.B.C.D.in-addr.arpa
     * @return either an IpNetAddress (normal query), or a String
     * (reverse-DNS-lookup)
     */
    public List<NetAddress> resolve(final String hostname) throws TorException, IOException {
        circuit.assignStreamId(this);
        // add resolved hostname to the history
        circuit.getStreamHistory().add(hostname);
        queue = new Queue(queueTimeoutS);
        setClosed(false);
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolving hostname " + hostname + " on stream " + toString());
        }
        // send RELAY-RESOLV
        sendCell(new CellRelayResolve(this, hostname));
        // wait for RELAY_RESOLVED
        final CellRelay relay = queue.receiveRelayCell(CellRelay.RELAY_RESOLVED);
        DynByteBuffer buffer = new DynByteBuffer(relay.getData());
        List<NetAddress> result = new ArrayList<NetAddress>();
        // read payload
        byte type = buffer.getNextByte();
        int len = buffer.getNextByteAsInt();

        byte[] value = buffer.getNextByteArray(len);
        buffer.getNextInt(); // ignore the TTL for now

        // check for error
        if (type == (byte) 0xf0) {
            throw new TorException("transient error: " + new String(value));
        }
        if (type == (byte) 0xf1) {
            throw new TorException("non transient error: " + new String(value));
        }
        // check return code
        if ((type != 0) && (type != 4) && (type != 6)) {
            throw new TorException("can't handle answers of type " + type);
        }

        while (len > 0) {
            // return payload
            if (type == 0) {
                result.add(new Hostname(value));
            } else {
                result.add(new IpNetAddress(value));
            }
            type = buffer.getNextByte();
            len = buffer.getNextByteAsInt();

            value = buffer.getNextByteArray(len);
            buffer.getNextInt(); // ignore the TTL for now
        }
        return result;
    }
}
