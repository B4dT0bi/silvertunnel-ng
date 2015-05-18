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
/**
 * Interface that merges the methods of
 * 
 * @see sun.net.spi.nameservice.NameService of Java version 1.5 and 1.6 and
 *      higher.
 * 
 * @author hapke
 * @author Tobias Boese
 */
interface NameServiceNetlibGenericAdapter
{
	/**
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	String getHostByAddr(byte[] ip) throws UnknownHostException;

	/**
	 * @see sun.net.spi.nameservice.NameService#lookupAllHostAddr(java.lang.String)
	 * 
	 *      Attention: This method is needed for Java 1.6 or higher only
	 */
	InetAddress[] lookupAllHostAddrJava6(String name)
			throws UnknownHostException;
}
