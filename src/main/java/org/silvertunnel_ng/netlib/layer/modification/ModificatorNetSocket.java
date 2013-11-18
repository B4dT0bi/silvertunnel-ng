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

package org.silvertunnel_ng.netlib.layer.modification;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;

/**
 * Bytewise modification of the input and output stream
 * 
 * @author hapke
 */
public class ModificatorNetSocket implements NetSocket
{
	private final NetSocket lowerLayerSocket;
	private ByteModificatorInputStream in;
	private ByteModificatorOutputStream out;
	private final ByteModificator inByteModificator;
	private final ByteModificator outByteModificator;

	public ModificatorNetSocket(NetSocket lowerLayerSocket,
			ByteModificator inByteModificator,
			ByteModificator outByteModificator)
	{
		this.lowerLayerSocket = lowerLayerSocket;
		this.inByteModificator = inByteModificator;
		this.outByteModificator = outByteModificator;
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
			in = new ByteModificatorInputStream(
					lowerLayerSocket.getInputStream(), inByteModificator);
		}
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		if (out == null)
		{
			out = new ByteModificatorOutputStream(
					lowerLayerSocket.getOutputStream(), outByteModificator);
		}
		return out;
	}
}
