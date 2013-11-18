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

package org.silvertunnel_ng.netlib.adapter.nameservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.nameservice.redirect.SwitchingNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that merges the implementations of
 * @see sun.net.spi.nameservice.NameService for Java version 1.5 and 1.6 and
 *      higher.
 * 
 * @author hapke
 * @author Tobias Boese
 */
class NameServiceNetlibAdapter implements NameServiceNetlibGenericAdapter
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NameServiceNetlibAdapter.class);

	/** {@link NetAddressNameService} to be used. */
	private final NetAddressNameService netAddressNameService;

	/**
	 * 
	 * @param netAddressNameService
	 *            typically a SwicthingNetAddressNameService to be able to
	 *            change the lower service later.
	 */
	NameServiceNetlibAdapter(final NetAddressNameService netAddressNameService)
	{
		this.netAddressNameService = netAddressNameService;
	}

	/**
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	@Override
	public String getHostByAddr(final byte[] ip) throws UnknownHostException
	{
		LOG.info("getHostByAddr(ip={})", Arrays.toString(ip));

		// action
		final String[] result = netAddressNameService
				.getNamesByAddress(new IpNetAddress(ip));

		// return single value/best matching result
		return result[0];
	}

	/**
	 * @see sun.net.spi.nameservice.NameService#lookupAllHostAddr(java.lang.String)
	 * 
	 *      Attention: This method is needed for Java 1.6 or higher only
	 */
	@Override
	public InetAddress[] lookupAllHostAddrJava6(final String name)
			throws UnknownHostException
	{
		String netAddressNS = "unknown";
		if (netAddressNameService.getClass().equals(SwitchingNetAddressNameService.class))
		{
			netAddressNS = ((SwitchingNetAddressNameService) netAddressNameService).getLowerNetAddressNameServiceClass();
		}
		LOG.info("InetAddress[] lookupAllHostAddrJava6(name={} netAddressNameService={})", name, netAddressNS);

		// action
		final NetAddress[] result = netAddressNameService
				.getAddressesByName(name);

		// convert result to return format
		final InetAddress[] resultFinal = new InetAddress[result.length];
		for (int i = 0; i < result.length; i++)
		{
			resultFinal[i] = ((IpNetAddress) result[i])
					.getIpaddressAsInetAddress();
		}

		return resultFinal;
	}
}
