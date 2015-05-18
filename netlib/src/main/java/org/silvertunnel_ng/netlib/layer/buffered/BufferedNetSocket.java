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

package org.silvertunnel_ng.netlib.layer.buffered;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * NetSocket of BufferedNetLayer that buffers data.
 * 
 * This layer is used for educational purposes to demonstrate the
 * NetLayer/NetSocket concept.
 * 
 * @author hapke
 */
public class BufferedNetSocket implements NetSocket
{
	private final NetSocket lowerLayerSocket;
	private BufferedInputStream in;
	private BufferedOutputStream out;

	public BufferedNetSocket(final NetSocket lowerLayerSocket)
	{
		this.lowerLayerSocket = lowerLayerSocket;
	}

	@Override
	public void close() throws IOException
	{
		lowerLayerSocket.close();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		if (in == null)
		{
			in = new BufferedInputStream(lowerLayerSocket.getInputStream());
		}
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		if (out == null)
		{
			out = new BufferedOutputStream(lowerLayerSocket.getOutputStream());
		}
		return out;
	}
}
