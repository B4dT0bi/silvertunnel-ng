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

package org.silvertunnel_ng.netlib.experimental;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Experiment: Try how Java sockets behave if a socket or an associated stream
 * is closed.
 * 
 * Observer results: observed with Sun Java 1.6.0.17, 32 bit Linux
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class CloseTcpipLocalTest
{
	private TcpipServer server;
	private TcpipClient client;

	/**
	 * Start TCP/IP server and client with established connection.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception
	{
		// start server
		server = new TcpipServer();
		server.start();
		Thread.sleep(1000);

		// start client
		client = new TcpipClient();
		client.start();
		Thread.sleep(1000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseClientSocket() throws Exception
	{

		// close client socket
		Thread.sleep(3000);
		client.getSocket().close();

		/*
		 * Possible/observer results: TcpipClient-receiv:
		 * java.net.SocketException: Socket closed TcpipServer-receiv: received
		 * one byte=-1 TcpipClient-sender: java.net.SocketException: Socket
		 * closed TcpipServer-sender: java.net.SocketException: Broken pipe
		 */

		// wait before end
		Thread.sleep(5000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseServerSocket() throws Exception
	{
		// close server socket
		Thread.sleep(3000);
		server.getSocket().close();

		/*
		 * Possible/observer results: TcpipClient-receiv: received one byte=-1
		 * TcpipServer-receiv: java.net.SocketException: Socket closed
		 * TcpipClient-sender: java.net.SocketException: Broken pipe
		 * TcpipServer-sender: java.net.SocketException: Socket closed
		 */

		// wait before end
		Thread.sleep(5000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseClientStreamSender() throws Exception
	{
		// close server socket
		Thread.sleep(3000);
		client.getStreamSender().stopNow();

		/*
		 * Possible/observer results: TcpipClient-sender: stopNow, loop stopped
		 * TcpipClient-receiv: java.net.SocketException: Socket closed
		 * TcpipServer-receiv: received one byte=-1 TcpipServer-sender:
		 * java.net.SocketException: Broken pipe
		 */

		// wait before end
		Thread.sleep(5000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseClientStreamReceiver() throws Exception
	{
		// close server socket
		Thread.sleep(3000);
		client.getStreamReceiver().stopNow();

		/*
		 * Possible/observer results: TcpipClient-receiv: stopNow,
		 * java.net.SocketException: Socket closed TcpipServer-receiv: received
		 * one byte=-1 TcpipClient-sender: java.net.SocketException: Socket
		 * closed TcpipServer-sender: java.net.SocketException: Broken pipe
		 */

		// wait before end
		Thread.sleep(5000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseServerStreamSender() throws Exception
	{
		// close server socket
		Thread.sleep(3000);
		server.getStreamSender().stopNow();

		/*
		 * Possible/observer results: TcpipServer-sender: stopNow, loop stopped
		 * TcpipServer-receiv: java.net.SocketException: Socket closed
		 * TcpipClient-receiv: received one byte=-1 TcpipClient-sender:
		 * java.net.SocketException: Broken pipe
		 */

		// wait before end
		Thread.sleep(5000);
	}

	//@Ignore("not needed at the moment - only for experiments")
	@Test(enabled = false)
	public void testCloseServerStreamReceiver() throws Exception
	{
		// close server socket
		Thread.sleep(3000);
		server.getStreamReceiver().stopNow();

		/*
		 * Possible/observer results: TcpipServer-receiv: stopNow,
		 * java.net.SocketException: Socket closed TcpipClient-receiv: received
		 * one byte=-1 TcpipServer-sender: java.net.SocketException: Socket
		 * closed TcpipClient-sender: java.net.SocketException: Broken pipe
		 */

		// wait before end
		Thread.sleep(5000);
	}
}
