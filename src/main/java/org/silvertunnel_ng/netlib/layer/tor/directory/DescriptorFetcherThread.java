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

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClient;
import org.silvertunnel_ng.netlib.tool.SimpleHttpClientCompressed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Descriptor-Fetcher Class. Implements a separate thread and fetches the
 * descriptor for the given node
 */
class DescriptorFetcherThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DescriptorFetcherThread.class);

	private boolean stopped = false;
	private boolean resolved = false;
	private boolean failed = false;
	private boolean idle = true;
	private int reloadRetries = 0;
	private String descriptor;
	private StringBuffer nodesDigests, nicks;
	private RouterStatusDescription loadFrom;
	private NetLayer lowerDirConnectionNetLayer;

	DescriptorFetcherThread(NetLayer lowerDirConnectionNetLayer)
	{
		this.lowerDirConnectionNetLayer = lowerDirConnectionNetLayer;
		this.start();
	}

	/**
	 * keep up to date with the directory informations.
	 */
	private void fetchDescriptor()
	{
		// download descriptor(s)
		final String newDescriptor = downloadSingleDescriptor(
				nodesDigests.toString(), loadFrom, lowerDirConnectionNetLayer);
		if (newDescriptor != null)
		{
			resolved = true;
		}
		else
		{
			failed = true;
			descriptor = "error";
		}
	}

	/**
	 * Download a single descriptor.
	 * 
	 * @param nodesDigestsToLoad
	 * @param directoryServer
	 * @param dirConnectionNetLayer
	 * @return the descriptor as single String; null in the case of an error
	 */
	public static String downloadSingleDescriptor(String nodesDigestsToLoad,
			RouterStatusDescription directoryServer,
			NetLayer dirConnectionNetLayer)
	{
		// download descriptor(s)
		try
		{
			final String path = "/tor/server/d" + nodesDigestsToLoad;
			final TcpipNetAddress hostAndPort = new TcpipNetAddress(
					directoryServer.getIp(), directoryServer.getDirPort());

			final String httpResponse = SimpleHttpClient.getInstance().get(
					dirConnectionNetLayer, hostAndPort, path);
			return httpResponse;

		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("downloadSingleDescriptor() from "
					+ directoryServer.getNickname() + " failed: "
					+ e.getMessage(), e);
			}
			return null;
		}
	}

	/**
	 * Download all descriptors.
	 * 
	 * @param directoryServer
	 * @param dirConnectionNetLayer
	 * @return the descriptors as String; null in the case of an error
	 */
	public static String downloadAllDescriptors(RouterImpl directoryServer,
			NetLayer dirConnectionNetLayer)
	{
		// download descriptor(s)
		try
		{
			final String path = "/tor/server/all";

			final String httpResponse = SimpleHttpClientCompressed
					.getInstance().get(dirConnectionNetLayer,
							directoryServer.getDirAddress(), path);
			return httpResponse;

		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("downloadAllDescriptors() from "
					+ directoryServer.getNickname() + " failed: "
					+ e.getMessage(), e);
			}
			return null;
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
				synchronized (this)
				{
					this.wait();
				}
				if ((!idle) && (!resolved) && (!failed))
				{
					fetchDescriptor();
				}
				// wait
				// sleep(Directory.FETCH_THREAD_IDLE_TIME);
			}
			catch (final Exception e)
			{
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public boolean isStopped()
	{
		return stopped;
	}

	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public void setResolved(boolean resolved)
	{
		this.resolved = resolved;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(boolean failed)
	{
		this.failed = failed;
	}

	public boolean isIdle()
	{
		return idle;
	}

	public void setIdle(boolean idle)
	{
		this.idle = idle;
	}

	public int getReloadRetries()
	{
		return reloadRetries;
	}

	public void setReloadRetries(int reloadRetries)
	{
		this.reloadRetries = reloadRetries;
	}

	public String getDescriptor()
	{
		return descriptor;
	}

	public void setDescriptor(String descriptor)
	{
		this.descriptor = descriptor;
	}

	public StringBuffer getNodesDigests()
	{
		return nodesDigests;
	}

	public void setNodesDigests(StringBuffer nodesDigests)
	{
		this.nodesDigests = nodesDigests;
	}

	public StringBuffer getNicks()
	{
		return nicks;
	}

	public void setNicks(StringBuffer nicks)
	{
		this.nicks = nicks;
	}

	public RouterStatusDescription getLoadFrom()
	{
		return loadFrom;
	}

	public void setLoadFrom(RouterStatusDescription loadFrom)
	{
		this.loadFrom = loadFrom;
	}

	public NetLayer getLowerDirConnectionNetLayer()
	{
		return lowerDirConnectionNetLayer;
	}

	public void setLowerDirConnectionNetLayer(
			NetLayer lowerDirConnectionNetLayer)
	{
		this.lowerDirConnectionNetLayer = lowerDirConnectionNetLayer;
	}

}
