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

package org.silvertunnel_ng.netlib.layer.logger;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that logs the passed data.
 * 
 * @author hapke
 */
public class LoggingInputStream extends FilterInputStream
{
	private final BufferedLogger blog;

	/**
	 * 
	 * @param bufferedLogger
	 */
	protected LoggingInputStream(InputStream is, BufferedLogger bufferedLogger)
	{
		super(is);
		this.blog = bufferedLogger;
	}

	@Override
	public int read() throws IOException
	{
		boolean unknownThrowableIsOnTheWay = true;
		int result = 0;

		try
		{
			result = in.read();
			unknownThrowableIsOnTheWay = false;

		}
		catch (final IOException e)
		{
			unknownThrowableIsOnTheWay = false;
			blog.flush();
			blog.logSummaryLine("throwable detected1: " + e);
			throw e;
		}
		catch (final RuntimeException e)
		{
			unknownThrowableIsOnTheWay = false;
			blog.flush();
			blog.logSummaryLine("throwable detected2: " + e);
			throw e;
		}
		finally
		{
			if (unknownThrowableIsOnTheWay)
			{
				// flush logger
				blog.flush();
				blog.logSummaryLine("throwable detected4");
			}
		}

		// log byte
		blog.log((byte) result);
		blog.flush();

		// forward byte
		return result;
	}

	@Override
	public int read(byte [] b, int off, int len) throws IOException
	{
		boolean unknownThrowableIsOnTheWay = true;
		int numOfBytes = 0;
		try
		{
			numOfBytes = in.read(b, off, len);
			unknownThrowableIsOnTheWay = false;

		}
		catch (final IOException e)
		{
			unknownThrowableIsOnTheWay = false;
			blog.flush();
			blog.logSummaryLine("throwable detected: " + e);
			throw e;
		}
		catch (final RuntimeException e)
		{
			unknownThrowableIsOnTheWay = false;
			blog.flush();
			blog.logSummaryLine("throwable detected: " + e);
			throw e;
		}
		finally
		{
			if (unknownThrowableIsOnTheWay)
			{
				// flush logger
				blog.flush();
				blog.logSummaryLine("throwable detected");
			}
		}

		// log bytes
		blog.log(b, off, numOfBytes);
		blog.flush();

		// forward result
		return numOfBytes;
	}

	@Override
	public void close() throws IOException
	{
		// finish logging
		blog.flush();
		blog.logSummaryLine("stream closed");

		// close output stream
		super.close();
	}
}
