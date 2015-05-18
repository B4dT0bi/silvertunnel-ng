/*
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelayData;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output stream for connections tunneled through Tor.
 * 
 * @author Lexi Pimenidis
 * @author hapke
 */
class TCPStreamOutputStream extends OutputStream
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TCPStreamOutputStream.class);

	private final TCPStream stream;
	/** read from this, and forward to tor. */
	private PipedOutputStream sout;
	/** private end of this pipe. */
	private PipedInputStream fromjava;
	/** as stop() is depreciated we use this toggle variable. */
	private boolean stopped;
	private final byte[] buffer;
	private int bufferFilled;

	TCPStreamOutputStream(final TCPStream stream)
	{
		this.stream = stream;
		buffer = new byte[CellRelay.RELAY_DATA_SIZE];
		bufferFilled = 0;
		try
		{
			sout = new PipedOutputStream();
			fromjava = new PipedInputStream(sout);
		}
		catch (final IOException e)
		{
			LOG.error("TCPStreamThreadJava2Tor: caught IOException "
					+ e.getMessage(), e);
		}
	}

	@Override
	public void close()
	{
		this.stopped = true;
	}

	@Override
	public void write(final int b) throws IOException
	{
		final byte[] bytes = new byte[1];
		bytes[0] = (byte) b;
		write(bytes, 0, 1);
	}

	@Override
	public synchronized void flush() throws IOException
	{
		if (stopped)
		{
			// do not throw new
			// IOException("TCPStreamOutputStream.flush(): output closed");
			// instead: continue:
			return;
		}
		if (bufferFilled < 1)
		{
			return;
		}
		if (bufferFilled > buffer.length)
		{
			throw new IOException(
					"TCPStreamOutputStream.flush(): there must be an error somewhere else");
		}
		final CellRelayData cell = new CellRelayData(stream);
		cell.setLength(bufferFilled);
		if (cell.getLength() > cell.getData().length)
		{
			cell.setLength(cell.getData().length);
		}
		System.arraycopy(buffer, 0, cell.getData(), 0, bufferFilled);
		try
		{
			stream.sendCell(cell);
		}
		catch (TorException exception)
		{
			throw new IOException(exception);
		}
		bufferFilled = 0;
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
			throws IOException
	{
		if (len == 0)
		{
			return;
			/*
			 * if (len<0) throw new
			 * IOException("TCPStreamOutputStream.write(): len = "+len); if
			 * (bufferFilled > buffer.length) throw new
			 * IOException("TCPStreamOutputStream.write(): filled = "+bufferFilled);
			 */
		}

		int bytesFree = buffer.length;
		if (bufferFilled == buffer.length)
		{
			flush();
		}
		else
		{
			bytesFree = buffer.length - bufferFilled;
		}

		if (len > bytesFree)
		{
			write(b, off, bytesFree);
			write(b, off + bytesFree, len - bytesFree);
		}
		else
		{
			System.arraycopy(b, off, buffer, bufferFilled, len);
			bufferFilled += len;
			// to be compatible with java.netSocketOutputStream.socketWrite0():
			// do not just flush if (bufferFilled==buffer.length)
			flush();
		}
	}

	@Override
	public void write(final byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}
}
