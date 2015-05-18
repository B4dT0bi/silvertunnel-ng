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

import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * NetServerSocket of SwitchingNetLayer.
 * 
 * @author hapke
 */

public class SwitchingNetServerSocket implements NetServerSocket
{
	/** reference to the layer instance of this socket */
	private final SwitchingNetLayer switchingNetLayer;
	/** forward to this NetServerSocket */
	private final NetServerSocket lowerNetServerSocket;
	/** true=this object is already closed */
	private boolean closed = false;

	protected SwitchingNetServerSocket(SwitchingNetLayer switchingNetLayer,
			NetServerSocket lowerNetServerSocket)
	{
		this.switchingNetLayer = switchingNetLayer;
		this.lowerNetServerSocket = lowerNetServerSocket;
	}

	@Override
	public synchronized NetSocket accept() throws IOException
	{
		if (!closed)
		{
			final SwitchingNetSocket result = new SwitchingNetSocket(
					switchingNetLayer, lowerNetServerSocket.accept());
			switchingNetLayer.addToLayer(result);
			return result;
		}
		else
		{
			throw new IOException(
					"accept(): SwitchingNetServerSocket already closed");
		}
	}

	@Override
	public synchronized void close() throws IOException
	{
		// remove from SwitchingNetLayer
		switchingNetLayer.removeFromLayer(this);

		closeLowerLayer();
	}

	protected synchronized void closeLowerLayer() throws IOException
	{
		closed = true;
		lowerNetServerSocket.close();
	}
}
