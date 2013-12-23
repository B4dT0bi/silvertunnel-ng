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
package org.silvertunnel_ng.netlib.adapter.nameservice;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

/**
 * Implementation of sun.net.spi.nameservice.NameService - for Java 1.6 or
 * higher only!
 * 
 * This method can only be compiled with Java 1.6 (not with Java 1.5). Therefore
 * it is pre-compiled in the src/main/resources/ tree.
 * 
 * @author hapke
 */
public class NameServiceNetlibJava6 implements NameService
{
	/**
	 * all method calls will be forwarded to this
	 * nameServiceNetlibGenericAdapter
	 */
	private final NameServiceNetlibGenericAdapter nameServiceNetlibGenericAdapter;

	/**
	 * 
	 * @param nameServiceNetlibGenericAdapter
	 *            all method calls will be forwarded to this
	 *            nameServiceNetlibGenericAdapter
	 */
	public NameServiceNetlibJava6(final NameServiceNetlibGenericAdapter nameServiceNetlibGenericAdapter)
	{
		this.nameServiceNetlibGenericAdapter = nameServiceNetlibGenericAdapter;
	}

	/**
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	@Override
	public String getHostByAddr(final byte[] ip) throws UnknownHostException
	{
		return nameServiceNetlibGenericAdapter.getHostByAddr(ip);
	}

	/**
	 * @see sun.net.spi.nameservice.NameService#lookupAllHostAddr(java.lang.String)
	 * 
	 *      Attention: This method is needed for Java 1.6 or higher only
	 */
	@Override
	public InetAddress[] lookupAllHostAddr(final String name) throws UnknownHostException
	{
		return nameServiceNetlibGenericAdapter.lookupAllHostAddrJava6(name);
	}
}
