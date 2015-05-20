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
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object holds all authority dir server keys.
 *
 * @author hapke
 */
public class AuthorityKeyCertificates {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(AuthorityKeyCertificates.class);

    /**
     * all certificates, accessible by their fingerprints.
     */
    private final Map<Fingerprint, AuthorityKeyCertificate> authorityKeyCertificates = new HashMap<Fingerprint, AuthorityKeyCertificate>();

    /**
     * pattern of a authorityKeyCertificate.
     */
    private static Pattern pattern;

    /**
     * list of official authorized authorityKeyCertificate identity
     * keys/fingerprints.
     */
    private static Collection<Fingerprint> authorizedAuthorityKeyIdentityKeys = new ArrayList<Fingerprint>();

    /**
     * Initialize in a way that exceptions get logged.
     */
    static {
        try {
            pattern = Pattern
                    .compile(
                            "^(dir-key-certificate-version 3\n"
                                    + ".*?"
                                    + "dir-key-certification\n-----BEGIN SIGNATURE-----.*?-----END SIGNATURE-----)",
                            Pattern.DOTALL + Pattern.MULTILINE
                                    + Pattern.CASE_INSENSITIVE
                                    + Pattern.UNIX_LINES);

            authorizedAuthorityKeyIdentityKeys = AuthorityServers
                    .getAuthorityDirIdentityKeyDigests();

        } catch (final Exception e) {
            LOG.error("could not initialze class AuthorityKeyCertificates", e);
        }
    }

    /**
     * Extracts all relevant information from the authority key certificate and
     * saves it in the member variables.
     * <br>
     * Only official Tor authorities will be considered.
     *
     * @param authorityKeyCertificatesStr string encoded authority dir key certificate version 3
     * @param minValidUntil               ignore entries with dirKeyExpires older than minValidUntil
     */
    public AuthorityKeyCertificates(String authorityKeyCertificatesStr,
                                    Date minValidUntil) throws TorException {
        this(authorityKeyCertificatesStr, minValidUntil,
                authorizedAuthorityKeyIdentityKeys);
    }

    /**
     * Extracts all relevant information from the authority key certificate and
     * saves it in the member variables.
     *
     * @param authorityKeyCertificatesStr          string encoded authority dir key certificate version 3
     * @param minValidUntil                        ignore entries with dirKeyExpires older than minValidUntil
     * @param allowedAuthorityKeyIdentFingerprints ignore entries with fingerprints not in this collection
     */
    public AuthorityKeyCertificates(String authorityKeyCertificatesStr,
                                    Date minValidUntil,
                                    Collection<Fingerprint> allowedAuthorityKeyIdentFingerprints)
            throws TorException {

        // split into single authorityKeyCertificateKeys
        final Matcher m = pattern.matcher(authorityKeyCertificatesStr);
        while (m.find()) {
            // parse single authorityKeyCertificateStr
            final String oneCertStr = m.group(1);
            try {
                final AuthorityKeyCertificate oneCert = new AuthorityKeyCertificate(
                        oneCertStr);

                // check certificate
                if (!oneCert.getDirKeyExpires().after(minValidUntil)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("skip authorityKeyCertificate because expired with fingerprint="
                                + oneCert.getDirIdentityKeyDigest()
                                + ", dirKeyExpires=" + oneCert.getDirKeyExpires());
                    }
                    continue;
                }
                if (!allowedAuthorityKeyIdentFingerprints.contains(oneCert
                        .getDirIdentityKeyDigest())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("skip authorityKeyCertificate because unauthorized fingerprint="
                                + oneCert.getDirIdentityKeyDigest());
                    }
                    continue;
                }

                // everything is fine
                authorityKeyCertificates.put(oneCert.getDirIdentityKeyDigest(),
                        oneCert);

            } catch (final Exception e) {
                LOG.info("skip authorityKeyCertificate because of error while parsing oneCertStr="
                        + oneCertStr, e);
            }
        }
    }

    /**
     * Revalidates the list of keys.
     *
     * @param minValidUntil check that all certificates are valid until minValidUntil or
     *                      longer
     * @return true=the object is still valid; false=the object is invalid (e.g.
     * too many certificates are outdated)
     */
    public boolean isValid(Date minValidUntil) {
        // how many certificates may be outdated
        final int MAX_CERT_OUTDATED_COUNT = 1;
        final int MIN_CERT_VALID_COUNT = 5;

        // count the valid and invalid certificates
        int certOutdatedCount = 0;
        int certValidCount = 0;
        for (final AuthorityKeyCertificate oneCert : authorityKeyCertificates
                .values()) {
            // check certificate
            if (!oneCert.getDirKeyExpires().after(minValidUntil)) {
                // authorityKeyCertificate expired
                certOutdatedCount++;
            } else {
                certValidCount++;
            }
        }

        // calculate result
        final boolean result = (certValidCount >= MIN_CERT_VALID_COUNT && certOutdatedCount <= MAX_CERT_OUTDATED_COUNT);
        if (LOG.isDebugEnabled()) {
            LOG.debug("isValid(): result=" + result + ", certValidCount="
                    + certValidCount + ", certOutdatedCount=" + certOutdatedCount); // TODO
        }
        return result;
    }

    /**
     * Get the AuthorityKeyCertificate of a identity/fingerprint.
     *
     * @param identityKeyFingerprint
     * @return the certificate; null if no matching certificate found
     */
    public AuthorityKeyCertificate getCertByFingerprints(
            Fingerprint identityKeyFingerprint,
            Fingerprint signingKeyFingerprint) {
        // get by identityKeyFingerprint
        final AuthorityKeyCertificate result = authorityKeyCertificates
                .get(identityKeyFingerprint);

        // check the signingKeyFingerprint,
        // TODO: store all different certificates (with different signing keys)
        // of a single identity key
        if (signingKeyFingerprint == null
                || result == null
                || !signingKeyFingerprint.equals(result
                .getDirSigningKeyDigest())) {
            return null; // signingKeyFingerprint is not the requested one
        }

        return result;
    }

    /**
     * used for debugging purposes.
     */
    @Override
    public String toString() {
        return "AuthorityKeyCertificates(" + authorityKeyCertificates + ")";
    }

    // /////////////////////////////////////////////////////
    // generated getters and setters
    // /////////////////////////////////////////////////////

    public Map<Fingerprint, AuthorityKeyCertificate> getAuthorityKeyCertificates() {
        return authorityKeyCertificates;
    }
}
