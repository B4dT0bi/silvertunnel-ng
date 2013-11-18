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

package org.silvertunnel_ng.netlib.layer.redirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * NetSocket of SwitchingNetLayer.
 * 
 * @author hapke
 */
public class SwitchingNetSocket implements NetSocket
{
	/** reference to the layer instance of this socket. */
	private final SwitchingNetLayer switchingNetLayer;
	/** forward to this NetSocket. */
	private final NetSocket lowerNetSocket;
	/** true=this object is already closed. */
	private boolean closed = false;

	protected SwitchingNetSocket(SwitchingNetLayer switchingNetLayer,
			NetSocket lowerNetSocket)
	{
		this.switchingNetLayer = switchingNetLayer;
		this.lowerNetSocket = lowerNetSocket;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException
	{
		if (!closed)
		{
			return lowerNetSocket.getInputStream();
		}
		else
		{
			throw new IOException(
					"getInputStream(): SwitchingNetSocket already closed");
		}
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException
	{
		if (!closed)
		{
			return lowerNetSocket.getOutputStream();
		}
		else
		{
			throw new IOException(
					"getOutputStream(): SwitchingNetSocket already closed");
		}
	}

	@Override
	public synchronized void close() throws IOException
	{
		// remove from SwitchingNetLayer
		switchingNetLayer.removeFromLayer(this);

		closeLowerLayer();
	}

	@Override
	public String toString()
	{
		return "SwitchingNetSocket(" + lowerNetSocket + ")";
	}

	protected synchronized void closeLowerLayer() throws IOException
	{
		closed = true;
		lowerNetSocket.close();
	}
}
