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

import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Node;
import org.silvertunnel_ng.netlib.layer.tor.directory.SDIntroductionPoint;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this cell is used to establish a connection to the introduction point.
 *
 * @author Andriy Panchenko
 * @author hapke
 * @author Tobias Boese
 */
public class CellRelayIntroduce1 extends CellRelay {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(CellRelayIntroduce1.class);

    /**
     * CellRelayIntroduce1: from Alice's OP to Introduction Point (section 1.8
     * of Tor Rendezvous Specification)
     * <br>
     * We only support version 2 here.
     *
     * @param circuit                        the {@link Circuit} to be used for sending the {@link Cell}
     * @param rendezvousCookie               a randomly created cookie
     * @param introPoint                     {@link SDIntroductionPoint} the descriptor of the chosen introduction point
     * @param introPointServicePublicKeyNode the {@link Node} of the introduction point
     * @param rendezvousPointRouter          the {@link Router} of the rendezvous point
     * @throws TorException an {@link TorException} when there is a problem with the encryption
     */
    public CellRelayIntroduce1(final Circuit circuit,
                               final byte[] rendezvousCookie,
                               final SDIntroductionPoint introPoint,
                               final Node introPointServicePublicKeyNode,
                               final Router rendezvousPointRouter) throws TorException {
        super(circuit, RELAY_INTRODUCE1);

        //
        // clear text part
        //

        // PK_ID Identifier for Bob's PK [20 octets]
        final byte[] clearText = Encryption.getDigest(Encryption
                .getPKCS1EncodingFromRSAPublicKey(introPoint
                        .getServicePublicKey()));
        System.arraycopy(clearText, 0, data, 0, clearText.length);

        //
        // encrypted text part
        //
        final byte[] rendezvousPointRouterOnionKey = Encryption
                .getPKCS1EncodingFromRSAPublicKey(rendezvousPointRouter.getOnionKey());
        final byte[] unencryptedData = ByteArrayUtil.concatByteArrays(
                //
                // "just like the hybrid encryption in CREATE cells",
                // not explicitly mentioned in section 1.8 of Tor Rendezvous
                // Specification
                //

                // OAEP padding [42 octets] (RSA-encrypted): gets added automatically
                // symmetric key [16 octets] gets added automatically

                //
                // the rest as mentioned in section 1.8 of Tor Rendezvous
                // Specification
                //

                // VER Version byte: set to 2. [1 octet]
                new byte[]{0x02},
                // IP Rendezvous point's address [4 octets]
                rendezvousPointRouter.getOrAddress().getIpaddress(),
                // PORT Rendezvous point's OR port [2 octets]
                Encoding.intTo2ByteArray(rendezvousPointRouter.getOrAddress().getPort()),
                // ID Rendezvous point identity ID [20 octets]
                rendezvousPointRouter.getFingerprint().getBytes(),
                // KLEN Length of onion key [2 octets]
                Encoding.intTo2ByteArray(rendezvousPointRouterOnionKey.length),
                // KEY Rendezvous point onion key [KLEN octets]
                rendezvousPointRouterOnionKey,
                // RC Rendezvous cookie [20 octets]
                rendezvousCookie,
                // g^x Diffie-Hellman data, part 1 [128 octets]
                introPointServicePublicKeyNode.getDhXBytes());
        final byte[] encryptedData = introPointServicePublicKeyNode.asymEncrypt(unencryptedData);
        if (LOG.isDebugEnabled()) {
            LOG.debug("CellRelayIntroduce1: unencryptedData=" + Encoding.toHexString(unencryptedData));
            LOG.debug("CellRelayIntroduce1: encryptedData=" + Encoding.toHexString(encryptedData));
        }

        // set encrypted part
        System.arraycopy(encryptedData, 0, data, clearText.length, encryptedData.length);
        setLength(clearText.length + encryptedData.length);

        if (LOG.isDebugEnabled()) {
            LOG.debug("CellRelayIntroduce1: cell=" + toString());
        }
    }
}
