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

import java.io.IOException;
import java.util.Map;

/**
 * Every separate network protocol/layer implements the NetLayer interface.
 * 
 * See {@link http
 * ://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi#NetLayer}
 * 
 * @author hapke
 */
public interface NetLayer
{
	/**
	 * Create a client connection. Similar to SocketFactory.createSocket()
	 * 
	 * @param localProperties
	 *            e.g. property "timeoutInMs"; can also be used to handle a
	 *            "security profile"; is optional and can be null
	 * @param localAddress
	 *            is optional and can be null
	 * @param remoteAddress
	 *            usually one NetAddress, but can be null for layers without
	 *            address
	 * @return a new NetSocket, not null
	 * 
	 * @thrown UnsupportedOperationException if not available or not allowed for
	 *         this NetLayer instance
	 * @throws IOException
	 *             in the case of any other error
	 */
	NetSocket createNetSocket(Map<String, Object> localProperties,
			NetAddress localAddress, NetAddress remoteAddress)
			throws IOException;

	/**
	 * Create a server connection (e.g. a hidden service endpoint). Methods
	 * similar to ServerSocketFactory.createServerSocket().
	 * 
	 * @param properties
	 *            e.g. property "backlog"; can also be used to handle a
	 *            "security profile"; is optional and can be null
	 * @param localListenAddress
	 *            usually one NetAddress, but can be null for layers without
	 *            address
	 * @return a new NetServerSocket, not null
	 * 
	 * @thrown UnsupportedOperationException if not available or not allowed for
	 *         this NetLayer instance
	 * @throws IOException
	 *             in the case of any other error
	 */
	NetServerSocket createNetServerSocket(Map<String, Object> properties,
			NetAddress localListenAddress) throws IOException;

	/**
	 * @return retrieve the status of the NetLayer - can be used to display it
	 *         to the user in a proper way
	 */
	NetLayerStatus getStatus();

	/**
	 * Wait (block the current thread) until this NetLayer instance is up and
	 * ready, i.e. wait until this NetLayer is in status NetLayerStatus.READY.
	 */
	void waitUntilReady();

	/**
	 * Delete all history information stored in this NetLayer instance.
	 * Depending on the implementation, this method can close open
	 * Net(Server)Sockets that
	 * 
	 * @thrown UnsupportedOperationException if not available or not allowed for
	 *         this NetLayer instance
	 * @throws IOException
	 *             in the case of any other error
	 */
	void clear() throws IOException;

	/**
	 * @return the NetAddressNameService instance that belongs to this NetLayer
	 *         instance.
	 * 
	 * @thrown UnsupportedOperationException if not available or not allowed for
	 *         this NetLayer instance
	 */
	NetAddressNameService getNetAddressNameService();
}
