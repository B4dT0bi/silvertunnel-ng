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

package org.silvertunnel_ng.netlib.layer.echo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * Echo output to input.
 * 
 * Used for educational purposes to demonstrate the NetSocket/NetLayer concept.
 * 
 * @author hapke
 */
public class EchoNetSocket implements NetSocket
{
	final PipedInputStream in;
	final PipedOutputStream out;

	public EchoNetSocket() throws IOException
	{
		in = new PipedInputStream();
		out = new PipedOutputStream(in);
	}

	@Override
	public void close() throws IOException
	{
		in.close();
		out.close();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return out;
	}
}
