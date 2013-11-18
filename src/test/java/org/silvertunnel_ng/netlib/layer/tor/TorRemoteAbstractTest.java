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
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel_ng.netlib.util.TempfileStringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of Tor RemoteTest classes.
 * 
 * Contains Tor startup as separate test case.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public abstract class TorRemoteAbstractTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorRemoteAbstractTest.class);

	protected static NetLayer loggingTcpipNetLayer;
	protected static NetLayer loggingTlsNetLayer;
	protected static TorNetLayer torNetLayer;

	protected void initializeTor() throws Exception
	{
		// do it only once
		if (loggingTcpipNetLayer == null)
		{
			// create TCP/IP layer
			final NetLayer tcpipNetLayer = new TcpipNetLayer();
			loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer, "upper tcpip  ");
			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);

			// create TLS/SSL over TCP/IP layer
			final TLSNetLayer tlsNetLayer = new TLSNetLayer(loggingTcpipNetLayer);
			loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer, "upper tls/ssl");
			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);

			// create TCP/IP layer for directory access (use different layer
			// here to get different logging output)
			final NetLayer tcpipDirNetLayer = new TcpipNetLayer();
			final NetLayer loggingTcpipDirNetLayer = new LoggingNetLayer(tcpipDirNetLayer, "upper tcpip tor-dir  ");

			// create Tor over TLS/SSL over TCP/IP layer
			torNetLayer = new TorNetLayer(loggingTlsNetLayer, /* loggingT */
			tcpipDirNetLayer, TempfileStringStorage.getInstance());
			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
			torNetLayer.waitUntilReady();
		}

		// refresh net layer registration
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
	}

	// /////////////////////////////////////////////////////
	// helper methods
	// /////////////////////////////////////////////////////
	/**
	 * Helper method to remove all HTML Tags.
	 * @param htmlText the HTML to be processed
	 * @return the given HTML-text without the tags
	 */
	public static String removeHtmlTags(final String htmlText)
	{
		String result = htmlText;
		result = result.replaceAll("<style.+?</style>", "");
		result = result.replaceAll("<.+?>", "");
		return result;
	}
}
