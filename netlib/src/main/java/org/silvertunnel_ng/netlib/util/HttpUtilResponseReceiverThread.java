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
package org.silvertunnel_ng.netlib.util;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extra thread that received data from an input stream.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class HttpUtilResponseReceiverThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtilResponseReceiverThread.class);

	/** What is the max chunk size? */
	private static final int DEFAULT_CHUNK_SIZE = 100000;

	private volatile boolean stopThread;
	private volatile boolean finished;
	private final DynByteBuffer tempResultBuffer;
	private final InputStream is;

	/**
	 * Start reading from is. A default maximum result size of 100,000 bytes
	 * is used.
	 * 
	 * @param is
	 */
	public HttpUtilResponseReceiverThread(final InputStream is)
	{
		this(is, DEFAULT_CHUNK_SIZE);
	}

	/**
	 * Start reading from is.readCurrentResultAndStopThread.
	 * 
	 * @param is
	 * @param maxResultSize
	 *            in bytes
	 */
	public HttpUtilResponseReceiverThread(final InputStream is, final int maxResultSize)
	{
		this.is = is;
		this.tempResultBuffer = new DynByteBuffer(maxResultSize);

		// the thread should not block the JVM shut down
		setDaemon(true);
	}

	/** read from is. */
	@Override
	public void run()
	{
		byte [] buffer = new byte [DEFAULT_CHUNK_SIZE / 2];
		try
		{
			while (!stopThread)
			{
				final int lastLen = is.read(buffer, 0, buffer.length);
				if (lastLen <= 0)
				{
					break;
				}
				tempResultBuffer.append(buffer, 0, lastLen);
			}
		}
		catch (final IOException e)
		{
			LOG.error("receiving data interupted by exception", e);
		}

		finished = true;
	}

	/**
	 * @return true if receiving data is finished (e.g. because end of stream).
	 */
	public boolean isFinished()
	{
		return finished;
	}

	/**
	 * Return the data as far as received until now and stop the thread.
	 * 
	 * @return the data as far as received until now
	 */
	public byte[] readCurrentResultAndStopThread()
	{
		// set flags
		stopThread = true;
		finished = true;

		return tempResultBuffer.toArray();
	}
}
