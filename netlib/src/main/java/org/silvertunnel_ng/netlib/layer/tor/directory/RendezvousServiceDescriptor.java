/*
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class that represents the Service Descriptor of a hidden service.
 * <p/>
 * see https
 * ://www.torproject.org/doc/design-paper/tor-design.html#sec:rendezvous
 * see http
 * ://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend
 * -spec.txt
 *
 * @author Andriy
 * @author Lexi
 * @author hapke
 * @author Tobias Boese
 */
public final class RendezvousServiceDescriptor implements Serializable {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(RendezvousServiceDescriptor.class);

    /**
     * pattern of a RendezvousServiceDescriptor String.
     */
    private static Pattern serviceDescriptorStringPattern;

    /**
     * two days in milliseconds.
     */
    private static final long MAX_SERVICE_DESCRIPTOR_AGE_IN_MS = 2L * 24L * 60L * 60L * 1000L;

    /**
     * descriptor-id.
     */
    private byte[] descriptorId;
    /**
     * version of this descriptor - usually 2.
     */
    private String version = "2";
    private RSAPublicKey permanentPublicKey;
    /**
     * highest 80 bits of the hash of the permanentPublicKey in base32.
     */
    private String z;
    /**
     * secret-id.
     */
    private byte[] secretIdPart;
    /**
     * publication time of this service descriptor.
     */
    private Long publicationTime;
    /**
     * recognized and permitted version numbers for use in INTRODUCE cells.
     * (currently, we do not support version 3)
     */
    private Collection<String> protocolVersions = Arrays.asList("2");
    private Collection<SDIntroductionPoint> introductionPoints;

    // TODO: can we drop this?
    private String url;

    /**
     * private key to sign an own service descriptor
     */
    private PrivateKey privateKey;

    private static final String DEFAULT_SERVICE_DESCRIPTOR_VERSION = "2";

    /**
     * Initialize pattern - do it in a way that exceptions get logged.
     */
    static {
        try {
            serviceDescriptorStringPattern = Pattern.compile("^(rendezvous-service-descriptor ([a-z2-7]+)\n" + "version (\\d+)\n"
                    + "permanent-key\n(-----BEGIN RSA PUBLIC KEY-----\n.*?-----END RSA PUBLIC KEY-----)\n" + "secret-id-part ([a-z2-7]+)\n"
                    + "publication-time (\\S+ \\S+)\n" + "protocol-versions (\\d+(?:,\\d+)?(?:,\\d+)?(?:,\\d+)?(?:,\\d+)?)\n"
                    + "introduction-points\n-----BEGIN MESSAGE-----\n(.*?)-----END MESSAGE-----\n"
                    + "signature\n)-----BEGIN SIGNATURE-----\n(.*?)-----END SIGNATURE-----", Pattern.DOTALL + Pattern.MULTILINE
                    + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
        } catch (final Exception e) {
            LOG.error("could not initialze class RendezvousServiceDescriptor", e);
        }
    }

    public String toServiceDescriptorString() {
        // protocolVersionsStr: create comma separated String as e.g. "2,3,4"
        final StringBuffer protocolVersionsStrBuf = new StringBuffer(10);
        boolean firstProtocolVersion = true;
        for (final String protocolVersion : protocolVersions) {
            if (!firstProtocolVersion) {
                protocolVersionsStrBuf.append(",");
            }
            protocolVersionsStrBuf.append(protocolVersion);
            firstProtocolVersion = false;
        }
        final String protocolVersionsStr = protocolVersionsStrBuf.toString();

        // introductionPointsBase64: create String
        final String introductionPointsStr = SDIntroductionPoint.formatMultipleIntroductionPoints(introductionPoints) + "\n";
        byte[] introductionPointsBytes = null;
        try {
            introductionPointsBytes = introductionPointsStr.getBytes(Util.UTF8);
        } catch (final UnsupportedEncodingException e) {
            LOG.debug("got UnsupportedEncodingException : {}", e.getMessage(), e);
        }
        final int BASE64_COLUMN_WITH = 64;
        final String introductionPointsBase64 = Encoding.toBase64(introductionPointsBytes, BASE64_COLUMN_WITH);

        // build the complete result
        final String dataToSignStr = "rendezvous-service-descriptor " + Encoding.toBase32(descriptorId) + "\n" + "version " + version + "\n"
                + "permanent-key\n" + Encryption.getPEMStringFromRSAPublicKey(permanentPublicKey) + "secret-id-part "
                + Encoding.toBase32(secretIdPart) + "\n" + "publication-time " + Util.formatUtcTimestamp(publicationTime) + "\n"
                + "protocol-versions " + protocolVersionsStr + "\n" + "introduction-points\n" + "-----BEGIN MESSAGE-----\n"
                + introductionPointsBase64 + "-----END MESSAGE-----\n" + "signature\n";

        // sign the signatureStr
        String signatureStr = "";
        if (privateKey != null) {
            // yes we can sign this descriptor
            byte[] dataToSign = null;
            try {
                dataToSign = dataToSignStr.getBytes(Util.UTF8);
            } catch (final UnsupportedEncodingException e) {
                LOG.warn("unexpected", e);
            }
            final byte[] signature = Encryption.signData(dataToSign, privateKey);
            signatureStr = Encoding.toBase64(signature, BASE64_COLUMN_WITH);
        }

        // create full descriptor
        return dataToSignStr + "-----BEGIN SIGNATURE-----\n" + signatureStr + "-----END SIGNATURE-----\n";
    }

    /**
     * Constructor for creating a service descriptor of the newest support
     * version.
     */
    public RendezvousServiceDescriptor(final String hiddenServicePermanentIdBase32,
                                       final int replica,
                                       final long now,
                                       final RSAPublicKey publicKey,
                                       final RSAPrivateKey privateKey,
                                       final Collection<SDIntroductionPoint> givenIntroPoints) throws TorException {
        this(DEFAULT_SERVICE_DESCRIPTOR_VERSION, hiddenServicePermanentIdBase32, replica, now, publicKey, privateKey, givenIntroPoints);
    }

    /**
     * Constructor for creating a service descriptor.
     */
    public RendezvousServiceDescriptor(final String version,
                                       final String hiddenServicePermanentIdBase32,
                                       final int replica,
                                       final Long publicationTime,
                                       final RSAPublicKey publicKey,
                                       final RSAPrivateKey privateKey,
                                       final Collection<SDIntroductionPoint> givenIntroPoints)
            throws TorException {

        if (!DEFAULT_SERVICE_DESCRIPTOR_VERSION.equals(version)) {
            // FIXME: service descriptors of version != 0 are not supported, yet
            throw new TorException("not implemented: service descriptors of version != " + DEFAULT_SERVICE_DESCRIPTOR_VERSION
                    + " are not supported, yet");
        }
        this.version = version;
        final RendezvousServiceDescriptorKeyValues calculatedValues = RendezvousServiceDescriptorUtil
                .getRendezvousDescriptorId(hiddenServicePermanentIdBase32, replica, publicationTime);
        this.descriptorId = calculatedValues.getDescriptorId();

        this.publicationTime = publicationTime;
        this.permanentPublicKey = publicKey;
        this.privateKey = privateKey;
        updateURL();

        // store introduction-points
        introductionPoints = givenIntroPoints;

        // calculate current time-period
        // TODO: delete?: byte[] rendezvousDescriptorServiceId =
        // Encoding.parseBase32(hiddenServicePermanentIdBase32);
        // get secret-id-part = h(time-period + descriptorCookie + replica)
        this.secretIdPart = calculatedValues.getSecretIdPart();

		/*
         * TODO remove? byte[] temp = new byte[introductionPoints.size() * 100];
		 * int tempFill = 0; Iterator<SDIntroductionPoint> i =
		 * introductionPoints.iterator(); while (i.hasNext()) { byte[] s =
		 * i.next().toString().getBytes(); System.arraycopy(s, 0, temp,
		 * tempFill, s.length); tempFill += s.length + 1; }
		 * this.bytesIntroductionPoints = new byte[tempFill];
		 * System.arraycopy(temp, 0, bytesIntroductionPoints, 0, tempFill);
		 */
    }

    /**
     * Constructor for parsing a service descriptor.
     *
     * @param serviceDescriptorStr
     * @param currentDate          is used to check whether the service descriptor is still valid
     */
    protected RendezvousServiceDescriptor(final String serviceDescriptorStr, final Long currentDate) throws TorException {
        this(serviceDescriptorStr, currentDate, true);
    }

    /**
     * Constructor for parsing a service descriptor.
     *
     * @param serviceDescriptorStr
     * @param currentTime          is used to check whether the service descriptor is still valid
     * @param checkSignature       true=check signature; false(only for testing)=ignore signature
     */
    protected RendezvousServiceDescriptor(final String serviceDescriptorStr,
                                          final Long currentTime,
                                          final boolean checkSignature)
            throws TorException {
        try {
            // parse the authorityKeyCertificateStr
            final Matcher m = serviceDescriptorStringPattern.matcher(serviceDescriptorStr);
            m.find();

            // read several fields
            final String descriptorIdBase32 = m.group(2);
            descriptorId = Encoding.parseBase32(descriptorIdBase32);

            version = m.group(3);

            // read and check public key
            final String permanentKeyStr = m.group(4);
            permanentPublicKey = Encryption.extractPublicRSAKey(permanentKeyStr);
            z = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(permanentPublicKey);

            final String secretIdPartBase32 = m.group(5);
            secretIdPart = Encoding.parseBase32(secretIdPartBase32);

            // parse and check publication time
            publicationTime = Util.parseUtcTimestampAsLong(m.group(6));
            if (!isPublicationTimeValid(currentTime)) {
                throw new TorException("invalid publication-time=" + publicationTime);
            }

            // parse: a comma-separated list of recognized and permitted version
            // numbers for use in INTRODUCE cells
            final String protocolVersionsStr = m.group(7);
            protocolVersions = Arrays.asList(protocolVersionsStr.split(","));

            // read and parse introduction-points
            String introductionPointsBase64 = m.group(8);
            while (introductionPointsBase64.length() % 4 != 0) {
                introductionPointsBase64 += "="; // add missing padding
            }
            final byte[] introductionPointsBytes = DatatypeConverter.parseBase64Binary(introductionPointsBase64);
            final String introductionPointsStr = new String(introductionPointsBytes, Util.UTF8);
            introductionPoints = SDIntroductionPoint.parseMultipleIntroductionPoints(introductionPointsStr);
            if (LOG.isDebugEnabled()) {
                LOG.debug("ips = " + introductionPoints);
            }

            // read and check signature
            String signatureStr = m.group(9);
            while (signatureStr.length() % 4 != 0) {
                signatureStr += "="; // add missing padding
            }
            final byte[] signature = DatatypeConverter.parseBase64Binary(signatureStr);
            final String signedDataStr = m.group(1);
            byte[] signedData = null;
            try {
                signedData = signedDataStr.getBytes(Util.UTF8);
            } catch (final UnsupportedEncodingException e) {
                LOG.warn("unexpected", e);
            }
            if (checkSignature && !Encryption.verifySignature(signature, permanentPublicKey, signedData)) {
                throw new TorException("dirKeyCertification check failed");
            }

        } catch (final TorException e) {
            // just pass it
            throw e;
        } catch (final Exception e) {
            // convert the exception
            LOG.info("long log", e);
            throw new TorException("could not parse service descriptor:" + e);
        }
    }

    /**
     * needs to be called, in case of service descriptor is self-generated and
     * shall be called with toByteArray().
     */
    void updateSignature() throws TorException {
        throw new UnsupportedOperationException("not yet implemented");
		/*
		 * TODO signature = Encryption.signData(toByteArray(false), privateKey);
		 */
    }

    /**
     * for sending the descriptor.
     */
    byte[] toByteArray() {
        try {
            return toServiceDescriptorString().getBytes(Util.UTF8);
        } catch (final UnsupportedEncodingException e) {
            LOG.warn("may not occur", e);
            return null;
        }
    }

    private void updateURL() {
        try {
            // create hash of public key
            final byte[] hash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(permanentPublicKey));
            // take top 80-bits and convert to biginteger
            final byte[] h1 = new byte[10];
            System.arraycopy(hash, 0, h1, 0, 10);
            // return encoding
            this.url = Encoding.toBase32(h1) + ".onion";

        } catch (final Exception e) {
            LOG.error("ServiceDescriptor.updateURL(): " + e.getMessage(), e);
            this.url = null;
        }
    }

    /**
     * checks whether the timestamp is no older than 24h.
     *
     * @param currentTime current time in ms
     * @return true if the publication time is still valid
     */
    public boolean isPublicationTimeValid(final Long currentTime) {
        if (publicationTime == null) {
            return false;
        }
        if (publicationTime > currentTime || (currentTime - publicationTime > MAX_SERVICE_DESCRIPTOR_AGE_IN_MS)) {
            return false;
        }
        return true;
    }

    /**
     * checks whether the timestamp is no older than 24h.
     *
     * @return true if the publication time is still valid
     */
    public boolean isPublicationTimeValid() {
        return isPublicationTimeValid(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "RendezvousServiceDescriptor=(descriptorIdBase32=" + Encoding.toBase32(descriptorId) + ",publicationTime=" + publicationTime
                + ",introductionPoints=" + introductionPoints + ")";
    }

    // /////////////////////////////////////////////////////
    // getters and setters
    // /////////////////////////////////////////////////////

    /**
     * returns the z-part of the url.
     */
    public String getURL() {
        return url;
    }

    public RSAPublicKey getPermamentPublicKey() {
        return permanentPublicKey;
    }

    public byte[] getDescriptorId() {
        return descriptorId;
    }

    public String getVersion() {
        return version;
    }

    public RSAPublicKey getPermanentPublicKey() {
        return permanentPublicKey;
    }

    public String getZ() {
        return z;
    }

    public byte[] getSecretIdPart() {
        return secretIdPart;
    }

    /**
     * Get the publication time of this {@link RendezvousServiceDescriptor}.
     *
     * @return the publication time in ms
     */
    public Long getPublicationTime() {
        return publicationTime;
    }

    public Collection<String> getProtocolVersions() {
        return protocolVersions;
    }

    public Collection<SDIntroductionPoint> getIntroductionPoints() {
        return introductionPoints;
    }
}
