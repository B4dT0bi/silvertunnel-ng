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

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.util.*;
import org.silvertunnel_ng.netlib.util.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object of this class stores a parsed directory protocol V3 network-status
 * consensus document of Tor.
 *
 * @author hapke
 */
public class DirectoryConsensus {
    /** */
    public static final Logger LOG = LoggerFactory.getLogger(DirectoryConsensus.class);

    private Date validAfter;
    private Date freshUntil;
    private Date validUntil;

    private Map<Fingerprint, RouterStatusDescription> fingerprintsNetworkStatusDescriptors = new HashMap<Fingerprint, RouterStatusDescription>();

    private static final Pattern VERSION_PATTERN = Parsing.compileRegexPattern("^network-status-version (\\d+)");
    private static final Pattern SIGNEDDATA_PATTERN = Parsing.compileRegexPattern("^(network-status-version.*?directory-signature )");

    /**
     * Parse a directory protocol V3 network-status consensus document.
     *
     * @param consensusStr             document received form directory server
     * @param authorityKeyCertificates all authority signing certificates - needed to check the
     *                                 consensus document
     * @param currentDate              current dae and time - needed to check the consensus document
     * @throws TorException if the consensus is invalid (e.g. empty or invalid signatures
     *                      or outdated)
     */
    public DirectoryConsensus(final String consensusStr,
                              final AuthorityKeyCertificates authorityKeyCertificates,
                              final Date currentDate) throws TorException, ParseException {

        // Check the version
        final String version = Parsing.parseStringByRE(consensusStr, VERSION_PATTERN, "");
        if (!version.equals("3")) {
            throw new TorException("wrong network status version");
        }

        // parse and check valid-after, fresh-until, valid-until
        setValidAfter(Parsing.parseTimestampLine("valid-after", consensusStr));
        setFreshUntil(Parsing.parseTimestampLine("fresh-until", consensusStr));
        setValidUntil(Parsing.parseTimestampLine("valid-until", consensusStr));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Directory.parseDirV3NetworkStatus: Consensus document validAfter="
                    + getValidAfter()
                    + ", freshUntil="
                    + getFreshUntil()
                    + ", validUntil=" + getValidUntil());
        }
        if (!isValidDate(currentDate)) {
            throw new TorException("invalid validAfter=" + getValidAfter()
                    + ", freshUntil=" + getFreshUntil() + " or and validUntil="
                    + getValidUntil() + " for currentDate=" + currentDate);
        }

        final byte[] signedData = Parsing.parseStringByRE(consensusStr, SIGNEDDATA_PATTERN, "").getBytes();
        LOG.debug("consensus: extracted signed data (length)={}", signedData.length);

        // Parse signatures
        final Pattern pSignature = Pattern
                .compile(
                        "^directory-signature (\\S+) (\\S+)\\s*\n-----BEGIN SIGNATURE-----\n(.*?)-----END SIGNATURE",
                        Pattern.UNIX_LINES + Pattern.MULTILINE
                                + Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
        final Matcher mSig = pSignature.matcher(consensusStr);
        final Set<Fingerprint> dirIdentityKeyDigestOfMatchingSignatures = new HashSet<Fingerprint>();
        while (mSig.find()) {
            final byte[] identityKeyDigest = DatatypeConverter.parseHexBinary(mSig.group(1));
            final byte[] signingKeyDigest = DatatypeConverter.parseHexBinary(mSig.group(2));
            String sigBase64 = mSig.group(3);
            while (sigBase64.length() % 4 != 0) {
                sigBase64 += "="; // add missing padding
            }
            final byte[] signature = DatatypeConverter.parseBase64Binary(sigBase64);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Directory.parseDirV3NetworkStatus: Extracted identityKeyDigest(hex)="
                        + Encoding.toHexString(identityKeyDigest));
                LOG.debug("Directory.parseDirV3NetworkStatus: Extracted signingKeyDigest(hex)="
                        + Encoding.toHexString(signingKeyDigest));
                LOG.debug("Directory.parseDirV3NetworkStatus: Found signature(base64)="
                        + DatatypeConverter.printBase64Binary(signature));
            }

            // verify signature
            final AuthorityKeyCertificate authorityKeyCertificate = authorityKeyCertificates
                    .getCertByFingerprints(new FingerprintImpl(identityKeyDigest),
                            new FingerprintImpl(signingKeyDigest));
            if (authorityKeyCertificate == null) {
                LOG.debug("No authorityKeyCertificate found");
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("authorityKeyCertificate signingKeyDigest(hex)="
                        + Encoding.toHexString(authorityKeyCertificate
                        .getDirSigningKeyDigest().getBytes()));
            }
            if (signature.length < 1) {
                LOG.debug("No signature found in network status");
                continue;
            }
            if (!Encryption.verifySignature(signature, authorityKeyCertificate.getDirSigningKey(), signedData)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Directory signature verification failed for identityKeyDigest(hex)="
                            + Encoding.toHexString(identityKeyDigest));
                }
                continue;
            }
            // verification successful for this signature
            dirIdentityKeyDigestOfMatchingSignatures.add(authorityKeyCertificate.getDirIdentityKeyDigest());
            if (LOG.isDebugEnabled()) {
                LOG.debug("single signature verification ok for identityKeyDigest(hex)="
                        + Encoding.toHexString(identityKeyDigest));
            }
        }
        final int CONSENSUS_MIN_VALID_SIGNATURES = 4;
        final int sigNum = dirIdentityKeyDigestOfMatchingSignatures.size();
        if (sigNum < CONSENSUS_MIN_VALID_SIGNATURES) {
            throw new TorException(
                    "Directory signature verification failed: only " + sigNum
                            + " (different) signatures found");
        }
        LOG.debug("signature verification accepted");

        // Parse the single routers
        final Pattern pRouter = Pattern
                .compile("^r (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\d+) (\\d+)\\s*\ns ([a-z0-9 ]+)?",
                        Pattern.UNIX_LINES + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
        final Matcher m = pRouter.matcher(consensusStr);
        // Loop to extract all routers
        while (m.find()) {
            final RouterStatusDescription sinfo = new RouterStatusDescription();
            sinfo.setNickname(m.group(1));
            sinfo.setFingerprint(m.group(2));
            sinfo.setDigestDescriptor(m.group(3));
            sinfo.setLastPublication(Util.parseUtcTimestampAsLong(m.group(4) + " " + m.group(5)));
            sinfo.setIp(m.group(6));
            sinfo.setOrPort(Integer.parseInt(m.group(7)));
            sinfo.setDirPort(Integer.parseInt(m.group(8)));
            sinfo.setRouterFlags(m.group(9));
            if (sinfo.getRouterFlags().isRunning()) {
                getFingerprintsNetworkStatusDescriptors().put(sinfo.getFingerprint(), sinfo);
            }
        }
    }

    /**
     * Check the timestamps. Check that at least MIN_NUMBER_OF_ROUTERS are
     * contained.
     *
     * @param now the current time
     * @return true=valid; false otherwise
     */
    public boolean isValid(final Date now) {
        // check time stamps
        if (!isValidDate(now)) {
            return false;
        }

        // check number of routers
        if (fingerprintsNetworkStatusDescriptors.size() < TorConfig.MIN_NUMBER_OF_ROUTERS_IN_CONSENSUS) {
            // too few
            LOG.warn("too few number of routers="
                    + fingerprintsNetworkStatusDescriptors.size());
            return false;
        }

        // everything is fine
        return true;
    }

    /**
     * Check the timestamps.
     * <br>
     * Final because called from inside the constructor.
     *
     * @param now the current time
     * @return true=valid; false otherwise
     */
    private boolean isValidDate(final Date now) {
        // check time stamps
        if (validAfter == null || validAfter.after(now)) {
            // too new
            LOG.warn("validAfter=" + validAfter
                    + " is too new  for currentDate=" + now
                    + " - this should never occur with consistent data");
            return false;
        }
        if (freshUntil == null /* || freshUntil.before(currentDate) */) {
            LOG.info("freshUntil=" + freshUntil
                    + " is invalid for currentDate=" + now);
        }
        if (validUntil == null || validUntil.before(now)) {
            // too old
            LOG.info("validUntil=" + validUntil
                    + " is too old for currentDate=" + now);
            return false;
        }

        // everything is fine
        return true;
    }

    /**
     * @param now the current time
     * @return true if a refresh should happen now
     */
    public boolean needsToBeRefreshed(final Date now) {
        if (validUntil.before(now)) {
            // too old
            LOG.warn("must be refrehed - but it is actually to late; validUntil="
                    + validUntil);
            return true;
        }

        // TODO: this algorithm must be improved based on the spec to prevent
        // dir server from damage
        if (freshUntil.before(now)) {
            // should be refreshed soon
            // TODO: return true;
            LOG.debug("should be refreshed soon");
        }

        // default check
        return !isValid(now);
    }

    // /////////////////////////////////////////////////////
    // generated getters and setters
    // /////////////////////////////////////////////////////

    public Date getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(Date validAfter) {
        this.validAfter = validAfter;
    }

    public Date getFreshUntil() {
        return freshUntil;
    }

    public void setFreshUntil(Date freshUntil) {
        this.freshUntil = freshUntil;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Map<Fingerprint, RouterStatusDescription> getFingerprintsNetworkStatusDescriptors() {
        return fingerprintsNetworkStatusDescriptors;
    }

    public void setFingerprintsNetworkStatusDescriptors(
            Map<Fingerprint, RouterStatusDescription> fingerprintsNetworkStatusDescriptors) {
        this.fingerprintsNetworkStatusDescriptors = fingerprintsNetworkStatusDescriptors;
    }
}
