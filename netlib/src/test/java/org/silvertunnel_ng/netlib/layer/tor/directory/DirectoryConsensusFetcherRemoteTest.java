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

import org.silvertunnel_ng.netlib.layer.tor.TorRemoteAbstractTest;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.clientimpl.Tor;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test for DirectoryConsensusFetcher.
 *
 * @author Tobias Boese
 */
public final class DirectoryConsensusFetcherRemoteTest extends TorRemoteAbstractTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryConsensusFetcherRemoteTest.class);
    /**
     * Directory Router used for testcase.
     */
    private List<Router> dirRouters = new ArrayList<Router>();

    private List<Router> allrouters;

    private StringStorage stringStorage = TempfileStringStorage.getInstance();

    private Directory directory;

    private AuthorityKeyCertificates authorityKeyCertificates;

    @Override
    @Test(timeOut = 600000)
    public void initializeTor() throws Exception {
        // repeat method declaration here to be the first test method of the class
        super.initializeTor();
    }

    @Test(dependsOnMethods = {"initializeTor"})
    public void testPrepare() throws Exception {
        // first get all Routers
        allrouters = (List<Router>) torNetLayer.getValidTorRouters();

        Field torField = torNetLayer.getClass().getDeclaredField("tor");
        torField.setAccessible(true);
        Tor tor = (Tor) torField.get(torNetLayer);
        directory = tor.getDirectory();

        Field authorityKeyCertificatesField = directory.getClass().getDeclaredField("authorityKeyCertificates");
        authorityKeyCertificatesField.setAccessible(true);
        authorityKeyCertificates = (AuthorityKeyCertificates) authorityKeyCertificatesField.get(directory);

        assertNotNull(allrouters);
        Collections.shuffle(allrouters);
        for (Router router : allrouters) {
            if ((router.isDirv2Authority() || router.isDirv2V2dir()) && router.getDirPort() > 0) {
                dirRouters.add(router);
            }
        }
        assertNotNull(dirRouters);
    }

    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor", "testPrepare"})
    public void testDownloadConsensus() throws IOException, DataFormatException {
        DirectoryConsensus directoryConsensus = DirectoryConsensusFetcher.getFromTorNetwork(new Date(), stringStorage, authorityKeyCertificates, dirRouters, loggingTcpipNetLayer);
        assertNotNull(directoryConsensus);
        Date now = new Date();
        assertTrue(directoryConsensus.isValid(now));
        assertTrue(now.after(directoryConsensus.getValidAfter()));
        assertTrue(now.before(directoryConsensus.getValidUntil()));
    }
}
