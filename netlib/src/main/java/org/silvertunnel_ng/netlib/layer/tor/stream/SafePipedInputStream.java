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
package org.silvertunnel_ng.netlib.layer.tor.stream;

import java.io.IOException;
import java.io.PipedInputStream;

/**
 * this class is meant to simulate the behavior of a standard stream. It's
 * necessary because at some point the PipedInputStream returns a IOException,
 * if the connection has been closed by the remote side, where a InputStream
 * would only return a 'null'.
 * 
 * @author Lexi Pimenidis
 * @see PipedInputStream
 * @see InputStream
 */
class SafePipedInputStream extends PipedInputStream
{

	@Override
	public int read() throws IOException
	{
		try
		{
			return super.read();
		}
		catch (final IOException e)
		{
			// catch only if the connected PipeOutputStream is closed. otherwise
			// rethrow the error
			final String msg = e.getMessage();
			if (msg != null && msg.equals("Write end dead"))
			{
				return -1;
			}
			else
			{
				throw e;
			}
		}
	}

	@Override
	public int read(byte[] b, final int off, final int len) throws IOException
	{
		try
		{
			return super.read(b, off, len);
		}
		catch (final IOException e)
		{
			// catch only if the connected PipeOutputStream is closed. otherwise
			// rethrow the error
			final String msg = e.getMessage();
			if (msg != null
					&& (msg.equals("Write end dead") || msg
							.equals("Pipe closed")))
			{
				b = null;
				return 0;
			}
			else
			{
				throw e;
			}
		}
	}
}
