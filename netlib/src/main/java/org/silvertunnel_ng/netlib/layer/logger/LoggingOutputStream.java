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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream that logs the passed data.
 * 
 * @author hapke
 */
public class LoggingOutputStream extends FilterOutputStream
{
	private final BufferedLogger blog;

	/**
	 * 
	 * @param os
	 * @param bufferedLogger
	 */
	protected LoggingOutputStream(OutputStream os, BufferedLogger bufferedLogger)
	{
		super(os);
		this.blog = bufferedLogger;
	}

	@Override
	public void write(int b) throws IOException
	{
		boolean unknownThrowableIsOnTheWay = true;
		try
		{
			// log byte
			blog.log((byte) b);

			// forward byte
			out.write(b);

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
	}

	@Override
	public void write(byte [] b, int off, int len) throws IOException
	{
		// HINT: do not call write(int b) here to avoid changes in flushing in
		// other layers

		boolean unknownThrowableIsOnTheWay = true;

		// parameter check
		if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
		{
			throw new IndexOutOfBoundsException();
		}

		try
		{
			// log bytes
			for (int i = 0; i < len; i++)
			{
				blog.log(b[off + i]);
			}
			blog.flush();

			// forward bytes
			out.write(b, off, len);

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
	}

	@Override
	public void flush() throws IOException
	{
		// flush logger
		blog.flush();

		// flush output stream
		super.flush();
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
