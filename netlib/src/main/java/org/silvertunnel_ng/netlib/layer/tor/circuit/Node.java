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
package org.silvertunnel_ng.netlib.layer.tor.circuit;

import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.common.TorKeyAgreement;
import org.silvertunnel_ng.netlib.layer.tor.util.AESCounterMode;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * represents a server as part of a specific circuit. Stores the additional data
 * and contains all of the complete crypto-routines.
 *
 * @author Lexi Pimenidis
 * @author Tobias Koelsch
 */
public class Node {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(Node.class);

    /**
     * length of SHA-1 digest in bytes.
     */
    private static final int DIGEST_LEN = 20;

    private Router router;
    /**
     * used to encrypt a part of the diffie-hellman key-exchange.
     */
    private byte[] symmetricKeyForCreate;
    /**
     * data for the diffie-hellman key-exchange.
     */
    private TorKeyAgreement dhKeyAgreement;
    private byte[] dhXBytes;
    private byte[] dhYBytes;
    /**
     * the derived key data.
     */
    private byte[] keyHandshake;
    /**
     * digest for all data send to this node.
     */
    private byte[] forwardDigest;
    /**
     * digest for all data received from this node.
     */
    private byte[] backwardDigest;
    /**
     * symmetric key for sending data.
     */
    private byte[] keyForward;
    /**
     * symmetric key for receiving data.
     */
    private byte[] keyBackward;
    private AESCounterMode aesEncrypt;
    private AESCounterMode aesDecrypt;
    private MessageDigest sha1Forward;
    private MessageDigest sha1Backward;

    private Node() {

    }

    /**
     * constructor for (hidden service) server-side.
     *
     * @throws TorException
     */
    Node(final Router init, final byte[] dhXBytes) throws TorException {
        if (init == null) {
            throw new NullPointerException("can't init node on NULL server");
        }
        // save a pointer to the server's data
        this.router = init;
        final SecureRandom rnd = new SecureRandom();
        // do Diffie-Hellmann
        dhKeyAgreement = new TorKeyAgreement();
        BigInteger dhX = new BigInteger(1, dhXBytes);
        BigInteger dhPrivate = new BigInteger(TorKeyAgreement.P1024.bitLength() - 1, rnd);
        final BigInteger dhXY = dhX.modPow(dhPrivate, TorKeyAgreement.P1024);
        final byte[] dhXYBytes = convertBigIntegerTo128Bytes(dhXY);
        // return dhY-Bytes
        final BigInteger dhY = TorKeyAgreement.G.modPow(dhPrivate, TorKeyAgreement.P1024);
        dhYBytes = convertBigIntegerTo128Bytes(dhY);

        // derive key material
        final int NUM_OF_DIGESTS = 5;
        final byte[] k = new byte[NUM_OF_DIGESTS * DIGEST_LEN];
        final byte[] sha1Input = new byte[dhXYBytes.length + 1];
        System.arraycopy(dhXYBytes, 0, sha1Input, 0, dhXYBytes.length);
        for (int i = 0; i < NUM_OF_DIGESTS; ++i) {
            sha1Input[sha1Input.length - 1] = (byte) i;
            final byte[] singleDigest = Encryption.getDigest(sha1Input);
            System.arraycopy(singleDigest, 0, k, i * DIGEST_LEN, DIGEST_LEN);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.<init>: dhX = \n"
                    + Encoding.toHexString(dhXBytes, 100) + "\n" + "dhY = \n"
                    + Encoding.toHexString(dhYBytes, 100) + "\n"
                    + "dhXY = keymaterial:\n"
                    + Encoding.toHexString(dhXYBytes, 100) + "\n"
                    + "Key Data:\n" + Encoding.toHexString(k, 100));
        }

        // derived key info is correct - save to final destination
        // handshake
        keyHandshake = new byte[DIGEST_LEN];
        System.arraycopy(k, 0, keyHandshake, 0, DIGEST_LEN);
        // backward digest
        backwardDigest = new byte[20];
        System.arraycopy(k, 20, backwardDigest, 0, DIGEST_LEN);
        sha1Backward = Encryption.getMessagesDigest();
        sha1Backward.update(backwardDigest, 0, DIGEST_LEN);
        // forward digest
        forwardDigest = new byte[DIGEST_LEN];
        System.arraycopy(k, 40, forwardDigest, 0, DIGEST_LEN);
        sha1Forward = Encryption.getMessagesDigest();
        sha1Forward.update(forwardDigest, 0, DIGEST_LEN);
        // secret key for sending data
        keyForward = new byte[16];
        System.arraycopy(k, 60, keyForward, 0, 16);
        aesDecrypt = new AESCounterMode(keyForward);
        // secret key for receiving data
        keyBackward = new byte[16];
        System.arraycopy(k, 76, keyBackward, 0, 16);
        aesEncrypt = new AESCounterMode(keyBackward);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.<init>: dhX = \n" + Encoding.toHexString(dhXBytes, 100)
                    + "\n" + "dhY = \n" + Encoding.toHexString(dhYBytes, 100)
                    + "\n" + "dhXY = keymaterial:\n"
                    + Encoding.toHexString(dhXYBytes, 100) + "\n" + "Key Data:\n"
                    + Encoding.toHexString(k, 100) + "\n" + "Key Data kf:\n"
                    + Encoding.toHexString(keyForward, 100) + "\n"
                    + "Key Data kb:\n" + Encoding.toHexString(keyBackward, 100));
        }
    }

    /**
     * constructor for client-side.
     *
     * @param init the {@link Router} which should be used as {@link Node}
     */
    public Node(final Router init) throws TorException {
        this(init, false);
    }

    /**
     * constructor for client-side.
     *
     * @param init       the {@link Router} which should be used as {@link Node}
     * @param createFast skip Diffie-Hellman? (see create_fast cell)
     */
    public Node(final Router init, final boolean createFast) throws TorException {
        if (init == null) {
            throw new NullPointerException("can't init node on NULL server");
        }
        // save a pointer to the server's data
        this.router = init;
        final SecureRandom secureRandom = new SecureRandom();

        if (createFast) {
            dhXBytes = new byte[DIGEST_LEN];
            secureRandom.nextBytes(dhXBytes);
        } else {
            // Diffie-Hellman: generate our secret
            try {
                dhKeyAgreement = new TorKeyAgreement();
            } catch (TorException e) {
                LOG.error("Error while doing dh! Exception : ", e);
                throw e;
            }
            dhXBytes = dhKeyAgreement.getPublicKeyBytes();
            // generate random symmetric key for circuit creation
            symmetricKeyForCreate = new byte[16];
            secureRandom.nextBytes(symmetricKeyForCreate);

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.<init client>: dhX = \n"
                    + Encoding.toHexString(dhXBytes, 100) + "\n" + "dhY = \n"
                    + Encoding.toHexString(dhYBytes, 100));
        }
    }

    /**
     * encrypt data with asymmetric key. create asymmetrical encrypted data:<br>
     * <ul>
     * <li>OAEP padding [42 bytes] (RSA-encrypted)</li>
     * <li>Symmetric key [16 bytes] FIXME: we assume that we ALWAYS need this</li>
     * <li>First part of data [70 bytes]</li>
     * <li>Second part of data [x-70 bytes] (Symmetrically encrypted)</li>
     * </ul>
     * encrypt and store in result
     *
     * @param data to be encrypted, needs currently to be at least 70 bytes long
     * @return the first half of the key exchange, ready to be send to the other
     * partner
     */
    public final byte[] asymEncrypt(final byte[] data) throws TorException {
        return Encryption.asymEncrypt(router.getOnionKey(),
                getSymmetricKeyForCreate(), data);
    }

    /**
     * called after receiving created or extended cell: finished DH-key
     * exchange. Expects the first 148 bytes of the data array to be filled
     * with:<br>
     * <ul>
     * <li>128 bytes of DH-data (g^y)
     * <li>20 bytes of derivated key data (KH) (see chapter 4.2 of torspec)
     * </ul>
     *
     * @param data expects the received second half of the DH-key exchange
     */
    public void finishDh(final byte[] data) throws TorException {
        byte[] dhXYBytes;
        if (dhKeyAgreement == null) {
            // create fast cell
            dhXYBytes = new byte[DIGEST_LEN * 2];
            dhYBytes = new byte[DIGEST_LEN];
            System.arraycopy(data, 0, dhYBytes, 0, DIGEST_LEN);
            System.arraycopy(dhXBytes, 0, dhXYBytes, 0, DIGEST_LEN);
            System.arraycopy(dhYBytes, 0, dhXYBytes, DIGEST_LEN, DIGEST_LEN);
        } else {
            // calculate g^xy
            // - fix some undocument stuff: all numbers are 128-bytes only!
            // - add a leading zero to all numbers
            dhYBytes = new byte[TorKeyAgreement.DH_LEN];
            System.arraycopy(data, 0, dhYBytes, 0, TorKeyAgreement.DH_LEN);
            BigInteger otherPublicSecret = new BigInteger(1, dhYBytes);
            if (!TorKeyAgreement.isValidPublicValue(otherPublicSecret)) {
                LOG.warn("other DH public value is invalid!");
                throw new TorException("other DH public value is invalid!");
            }
            dhXYBytes = dhKeyAgreement.getSharedSecret(otherPublicSecret);
        }
        // derive key material
        final int NUM_OF_DIGESTS = 5;
        final byte[] keyData = new byte[NUM_OF_DIGESTS * DIGEST_LEN];
        final byte[] sha1Input = new byte[dhXYBytes.length + 1];
        System.arraycopy(dhXYBytes, 0, sha1Input, 0, dhXYBytes.length);
        for (int i = 0; i < NUM_OF_DIGESTS; i++) {
            sha1Input[sha1Input.length - 1] = (byte) i;
            final byte[] singleDigest = Encryption.getDigest(sha1Input);
            System.arraycopy(singleDigest, 0, keyData, i * DIGEST_LEN,
                    DIGEST_LEN);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.finishDh: dhX = \n"
                    + Encoding.toHexString(dhXBytes, 100) + "\n" + "dhY = \n"
                    + Encoding.toHexString(dhYBytes, 100) + "\n"
                    + "dhXY = keymaterial:\n"
                    + Encoding.toHexString(dhXYBytes, 100) + "\n"
                    + "Key Data:\n" + Encoding.toHexString(keyData, 100));
        }

        // check if derived key data is equal to bytes 128-147 of data[]

        boolean equal = true;
        for (int i = 0; equal && i < DIGEST_LEN; ++i) {
            equal = keyData[i] == data[dhYBytes.length + i];
        }
        // is there some error in the key data?
        if (!equal) {
            throw new TorException("derived key material is wrong!");
        }

        // derived key info is correct - save to final destination
        // handshake
        keyHandshake = new byte[20];
        System.arraycopy(keyData, 0, keyHandshake, 0, DIGEST_LEN);
        // forward digest
        forwardDigest = new byte[DIGEST_LEN];
        System.arraycopy(keyData, 20, forwardDigest, 0, DIGEST_LEN);
        sha1Forward = Encryption.getMessagesDigest();
        sha1Forward.update(forwardDigest);
        // backward digest
        backwardDigest = new byte[DIGEST_LEN];
        System.arraycopy(keyData, 40, backwardDigest, 0, DIGEST_LEN);
        sha1Backward = Encryption.getMessagesDigest();
        sha1Backward.update(backwardDigest);
        // secret key for sending data
        keyForward = new byte[16];
        System.arraycopy(keyData, 60, keyForward, 0, 16);
        aesEncrypt = new AESCounterMode(keyForward);
        // secret key for receiving data
        keyBackward = new byte[16];
        System.arraycopy(keyData, 76, keyBackward, 0, 16);
        aesDecrypt = new AESCounterMode(keyBackward);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.finishDh: dhX = \n"
                    + Encoding.toHexString(dhXBytes, 100) + "\n" + "dhY = \n"
                    + Encoding.toHexString(dhYBytes, 100) + "\n"
                    + "dhXY = keymaterial:\n"
                    + Encoding.toHexString(dhXYBytes, 100) + "\n" + "Key Data:\n"
                    + Encoding.toHexString(keyData, 100) + "\n"
                    + "Key Data keyForward:\n"
                    + Encoding.toHexString(keyForward, 100) + "\n"
                    + "Key Data keyBackward:\n"
                    + Encoding.toHexString(keyBackward, 100));
        }
    }

    /**
     * calculate the forward digest.
     *
     * @param data
     * @return a four-byte array containing the digest
     */
    public byte[] calcForwardDigest(final byte[] data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.calcForwardDigest() on:\n"
                    + Encoding.toHexString(data, 100));
        }
        sha1Forward.update(data, 0, data.length);
        final byte[] digest = Encryption.intermediateDigest(sha1Forward);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" result:\n" + Encoding.toHexString(digest, 100));
        }
        final byte[] fourBytes = new byte[4];
        System.arraycopy(digest, 0, fourBytes, 0, 4);
        return fourBytes;
    }

    /**
     * calculate the backward digest.
     *
     * @param data
     * @return a four-byte array containing the digest
     */
    public byte[] calcBackwardDigest(final byte[] data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.calcBackwardDigest() on:\n"
                    + Encoding.toHexString(data, 100));
        }
        sha1Backward.update(data, 0, data.length);
        final byte[] digest = Encryption.intermediateDigest(sha1Backward);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" result:\n" + Encoding.toHexString(digest, 100));
        }
        final byte[] fourBytes = new byte[4];
        System.arraycopy(digest, 0, fourBytes, 0, 4);
        return fourBytes;
    }

    /**
     * encrypt data with symmetric key.
     *
     * @param data is used for input and output.
     */
    public void symEncrypt(final byte[] data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.symEncrypt for node " + router.getNickname());
            LOG.debug("Node.symEncrypt in:\n" + Encoding.toHexString(data, 100));
        }

        // encrypt data
        final byte[] encrypted = aesEncrypt.processStream(data);
        // copy to output
        if (encrypted.length > data.length) {
            System.arraycopy(encrypted, 0, data, 0, data.length);
        } else {
            System.arraycopy(encrypted, 0, data, 0, encrypted.length);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.symEncrypt out:\n" + Encoding.toHexString(data, 100));
        }
    }

    /**
     * decrypt data with symmetric key.
     *
     * @param data is used for input and output.
     */

    public void symDecrypt(byte[] data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.symDecrypt for node " + router.getNickname());
        }

        // decrypt data
        final byte[] decrypted = aesDecrypt.processStream(data);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Node.symDecrypt in:\n"
                    + Encoding.toHexString(data, 100));
            LOG.debug("Node.symDecrypt out:\n"
                    + Encoding.toHexString(decrypted, 100));
        }

        // copy to output
        if (decrypted.length > data.length) {
            System.arraycopy(decrypted, 0, data, 0, data.length);
        } else {
            System.arraycopy(decrypted, 0, data, 0, decrypted.length);
        }

    }

    /**
     * helper function to convert a bigInteger to a fixed-sized array for
     * TOR-Usage.
     */
    private byte[] convertBigIntegerTo128Bytes(final BigInteger a) {
        final byte[] temp = a.toByteArray();
        final byte[] result = new byte[128];
        if (temp.length > 128) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("convertBigIntegerTo128Bytes temp longer than 128!");
                LOG.debug("Big Integer a = " + a);
                LOG.debug("temp.length = " + temp.length);
                LOG.debug("temp data :\n" + Encoding.toHexString(temp, 100));
            }
            System.arraycopy(temp, temp.length - 128, result, 0, 128);
        } else {
            System.arraycopy(temp, 0, result, 128 - temp.length, temp.length);
        }
        return result;
    }

    // /////////////////////////////////////////////////////
    // getters and setters
    // /////////////////////////////////////////////////////

    public Router getRouter() {
        return router;
    }

    public byte[] getSymmetricKeyForCreate() {
        return symmetricKeyForCreate;
    }

    public byte[] getDhXBytes() {
        return dhXBytes;
    }

    public byte[] getDhYBytes() {
        return dhYBytes;
    }

    public byte[] getKeyHandshake() {
        return keyHandshake;
    }

    public byte[] getForwardDigest() {
        return forwardDigest;
    }

    public byte[] getBackwardDigest() {
        return backwardDigest;
    }

    public byte[] getKf() {
        return keyForward;
    }

    public byte[] getKb() {
        return keyBackward;
    }

    public AESCounterMode getAesEncrypt() {
        return aesEncrypt;
    }

    public AESCounterMode getAesDecrypt() {
        return aesDecrypt;
    }

}
