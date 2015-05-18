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
package org.silvertunnel_ng.netlib.layer.tor.stream;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class is used to build a TCPStream in the background.
 * 
 * @author Lexi
 * @author hapke
 */
public class StreamThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(StreamThread.class);

	private TCPStream stream;
	private final Circuit cs;
	private final TCPStreamProperties sp;

	// private boolean finished = false;

	/** copy data to local variables and start background thread. */
	public StreamThread(final Circuit cs, final TCPStreamProperties sp)
	{
		this.cs = cs;
		this.sp = sp;
		this.start();
	}

	/**
	 * build stream in background and return. possibly the stream is closed
	 * prematurely by another thread by having its queue closed
	 */
	@Override
	public void run()
	{
		try
		{
			stream = new TCPStream(cs, sp);

		}
		catch (final Exception e)
		{
			if ((stream != null) && (stream.queue != null)
					&& (!stream.queue.isClosed()))
			{
				LOG.warn("Tor.StreamThread.run(): " + e.getMessage());
			}
			stream = null;
		}
		// finished = true;
	}

	public TCPStream getStream()
	{
		return stream;
	}
}
