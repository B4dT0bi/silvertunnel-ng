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

import java.util.*;

/**
 * The GuardList is used for maintaining a list of Guard nodes.
 */
public class GuardList {
    private List<GuardEntry> guardNodes = new ArrayList<GuardEntry>(5);
    private Map<Fingerprint, Router> candidates = new HashMap<Fingerprint, Router>();
    private Directory directory;
    /**
     * Load the list from cache.
     */
    private void load() {

    }

    /**
     * Save the list to cache.
     */
    protected void save() {

    }

    private Router getGuard() {
        List<Router> routers = getUsableRouter();
        while (routers.size() <= 2) {

        }
    }

    /**
     * Get a list of usable guard nodes.
     * @return a list of Router
     */
    private List<Router> getUsableRouter() {
        List<Router> result = new ArrayList<Router>();
        for (GuardEntry guardEntry : guardNodes){
            if (isRouterUsable(guardEntry)) {
                result.add(guardEntry.router);
            }
        }
        return result;
    }

    /**
     * Check if a specific Router is usable as Guard node.
     * @param guardEntry
     * @return
     */
    private boolean isRouterUsable(final GuardEntry guardEntry) {
        Router routerFromDirectory = directory.getRouterByFingerprint(guardEntry.router.getFingerprint());
        // check if Router is still guard, valid and running
        if (!routerFromDirectory.isDirv2Guard() || !routerFromDirectory.isDirv2Running() || !routerFromDirectory.isDirv2Valid()) {
            return false;
        }

        return true;
    }
    private void addGuardsToList() {

    }

    /**
     * The connection to this Guard was successful.
     * @param router
     */
    public void successful(final Router router) {
        if (candidates.containsKey(router.getFingerprint())) {
            guardNodes.add(candidates.remove(router.getFingerprint()));
        }
    }
}
