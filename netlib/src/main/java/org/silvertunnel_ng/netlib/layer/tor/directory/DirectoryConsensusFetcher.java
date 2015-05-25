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
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClient;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClientCompressed;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipException;

/**
 * DirectoryConsensusFetcher is responsible for loading the cached DirectoryConsensus or download it from the tor network.
 *
 * @author Tobias Boese
 */
public class DirectoryConsensusFetcher {

    /**
     * key to locally cache the consensus.
     */
    private static final String STORAGEKEY_DIRECTORY_CACHED_CONSENSUS_TXT = "directory-cached-consensus.txt";

    private static final int MIN_LENGTH_OF_CONSENSUS_STR = 100;

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryConsensusFetcher.class);

    /**
     * Get the directory Consensus from Cache.
     *
     * @param now                      current date (will be used to check if the Consensus is still valid
     * @param stringStorage            StringStorage where Consensus has been chached
     * @param authorityKeyCertificates AuthorityKeyCertificates for verifying the Consensus
     * @return DirectoryConsensus if cached Version is still valid, null if nothing found or not valid anymore
     */
    public static DirectoryConsensus getFromCache(final Date now, StringStorage stringStorage, AuthorityKeyCertificates authorityKeyCertificates) {
        DirectoryConsensus result;
        LOG.debug("consensus first initialization attempt: try to use document from local cache ...");
        // first initialization: try to load consensus from cache
        final String newDirectoryConsensusStr = stringStorage.get(STORAGEKEY_DIRECTORY_CACHED_CONSENSUS_TXT);
        if (newDirectoryConsensusStr != null && newDirectoryConsensusStr.length() > MIN_LENGTH_OF_CONSENSUS_STR) {
            try {
                result = new DirectoryConsensus(newDirectoryConsensusStr, authorityKeyCertificates, now);
                if (!result.isValid(now)) {
                    // cache result was not acceptable
                    result = null;
                    LOG.debug("consensus from local cache (is too small and) could not be used");
                } else {
                    LOG.debug("consensus still valid so use it from local cache");
                }
            } catch (final TorException e) {
                result = null;
                LOG.debug("consensus from local cache is not valid (e.g. too old) and could not be used");
            } catch (final Exception e) {
                result = null;
                LOG.debug("error while loading consensus from local cache: {}", e, e);
            }
        } else {
            result = null;
            LOG.debug("consensus from local cache (is null or invalid and) could not be used");
        }
        return result;
    }

    /**
     * Random number generator for choosing a directory router.
     */
    private static final Random rnd = new Random();

    /**
     * Download the Directory consensus document from one of the given Directory routers.
     *
     * @param now                        current date/time
     * @param stringStorage              StringStorage to store the result for caching
     * @param authorityKeyCertificates   AuthorityKeyCertificates for verifying the Consensus
     * @param directoryRouters           a Collection of Routers which are suitable to provide the Consensus
     * @param lowerDirConnectionNetLayer the NetLay which should be used to download the Consensus
     * @return a DirectoryConsensus if download was successfull, null if not
     */

    public static DirectoryConsensus getFromTorNetwork(final Date now, StringStorage stringStorage, AuthorityKeyCertificates authorityKeyCertificates, Collection<Router> directoryRouters, final NetLayer lowerDirConnectionNetLayer) {
        DirectoryConsensus result = null;

        LOG.debug("load consensus from Tor network");
        // all v3 directory servers

        List<Router> dirRouters = new ArrayList<Router>(directoryRouters);

        // remove all dir Routers which dont provide a dirPort
        Iterator<Router> routerIterator = dirRouters.iterator();
        while (routerIterator.hasNext()) {
            if (routerIterator.next().getDirPort() == 0) {
                routerIterator.remove();
            }
        }

        final String path = "/tor/status-vote/current/consensus";
        String newPath = path + "/";
        for (Fingerprint fingerprint : authorityKeyCertificates.getAuthorityKeyCertificates().keySet()) {
            newPath += fingerprint.getHex() + "+";
        }
        newPath = newPath.substring(0, newPath.length() - 2);

        // Choose one randomly
        while (dirRouters.size() > 0) {
            final int index = rnd.nextInt(dirRouters.size());
            final Router dirRouter = dirRouters.get(index);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFromTorNetwork: Randomly chosen dirRouter to fetch consensus document: "
                        + dirRouter.getFingerprint()
                        + " (" + dirRouter.getNickname() + ")");
            }
            try {
                String newDirectoryConsensusStr = null;
                try {
                    newDirectoryConsensusStr = SimpleHttpClientCompressed.getInstance().get(lowerDirConnectionNetLayer, dirRouter.getDirAddress(), newPath);
                } catch (ZipException e) {
                    LOG.debug("got ZipException while downloading DirectoryConsensus trying to fetch it uncompressed.");
                    newDirectoryConsensusStr = SimpleHttpClient.getInstance().get(lowerDirConnectionNetLayer, dirRouter.getDirAddress(), path);
                } catch (IOException e) {
                    if (e.getMessage().contains("transfer was not successful")) {
                        newDirectoryConsensusStr = SimpleHttpClientCompressed.getInstance().get(lowerDirConnectionNetLayer, dirRouter.getDirAddress(), path);
                    }
                }

                // Parse the document
                result = new DirectoryConsensus(newDirectoryConsensusStr, authorityKeyCertificates, now);
                if (!result.needsToBeRefreshed(now)) {
                    // result is acceptable
                    LOG.debug("use new consensus");
                    // save the directoryConsensus for later Tor-startups
                    stringStorage.put(STORAGEKEY_DIRECTORY_CACHED_CONSENSUS_TXT, newDirectoryConsensusStr);
                    break;
                }
                result = null;
            } catch (final Exception e) {
                LOG.warn("Directory.updateNetworkStatusNew Exception", e);
                dirRouters.remove(index);
                result = null;
            }
        }
        return result;
    }
}
