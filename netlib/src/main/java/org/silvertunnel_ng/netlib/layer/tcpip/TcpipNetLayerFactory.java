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

package org.silvertunnel_ng.netlib.layer.tcpip;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory used to manage the default instance of the TcpipNetLayer. This
 * factory will be instantiated via default constructor.
 * 
 * Needed only by convenience-class NetFactory.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TcpipNetLayerFactory implements NetLayerFactory
{
	/** class Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(TcpipNetLayerFactory.class);
	/** Cached {@link TcpipNetLayer} instance. */
	private NetLayer netLayer;

	/**
	 * @see NetLayerFactory#getNetLayerById(org.silvertunnel_ng.netlib.api.NetLayerIDs)
	 * 
	 * @param netLayerId valid netLayerId (check {@link NetLayerIDs})
	 * @return the requested NetLayer if found; null if not found; it is not
	 *         guaranteed that the type is TcpipNetLayer
	 */
	@Override
	public synchronized NetLayer getNetLayerById(final NetLayerIDs netLayerId)
	{
		if (netLayerId == NetLayerIDs.TCPIP)
		{
			if (netLayer == null)
			{
				// create a new netLayer instance
				final NetLayer tcpipNetLayer = new TcpipNetLayer();
				final NetLayer loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer, "upper tcpip  ");
				netLayer = loggingTcpipNetLayer;
			}
			return netLayer;
		}
		LOG.debug("NetLayer {} is not supported.", netLayerId);
		// unsupported netLayerId
		return null;
	}
}
