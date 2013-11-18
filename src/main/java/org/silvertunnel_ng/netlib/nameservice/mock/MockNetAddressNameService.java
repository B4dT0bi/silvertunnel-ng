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

package org.silvertunnel_ng.netlib.nameservice.mock;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of NetAddressNameService.
 * 
 * It uses predefined mappings passed as arguments to the constructor.
 * 
 * @author hapke
 */
public class MockNetAddressNameService implements NetAddressNameService
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(MockNetAddressNameService.class);

	private final Map<String, NetAddress[]> name2AddressesMapping;
	private final Map<NetAddress, String[]> address2NamesMapping;

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
	public MockNetAddressNameService(
			Map<String, NetAddress> name2AddressMapping,
			Map<NetAddress, String> address2NameMapping)
	{
		// check arguments
		if (name2AddressMapping == null)
		{
			throw new NullPointerException("invalid name2AddressMapping=null");
		}
		if (address2NameMapping == null)
		{
			throw new NullPointerException("invalid address2NameMapping=null");
		}

		// convert configuration (1st Map)
		name2AddressesMapping = new HashMap<String, NetAddress[]>(
				name2AddressMapping.size());
		for (final Map.Entry<String, NetAddress> name2Address : name2AddressMapping
				.entrySet())
		{
			name2AddressesMapping.put(name2Address.getKey(),
					new NetAddress[] { name2Address.getValue() });
		}

		// convert configuration (2nd Map)
		address2NamesMapping = new HashMap<NetAddress, String[]>(
				address2NameMapping.size());
		for (final Map.Entry<NetAddress, String> address2Name : address2NameMapping
				.entrySet())
		{
			address2NamesMapping.put(address2Name.getKey(),
					new String[] { address2Name.getValue() });
		}
	}

	/**
	 * Initialize this name service.
	 * 
	 * @param name2AddressMapping
	 *            mapping used by method getAddresses(); Map, keys, values and
	 *            value array elements may not be null
	 * @param address2NameMapping
	 *            mapping used by method getNames(); Map, keys, values and value
	 *            array elements may not be null
	 * @param details
	 *            will be ignored (is only needed to have a different method
	 *            signature)
	 */
	public MockNetAddressNameService(
			Map<String, NetAddress[]> name2AddressesMapping,
			Map<NetAddress, String[]> address2NamesMapping, boolean details)
	{
		// check arguments
		if (name2AddressesMapping == null)
		{
			throw new NullPointerException("invalid name2AddressesMapping=null");
		}
		if (address2NamesMapping == null)
		{
			throw new NullPointerException("invalid address2NamesMapping=null");
		}

		// store configuration
		this.name2AddressesMapping = name2AddressesMapping;
		this.address2NamesMapping = address2NamesMapping;
	}

	/** @see NetAddressNameService#getAddresses */
	@Override
	public NetAddress[] getAddressesByName(String name)
			throws UnknownHostException
	{
		final NetAddress[] result = name2AddressesMapping.get(name);
		if (result != null && result.length > 0)
		{
			return result;
		}
		else
		{
			throw new UnknownHostException("name=" + name
					+ " could not be resolved");
		}
	}

	/** @see NetAddressNameService#getNames */
	@Override
	public String[] getNamesByAddress(NetAddress address)
			throws UnknownHostException
	{
		final String[] result = address2NamesMapping.get(address);
		if (result != null && result.length > 0)
		{
			return result;
		}
		else
		{
			throw new UnknownHostException("address=" + address
					+ " could not be resolved");
		}
	}
}
