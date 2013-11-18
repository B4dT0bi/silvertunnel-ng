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

package org.silvertunnel_ng.netlib.tool;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.InterconnectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hapke
 * @author Tobias Boese
 *
 */
class NetProxySingleConnectionThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NetProxySingleConnectionThread.class);

	private final NetSocket upperLayerNetSocket;
	private final NetLayerIDs lowerNetLayerId;

	private static long id;

	/**
	 * Be a proxy for a single connection.
	 * 
	 * @param upperLayerNetSocket
	 *            socket of a open connection
	 * @param lowerNetLayerId
	 *            use this NetLayer to forward the data of the connection
	 */
	public NetProxySingleConnectionThread(NetSocket upperLayerNetSocket,
			NetLayerIDs lowerNetLayerId)
	{
		super(createUniqueThreadName());
		this.upperLayerNetSocket = upperLayerNetSocket;
		this.lowerNetLayerId = lowerNetLayerId;
	}

	@Override
	public void run()
	{
		try
		{
			// open lower layer socket
			final NetAddress remoteAddress = null;
			final NetSocket lowerLayerNetSocket = NetFactory.getInstance()
					.getNetLayerById(lowerNetLayerId)
					.createNetSocket(null, null, remoteAddress);

			// interconnect both sockets
			InterconnectUtil.relay(upperLayerNetSocket, lowerLayerNetSocket);
		}
		catch (final Exception e)
		{
			LOG.warn("connection abborted", e);
		}
	}

	/**
	 * @return a new unique name for a thread
	 */
	protected static synchronized String createUniqueThreadName()
	{
		id++;
		return NetProxySingleConnectionThread.class.getName() + id;
	}
}
