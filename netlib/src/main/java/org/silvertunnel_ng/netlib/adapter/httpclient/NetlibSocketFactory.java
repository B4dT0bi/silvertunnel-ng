package org.silvertunnel_ng.netlib.adapter.httpclient;

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.NetSocket2Socket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.mock.MockNetSocket;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;

/**
 * The default class for creating plain (unencrypted) sockets.
 * <p>
 * The following parameters can be used to customize the behavior of this class:
 * <ul>
 * <li>{@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
 * </ul>
 * @author Apache HttpClient
 * @author hapke
 */
public class NetlibSocketFactory implements SocketFactory // TODO : use SchemeSocketFactory instead
{

	private final NetLayer lowerNetLayer;

	public NetlibSocketFactory(final NetLayer lowerNetLayer)
	{
		this.lowerNetLayer = lowerNetLayer;
	}
	private static final int WAIT_ENDLESS = -1;
	/**
	 * @return unconnected Socket
	 */
	@Override
	public Socket createSocket() throws IOException
	{
		return new NetSocket2Socket(new MockNetSocket(new byte[0], WAIT_ENDLESS));
	}

	/**
	 * @param sock
	 *            will be ignored; can be null
	 */
	@Override
	public Socket connectSocket(final Socket sock, 
	                            final String host, 
	                            final int port,
	                            final InetAddress localAddress, 
	                            int localPort, 
	                            final HttpParams params)
			throws IOException
	{

		if (host == null)
		{
			throw new IllegalArgumentException("Target host may not be null.");
		}
		if (params == null)
		{
			throw new IllegalArgumentException("Parameters may not be null.");
		}

		TcpipNetAddress localNetAddress = null;
		if (localAddress != null || localPort > 0)
		{
			// we need to bind explicitly
			if (localPort < 0)
			{
				localPort = 0; // indicates "any"
			}

			localNetAddress = new TcpipNetAddress(localAddress, localPort);
		}

		final int timeoutInMs = HttpConnectionParams
				.getConnectionTimeout(params);

		// open connection without explicit DNS resolution
		final Map<String, Object> localProperties = new HashMap<String, Object>();
		localProperties.put(TcpipNetLayer.TIMEOUT_IN_MS, Integer.valueOf(timeoutInMs));
		final TcpipNetAddress remoteNetAddress = new TcpipNetAddress(host, port);

		try
		{
			final NetSocket netSocket = lowerNetLayer.createNetSocket(
					localProperties, localNetAddress, remoteNetAddress);
			if (sock != null && sock instanceof NetSocket2Socket)
			{
				// change NetSocket of exiting wrapper
				final NetSocket2Socket netSocket2Socket = (NetSocket2Socket) sock;
				netSocket2Socket.setNetSocket(netSocket);
				return netSocket2Socket;
			}
			else
			{
				// create new wrapper
				return new NetSocket2Socket(netSocket);
			}

		}
		catch (final SocketTimeoutException ex)
		{
			throw new ConnectTimeoutException("Connect to " + remoteNetAddress
					+ " timed out");
		}
	}

	/**
	 * Checks whether a socket connection is secure. This factory creates plain
	 * socket connections which are not considered secure.
	 * 
	 * @param sock
	 *            the connected socket
	 * 
	 * @return <code>false</code>
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument is invalid
	 */
	@Override
	public final boolean isSecure(final Socket sock) throws IllegalArgumentException
	{

		if (sock == null)
		{
			throw new IllegalArgumentException("Socket may not be null.");
		}
		// This check is performed last since it calls a method implemented
		// by the argument object. getClass() is final in java.lang.Object.
		if (sock.isClosed())
		{
			throw new IllegalArgumentException("Socket is closed.");
		}
		return false;
	}
}
