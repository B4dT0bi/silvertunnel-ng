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
import java.io.OutputStream;

import org.silvertunnel_ng.netlib.api.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to support interconnection of streams.
 * 
 * @author hapke
 */
public class InterconnectUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(InterconnectUtil.class);

	private static final int SLEEP_ON_INACTIVITY_MS = 10;
	private static final int DEFAULT_BUFFER_SIZE = 2048;

	private static long id;

	/**
	 * Copy the streams of the two sockets to the other side.
	 * 
	 * The method blocks until all the streams are closed.
	 * 
	 */
	public static void relay(NetSocket netSocket1, NetSocket netSocket2)
			throws IOException
	{
		relay(netSocket1.getInputStream(), netSocket2.getOutputStream(),
				netSocket2.getInputStream(), netSocket1.getOutputStream(),
				DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copy two streams: in1-&gt;out1 and in2-&gt;out2.
	 * 
	 * The method blocks until all the streams are closed.
	 * 
	 * @param in1
	 *            open stream; not null
	 * @param out1
	 *            open stream; not null
	 * @param in2
	 *            open stream; not null
	 * @param out2
	 *            open stream; not null
	 * @param bufferSize
	 *            use 2048 if you don't know what to take
	 */
	public static void relay(InputStream in1, OutputStream out1,
			InputStream in2, OutputStream out2, final int bufferSize)
	{
		relayInTwoThreads(in1, out1, in2, out2, bufferSize);
	}

	/**
	 * Copy two streams: in1-&gt;out1 and in2-&gt;out2.
	 * 
	 * The method doesn't block. All operations are done in two extra threads.
	 * 
	 * @param in1
	 *            open stream; not null
	 * @param out1
	 *            open stream; not null
	 * @param in2
	 *            open stream; not null
	 * @param out2
	 *            open stream; not null
	 * @param bufferSize
	 *            use 2048 if you don't know what to take
	 */
	public static void relayNonBlocking(final InputStream in1,
			final OutputStream out1, final InputStream in2,
			final OutputStream out2, final int bufferSize)
	{
		// open the first of two new threads
		new Thread(createUniqueThreadName())
		{
			@Override
			public void run()
			{
				relayInTwoThreads(in1, out1, in2, out2, bufferSize);
			}
		}.start();
	}

	/**
	 * Copy two streams: in1-&gt;out1 and in2-&gt;out2.
	 * 
	 * The method blocks until all the streams are closed.
	 * 
	 * @param in1
	 *            open stream; not null
	 * @param out1
	 *            open stream; not null
	 * @param in2
	 *            open stream; not null
	 * @param out2
	 *            open stream; not null
	 * @param bufferSize
	 *            use 2048 if you don't know what to take
	 */
	public static void relayInOneThread(InputStream in1, OutputStream out1,
			InputStream in2, OutputStream out2, final int bufferSize)
	{
		long byteCounterForLog1 = 0;
		long byteCounterForLog2 = 0;
		try
		{
			final byte[] buffer = new byte[bufferSize];
			boolean tryToClose = false;
			while (true)
			{
				boolean action = false;

				// data from in1?
				try
				{
					if (in1.available() > 0)
					{
						final int cc = in1.read(buffer);
						byteCounterForLog1 += cc;
						LOG.info(" > " + cc + " bytes (" + byteCounterForLog1
								+ " bytes total)");
						out1.write(buffer, 0, cc);
						out1.flush();
						action = true;
					}
				}
				catch (final IOException e)
				{
					LOG.debug("relay1: {}", e.toString(), e);
					tryToClose = true;
				}

				// data from in2?
				try
				{
					if (in2.available() > 0)
					{
						final int cc = in2.read(buffer);
						byteCounterForLog2 += cc;
						LOG.info(" < " + cc + " bytes (" + byteCounterForLog2
								+ " bytes total)");
						out2.write(buffer, 0, cc);
						out2.flush();
						action = true;
					}
				}
				catch (final IOException e)
				{
					LOG.debug("relay2: {}", e.toString(), e);
					tryToClose = true;
				}

				// close?
				if (!action && tryToClose)
				{
					// yes
					in1.close();
					in2.close();
					out1.close();
					out2.close();
					break;
				}

				// rest a bit, if no action
				if (!action)
				{
					Thread.sleep(SLEEP_ON_INACTIVITY_MS);
				}
			}
		}
		catch (final Exception e)
		{
			LOG.warn("connection interrupted", e);
		}
	}

	/**
	 * Copy two streams: in1-&gt;out1 and in2-&gt;out2.
	 * 
	 * The method blocks until all the streams are closed.
	 * 
	 * One copy direction will be handled in the current thread, the other in a
	 * new thread.
	 * 
	 * @param in1
	 *            open stream; not null
	 * @param out1
	 *            open stream; not null
	 * @param in2
	 *            open stream; not null
	 * @param out2
	 *            open stream; not null
	 * @param bufferSize
	 *            use 2048 if you don't know what to take
	 */
	public static void relayInTwoThreads(final InputStream in1,
			final OutputStream out1, final InputStream in2,
			final OutputStream out2, final int bufferSize)
	{
		final BooleanHolder tryToClose = new BooleanHolder();
		tryToClose.value = false;

		// open the new thread for direction 1
		new Thread(createUniqueThreadName())
		{
			@Override
			public void run()
			{
				relayOneDirection2(" >1> ", in1, out1, bufferSize, tryToClose);
			}
		}.start();

		// current thread for direction 2
		relayOneDirection2(" <2< ", in2, out2, bufferSize, tryToClose);
	}

	/**
	 * Copy data from in to out until any stream is closed.
	 * 
	 * Attention: relayOneDirection2() is much faster than relayOneDirection1().
	 * 
	 * @param in
	 *            open stream
	 * @param out
	 *            open stream
	 * @param bufferSize
	 * @param tryToClose
	 *            if false than at least one stream is closed - now close the
	 *            other streams as soon as possible
	 */
	static void relayOneDirection1(String logMsg, final InputStream in,
			OutputStream out, final int bufferSize, BooleanHolder tryToClose)
	{
		long byteCounterForLog = 0;
		try
		{
			final byte[] buffer = new byte[bufferSize];
			while (true)
			{
				boolean action = false;

				// data from in?
				try
				{
					if (in.available() > 0)
					{
						final int cc = in.read(buffer);
						byteCounterForLog += cc;
						if (logMsg != null)
						{
							LOG.info(logMsg + " " + cc + " bytes ("
									+ byteCounterForLog + " bytes total)");
						}
						out.write(buffer, 0, cc);
						out.flush();
						action = true;
					}
				}
				catch (final IOException e)
				{
					LOG.debug("relay: {}", e.toString(), e);
					tryToClose.value = true;
				}

				// close?
				if (!action && tryToClose.value)
				{
					// yes
					in.close();
					out.close();
					break;
				}

				// rest a bit, if no action
				if (!action)
				{
					Thread.sleep(SLEEP_ON_INACTIVITY_MS);
				}
			}
		}
		catch (final Exception e)
		{
			LOG.warn("connection interrupted", e);
		}
	}

	/**
	 * Copy data from in to out until any stream is closed.
	 * 
	 * Attention: relayOneDirection2() is much faster than relayOneDirection1().
	 * 
	 * @param in
	 *            open stream
	 * @param out
	 *            open stream
	 * @param bufferSize
	 * @param tryToClose
	 *            if false than at least one stream is closed - now close the
	 *            other streams as soon as possible
	 */
	static void relayOneDirection2(String logMsg, InputStream in,
			OutputStream out, final int bufferSize, BooleanHolder tryToClose)
	{
		long byteCounterForLog = 0;
		try
		{
			final byte[] buffer = new byte[bufferSize];
			try
			{
				while (true)
				{
					final int cc = in.read(buffer);
					if (cc <= 0)
					{
						// input stream closed
						LOG.info(logMsg
								+ " input stream closed - close the rest");
						break;
					}
					else
					{
						byteCounterForLog += cc;
						if (logMsg != null)
						{
							LOG.info(logMsg + " " + cc + " bytes ("
									+ byteCounterForLog + " bytes total)");
						}
						out.write(buffer, 0, cc);
						out.flush();
					}
				}
			}
			catch (final IOException e)
			{
				// e.g. output stream closed
				LOG.info(logMsg + " close all because of " + e.toString());
			}

			// close
			tryToClose.value = true;
			try
			{
				in.close();
			}
			catch (final IOException e)
			{
				// no problem
				LOG.debug("got IOException : {}", e.getMessage(), e);
			}
			try
			{
				out.close();
			}
			catch (final IOException e)
			{
				// no problem
				LOG.debug("got IOException : {}", e.getMessage(), e);
			}
		}
		catch (final Exception e)
		{
			LOG.warn("connection interrupted", e);
		}
	}

	/**
	 * @return a new unique name for a thread
	 */
	protected static synchronized String createUniqueThreadName()
	{
		id++;
		return InterconnectUtil.class.getName() + id + "-"
				+ Thread.currentThread().getName();
	}
}
