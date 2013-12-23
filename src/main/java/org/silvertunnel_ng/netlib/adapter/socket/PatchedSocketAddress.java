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

import java.net.InetSocketAddress;
/**
 * An object of this class delivers different addresses depending on the caller.
 * 
 * See class SocketUtil.
 * 
 * @author hapke
 */
class PatchedSocketAddress extends InetSocketAddress
{

	private static final long serialVersionUID = 1L;

	public PatchedSocketAddress()
	{
		super("localhost", 0);
	}

	/**
	 * This method is called from java.net.Socket.Socket(Proxy proxy) to check
	 * the proxy address: return the ordinary address of this object and from
	 * java.net.SocksSocketImpl.SocksSocketImpl(Proxy proxy) to determine the
	 * implementation details: return null
	 */
	/*
	 * @Override TODO public SocketAddress address() { // determine the calling
	 * class StackTraceElement[] elements = new Exception().getStackTrace();
	 * String callingClassFullName = elements[1].getClassName();
	 * 
	 * final String SOCKS_COCKET_IMPL = "java.net.SocksSocketImpl";
	 * 
	 * if (!SOCKS_COCKET_IMPL.equals(callingClassFullName)) { // called from
	 * java.net.Socket.Socket(Proxy proxy) to check the proxy address (or from
	 * somewhere else) return super.address(); } else { // called from
	 * java.net.SocksSocketImpl.SocksSocketImpl(Proxy proxy) to determine the
	 * implementation details return new InvalidSocketAddress(); } }
	 */
}
