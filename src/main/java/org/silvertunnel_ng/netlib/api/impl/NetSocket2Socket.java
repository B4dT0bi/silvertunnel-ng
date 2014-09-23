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

package org.silvertunnel_ng.netlib.api.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * Wrapper NetSocket -&gt; Socket.
 * 
 * @author hapke
 */
public class NetSocket2Socket extends Socket
{
	private final NetSocket2SocketImpl netSocket2SocketImpl;

	public NetSocket2Socket(final NetSocket netSocket) throws IOException
	{
		this(new NetSocket2SocketImpl(netSocket));

		// TODO: connect with dummy address
		final int IP4SIZE = 4;
		final InetAddress dummyInetAddress = InetAddress.getByAddress(
				"NetSocket-dummy-host", new byte[IP4SIZE]);
		final int dummyPort = 0;
		connect(new InetSocketAddress(dummyInetAddress, dummyPort));
	}

	private NetSocket2Socket(final NetSocket2SocketImpl netSocket2SocketImpl) throws IOException
	{
		super(netSocket2SocketImpl);
		this.netSocket2SocketImpl = netSocket2SocketImpl;
	}

	/**
	 * Change the NetSocket used by this object.
	 * 
	 * @param newNetSocket
	 */
	public void setNetSocket(final NetSocket newNetSocket)
	{
		netSocket2SocketImpl.setNetSocket(newNetSocket);
	}
}
