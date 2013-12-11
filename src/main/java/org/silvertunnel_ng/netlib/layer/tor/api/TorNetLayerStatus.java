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

package org.silvertunnel_ng.netlib.layer.tor.api;

import org.silvertunnel_ng.netlib.api.NetLayerStatus;

/**
 * An object that represents the state of a TorNetLayer.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class TorNetLayerStatus extends NetLayerStatus
{
	public static final TorNetLayerStatus CONSENSUS_LOADING = new TorNetLayerStatus(
			"Consensus document (Tor router overview) will be loaded", 0.10);
	public static final TorNetLayerStatus ROUTER_DESCRIPTORS_LOADING = new TorNetLayerStatus(
			"Router descriptors (Tor router details) will be loaded", 0.30);
	public static final TorNetLayerStatus INITIAL_CIRCUITES_ESTABLISHING = new TorNetLayerStatus(
			"Initial circuits to Tor exit nodes will be established", 0.60);

	/**
	 * Create a new TorNetLayerStatus instance.
	 * 
	 * @param name
	 *            English name of this status, not null
	 * @param readyIndicator
	 *            between 0 (absolutely not ready) and 1 (ready for use)
	 */
	protected TorNetLayerStatus(final String name, final double readyIndicator)
	{
		super(name, readyIndicator);
	}

}
