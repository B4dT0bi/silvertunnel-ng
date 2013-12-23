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

package org.silvertunnel_ng.netlib.adapter.socket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.SocketImpl;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.util.JavaVersion;
import org.silvertunnel_ng.netlib.layer.mock.NopNetLayer;
import org.silvertunnel_ng.netlib.layer.tcpip.TcpipNetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows modification of the JVM global socket handling.
 * 
 * This class contains Java version specific code and does maybe not always
 * work! Detailed description:
 * http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibAdapterSocket
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SocketGlobalUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SocketGlobalUtil.class);

	private static NetlibSocketImplFactory netlibSocketImplFactory;

	/**
	 * Initialize the SocketImplFactory of class java.net.Socket.
	 * 
	 * The first lower NetLayer is {@link NopNetLayer} and NOT
	 * {@link TcpipNetLayer} i.e. all requests will fail (and do NOT behave,
	 * from the user perspective, as before calling this method).
	 * 
	 * This method call influences the complete Java JVM.
	 * 
	 * This method can be called multiple times without any problems, but
	 * Socket.setSocketImplFactory() may not be called before the first call of
	 * this method to avoid problems.
	 */
	public static synchronized void initSocketImplFactory()
	{
		if (netlibSocketImplFactory == null)
		{
			try
			{
				final NetLayer defaultNetLayer = NetFactory.getInstance()
						.getNetLayerById(NetLayerIDs.NOP);
				netlibSocketImplFactory = new NetlibSocketImplFactory(
						defaultNetLayer);
				Socket.setSocketImplFactory(netlibSocketImplFactory);
			}
			catch (final IOException e)
			{
				LOG.warn("Socket.setSocketImplFactory() was already called before,"
						+ " but not from SocketUtil, i.e. maybe the wrong factory is set");
			}
		}
	}

	/**
	 * Set a new NetLayer that will be used as/by the SocketImpl inside
	 * java.net.Socket.
	 * 
	 * This method call influences the complete Java JVM.
	 * 
	 * @param netLayer
	 *            the new NetLayer
	 * @throws IllegalStateException
	 *             if initSocketImplFactory() was not called before calling this
	 *             method
	 */
	public static synchronized void setNetLayerUsedBySocketImplFactory(final NetLayer netLayer) throws IllegalStateException
	{
		if (netlibSocketImplFactory == null)
		{
			throw new IllegalStateException(
					"initSocketImplFactory() must be called first (but was not)");
		}

		// action
		netlibSocketImplFactory.setNetLayer(netLayer);
	}

	/**
	 * Create a new Socket with the JDK built-in SocketImpl.
	 * 
	 * The result does not use/depend on a SocketImplFactory set in class
	 * Socket.
	 * 
	 * @return a new (Extended)Socket, as it would be created with new Socket()
	 *         with unset SocketImplFactory
	 * @throws RuntimeException
	 *             if the socket could not be created
	 */
	@SuppressWarnings("unchecked")
	public static ExtendedSocket createOriginalSocket() throws RuntimeException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createOriginalSocket() called here:",
					new Throwable("stacktrace for debugging - not an error"));
		}

		if (netlibSocketImplFactory == null)
		{
			// SocketImplFactory not yet set (by this class) in class Socket:
			// use default code
			return new ExtendedSocket();
		}
		else
		{
			// SocketImplFactory set in class Socket:
			// use specific special and maybe unportable code

			// 1st attempt,
			// this solution works for Java 1.6, but it does break security
			// restrictions:
			try
			{
				@SuppressWarnings("rawtypes")
				final Class clazz = Class.forName("java.net.SocksSocketImpl");
				@SuppressWarnings("rawtypes")
				final Constructor constructor = clazz.getDeclaredConstructor();
				// this maybe does not work because of security restrictions:
				constructor.setAccessible(true);
				final SocketImpl impl = (SocketImpl) constructor.newInstance();
				return new ExtendedSocket(impl);

			}
			catch (final Throwable t1)
			{
				String msg = "createOriginalSocket()#1st attempt: could not create a Socket for Java Version: "
						+ JavaVersion.getJavaVersion();
				LOG.error(msg, t1);

				// 2nd attempt,
				// this solution doen't work for Java 1.6, but it does not break
				// security restrictions:
				try
				{
					return new ExtendedSocket(new PatchedProxy());

				}
				catch (final Throwable t2)
				{
					msg = "createOriginalSocket()#2nd attempt: could not create a Socket for Java Version: "
							+ JavaVersion.getJavaVersion();
					LOG.error(msg + ", " + t2, t2);

					throw new RuntimeException(msg + ", " + t2);
				}
			}
		}
	}

}
