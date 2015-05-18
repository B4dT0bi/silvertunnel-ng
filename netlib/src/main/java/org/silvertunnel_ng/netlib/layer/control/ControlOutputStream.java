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
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * OutputStream that passed all data unchanged and that tracks the time stamp of
 * the last activity.
 * 
 * @author hapke
 */
public class ControlOutputStream extends OutputStream
{
	private final OutputStream lowerLevelOutputStream;
	private final ControlNetSocket timeControlNetSocket;

	/**
	 * 
	 * @param timeControlNetSocket
	 */
	protected ControlOutputStream(final OutputStream lowerLevelOutputStream,
			final ControlNetSocket timeControlNetSocket)
	{
		this.lowerLevelOutputStream = lowerLevelOutputStream;
		this.timeControlNetSocket = timeControlNetSocket;
	}

	// /////////////////////////////////////////////////////
	// the following methods pass a method call transparently to
	// lowerLevelOutputStream
	// and update the last activity time stamp
	// /////////////////////////////////////////////////////

	@Override
	public void write(final int b) throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			lowerLevelOutputStream.write(b);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public void write(final byte [] b) throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			lowerLevelOutputStream.write(b);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public void write(final byte[] b, int off, int len) throws IOException,
			InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			lowerLevelOutputStream.write(b, off, len);
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}

	@Override
	public void flush() throws IOException, InterruptedIOException
	{
		timeControlNetSocket.setLastActivity();
		try
		{
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
			lowerLevelOutputStream.flush();
			timeControlNetSocket.throwInterruptedIOExceptionIfNecessary();
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
			lowerLevelOutputStream.close();
		}
		finally
		{
			timeControlNetSocket.setLastActivity();
		}
	}
}
