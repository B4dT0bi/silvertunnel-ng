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
 */
public class HttpUtilResponseReceiverThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtilResponseReceiverThread.class);

	private static final int DEFAULT_MAX_RESULT_SIZE = 10000000;

	private volatile boolean stopThread;
	private volatile boolean finished;
	private final byte[] tempResultBuffer;
	/** how many bytes are currently in the tempResultBuffer? */
	private volatile int tempResultBufferLen;
	private final InputStream is;

	/**
	 * Start reading from is. A default maximum result size of 10,000,000 bytes
	 * is used.
	 * 
	 * @param is
	 */
	public HttpUtilResponseReceiverThread(InputStream is)
	{
		this(is, DEFAULT_MAX_RESULT_SIZE);
	}

	/**
	 * Start reading from is.readCurrentResultAndStopThread
	 * 
	 * @param is
	 * @param maxResultSize
	 *            in bytes
	 */
	public HttpUtilResponseReceiverThread(InputStream is, int maxResultSize)
	{
		this.is = is;
		this.tempResultBuffer = new byte[maxResultSize];

		// the thread should not block the JVM shut down
		setDaemon(true);
	}

	/** read from is. */
	@Override
	public void run()
	{
		try
		{
			while (!stopThread)
			{
				if (tempResultBufferLen >= tempResultBuffer.length)
				{
					//LOG.info("result buffer is full");
					break;
				}
				final int lastLen = is.read(tempResultBuffer,
						tempResultBufferLen, tempResultBuffer.length
								- tempResultBufferLen);
				//LOG.info("read bytes: "+lastLen);
				if (lastLen < 0)
				{
					//LOG.info("end of result stream tot len : " + tempResultBufferLen);
					break;
				}
				tempResultBufferLen += lastLen;

				// byte[] logBuffer = new byte[tempResultBufferLen];
				// System.arraycopy(tempResultBuffer, 0, logBuffer, 0,
				// tempResultBufferLen);
				// LOG.info("response(part/s)="+new String(logBuffer, Util.UTF8));
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
		// copy to result buffer
		final int len = tempResultBufferLen;
		final byte[] result = new byte[len];
		System.arraycopy(tempResultBuffer, 0, result, 0, len);

		// set flags
		stopThread = true;
		finished = true;

		// LOG.info("readCurrentResultAndStopThread: "+java.util.Arrays.toString(result));

		return result;
	}
}
