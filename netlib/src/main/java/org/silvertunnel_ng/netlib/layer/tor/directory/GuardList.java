/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2014 silvertunnel-ng.org
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
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.util.ConvenientStreamReader;
import org.silvertunnel_ng.netlib.util.DynByteBuffer;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.*;

/**
 * The GuardList is used for maintaining a list of Guard nodes.
 */
public class GuardList {
    private static final Logger LOG = LoggerFactory.getLogger(GuardList.class);
    private static final String GUARDLIST_LOCATION = "guards.cache";
    private static final SecureRandom RND = new SecureRandom();
    private List<GuardEntry> guardNodes = new ArrayList<GuardEntry>(5);
    private HashSet<Fingerprint> candidates = new HashSet<Fingerprint>();
    private Directory directory;

    /**
     * Create a GuardList.
     *
     * @param directory the Directory from which the GuardList can get information about the current networkstatus.
     */
    protected GuardList(final Directory directory) {
        this.directory = directory;
        load();
    }

    /**
     * Load the list from cache.
     */
    private void load() {
        // try to load from local cache
        try {
            FileInputStream fileInputStream = new FileInputStream(
                    TempfileStringStorage.getTempfileFile(GUARDLIST_LOCATION));
            ConvenientStreamReader convenientStreamReader = new ConvenientStreamReader(fileInputStream);
            int count = convenientStreamReader.readInt();
            for (int i = 0; i < count; i++) {
                GuardEntry entry = new GuardEntry(convenientStreamReader);
                guardNodes.add(entry);
                LOG.debug("guard loaded from cache {}", entry.fingerprint.getHex());
            }
            fileInputStream.close();
        } catch (FileNotFoundException exception) {
            LOG.debug("no guard nodes found");
        } catch (Exception exception) {
            LOG.warn("could not load guard nodes due to exception", exception);
        }
    }

    /**
     * Save the list to cache.
     */
    protected void save() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(
                    TempfileStringStorage.getTempfileFile(GUARDLIST_LOCATION));
            DynByteBuffer buffer = new DynByteBuffer();
            buffer.append(guardNodes.size());
            for (GuardEntry entry : guardNodes) {
                entry.save(buffer);
            }
            fileOutputStream.write(buffer.toArray());
            fileOutputStream.close();
            LOG.debug("wrote guard list to local cache");
        } catch (Exception exception) {
            LOG.warn("Could not write guard list due to exception", exception);
        }
    }

    /**
     * Get a guard node.
     *
     * @param excluded fingerprints which should be excluded from the selection
     * @return a Router
     */
    public Router getGuard(final HashSet<Fingerprint> excluded, final TCPStreamProperties props) {
        List<Router> routers = getUsableRouter(excluded);
        while (routers.size() <= 2) {
            addGuardsToList(routers, excluded, props);
        }
        int max = Math.min(routers.size(), TorConfig.numEntryGuards);
        Router candidate = routers.get(RND.nextInt(max));
        if (!isGuardInList(candidate.getFingerprint())) {
            candidates.add(candidate.getFingerprint());
        }
        LOG.debug("returning guard {}", candidate.getFingerprint().getHex());
        return candidate;
    }

    /**
     * Check if the given Fingerprint is in our list of Guards.
     *
     * @param fingerprint the fingerprint of the router
     * @return true if it is in our list of guard nodes
     */
    private boolean isGuardInList(final Fingerprint fingerprint) {
        for (GuardEntry entry : guardNodes) {
            if (entry.fingerprint.equals(fingerprint)) return true;
        }
        return false;
    }

    /**
     * Get a list of usable guard nodes.
     *
     * @return a list of Router
     */
    private List<Router> getUsableRouter(final HashSet<Fingerprint> excluded) {
        List<Router> result = new ArrayList<Router>();
        List<GuardEntry> tmpList = new ArrayList<GuardEntry>(guardNodes);
        for (GuardEntry guardEntry : tmpList) {
            if (isRouterUsable(guardEntry, excluded)) {
                result.add(directory.getRouterByFingerprint(guardEntry.fingerprint));
            }
        }
        return result;
    }

    /**
     * 30 day timeout, if a guard node has been excluded because of its status for more than 30 days it needs to be removed from the list of guards.
     */
    private static final long TIMEOUT_TILL_REMOVE = 30L * 24L * 60L * 60L * 1000L;
    /**
     * Check if a specific Router is usable as Guard node.
     *
     * @param guardEntry
     * @return
     */
    private boolean isRouterUsable(final GuardEntry guardEntry, final HashSet<Fingerprint> excluded) {
        if (excluded.contains(guardEntry.fingerprint)) {
            return false; // Guard is either already in use by this Circuit or is in the same family or country (depending on config)
        }
        Router routerFromDirectory = directory.getRouterByFingerprint(guardEntry.fingerprint);
        // check if Router is still guard, valid and running
        if (routerFromDirectory == null || !routerFromDirectory.isDirv2Guard() || !routerFromDirectory.isDirv2Running() || !routerFromDirectory.isDirv2Valid()) {
            if (guardEntry.firstDiscard > 0) {
                if (System.currentTimeMillis() - guardEntry.firstDiscard > TIMEOUT_TILL_REMOVE) {
                    guardNodes.remove(guardEntry);
                    LOG.debug("Guard {} has been removed because it was excluded for more than 30 days because of its status", guardEntry.fingerprint.getHex());
                }
            } else {
                guardEntry.firstDiscard = System.currentTimeMillis();
            }
            return false;
        }

        // check if we should try to reconnect
        if (guardEntry.unsuccessfulConnect > 0) {
            long timeout = 36L * 60L * 60L * 1000L;
            if (guardEntry.unsuccessfulConnect <= 6) {
                timeout = 60L * 60L * 1000L;
            } else {
                if (guardEntry.unsuccessfulConnect <= 18 + 6) {
                    timeout = 4L * 60L * 60L * 1000L;
                } else {
                    if (guardEntry.unsuccessfulConnect <= 18 + 6 + 9) {
                        timeout = 18L * 60L * 60L * 1000L;
                    }
                }

            }
            if (System.currentTimeMillis() - guardEntry.lastUnsuccessfulConnect < timeout) {
                return false;
            }
        }

        return true;
    }

    private void addGuardsToList(final List<Router> routerList, final HashSet<Fingerprint> excluded, final TCPStreamProperties prop) {
        RouterFlags flags = new RouterFlags();
        if (prop.isFastRoute()) flags.setFast(true);
        if (prop.isStableRoute()) flags.setStable(true);
        flags.setGuard(true);
        flags.setRunning(true);
        flags.setValid(true);

        Map<Fingerprint, Router> guards = directory.getValidRoutersByFlags(flags);
        Router guard = directory.selectRandomNode(guards, excluded, prop.getRankingInfluenceIndex(), prop.isFastRoute(), prop.isStableRoute());
        routerList.add(guard);
        LOG.debug("adding guard {} to list", guard.getFingerprint().getHex());
    }

    /**
     * The connection to this Guard was successful.
     *
     * @param fingerprint
     */
    public void successful(final Fingerprint fingerprint) {
        if (candidates.contains(fingerprint)) {
            GuardEntry entry = new GuardEntry();
            entry.fingerprint = fingerprint;
            candidates.remove(fingerprint);
            guardNodes.add(entry);
            save(); // TODO : check if we are not saving too often
        } else {
            for (GuardEntry entry : guardNodes) {
                if (entry.fingerprint.equals(fingerprint)) {
                    entry.lastUnsuccessfulConnect = 0;
                    entry.unsuccessfulConnect = 0;
                }
            }
        }
    }

    /**
     * Should be called when a connection to this router was not possible.
     *
     * @param fingerprint the fingerprint of the router
     */
    public void unsuccessful(final Fingerprint fingerprint) {
        if (candidates.contains(fingerprint)) {
            candidates.remove(fingerprint);
        } else {
            for (GuardEntry entry : guardNodes) {
                if (entry.fingerprint.equals(fingerprint)) {
                    entry.lastUnsuccessfulConnect = System.currentTimeMillis();
                    entry.unsuccessfulConnect++;
                }
            }
        }
    }
}
