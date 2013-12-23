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

public class DataNetSocketImpl implements DataNetSocket
{
	private final DataInputStream is;
	private final DataOutputStream os;

	public DataNetSocketImpl(final InputStream is, final OutputStream os)
	{
		this.is = is instanceof DataInputStream ? (DataInputStream) is : new DataInputStream(is);
		this.os = os instanceof DataOutputStream ? (DataOutputStream) os : new DataOutputStream(os);
	}

	@Override
	public DataInputStream getDataInputStream() throws IOException
	{
		return is;
	}

	@Override
	public DataOutputStream getDataOutputStream() throws IOException
	{
		return os;
	}

	@Override
	public DataInputStream getInputStream() throws IOException
	{
		return is;
	}

	@Override
	public DataOutputStream getOutputStream() throws IOException
	{
		return os;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
		os.close();
	}
}
