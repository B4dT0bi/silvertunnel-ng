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

package org.silvertunnel_ng.netlib.layer.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * InputStream that passed all data unchanged and that tracks the time stamp of
 * the last activity.
 * 
 * @author hapke
 */
public class ControlInputStream extends InputStream
{
	private final InputStream lowerLevelInputStream;
	private final ControlNetSocket timeControlNetSocket;

	/**
	 * 
	 * @param timeControlNetSocket
	 */
	protected ControlInputStream(final InputStream lowerLevelInputStream,
			final ControlNetSocket timeControlNetSocket)
	{
		this.lowerLevelInputStream = lowerLevelInputStream;
		this.timeControlNetSocket = timeControlNetSocket;
	}

	// /////////////////////////////////////////////////////
	// the following methods pass a method call transparently to
	// lowerLevelInputStream
	// and update the last activity time stamp
	// /////////////////////////////////////////////////////

	@Override
	public int read() throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			final int result = lowerLevelInputStream.read();
			timeControlNetSocket.addInputBytes(1);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			return result;
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public int read(byte[] b) throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			final int result = lowerLevelInputStream.read(b);
			timeControlNetSocket.addInputBytes(result);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			return result;
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException,
			InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			final int result = lowerLevelInputStream.read(b, off, len);
			timeControlNetSocket.addInputBytes(result);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			return result;
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public long skip(final long n) throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			final long result = lowerLevelInputStream.skip(n);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			return result;
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public int available() throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			final int result = lowerLevelInputStream.available();
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			return result;
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public void close() throws IOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			lowerLevelInputStream.close();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public synchronized void mark(final int readlimit)
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			lowerLevelInputStream.mark(readlimit);
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public synchronized void reset() throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			lowerLevelInputStream.reset();
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public boolean markSupported()
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			return lowerLevelInputStream.markSupported();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}
}
