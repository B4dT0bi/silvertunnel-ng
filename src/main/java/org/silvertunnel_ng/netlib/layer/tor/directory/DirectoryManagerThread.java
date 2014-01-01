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
package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory-Manager Class. This class is done in a separate thread to avoid
 * stalling the other management tasks: updating a directory can take quite an
 * amount of time :-/
 */
public final class DirectoryManagerThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryManagerThread.class);

	/** general factor seconds:milliseconds. */
	private static final int MILLISEC = 1000;
	/** time to wait between working loads in seconds. */
	static final int INTERVAL_S = 3;

	/** Are we still running? */
	private boolean stopped = false;
	/** Contains the Directory of Routers. */
	private final Directory directory;
	/** time stamp. */
	private long currentTimeMillis;
	/** time stamp. */
	private long dirNextUpdateTimeMillis;

	public DirectoryManagerThread(final Directory directory)
	{
		this.directory = directory;
		dirNextUpdateTimeMillis = currentTimeMillis;
		setName(getClass().getName());
		setDaemon(true);
		start();
	}

	/**
	 * keep up to date with the directory informations.
	 */
	private void updateDirectory()
	{
		currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis > dirNextUpdateTimeMillis || directory.getValidRoutersByFingerprint().isEmpty())
		{
			LOG.debug("DirectoryManagerThread.updateDirectory: updating directory");
			dirNextUpdateTimeMillis = currentTimeMillis + TorConfig.getIntervalDirectoryRefresh() * 60 * MILLISEC;
			directory.refreshListOfServers();
		}
	}

	@Override
	public void run()
	{
		// run until killed
		while (!stopped)
		{
			try
			{
				// do work
				updateDirectory();
				// wait
				long sleepTime = dirNextUpdateTimeMillis - System.currentTimeMillis();
				if (sleepTime <= 0)
				{
					sleepTime = INTERVAL_S * MILLISEC; 
				}
				sleep(sleepTime);
			}
			catch (final Exception e)
			{
				stopped = true;
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////
	/**
	 * Stop the Thread.
	 */
	public void setStopped()
	{
		this.stopped = true;
	}
}
