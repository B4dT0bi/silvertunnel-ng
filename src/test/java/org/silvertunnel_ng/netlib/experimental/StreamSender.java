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

package org.silvertunnel_ng.netlib.experimental;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental code.
 * 
 * OutputStream sender that sends (and flushes) one byte per seconds.
 * 
 * @author hapke
 */
public class StreamSender extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(StreamSender.class);

	private volatile boolean stopped = false;

	private final OutputStream out;
	private final String name;
	private byte counter = 0;

	/**
	 * Initialize.
	 * 
	 * @param name
	 * @param out
	 */
	public StreamSender(final String name, final OutputStream out, final byte startByte)
	{
		this.name = name;
		this.out = out;
		this.counter = startByte;
	}

	@Override
	public void run()
	{
		try
		{
			while (!stopped)
			{
				// send one byte and flush
				LOG.info(name + ": send one byte=" + counter);
				out.write(counter++);
				out.flush();

				// wait one second
				for (int i = 0; i < 10 && !stopped; i++)
				{
					Thread.sleep(100);
				}
			}
			LOG.info(name + ": loop stopped");
		}
		catch (final Exception e)
		{
			LOG.warn(name + ": end because of exception", e);
		}
	}

	public void stopNow()
	{
		LOG.info(name + ": stopNow");
		this.stopped = true;
		try
		{
			out.close();
		}
		catch (final IOException e)
		{
			LOG.warn(name, e);
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////
	public boolean isStopped()
	{
		return stopped;
	}
}
