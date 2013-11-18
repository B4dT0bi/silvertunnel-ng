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

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental code.
 * 
 * TCP/IP client.
 * 
 * @author hapke
 */
public class TcpipClient extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TcpipClient.class);

	public static final String NAME = "                      TcpipClient";

	private StreamSender streamSender;
	private StreamReceiver streamReceiver;
	private Socket socket;

	/**
	 * Start a TCP server (socket) that can handle one connection request.
	 */
	@Override
	public void run()
	{
		try
		{
			socket = new Socket("localhost", TcpipServer.SERVER_PORT);
			LOG.info(NAME + ": connection started");

			// start handling threads
			streamReceiver = new StreamReceiver(NAME + "-receiv",
					socket.getInputStream());
			streamReceiver.start();
			streamSender = new StreamSender(NAME + "-sender",
					socket.getOutputStream(), (byte) 0);
			streamSender.start();

			LOG.info(NAME + ": receiver and sender threads started");

		}
		catch (final Exception e)
		{
			LOG.warn(NAME + ": end because of exception", e);
		}
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////
	public StreamSender getStreamSender()
	{
		return streamSender;
	}

	public StreamReceiver getStreamReceiver()
	{
		return streamReceiver;
	}

	public Socket getSocket()
	{
		return socket;
	}
}
