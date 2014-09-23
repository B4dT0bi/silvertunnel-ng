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

package org.silvertunnel_ng.netlib.nameservice.redirect;

import java.net.UnknownHostException;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetAddressNameService that transparently forwards all traffic to
 * switchable/exchangeable lower NetAddressNameService.
 * 
 * @author hapke
 */
public class SwitchingNetAddressNameService implements NetAddressNameService
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SwitchingNetAddressNameService.class);

	/** Currently used lower NetAddressNameService. */
	private volatile NetAddressNameService lowerNetAddressNameService;

	/**
	 * Initialize this name service.
	 * 
	 * @param name2AddressMapping
	 *            mapping used by method getAddresses(); Map, keys and values
	 *            may not be null
	 * @param address2NameMapping
	 *            mapping used by method getNames(); Map, keys and values may
	 *            not be null
	 */
	public SwitchingNetAddressNameService(
			Map<String, NetAddress> name2AddressMapping,
			Map<NetAddress, String> address2NameMapping)
	{
	}

	/**
	 * Start with the provided lowerNetAddressNameService. The
	 * lowerNetAddressNameService can be exchanged later by calling the method
	 * setLowerNetAddressNameService().
	 * 
	 * @param lowerNetAddressNameService
	 */
	public SwitchingNetAddressNameService(final NetAddressNameService lowerNetAddressNameService)
	{
		this.lowerNetAddressNameService = lowerNetAddressNameService;
	}

	/**
	 * Exchange the lower NetAddressNameService.
	 * 
	 * @param lowerNetAddressNameService
	 *            new lower NetAddressNameService
	 */
	public void setLowerNetAddressNameService(final NetAddressNameService lowerNetAddressNameService)
	{
		this.lowerNetAddressNameService = lowerNetAddressNameService;
	}

	/** @see NetAddressNameService#getAddressesByName(String) */
	@Override
	public NetAddress[] getAddressesByName(final String name)
			throws UnknownHostException
	{
		// forward to the lower NetAddressNameService
		return lowerNetAddressNameService.getAddressesByName(name);
	}

	/** @see NetAddressNameService#getNamesByAddress(NetAddress) */
	@Override
	public String[] getNamesByAddress(final NetAddress address)
			throws UnknownHostException
	{
		// forward to the lower NetAddressNameService
		return lowerNetAddressNameService.getNamesByAddress(address);
	}
	/**
	 * @return get the lower {@link NetAddressNameService}.
	 */
	public String getLowerNetAddressNameServiceClass()
	{
		if (lowerNetAddressNameService == null)
		{
			return "null";
		}
		return lowerNetAddressNameService.getClass().getCanonicalName();
	}
}
