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

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;

/**
 * Simple NetAddressNameService that always fails to resolve a query.
 * 
 * @author hapke
 */
public class NopNetAddressNameService implements NetAddressNameService
{
	/** special check name and ip - used to detect the usage of this class. */
	public static final String CHECKER_NAME = "checker.mock.dnstest.silvertunnel.org"; //TODO : change to silvertunnel-ng.org
	/** special check name and ip - used to detect the usage of this class. */
	public static final IpNetAddress[] CHECKER_IP = new IpNetAddress[] { new IpNetAddress(
			"0.0.0.1") };

	/** a singleton instance of this class. */
	private static NopNetAddressNameService instance;

	/** @see NetAddressNameService#getAddresses */
	@Override
	public NetAddress[] getAddressesByName(String name)
			throws UnknownHostException
	{
		if (CHECKER_NAME.equals(name))
		{
			// special check
			return CHECKER_IP;
		}
		else
		{
			throw new UnknownHostException(
					"NopNetAddressNameService.getAddressesByName() always throws this IOException");
		}
	}

	/** @see NetAddressNameService#getNames */
	@Override
	public String[] getNamesByAddress(NetAddress address)
			throws UnknownHostException
	{
		throw new UnknownHostException(
				"NopNetAddressNameService.getNamesByAddress() always throws this IOException");
	}

	/**
	 * @return a singleton instance of this class
	 */
	public static synchronized NopNetAddressNameService getInstance()
	{
		if (instance == null)
		{
			instance = new NopNetAddressNameService();
		}

		return instance;
	}
}
