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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.silvertunnel_ng.netlib.api.NetSocket;

public class Socket2NetSocket implements NetSocket
{
	/** the wrapped Socket object */
	private final Socket socket;

	public Socket2NetSocket(Socket socket) throws IOException
	{
		this.socket = socket;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}

	@Override
	public void close() throws IOException
	{
		socket.close();
	}

	@Override
	public String toString()
	{
		return "Socket2NetSocket(" + socket + ")";
	}

}
