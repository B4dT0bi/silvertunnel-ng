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
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental code.
 * 
 * OutputStream sender that received bytes.
 * 
 * @author hapke
 */
public class StreamReceiver extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(StreamReceiver.class);

	private volatile boolean stopped = false;

	private final InputStream in;
	private final String name;

	/**
	 * Initialize.
	 * 
	 * @param name
	 * @param out
	 */
	public StreamReceiver(final String name, final InputStream in)
	{
		this.name = name;
		this.in = in;
	}

	@Override
	public void run()
	{
		try
		{
			while (!stopped)
			{
				// read and log one byte
				final int oneByte = in.read();
				LOG.info(name + ": received one byte=" + oneByte);
				if (oneByte < 0)
				{
					LOG.info(name + ": end of stream");
					break;
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
			in.close();
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
