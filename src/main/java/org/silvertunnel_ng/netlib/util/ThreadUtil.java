/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel_ng.netlib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to log running threads.
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */
public class ThreadUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ThreadUtil.class);

	/**
	 * This method recursively visits (logs with INFO level) all threads.
	 */
	public static void logAllRunningThreads()
	{
		logAllRunningThreads(LOG);
	}

	/**
	 * This method recursively visits (logs) all threads.
	 * 
	 * @param log the {@link Logger}to be used for logging
	 */
	public static void logAllRunningThreads(final Logger log)
	{
		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
		while (root.getParent() != null)
		{
			root = root.getParent();
		}

		// Visit each thread group
		logThreadGroup(log, root, 0);
	}

	/**
	 * This method recursively visits (LOG.info()) all thread groups under
	 * `group'.
	 * 
	 * @param log the {@link Logger}to be used for logging
	 */
	public static void logThreadGroup(final Logger log, ThreadGroup group, int level)
	{
		// Get threads in `group'
		int numThreads = group.activeCount();
		final Thread[] threads = new Thread[numThreads * 2];
		numThreads = group.enumerate(threads, false);

		// Enumerate each thread in `group'
		for (int i = 0; i < numThreads; i++)
		{
			// Get thread/
			final Thread thread = threads[i];
			log.info(thread.toString());
		}

		// Get thread subgroups of `group'
		int numGroups = group.activeGroupCount();
		final ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
		numGroups = group.enumerate(groups, false);

		// Recursively visit each subgroup
		for (int i = 0; i < numGroups; i++)
		{
			logThreadGroup(log, groups[i], level + 1);
		}
	}
}
