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

package org.silvertunnel_ng.netlib.layer.control;

import java.io.IOException;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transparent NetLayer that enforces time(out) and throughput limits of a
 * wrapped NetLayer. It aborts connections that hits the configured limits.
 * 
 * @author hapke
 */
public class ControlNetLayer implements NetLayer
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ControlNetLayer.class);

	private final NetLayer lowerNetLayer;
	private final ControlParameters controlParameters;

	/**
	 * Initialize a new layer.
	 * 
	 * @param lowerNetLayer
	 * @param controlParameters
	 *            definition when to terminate a connection
	 */
	public ControlNetLayer(final NetLayer lowerNetLayer,
			final ControlParameters controlParameters)
	{
		this.lowerNetLayer = lowerNetLayer;
		this.controlParameters = controlParameters;
	}

	/** @see NetLayer#createNetSocket(Map, NetAddress, NetAddress) */
	@Override
	public NetSocket createNetSocket(final Map<String, Object> localProperties,
			final NetAddress localAddress, final NetAddress remoteAddress)
			throws IOException
	{
		return new ControlNetSocket(lowerNetLayer.createNetSocket(
				localProperties, localAddress, remoteAddress),
				controlParameters);
	}

	/** @see NetLayer#createNetServerSocket(Map, NetAddress) */
	@Override
	public NetServerSocket createNetServerSocket(
			final Map<String, Object> properties,
			final NetAddress localListenAddress) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() throws IOException
	{
		lowerNetLayer.clear();
	}

	@Override
	public NetAddressNameService getNetAddressNameService()
	{
		return lowerNetLayer.getNetAddressNameService();
	}

	@Override
	public NetLayerStatus getStatus()
	{
		return lowerNetLayer.getStatus();
	}

	@Override
	public void waitUntilReady()
	{
		lowerNetLayer.waitUntilReady();
	}
}
