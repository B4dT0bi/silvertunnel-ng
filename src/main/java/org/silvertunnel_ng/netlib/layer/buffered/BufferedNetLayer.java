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

package org.silvertunnel_ng.netlib.layer.buffered;

import java.io.IOException;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * NetLayer that buffers data.
 * 
 * This layer is used for educational purposes to demonstrate the NetLayer
 * concept.
 * 
 * @author hapke
 */
public class BufferedNetLayer implements NetLayer
{
	private final NetLayer lowerNetLayer;

	public BufferedNetLayer(final NetLayer lowerNetLayer)
	{
		this.lowerNetLayer = lowerNetLayer;
	}

	/** @see NetLayer#createNetSocket(Map, NetAddress, NetAddress) */
	@Override
	public NetSocket createNetSocket(final Map<String, Object> localProperties,
			NetAddress localAddress, NetAddress remoteAddress)
			throws IOException
	{
		final NetSocket lowerLayerSocket = lowerNetLayer.createNetSocket(
				localProperties, localAddress, remoteAddress);
		return new BufferedNetSocket(lowerLayerSocket);
	}

	/** @see NetLayer#createNetServerSocket(Map, NetAddress) */
	@Override
	public NetServerSocket createNetServerSocket(
			final Map<String, Object> properties,
			final NetAddress localListenAddress)
	{
		throw new UnsupportedOperationException();
	}

	/** @see NetLayer#getStatus() */
	@Override
	public NetLayerStatus getStatus()
	{
		return lowerNetLayer.getStatus();
	}

	/** @see NetLayer#waitUntilReady() */
	@Override
	public void waitUntilReady()
	{
		lowerNetLayer.waitUntilReady();
	}

	/** @see NetLayer#clear() */
	@Override
	public void clear() throws IOException
	{
		lowerNetLayer.clear();
	}

	/** @see NetLayer#getNetAddressNameService() */
	@Override
	public NetAddressNameService getNetAddressNameService()
	{
		return lowerNetLayer.getNetAddressNameService();
	}
}
