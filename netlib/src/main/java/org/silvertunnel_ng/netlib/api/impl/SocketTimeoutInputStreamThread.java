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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background thread of class SocketTimeoutInputStream.
 */
class SocketTimeoutInputStreamThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SocketTimeoutInputStreamThread.class);

	private final SocketTimeoutInputStream stis;
	private final InputStream wrappedInputStream;

	private static final long WAIT_TIMEOUT_MS = 60000;

	private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	SocketTimeoutInputStreamThread(final SocketTimeoutInputStream socketTimeoutInputStream,
	                               final InputStream wrappedInputStream)
	{
		super(createThreadName());
		this.stis = socketTimeoutInputStream;
		this.wrappedInputStream = wrappedInputStream;
		setDaemon(true);
	}

	private static synchronized String createThreadName()
	{
		return Thread.currentThread().getName()
				+ " - SocketTimeoutInputStreamThread (created="
				+ DF.format(new Date()) + ")";
	}

	/**
	 * Action.
	 */
	@Override
	public void run()
	{
		try
		{
			copyBytesFromInputStreamToBuffer();
		}
		catch (final IOException e)
		{
			synchronized (stis)
			{
				stis.lastPendingIOException = e;
			}
		}
		finally
		{
			waitUntilClosed();
			try
			{
				wrappedInputStream.close();
			}
			catch (final IOException e)
			{
				synchronized (stis)
				{
					stis.lastPendingIOException = e;
				}
			}
			finally
			{
				synchronized (stis)
				{
					stis.notify();
				}
			}
		}
	}

	/**
	 * Waits until the user called close() or WAIT_TIMEOUT_MS.
	 */
	private void waitUntilClosed()
	{
		synchronized (stis)
		{
			stis.waitingForClose = true;
			stis.notify();
		}
	}

	/**
	 * Reads bytes into the buffer until EOF, closed, or error.
	 */
	private void copyBytesFromInputStreamToBuffer() throws IOException
	{
		while (true)
		{
			int offset;
			int len;
			synchronized (stis)
			{
				// is buffer full?
				while (stis.bufferLen == stis.buffer.length)
				{
					// yes
					if (stis.closeRequestedByServiceUser)
					{
						// user called close()
						return;
					}
					waitForRead();
				}
				offset = (stis.bufferHead + stis.bufferLen) % stis.buffer.length;
				len = ((stis.bufferHead > offset) ? stis.bufferHead : stis.buffer.length) - offset;
			}
			int count;
			try
			{
				// do this outside of synchronize to avoid dead lock
				count = wrappedInputStream.read(stis.buffer, offset, len);
				if (count == -1)
				{
					// EOF
					return;
				}
			}
			catch (final InterruptedIOException e)
			{
				count = e.bytesTransferred;
			}
			synchronized (stis)
			{
				stis.bufferLen += count;
				stis.notify();
			}
		}
	}

	/**
	 * Wait for a read when the buffer is full.
	 */
	private void waitForRead()
	{
		synchronized (stis)
		{
			try
			{
				stis.wait(WAIT_TIMEOUT_MS);
			}
			catch (final InterruptedException e)
			{
				stis.closeRequestedByServiceUser = true;
			}
		}
	}
}
