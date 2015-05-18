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

package org.silvertunnel_ng.netlib.api.impl;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrap a NetSocket to be a SocketImpl.
 * 
 * @author hapke
 */
public class NetSocket2SocketImpl extends SocketImpl
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NetSocket2SocketImpl.class);

	public static final String TCPIP_NET_LAYER_TIMEOUT_IN_MS = "TcpipNetLayer.timeoutInMs";

	/**
	 * Either the netSocket or the netLayer will be initialized in the
	 * constructor. if no netSocket is set in the constructor, then it will be
	 * set in method connect().
	 */
	private NetSocket netSocket;
	/**
	 * either the netSocket or the netLayer will be initialized in the
	 * constructor.
	 */
	private NetLayer netLayer;

	private static final int DEFAULT_INPUTSTREAM_TIMEOUT = 60000;
	private int inputStreamTimeout = DEFAULT_INPUTSTREAM_TIMEOUT;
	private static final Boolean DEFAULT_TCP_NODELAY = true; // false;
	private Boolean tcpNodelay = DEFAULT_TCP_NODELAY;
	private static final Integer DEFAULT_SO_LINGER = 0;
	private Integer soLinger = DEFAULT_SO_LINGER;

	private SocketTimeoutInputStream inputStream;

	/**
	 * @param netSocket
	 *            connect using this NetSocket
	 */
	public NetSocket2SocketImpl(final NetSocket netSocket)
	{
		FileDescriptor fd = new FileDescriptor();
		this.fd = fd;
		this.netSocket = netSocket;
	}

	/**
	 * @param netLayer
	 *            connect using a new NetSocket created by this NetLayer
	 */
	public NetSocket2SocketImpl(final NetLayer netLayer)
	{
		FileDescriptor fd = new FileDescriptor();
		this.fd = fd;
		this.netLayer = netLayer;
	}

	/**
	 * Set or change the NetSocket used by this object.
	 * 
	 * @param netSocket
	 */
	public void setNetSocket(final NetSocket netSocket)
	{
		this.netSocket = netSocket;
	}

	@Override
	protected void accept(final SocketImpl arg0) throws IOException
	{
		LOG.warn("method empty/not implemented", new Throwable("method empty/not implemented"));
		// TODO Auto-generated method stub
	}

	@Override
	protected int available() throws IOException
	{
		LOG.warn("method empty/not implemented", new Throwable("method empty/not implemented"));
		return 0;
	}

	@Override
	protected void bind(final InetAddress arg0, final int arg1)
			throws IOException
	{
		// do nothing
	}

	@Override
	protected void close() throws IOException
	{
		if (netSocket != null)
		{
			netSocket.close();
		}
		else
		{
			LOG.info("close() with netSocket=null", new Throwable("Just to dump a trace"));
		}
	}

	@Override
	protected void connect(final String remoteHost, final int port)
			throws IOException
	{
		connect(new InetSocketAddress(remoteHost, port), 0);
	}

	@Override
	protected void connect(final InetAddress remoteAddress, final int port)
			throws IOException
	{
		connect(new InetSocketAddress(remoteAddress, port), 0);
	}

	/**
	 * Connects the Socket to the specified remoteAddress.
	 * 
	 * @param remoteAddress
	 *            the <code>SocketAddress</code> to connect to.
	 * @param timeoutInMs
	 *            the timeout value in milliseconds, or zero for no timeout.
	 * @throws IOException
	 *             if the connection can't be established.
	 * @throws IllegalArgumentException
	 *             if remoteAddress is null or a SocketAddress subclass not
	 *             supported by this socket
	 */
	@Override
	protected void connect(final SocketAddress remoteAddress,
			final int timeoutInMs) throws IOException
	{
		if (netSocket != null)
		{
			// already connected
			return;
		}

		//
		// connect = create a new NetSocket based on the newLayer
		//

		// checks
		if (netLayer == null)
		{
			throw new IllegalStateException("netLayer not set");
		}
		LOG.debug("method empty implemented");
		if (remoteAddress == null
				|| !(remoteAddress instanceof InetSocketAddress))
		{
			throw new IllegalArgumentException("Unsupported address type");
		}

		// convert address
		final InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) remoteAddress;
		TcpipNetAddress remoteNetAddress;
		if (remoteInetSocketAddress.isUnresolved())
		{
			remoteNetAddress = new TcpipNetAddress(
					remoteInetSocketAddress.getHostName(),
					remoteInetSocketAddress.getPort());
		}
		else
		{
			remoteNetAddress = new TcpipNetAddress(remoteInetSocketAddress
					.getAddress().getHostAddress(),
					remoteInetSocketAddress.getPort());
		}

		// create NetSocket object
		final Map<String, Object> localProperties = new HashMap<String, Object>();
		localProperties.put(TCPIP_NET_LAYER_TIMEOUT_IN_MS, Integer.valueOf(timeoutInMs));
		final TcpipNetAddress localNetAddress = null;
		netSocket = netLayer.createNetSocket(localProperties, localNetAddress,
				remoteNetAddress);
	}

	@Override
	protected void create(final boolean arg0) throws IOException
	{
		// do nothing here
		// if netSocket is not yet initialized then initialize it in method
		// connect()
	}

	@Override
	protected synchronized InputStream getInputStream() throws IOException
	{
		if (inputStream == null)
		{
			// create new wrapper
			inputStream = new SocketTimeoutInputStream(
					netSocket.getInputStream(), inputStreamTimeout);
		}
		return inputStream;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException
	{
		return netSocket.getOutputStream();
	}

	@Override
	protected void listen(final int arg0) throws IOException
	{
		LOG.warn("method empty/not implemented", new Throwable("method empty/not implemented"));
		// TODO Auto-generated method stub
	}

	@Override
	protected void sendUrgentData(final int arg0) throws IOException
	{
		LOG.warn("method empty/not implemented", new Throwable("method empty/not implemented"));
		// TODO Auto-generated method stub
	}

	@Override
	public Object getOption(final int key) throws SocketException
	{
		if (key == SocketOptions.SO_TIMEOUT)
		{
			// get timeout
			return inputStreamTimeout;
		}
		if (key == SocketOptions.TCP_NODELAY)
		{
			return tcpNodelay;
		}
		if (key == SocketOptions.SO_LINGER)
		{
			// get timeout
			return soLinger;
		}

		// unsupported option
		final String msg = "no implementation for getOption(" + key
				+ "). List of all options in java.net.SocketOptions.";
		if (LOG.isDebugEnabled())
		{
			LOG.debug(msg, new Throwable("method not completely implemented"));
		}
		else
		{
			LOG.info(msg + " - Log with level=debug to get call hierarchy.");
		}
		return null;
	}

	@Override
	public synchronized void setOption(final int key, final Object value)
			throws SocketException
	{
		LOG.debug("setOption(key={},value={})", key, value);
		if (key == SocketOptions.SO_TIMEOUT)
		{
			// set timeout
			if (value instanceof Integer)
			{
				inputStreamTimeout = (Integer) value;
				if (inputStream != null)
				{
					inputStream.setTimeout(inputStreamTimeout);
				}
			}
			else
			{
				LOG.warn("ignored value of wrong type of setOption(key={}"
						+ ",value={}"
						+ "). List of all options in java.net.SocketOptions.", key, value);
			}
			return;
		}
		if (key == SocketOptions.TCP_NODELAY)
		{
			// ignore the value but keep it for later get operations
			if (value instanceof Boolean)
			{
				tcpNodelay = (Boolean) value;
			}
			else
			{
				LOG.warn("ignored value of wrong type of setOption(key={}"
						+ ",value={}). List of all options in java.net.SocketOptions.", key, value);
			}
			return;
		}
		if (key == SocketOptions.SO_TIMEOUT)
		{
			// ignore the value but keep it for later get operations
			if (value instanceof Integer)
			{
				soLinger = (Integer) value;
			}
			else
			{
				LOG.warn("ignored value of wrong type of setOption(key={}"
						+ ",value={}). List of all options in java.net.SocketOptions.", key, value);
			}
			return;
		}

		// unsupported option
		final String msg = "no implementation for setOption(key=" + key
				+ ",value=" + value
				+ "). List of all options in java.net.SocketOptions.";
		if (LOG.isDebugEnabled())
		{
			LOG.debug(msg, new Throwable("method not completely implemented"));
		}
		else
		{
			LOG.warn(msg + " Log with level=debug to get call hierarchy.");
		}
	}
}
