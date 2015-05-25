/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2015 silvertunnel-ng.org
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

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClient;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClientCompressed;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipException;

/**
 * AuthorityKeyCertificatesFetcher is responsible for getting the AuthorityKeyCertificates from cache or from network.
 *
 * @author Tobias Boese
 */
public class AuthorityKeyCertificatesFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(AuthorityKeyCertificatesFetcher.class);
    /**
     * key to locally cache the authority key certificates.
     */
    private static final String STORAGEKEY_AUTHORITY_KEY_CERTIFICATES_TXT = "authority-key-certificates.txt";
    private static final int MIN_LENGTH_OF_AUTHORITY_KEY_CERTS_STR = 100;

    public static AuthorityKeyCertificates getFromCache(final Date minValidUntil, StringStorage stringStorage) {
        // loading is needed - try to load authority key certificates from
        // cache first
        LOG.debug("getAuthorityKeyCertificates(): try to load from local cache ...");
        final String authorityKeyCertificatesStr = stringStorage.get(STORAGEKEY_AUTHORITY_KEY_CERTIFICATES_TXT);
        if (authorityKeyCertificatesStr != null && authorityKeyCertificatesStr.length() > MIN_LENGTH_OF_AUTHORITY_KEY_CERTS_STR) {
            // parse loaded result
            try {
                final AuthorityKeyCertificates newAuthorityKeyCertificates = new AuthorityKeyCertificates(authorityKeyCertificatesStr,
                        minValidUntil);

                // no exception thrown: certificates are OK
                if (newAuthorityKeyCertificates.isValid(minValidUntil)) {
                    LOG.debug("getAuthorityKeyCertificates(): successfully loaded from local cache");
                    return newAuthorityKeyCertificates;
                } else {
                    // do not use outdated or invalid certificates from
                    // local cache
                    LOG.debug("getAuthorityKeyCertificates(): loaded from local cache - but not valid: try (re)load from remote site now");
                }

            } catch (final TorException e) {
                LOG.warn("getAuthorityKeyCertificates(): could not parse from local cache: try (re)load from remote site now", e);
            }
        } else {
            LOG.debug("getAuthorityKeyCertificates(): no data in cache: try (re)load from remote site now");
        }
        return null;
    }

    public static AuthorityKeyCertificates getFromTorNetwork(final Date minValidUntil, final StringStorage stringStorage, final NetLayer lowerDirConnectionNetLayer) {
        // (re)load is needed
        LOG.debug("getAuthorityKeyCertificates(): load and parse authorityKeyCertificates...");
        final List<String> authServerIpAndPorts = new ArrayList<String>(AuthorityServers.getAuthorityIpAndPorts());
        Collections.shuffle(authServerIpAndPorts);
        String httpResponse = null;
        for (final String authServerIpAndPort : authServerIpAndPorts) {
            // download authority key certificates
            try {
                final TcpipNetAddress hostAndPort = new TcpipNetAddress(authServerIpAndPort);
                final String path = "/tor/keys/all";
                try {
                    httpResponse = SimpleHttpClientCompressed.getInstance().get(lowerDirConnectionNetLayer, hostAndPort, path);
                } catch (ZipException e) {
                    LOG.debug("got ZipException trying to get data uncompressed");
                    httpResponse = SimpleHttpClient.getInstance().get(lowerDirConnectionNetLayer, hostAndPort, path);
                }
                // parse loaded result
                final AuthorityKeyCertificates newAuthorityKeyCertificates = new AuthorityKeyCertificates(httpResponse, minValidUntil);

                // no exception thrown: certificates are OK
                if (newAuthorityKeyCertificates.isValid(minValidUntil)) {
                    LOG.debug("getAuthorityKeyCertificates(): successfully loaded from {}", authServerIpAndPort);
                    // save in cache
                    stringStorage.put(STORAGEKEY_AUTHORITY_KEY_CERTIFICATES_TXT, httpResponse);
                    // use as result
                    return newAuthorityKeyCertificates;
                } else {
                    LOG.debug("getAuthorityKeyCertificates(): loaded from {} - but not valid: try next", authServerIpAndPort);
                }
            } catch (final TorException e) {
                LOG.warn("getAuthorityKeyCertificates(): could not parse from " + authServerIpAndPort + " result=" + httpResponse
                        + ", try next", e);
            } catch (final Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAuthorityKeyCertificates(): error while loading from {}, try next", authServerIpAndPort, e);
                }
            }
        }
        LOG.error("getAuthorityKeyCertificates(): could NOT load and parse authorityKeyCertificates");
        // use outdated certificates if no newer could be retrieved
        return null;
    }
}
