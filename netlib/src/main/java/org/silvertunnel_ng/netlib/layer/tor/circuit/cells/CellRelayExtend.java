/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * <p/>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel_ng.netlib.layer.tor.circuit.cells;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Node;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * this cell helps extending existing circuits.
 *
 * @author Lexi Pimenidis
 */
public class CellRelayExtend extends CellRelayEarly {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(CellRelayExtend.class);

    /**
     * build an EXTEND-cell.<br>
     * <ul>
     * <li>address (4 bytes)
     * <li>port (2 bytes)
     * <li>onion skin (186 bytes)
     * <li>hash (20 bytes)
     * </ul>
     *
     * @param cell     the circuit that needs to be extended
     * @param nextNode the node to which the circuit shall be extended
     * @throws IOException
     */
    public CellRelayExtend(final Circuit cell, final Node nextNode) throws IOException, TorException {
        // initialize a new RELAY-cell
        super(cell, CellRelay.RELAY_EXTEND);

        // Address [4 bytes] next.server.address
        final byte[] address = nextNode.getRouter().getAddress().getAddress();
        // Port [2 bytes] next.server.port
        final byte[] orPort = Encoding.intTo2ByteArray(nextNode.getRouter().getOrPort());
        // Onion skin [186 bytes]
        final byte[] onionSkin = nextNode.asymEncrypt(nextNode.getDhXBytes());
        // Public key hash [20 bytes]
        // (SHA1-hash of the PKCS#1 ASN1-encoding of the next OR's signing key)
        final byte[] keyHash = nextNode.getRouter().getFingerprint().getBytes();

        // save everything in payload
        setLength(address.length + orPort.length + onionSkin.length + keyHash.length);
        System.arraycopy(address, 0, data, 0, address.length);
        System.arraycopy(orPort, 0, data, 4, orPort.length);
        System.arraycopy(onionSkin, 0, data, 6, onionSkin.length);
        System.arraycopy(keyHash, 0, data, 192, keyHash.length);
        if (LOG.isDebugEnabled()) {
            LOG.debug("CellRelayExtend Router :\n" + nextNode.getRouter().toLongString());
            LOG.debug("CellRelayExtend address :\n" + Encoding.toHexString(address, 100));
            LOG.debug("CellRelayExtend orPort :\n" + Encoding.toHexString(orPort, 100));
            LOG.debug("CellRelayExtend onionSkin :\n" + Encoding.toHexString(onionSkin, 100));
            LOG.debug("CellRelayExtend keyhash :\n" + Encoding.toHexString(keyHash, 100));
            LOG.debug("CellRelayExtend data :\n" + Encoding.toHexString(data, 100));
        }
    }
}
