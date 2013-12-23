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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

public class DataNetSocketWrapper implements DataNetSocket
{
	/** wrapped object. */
	NetSocket netSocket;

	private final DataInputStream dis;
	private final DataOutputStream dos;

	public DataNetSocketWrapper(final NetSocket netSocket) throws IOException
	{
		final InputStream is = netSocket.getInputStream();
		final OutputStream os = netSocket.getOutputStream();

		this.dis = is instanceof DataInputStream ? (DataInputStream) is : new DataInputStream(is);
		this.dos = os instanceof DataOutputStream ? (DataOutputStream) os : new DataOutputStream(os);
		this.netSocket = netSocket;
	}

	@Override
	public DataInputStream getDataInputStream() throws IOException
	{
		return dis;
	}

	@Override
	public DataOutputStream getDataOutputStream() throws IOException
	{
		return dos;
	}

	@Override
	public DataInputStream getInputStream() throws IOException
	{
		return dis;
	}

	@Override
	public DataOutputStream getOutputStream() throws IOException
	{
		return dos;
	}

	@Override
	public void close() throws IOException
	{
		netSocket.close();

		// TODO: with or without:
		// dis.close();
		// dos.close();
	}
}
