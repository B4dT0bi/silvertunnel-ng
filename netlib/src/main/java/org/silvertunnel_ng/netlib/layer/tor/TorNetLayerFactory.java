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


package org.silvertunnel_ng.netlib.layer.tor;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.socks.SocksServerNetLayer;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory used to manage the default instance of the TorNetLayer. This factory
 * will be instantiated via default constructor.
 * 
 * Needed only by convenience-class NetFactory.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class TorNetLayerFactory implements NetLayerFactory
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorNetLayerFactory.class);

	private NetLayer torNetLayer;
	private NetLayer socksOverTorNetLayer;

	/**
	 * @see NetLayerFactory#getNetLayerById(org.silvertunnel_ng.netlib.api.NetLayerIDs)
	 * 
	 * @param netLayerId valid netLayerId (check {@link NetLayerIDs})
	 * @return the requested NetLayer if found; null if not found; it is not
	 *         guaranteed that the type is TorNetLayer
	 */
	@Override
	public NetLayer getNetLayerById(final NetLayerIDs netLayerId)
	{
		try
		{
			if (netLayerId == NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP || netLayerId == NetLayerIDs.TOR)
			{
				if (torNetLayer == null)
				{
					// create a new netLayer instance
					final NetLayer tcpipNetLayer = NetFactory.getInstance()
							.getNetLayerById(NetLayerIDs.TCPIP);
					final NetLayer tlsNetLayer = NetFactory.getInstance()
							.getNetLayerById(NetLayerIDs.TLS_OVER_TCPIP);

					torNetLayer = new TorNetLayer(tlsNetLayer, tcpipNetLayer,
							TempfileStringStorage.getInstance());
				}
				return torNetLayer;

			}
			else if (netLayerId == NetLayerIDs.SOCKS_OVER_TOR_OVER_TLS_OVER_TCPIP || netLayerId == NetLayerIDs.SOCKS_OVER_TOR)
			{
				if (socksOverTorNetLayer == null)
				{
					// create a new netLayer instance
					if (torNetLayer == null)
					{
						// fill torNetLayer first
						torNetLayer = getNetLayerById(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP);
					}

					socksOverTorNetLayer = new SocksServerNetLayer(torNetLayer);
				}
				return socksOverTorNetLayer;
			}

			// unsupported netLayerId
			return null;

		}
		catch (final Exception e)
		{
			LOG.error("could not create " + netLayerId, e);
			return null;
		}
	}
}
