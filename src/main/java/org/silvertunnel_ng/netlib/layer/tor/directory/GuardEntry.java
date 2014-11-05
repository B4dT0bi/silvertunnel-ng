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

import org.silvertunnel_ng.netlib.layer.tor.api.Router;

/**
 * The GuardEntry contains the Router information and some other information about the Guard node.
 */
public class GuardEntry {
    /** The Router information of this Guardnode. */
    protected Router router;
    /** When has this Guard first been discarded because of its status in the networkstatus? */
    protected long firstDiscard = 0;
    /** Unsuccessful connect ocunter. */
    protected int unsuccessfulConnect = 0;
    /** When was the last unsuccessful connect? */
    protected long lastUnsuccessfulConnect = 0;
}
