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

package org.silvertunnel_ng.netlib.layer.tor.util;

import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtil;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtilLocalTest;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptorUtil;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Test class Parsing.
 *
 * @author hapke
 * @author Tobias Boese
 */
public class EncryptionLocalTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(EncryptionLocalTest.class);

    @Test(timeOut = 50000)
    public void testExtractRSAKeyPair() throws Exception {
        // parse private key from PEM
        final String privateKeyPEM = FileUtil.readFileFromClasspath(TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH);
        final RSAKeyPair keyPair = Encryption.extractRSAKeyPair(privateKeyPEM);
        assertNotNull("could not parse privat key from PEM format", keyPair);

        // check the the public part of the key is as expected
        final String z = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(keyPair.getPublic());
        assertEquals(
                "public part of the parted key pair does not create the correct z value",
                TorNetLayerUtilLocalTest.EXAMPLE_ONION_DOMAIN_DERIVED_FROM_PRIVATE_KEY,
                z);
    }

    @Test(timeOut = 5000)
    public void testFormattingPrivateKeyAsPEM() throws Exception {
        // read one private key as PEM to have an example key
        String privateKeyPEM = FileUtil.readFileFromClasspath(TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH);
        final RSAKeyPair keyPair = Encryption.extractRSAKeyPair(privateKeyPEM);

        assertNotNull("extractRSAKeyPair didnt worked (return should be not null)", keyPair);
        LOG.info("keyPair=" + keyPair);

        // convert private key to PEM and compare with original PEM
        String newPrivateKeyPEM = Encryption.getPEMStringFromRSAKeyPair(keyPair);
        final RSAKeyPair newKeyPair = Encryption.extractRSAKeyPair(newPrivateKeyPEM);

        LOG.info("\n\nnewKeyPair=" + newKeyPair);

        // replace operation system specific line separators
        privateKeyPEM = privateKeyPEM.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        newPrivateKeyPEM = newPrivateKeyPEM.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

        // check the the conversion PEM -> keyPair -> PEM did not change the
        // content
        assertEquals("wrong private key", keyPair.getPrivate(), newKeyPair.getPrivate());
        assertEquals("wrong public key", keyPair.getPublic(), newKeyPair.getPublic());
        assertEquals("wrong private key PEM string", privateKeyPEM, newPrivateKeyPEM);
    }

    private static final byte[] DATA_TO_SIGN = new byte[]{0, -116, 48, -127, -119, 2, -127, -127, 0, -71, -12, 7, -33, -89, 40, -48, -24, -78, -62, 22, 77, 61, 12, -72, -86, 10, 6, -128, 69, 23, -75, -63, 37, 49, -100, 55, -101, 59, 113, -25, 49, -111, -127, 109, -118, -107, -75, -14, 66, 78, -15, 123, 4, 60, 8, -13, 114, -68, -79, 103, -10, 48, 106, 58, -63, 103, 68, 84, -73, -93, 61, -37, 103, 111, 95, -47, -103, 112, -96, -126, -64, -15, 95, -128, -104, -101, 79, 44, -60, -67, -7, -21, 67, 2, -26, 26, -94, -88, -11, -87, 120, -77, 47, -105, 107, -50, -81, 125, 45, -116, -11, -127, -119, -126, -69, -88, 25, -79, -22, -96, -106, -48, -86, 94, -32, -95, -115, -82, -86, -119, -44, 68, 52, -45, 53, 88, 53, 2, 3, 1, 0, 1, 87, -57, -79, -108, 9, -18, 77, -15, 74, -78, 123, -85, -124, 127, 122, -4, 120, 122, 95, 71};
    private static final byte[] EXPECTED_SIGNATURE = new byte[]{118, 1, 95, 10, 123, 20, -44, 2, 116, 1, -31, 14, 104, 84, 73, -95, 22, -21, -8, -59, -13, 87, -36, -75, 43, 104, 99, -15, -73, 64, -112, 30, -115, -123, 64, -63, -30, 39, 26, -123, 123, -22, -38, -68, 91, 78, -118, 117, -68, 12, 50, 106, -20, -69, 78, -86, 25, -55, -78, 61, -128, 7, 68, -38, -17, -34, 3, 74, 10, 122, 18, 38, 119, 35, -88, 51, -48, 16, 72, 19, -92, 29, 24, 118, -71, -64, 4, -32, -126, -85, -69, 74, 22, -56, -23, 12, 127, -106, 32, -111, -107, 123, 47, 105, 104, 33, 43, 90, -112, 20, -24, 97, 78, 39, -126, 27, -43, -93, -86, 109, -13, 74, 5, -127, -126, -74, -36, 76};
    private static final byte[] PRIVATE_KEY_BYTES = new byte[]{48, -126, 2, 118, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 2, 96, 48, -126, 2, 92, 2, 1, 0, 2, -127, -127, 0, -71, -12, 7, -33, -89, 40, -48, -24, -78, -62, 22, 77, 61, 12, -72, -86, 10, 6, -128, 69, 23, -75, -63, 37, 49, -100, 55, -101, 59, 113, -25, 49, -111, -127, 109, -118, -107, -75, -14, 66, 78, -15, 123, 4, 60, 8, -13, 114, -68, -79, 103, -10, 48, 106, 58, -63, 103, 68, 84, -73, -93, 61, -37, 103, 111, 95, -47, -103, 112, -96, -126, -64, -15, 95, -128, -104, -101, 79, 44, -60, -67, -7, -21, 67, 2, -26, 26, -94, -88, -11, -87, 120, -77, 47, -105, 107, -50, -81, 125, 45, -116, -11, -127, -119, -126, -69, -88, 25, -79, -22, -96, -106, -48, -86, 94, -32, -95, -115, -82, -86, -119, -44, 68, 52, -45, 53, 88, 53, 2, 3, 1, 0, 1, 2, -127, -128, 79, -46, -18, -46, 86, 106, -110, 11, 98, 57, 40, -29, -83, 50, 89, -49, 60, -112, 127, -35, -87, 8, -17, -55, -84, -101, 55, -49, -98, 92, 13, -9, -36, 83, 104, 23, -106, -98, -51, 73, 35, -92, 75, -65, -116, -103, -9, 15, -88, -22, -85, 11, 38, -53, 99, 63, -124, -71, -13, 120, 5, 78, 87, 113, 58, 74, -55, 10, 94, -65, 36, 90, -82, 68, 75, 33, 100, -122, -44, 12, -47, -119, 33, 105, 52, -94, 29, -103, -127, -101, 76, 11, -91, 79, -83, 0, -122, 60, -96, -34, 92, 41, 92, 126, -111, 30, -116, -53, -73, 109, 26, 81, 18, -82, 7, -13, -94, 22, -107, 12, -13, 66, -86, -31, -95, 93, -67, -35, 2, 65, 0, -16, -122, -106, 110, 54, 17, -39, -34, 94, 35, 54, 81, 8, -81, -66, 30, 94, -99, -100, -94, -4, -26, -17, -36, 99, -103, -2, 37, -101, -64, -126, -59, 104, -61, 113, 46, 69, -99, -90, -91, -20, -32, 80, -28, 123, 116, 125, 108, -56, 121, 17, 60, 112, -61, -6, -82, 67, 117, -97, -93, -9, -75, -101, 123, 2, 65, 0, -59, -22, -91, 17, -87, 35, -12, -36, 9, 52, 17, -49, 99, 77, 124, 26, 52, -100, 103, -54, -89, -95, 112, 81, 61, 24, -17, -119, 52, 45, 61, -15, 59, 57, -91, -57, -10, 52, 75, -6, -37, 77, -36, 61, -7, 66, 76, -112, -32, 71, -40, -81, -44, 34, 43, 31, 66, -104, -55, -57, -40, 32, -12, 15, 2, 64, 113, 27, -87, 116, 44, -125, 26, -124, 98, -102, -122, 121, 118, -17, 70, 37, 123, -14, -4, 108, -3, 39, 16, -105, 0, 83, -77, 91, 54, 50, 66, -116, -40, -26, 71, -68, 45, -54, -92, 74, -108, -23, 43, -63, 54, 121, 34, 94, 92, -85, -22, 58, 21, 1, 100, 74, 60, -72, -4, -104, -101, -19, 80, 47, 2, 64, 25, -96, -122, 123, 64, -15, 124, -11, 56, -29, -37, 34, -123, 115, 53, 12, -105, -97, 96, -7, 38, -73, -118, -71, -102, 53, -82, 94, -113, -71, 3, -6, 70, 118, -14, 118, 111, -12, -39, -103, -61, -25, -45, 10, -37, 73, -80, 108, 89, -2, 51, -102, -96, -41, 67, -54, 42, 111, 106, 65, 76, 69, 8, 21, 2, 65, 0, -19, -55, -46, -98, 66, -75, 77, 121, -43, -98, 23, 3, 3, -76, -88, -59, -48, 19, 90, 16, -65, 2, -85, -126, -55, -95, 94, -36, -121, -78, 49, 29, 23, -111, -9, -26, -9, -85, -108, -42, -31, 90, 82, 67, 2, 17, -90, 61, -16, 56, -33, 65, 19, -116, 45, 18, -11, 40, 29, -127, -101, -63, 88, 88};
    private static final byte[] DIGEST_OF_DATA = new byte[]{76, -124, -54, -25, 126, 102, 51, 111, 120, -89, 24, 107, 75, -40, -98, 27, 112, 97, -11, -107};

    @Test
    public void testSignData() throws IOException {
        String privateKeyPEMStr = FileUtil.readFileFromClasspath(TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH);
        TorHiddenServicePrivateNetAddress netAddressWithoutPort = TorNetLayerUtil.getInstance()
                .parseTorHiddenServicePrivateNetAddressFromStrings(privateKeyPEMStr, null, false);

        Assert.assertEquals(PRIVATE_KEY_BYTES, netAddressWithoutPort.getPrivateKey().getEncoded());
        Assert.assertEquals(EXPECTED_SIGNATURE, Encryption.signData(DATA_TO_SIGN, netAddressWithoutPort.getPrivateKey()));
        Assert.assertEquals(DIGEST_OF_DATA, Encryption.getDigest(DATA_TO_SIGN));
    }
}
