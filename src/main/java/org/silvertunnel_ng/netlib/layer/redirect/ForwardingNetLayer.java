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

package org.silvertunnel_ng.netlib.layer.redirect;

import java.io.IOException;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * NetLayer that forwards all requests to a NetAddress specified in the
 * constructor
 * 
 * @author hapke
 */
public class ForwardingNetLayer implements NetLayer
{
	private final NetLayer lowerNetLayer;
	private final Map<String, Object> lowerNetLayerLocalProperties;
	private final NetAddress lowerNetLayerLocalAddress;
	private final NetAddress lowerNetLayerRemoteAddress;

	/**
	 * Forward all connections the the layer and address specified here.
	 * 
	 * @param lowerNetLayer
	 * @param lowerLayerNetAddress
	 */
	public ForwardingNetLayer(NetLayer lowerNetLayer,
			Map<String, Object> lowerNetLayerLocalProperties,
			NetAddress lowerNetLayerLocalAddress,
			NetAddress lowerNetLayerRemoteAddress)
	{
		this.lowerNetLayer = lowerNetLayer;
		this.lowerNetLayerLocalProperties = lowerNetLayerLocalProperties;
		this.lowerNetLayerLocalAddress = lowerNetLayerLocalAddress;
		this.lowerNetLayerRemoteAddress = lowerNetLayerRemoteAddress;
	}

	/**
	 * Create a connection using the lower layer with its predefined address.
	 * 
	 * @param localProperties
	 *            will be ignored
	 * @param localAddress
	 *            will be ignored
	 * @param remoteAddress
	 *            will be ignored
	 * 
	 * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
	 */
	@Override
	public NetSocket createNetSocket(Map<String, Object> localProperties,
			NetAddress localAddress, NetAddress remoteAddress)
			throws IOException
	{
		// forward the request
		return lowerNetLayer.createNetSocket(lowerNetLayerLocalProperties,
				lowerNetLayerLocalAddress, lowerNetLayerRemoteAddress);
	}

	/**
	 * Not implemented.
	 * 
	 * @see NetLayer#createNetServerSocket(Map, NetAddress)
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public NetServerSocket createNetServerSocket(
			Map<String, Object> properties, NetAddress localListenAddress)
	{
		throw new UnsupportedOperationException();
	}

	/** @see NetLayer#getStatus() */
	@Override
	public NetLayerStatus getStatus()
	{
		return NetLayerStatus.READY;
	}

	/** @see NetLayer#waitUntilReady() */
	@Override
	public void waitUntilReady()
	{
		// nothing to do
	}

	/** @see NetLayer#clear() */
	@Override
	public void clear() throws IOException
	{
		// nothing to do
	}

	/** @see NetLayer#getNetAddressNameService() */
	@Override
	public NetAddressNameService getNetAddressNameService()
	{
		throw new UnsupportedOperationException();
	}
}
