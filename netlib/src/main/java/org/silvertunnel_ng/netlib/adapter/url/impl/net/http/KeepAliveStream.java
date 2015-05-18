/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package org.silvertunnel_ng.netlib.adapter.url.impl.net.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream that has the property of being able to be kept alive for multiple
 * downloads from the same server.
 * 
 * @author Stephen R. Pietrowicz (NCSA)
 * @author Dave Brown
 */
public class KeepAliveStream extends MeteredStream implements Hurryable
{

	// instance variables
	HttpClient hc;

	boolean hurried;

	// has this KeepAliveStream been put on the queue for asynchronous cleanup.
	protected boolean queuedForCleanup = false;

	private static KeepAliveStreamCleaner queue = new KeepAliveStreamCleaner();
	private static Thread cleanerThread = null;
	private static boolean startCleanupThread;

	/**
	 * Constructor
	 */
	public KeepAliveStream(InputStream is, ProgressSource pi, long expected,
			HttpClient hc)
	{
		super(is, pi, expected);
		this.hc = hc;
	}

	/**
	 * Attempt to cache this connection
	 */
	@Override
	public void close() throws IOException
	{
		// If the inputstream is closed already, just return.
		if (closed)
		{
			return;
		}

		// If this stream has already been queued for cleanup.
		if (queuedForCleanup)
		{
			return;
		}

		// Skip past the data that's left in the Inputstream because
		// some sort of error may have occurred.
		// Do this ONLY if the skip won't block. The stream may have
		// been closed at the beginning of a big file and we don't want
		// to hang around for nothing. So if we can't skip without blocking
		// we just close the socket and, therefore, terminate the keepAlive
		// NOTE: Don't close super class
		try
		{
			if (expected > count)
			{
				long nskip = expected - count;
				if (nskip <= available())
				{
					long n = 0;
					while (n < nskip)
					{
						nskip = nskip - n;
						n = skip(nskip);
					}
				}
				else if (expected <= KeepAliveStreamCleaner.MAX_DATA_REMAINING
						&& !hurried)
				{
					// put this KeepAliveStream on the queue so that the data
					// remaining
					// on the socket can be cleanup asyncronously.
					queueForCleanup(new MyKeepAliveCleanerEntry(this, hc));
				}
				else
				{
					hc.closeServer();
				}
			}
			if (!closed && !hurried && !queuedForCleanup)
			{
				hc.finished();
			}
		}
		finally
		{
			if (pi != null)
			{
				pi.finishTracking();
			}

			if (!queuedForCleanup)
			{
				// nulling out the underlying inputstream as well as
				// httpClient to let gc collect the memories faster
				in = null;
				hc = null;
				closed = true;
			}
		}
	}

	/* we explicitly do not support mark/reset */

	@Override
	public boolean markSupported()
	{
		return false;
	}

	@Override
	public void mark(int limit)
	{
	}

	@Override
	public void reset() throws IOException
	{
		throw new IOException("mark/reset not supported");
	}

	@Override
	public synchronized boolean hurry()
	{
		try
		{
			/* CASE 0: we're actually already done */
			if (closed || count >= expected)
			{
				return false;
			}
			else if (in.available() < (expected - count))
			{
				/* CASE I: can't meet the demand */
				return false;
			}
			else
			{
				/*
				 * CASE II: fill our internal buffer Remind: possibly check
				 * memory here
				 */
				final int size = (int) (expected - count);
				final byte[] buf = new byte[size];
				final DataInputStream dis = new DataInputStream(in);
				dis.readFully(buf);
				in = new ByteArrayInputStream(buf);
				hurried = true;
				return true;
			}
		}
		catch (final IOException e)
		{
			// e.printStackTrace();
			return false;
		}
	}

	private static synchronized void queueForCleanup(
			MyKeepAliveCleanerEntry kace)
	{
		if (queue != null && !kace.getQueuedForCleanup())
		{
			if (!queue.offer(kace))
			{
				kace.getHttpClient().closeServer();
				return;
			}

			kace.setQueuedForCleanup();
		}

		startCleanupThread = (cleanerThread == null);
		if (!startCleanupThread)
		{
			if (!cleanerThread.isAlive())
			{
				startCleanupThread = true;
			}
		}

		if (startCleanupThread)
		{
			java.security.AccessController
					.doPrivileged(new java.security.PrivilegedAction<Void>()
					{
						@Override
						public Void run()
						{
							// We want to create the Keep-Alive-SocketCleaner in
							// the
							// system threadgroup
							ThreadGroup grp = Thread.currentThread()
									.getThreadGroup();
							ThreadGroup parent = null;
							while ((parent = grp.getParent()) != null)
							{
								grp = parent;
							}

							cleanerThread = new Thread(grp, queue,
									"Keep-Alive-SocketCleaner");
							cleanerThread.setDaemon(true);
							cleanerThread.setPriority(Thread.MAX_PRIORITY - 2);
							cleanerThread.start();
							return null;
						}
					});
		}
	}

	protected long remainingToRead()
	{
		return expected - count;
	}

	protected void setClosed()
	{
		in = null;
		hc = null;
		closed = true;
	}
}

class MyKeepAliveCleanerEntry
{
	KeepAliveStream kas;
	HttpClient hc;

	public MyKeepAliveCleanerEntry(KeepAliveStream kas, HttpClient hc)
	{
		this.kas = kas;
		this.hc = hc;
	}

	protected KeepAliveStream getKeepAliveStream()
	{
		return kas;
	}

	protected HttpClient getHttpClient()
	{
		return hc;
	}

	protected void setQueuedForCleanup()
	{
		kas.queuedForCleanup = true;
	}

	protected boolean getQueuedForCleanup()
	{
		return kas.queuedForCleanup;
	}

}
