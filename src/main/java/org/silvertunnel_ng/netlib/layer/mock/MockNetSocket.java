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

package org.silvertunnel_ng.netlib.layer.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * Mock
 * 
 * @author hapke
 */
public class MockNetSocket implements NetSocket
{
	private final ByteArrayInputStream in;
	private final ByteArrayOutputStream out;

	/**
	 * @param response
	 * @param waitAtEndOfResponseBeforeClosingMs
	 *            if all data of response read: wait this time (milliseconds)
	 *            before "end" is signaled; 0 = do not wait; -1 = wait endless
	 */
	public MockNetSocket(byte[] response,
			long waitAtEndOfResponseBeforeClosingMs)
	{
		in = new MockByteArrayInputStream(response,
				waitAtEndOfResponseBeforeClosingMs);
		out = new ByteArrayOutputStream();
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

	public ByteArrayInputStream getByteArrayInputStream() throws IOException
	{
		return in;
	}

	public ByteArrayOutputStream getByteArrayOutputStream() throws IOException
	{
		return out;
	}
}
