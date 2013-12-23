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

package org.silvertunnel_ng.netlib.adapter.socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
/**
 * See class SocketUtil.
 * 
 * @author hapke
 */
class PatchedProxy extends Proxy
{
	private static final int SOCKS_DEFAULT_PORT = 1080;

	public PatchedProxy()
	{
		// address any port at any local address
		super(Type.SOCKS, new InetSocketAddress((InetAddress) null, SOCKS_DEFAULT_PORT));
	}
	private static final String SOCKS_SOCKET_IMPL = "java.net.SocksSocketImpl";

	/**
	 * This method is called from java.net.Socket.Socket(Proxy proxy) to check
	 * the proxy address: return the ordinary address of this object of type
	 * InetSocketAddress and from java.net.SocksSocketImpl.SocksSocketImpl(Proxy
	 * proxy) to determine the implementation details: return an object that is
	 * not of type InetSocketAddress.
	 */
	@Override
	public SocketAddress address()
	{
		// determine the calling class
		final StackTraceElement[] elements = new Exception().getStackTrace();
		final String callingClassFullName = elements[1].getClassName();


		if (!SOCKS_SOCKET_IMPL.equals(callingClassFullName))
		{
			// called from java.net.Socket.Socket(Proxy/ proxy) to
			// check the proxy address (or from somewhere else)
			return super.address();
		}
		else
		{
			// called from java.net.SocksSocketImpl.SocksSocketImpl(Proxy proxy)
			// to determine the implementation details
			return new InvalidSocketAddress();
		}
	}
}
