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

package org.silvertunnel_ng.netlib.api;

import java.net.UnknownHostException;

/**
 * Abstract interface to a name service, e.g. to DNS.
 * 
 * @author hapke
 */
public interface NetAddressNameService
{
	/**
	 * Resolve the (IP) address(es) from the (host) name.
	 * 
	 * E.g. DNS lookup.
	 * 
	 * @param name
	 *            (host) name to lookup
	 * @return one or more addresses that match; ordered by relevance, i.e.
	 *         prefer to use the first element of the array
	 * @throws UnknownHostException
	 *             if the resolution failed
	 */
	NetAddress[] getAddressesByName(String name) throws UnknownHostException;

	/**
	 * Resolve the (host) name of an (IP) address.
	 * 
	 * E.g. DNS reverse lookup.
	 * 
	 * @param address
	 *            (IP) address to lookup.
	 * @return one or more names that match; ordered by relevance, i.e. prefer
	 *         to use the first element of the array
	 * @throws UnknownHostException
	 *             if the resolution failed
	 */
	String[] getNamesByAddress(NetAddress address) throws UnknownHostException;
}
