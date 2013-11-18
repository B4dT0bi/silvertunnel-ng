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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Bytewise modification of the input stream.
 * 
 * @author hapke
 */
public class ByteModificatorInputStream extends FilterInputStream
{
	private final ByteModificator byteModificator;

	protected ByteModificatorInputStream(InputStream arg0,
			ByteModificator byteModificator)
	{
		super(arg0);
		this.byteModificator = byteModificator;
	}

	@Override
	public int read() throws IOException
	{
		return byteModificator.modify((byte) in.read());
	}

	@Override
	public int read(final byte[] b, int off, int len) throws IOException
	{
		final int numOfBytes = in.read(b, off, len);
		for (int i = 0; i < numOfBytes; i++)
		{
			b[off + i] = byteModificator.modify(b[off + i]);
		}
		return numOfBytes;
	}
}
