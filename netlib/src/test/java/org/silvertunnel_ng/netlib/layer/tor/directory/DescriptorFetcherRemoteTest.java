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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.layer.tor.TorRemoteAbstractTest;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.clientimpl.Tor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test which tries to download Descriptors for Tor Routers.
 *
 * @author Tobias Boese
 */
public final class DescriptorFetcherRemoteTest extends TorRemoteAbstractTest {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(DescriptorFetcherRemoteTest.class);
    /**
     * Directory Router used for testcase.
     */
    private List<Router> dirRouters = new ArrayList<Router>();

    private Router dirRouter;

    private List<Router> allrouters;

    private Directory directory;

    private DirectoryConsensus directoryConsensus;

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

        Field directoryConsensusField = directory.getClass().getDeclaredField("directoryConsensus");
        directoryConsensusField.setAccessible(true);
        directoryConsensus = (DirectoryConsensus) directoryConsensusField.get(directory);

        assertNotNull(allrouters);
        Collections.shuffle(allrouters);
        for (Router router : allrouters) {
            if ((router.isDirv2Authority() || router.isDirv2V2dir()) && router.getDirPort() > 0) {
                if (router.getFingerprint().getHex().equals("98ECD759557A3D114A80C676A86964BECDAC9055")) {
                    dirRouter = router;
                }
                dirRouters.add(router);
            }
        }
        assertNotNull(dirRouters);
        if (dirRouter == null) {
            dirRouter = dirRouters.get(0);
            LOG.warn("desired Router not found, choosing a random one");
        }
    }

    @Test(timeOut = 120000, dependsOnMethods = {"initializeTor", "testPrepare"})
    public void testDownloadEverything() throws Exception {
        List<Router> randomDir = new ArrayList<Router>();
        randomDir.add(dirRouter);
        Collections.shuffle(dirRouters);
        for (int i = 0; i < 10; i++) {
            if (!dirRouters.get(i).getFingerprint().equals(dirRouter.getFingerprint())) {
                randomDir.add(dirRouters.get(i));
            }
        }
        int tries = 0;
        String result = null;
        while (result == null && tries < randomDir.size() - 1) {
            try {
                result = DescriptorFetcher.downloadAllDescriptors(randomDir.get(tries++), loggingTcpipNetLayer);
            } catch (Exception e) {
                if (!e.getMessage().contains("Server returned HTTP response code: 503")) {
                    throw new Exception(e);
                }
            }
        }
        assertNotNull(result);
    }

    @Test(timeOut = 60000, dependsOnMethods = {"initializeTor", "testPrepare"})
    public void testDownloadParts() throws IOException, DataFormatException {
        List<String> digests = new ArrayList<String>();
        List<String> fingerprints = new ArrayList<String>();
        for (RouterStatusDescription router : directoryConsensus.getFingerprintsNetworkStatusDescriptors().values()) {
            digests.add(router.getDigestDescriptorAsHex());
            fingerprints.add(router.getFingerprint().getHexWithSpaces().trim());
            if (digests.size() > 20) break;
        }
        String result = DescriptorFetcher.downloadDescriptorsByDigest(digests, dirRouter, loggingTcpipNetLayer);
        assertNotNull(result);

        for (String fingerprint : fingerprints) {
            assertTrue(fingerprint + " was not found in answer " + result, result.contains(fingerprint));
        }
    }
}
