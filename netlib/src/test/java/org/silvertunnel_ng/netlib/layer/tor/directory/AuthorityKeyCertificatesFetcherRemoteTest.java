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

import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Test for AuthorityKeyCertificatesFetcher.
 *
 * @author Tobias Boese
 */
public final class AuthorityKeyCertificatesFetcherRemoteTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(AuthorityKeyCertificatesFetcherRemoteTest.class);

    private StringStorage stringStorage = TempfileStringStorage.getInstance();

    private static final long ONE_DAY_IN_MS = 24L * 60L * 60L * 1000L;

    /**
     * Test if the Download of the AuthorityKeyCertificates works.
     */
    @Test(timeOut = 15000)
    public void testKeyCertificates() {
        final Date now = new Date();
        final Date minValidUntil = new Date(now.getTime() + ONE_DAY_IN_MS);
        AuthorityKeyCertificates authorityKeyCertificates = AuthorityKeyCertificatesFetcher.getFromTorNetwork(minValidUntil, stringStorage, new TcpipNetLayer());
        assertNotNull(authorityKeyCertificates);
        assertNotNull(authorityKeyCertificates.getAuthorityKeyCertificates());
        assertFalse(authorityKeyCertificates.getAuthorityKeyCertificates().isEmpty());
    }
}
