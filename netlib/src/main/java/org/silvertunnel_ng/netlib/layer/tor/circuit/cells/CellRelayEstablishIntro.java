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
package org.silvertunnel_ng.netlib.layer.tor.circuit.cells;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;

import java.util.Arrays;

/**
 * this cell is used to establish introduction point
 *
 * @author Lexi Pimenidis
 * @author Tobias Boese
 */
public class CellRelayEstablishIntro extends CellRelay {
    private static final byte[] CELL_MAGIC = new byte[]{'I', 'N', 'T', 'R', 'O', 'D', 'U', 'C', 'E'};

    public CellRelayEstablishIntro(Circuit circuit, HiddenServiceProperties service) {
        super(circuit, RELAY_ESTABLISH_INTRO);
        // 'hash of session info'
        final byte[] hsInput = new byte[20 + 9];
        System.arraycopy(circuit.getLastRouteNode().getKeyHandshake(), 0, hsInput, 0, 20);
        System.arraycopy("INTRODUCE".getBytes(), 0, hsInput, 20, 9);
        final byte[] hs = Encryption.getDigest(hsInput);
        // concat all data
        final byte[] pk = Encryption.getPKCS1EncodingFromRSAPublicKey(service.getPublicKey());
        final byte[] kl = Encoding.intToNByteArray(pk.length, 2);
        final byte[] input = new byte[pk.length + kl.length + hs.length];
        System.arraycopy(kl, 0, input, 0, 2);
        System.arraycopy(pk, 0, input, 2, pk.length);
        System.arraycopy(hs, 0, input, 2 + pk.length, hs.length);
        // signature
        final byte[] signature = Encryption.signData(input, service.getPrivateKey());
        // copy to payload
        System.arraycopy(input, 0, data, 0, input.length);
        System.arraycopy(signature, 0, data, input.length, signature.length);
        setLength(input.length + signature.length);
    }
}
