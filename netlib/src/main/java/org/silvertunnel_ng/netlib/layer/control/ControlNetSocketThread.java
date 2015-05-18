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
/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2013 silvertunnel-ng.org
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

package org.silvertunnel_ng.netlib.layer.control;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetSocket of transparent NetLayer that tracks the time stamp of the last
 * activity.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class ControlNetSocketThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ControlNetSocketThread.class);

	private static ControlNetSocketThread instance;

	/** all ControlNetSockets to control. */
	private final Map<ControlNetSocket, ControlParameters> sockets = Collections
			.synchronizedMap(new WeakHashMap<ControlNetSocket, ControlParameters>());

	static
	{
		try
		{
			// first class access: start a single instance of this thread now
			instance = new ControlNetSocketThread();
			instance.setName("ControlNetSocketThread");
			instance.setDaemon(true);
			instance.start();
			LOG.info("ControlNetSocketThread instance started");

		}
		catch (final Throwable t)
		{
			LOG.error("could not construct class ControlNetSocketThread", t);
		}
	}

	/**
	 * Start checking on ControlNetSocket with the provided ControlParameters.
	 * 
	 * @param socket
	 * @param parameters
	 */
	public static void startControlingControlNetSocket(final ControlNetSocket socket,
			final ControlParameters parameters)
	{
		synchronized (instance.sockets)
		{
			instance.sockets.put(socket, parameters);
		}
	}

	/**
	 * Stop checking on ControlNetSocket.
	 * 
	 * @param socket
	 */
	public static void stopControlingControlNetSocket(final ControlNetSocket socket)
	{
		synchronized (instance.sockets)
		{
			instance.sockets.remove(socket);
		}
	}

	@Override
	public void run()
	{
		while (true)
		{
			// check all sockets
			final Map<ControlNetSocket, String> socketsToRemoveFromChecklist = new HashMap<ControlNetSocket, String>(); // value=timeout
			// text
			synchronized (sockets)
			{
				for (final Entry<ControlNetSocket, ControlParameters> e : sockets.entrySet())
				{
					// check one socket
					final String timeoutText = checkSingleSocketOnce(e.getKey(), e.getValue());
					if (timeoutText != null)
					{
						socketsToRemoveFromChecklist.put(e.getKey(), timeoutText);
					}
				}

				// cleanup sockets
				for (final Entry<ControlNetSocket, String> e : socketsToRemoveFromChecklist.entrySet())
				{
					sockets.remove(e.getKey());
				}
			}

			// close sockets that timed out
			for (final Entry<ControlNetSocket, String> e : socketsToRemoveFromChecklist.entrySet())
			{
				sendTimeoutToSingleSocket(e.getKey(), e.getValue());
			}

			// wait a bit
			try
			{
				Thread.sleep(100);
			}
			catch (final InterruptedException e)
			{ /* ignore it */
				LOG.debug("got IterruptedException : {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * Check whether socket should be closed.
	 * 
	 * @param socket
	 * @param parameters
	 * @return error message: null=no timeout text=timeout with this text
	 */
	private String checkSingleSocketOnce(final ControlNetSocket socket,
			final ControlParameters parameters)
	{
		// check overall timeout
		if (parameters.getOverallTimeoutMillis() > 0
				&& socket.getOverallMillis() > parameters
						.getOverallTimeoutMillis())
		{
			return "overall timeout reached";
		}
		if (parameters.getThroughputTimeframeMillis() > 0
				&& socket.getCurrentTimeframeMillis() >= parameters
						.getThroughputTimeframeMillis())
		{
			// current time frame is over
			final long bytes = socket
					.getCurrentTimeframeStartInputOutputBytesAndStartNewTimeframe();
			if (parameters.getThroughputTimeframeMinBytes() > 0
					&& bytes < parameters.getThroughputTimeframeMinBytes())
			{
				// timeout!!!
				return "throughput is too low";
			}
		}

		// no timeout
		return null;
	}

	private void sendTimeoutToSingleSocket(final ControlNetSocket socket, final String msg)
	{
		LOG.info("send timeout to " + socket + ": " + msg);
		try
		{
			final InterruptedIOException exceptionToBeThrownBySockets = new InterruptedIOException(
					"Stream of ControlNetLayer closed because of: " + msg);
			socket.setInterruptedIOException(exceptionToBeThrownBySockets);
			socket.close();
		}
		catch (final IOException e)
		{
			LOG.debug("IOException while calling close() (want to close because of: {})", msg, e);
		}
		catch (final Exception e)
		{
			LOG.info("Exception while calling close() (want to close because of: {})", msg, e);
		}
	}
}
