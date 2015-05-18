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
package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;

/**
 * Organization of all listen ports of one hidden service (server side).
 * 
 * @author hapke
 */
public class HiddenServiceInstance
{
	/** all listen ports of this HiddenServiceInstance */
	private final Map<Integer, HiddenServicePortInstance> listenPortsOfThisHiddenService = new HashMap<Integer, HiddenServicePortInstance>();

	private final HiddenServiceProperties hiddenServiceProperties;

	public HiddenServiceInstance(HiddenServiceProperties hiddenServiceProperties)
	{
		this.hiddenServiceProperties = hiddenServiceProperties;
	}

	/**
	 * @return public and private key of the hidden service
	 */
	public HiddenServiceProperties getHiddenServiceProperties()
	{
		return hiddenServiceProperties;
	}

	/**
	 * Assign a HiddenServicePortInstance to a port.
	 * 
	 * @param instance
	 *            instance inclusive port number
	 * @throws BindException
	 *             if the port is already in use
	 */
	public synchronized void putHiddenServicePortInstance(
			HiddenServicePortInstance instance) throws BindException
	{
		// check that port is still available
		final int port = instance.getPort();
		final HiddenServicePortInstance old = getHiddenServicePortInstance(port);
		if (old != null && old.isOpen())
		{
			// port is already occupied
			throw new BindException("port=" + port
					+ " is already in use - instance=" + instance
					+ " cannot be bound to");
		}

		// reserve the port
		listenPortsOfThisHiddenService.put(port, instance);
		instance.setHiddenServiceInstance(this);
	}

	/**
	 * Get the HiddenServicePortInstance of this HiddenServiceInstance and the
	 * specified port number.
	 * 
	 * @param port
	 * @return null if nothing found
	 */
	public synchronized HiddenServicePortInstance getHiddenServicePortInstance(
			int port)
	{
		return listenPortsOfThisHiddenService.get(port);
	}
}
