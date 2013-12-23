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

package org.silvertunnel_ng.netlib.adapter.socket;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.impl.NetSocket2SocketImpl;
/**
 * See class SocketUtil.
 * 
 * @author hapke
 */
class NetlibSocketImplFactory implements SocketImplFactory
{

	private NetLayer netLayer;

	/**
	 * Create an instance.
	 * 
	 * @param netLayer
	 *            use this layer for network connections; if null then prevent
	 *            network connections
	 */
	public NetlibSocketImplFactory(final NetLayer netLayer)
	{
		this.netLayer = netLayer;
	}

	/**
	 * Change the netLayer.
	 * 
	 * @param netLayer
	 *            use this layer for all future network connections; if null
	 *            then prevent network connections
	 */
	public synchronized void setNetLayer(final NetLayer netLayer)
	{
		this.netLayer = netLayer;
	}

	@Override
	public synchronized SocketImpl createSocketImpl()
	{
		if (netLayer != null)
		{
			return new NetSocket2SocketImpl(netLayer);
		}
		else
		{
			// prevent network access
			return new InvalidSocketImpl();
		}
	}
}
