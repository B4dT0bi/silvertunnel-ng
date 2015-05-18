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

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental code.
 * 
 * TCP/IP server that can handle one connection request.
 * 
 * @author hapke
 */
public class TcpipServer extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TcpipServer.class);

	public static int SERVER_PORT = 11223;
	public static final String name = "                                                                  TcpipServer";

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
			final ServerSocket ss = new ServerSocket(SERVER_PORT);
			LOG.info(name + ": server socket started");

			socket = ss.accept();
			LOG.info(name + ": connection accepted");

			// start handling threads
			streamReceiver = new StreamReceiver(name + "-receiv",
					socket.getInputStream());
			streamReceiver.start();
			streamSender = new StreamSender(name + "-sender",
					socket.getOutputStream(), (byte) 100);
			streamSender.start();

			LOG.info(name + ": receiver and sender threads started");

		}
		catch (final Exception e)
		{
			LOG.warn(name + ": end because of exception", e);
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
