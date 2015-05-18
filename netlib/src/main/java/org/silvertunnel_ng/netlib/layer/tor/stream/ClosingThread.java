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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this background thread closes all streams that have been build by
 * StreamThreads but are not used any more.<br>
 * FIXME: cache ready streams and possibly reuse them later on
 * 
 * @author Lexi
 * @author hapke
 */
public class ClosingThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ClosingThread.class);

	private final StreamThread[] threads;
	private final int chosenOne;

	public ClosingThread(final StreamThread[] threads, final int chosenOne)
	{
		this.threads = threads;
		this.chosenOne = chosenOne;
		this.start();
	}

	@Override
	public void run()
	{
		// loop and check when threads finish and then close the streams
		for (int i = 0; i < threads.length; ++i)
		{
			if (i != chosenOne)
			{
				if (threads[i].getStream() != null)
				{
					try
					{ // finish the queue
						threads[i].getStream().setClosed(true);
						threads[i].getStream().queue.close();
					}
					catch (final Exception e)
					{
						LOG.warn("Tor.ClosingThread.run(): " + e.getMessage(), e);
					}
					try
					{
						threads[i].getStream().close();
					}
					catch (final Exception e)
					{
						LOG.warn("Tor.ClosingThread.run(): " + e.getMessage(), e);
					}
				}
				try
				{
					threads[i].join();
				}
				catch (final Exception e)
				{
					LOG.warn("Tor.ClosingThread.run(): " + e.getMessage(), e);
				}
			}
		}
	}
}
