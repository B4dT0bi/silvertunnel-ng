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

package org.silvertunnel_ng.netlib.nameservice.inetaddressimpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to the JDK built-in name service (java.net.InetAddressImpl and Co.).
 * 
 * The result does not use/depend on settings of the NameServiceGlobalUtil.
 * 
 * @author hapke
 */
public class DefaultIpNetAddressNameService implements NetAddressNameService
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DefaultIpNetAddressNameService.class);

	/** reference to the underlying InetAddressImpl object used for reflection. */
	private Object inetAddressImpl;
	/** reference to the underlying InetAddressImpl object used for reflection. */
	private Method lookupAllHostAddrMethod;
	/** reference to the underlying InetAddressImpl object used for reflection. */
	private Method getHostByAddrMethod;

	private static final String TEST_HOSTNAME = "localhost";
	private static final IpNetAddress TEST_IP = new IpNetAddress("127.0.0.1");

	/** a singleton instance of this class. */
	private static DefaultIpNetAddressNameService instance;

	/**
	 * Initialize access to the JDK built-in name service.
	 * 
	 * @throws UnsupportedOperationException
	 *             if this class cannot work (e.g. because of security
	 *             restrictions)
	 */
	@SuppressWarnings("unchecked")
	public DefaultIpNetAddressNameService()
			throws UnsupportedOperationException
	{
		// this solution works for Java 1.6, but it does break security
		// restrictions:
		try
		{
			// determine the underlying class
			String inetAddressImplClassName;
			if (isIPv6Supported())
			{
				inetAddressImplClassName = "java.net.Inet6AddressImpl";
			}
			else
			{
				inetAddressImplClassName = "java.net.Inet4AddressImpl";
			}

			// load native "net" library ( AccessController.doPrivileged(new
			// LoadLibraryAction("net")); )
			// do not initialize class InetAddress (e.g. with
			// InetAddress.getByAddress(new byte[4]); )
			// to avoid side effects in the static initializer of this class
			try
			{
				// use class NetworkInterface instead to load native "net"
				// library
				NetworkInterface.getByName(null);
			}
			catch (final NullPointerException e)
			{
				// NPE is expected here
//				LOG.debug("got NullPointerException (expected)", e);
			}

			// prepare reflection
			@SuppressWarnings("rawtypes")
			final Class clazz = Class.forName(inetAddressImplClassName);
			@SuppressWarnings("rawtypes")
			final Constructor constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			inetAddressImpl = constructor.newInstance();
			lookupAllHostAddrMethod = clazz.getDeclaredMethod("lookupAllHostAddr", String.class);
			lookupAllHostAddrMethod.setAccessible(true);
			getHostByAddrMethod = clazz.getDeclaredMethod("getHostByAddr", new byte[0].getClass());
			getHostByAddrMethod.setAccessible(true);

			// test the reflection
			checkThatReflectionWorks();

		}
		catch (final UnsupportedOperationException e)
		{
			LOG.error("error during initialization (1)", e);
			throw e;

		}
		catch (final Throwable e)
		{
			LOG.error("error during initialization (2)", e);
			throw new UnsupportedOperationException(
					"error during initialization (2)", e);
		}
	}

	/**
	 * Check that (the reflection of) the methods getAddresses() and getNames()
	 * work(s).
	 * 
	 * @throws UnsupportedOperationException
	 *             if the execution does not work (e.g. because of security
	 *             restrictions)
	 */
	private void checkThatReflectionWorks()
			throws UnsupportedOperationException
	{
		try
		{
			// test that the reflection works
			getAddressesByName(TEST_HOSTNAME);
			getNamesByAddress(TEST_IP);

		}
		catch (final UnknownHostException e)
		{
			// this exception can occur in unconventional environments:
			// ignore it
			LOG.debug("got UnknownHostException", e);
		}
	}

	/**
	 * @return true=IPv6 is supported; false=IPv6 is not supported.
	 */
	private static boolean isIPv6Supported()
	{
		return false;
	}

	/**
	 * @see NetAddressNameService#getAddressesByName(String)
	 * 
	 * @param hostname
	 *            host name to lookup
	 * @return one or more addresses that match; ordered by relevance, i.e.
	 *         prefer to use the first element of the array
	 * @throws UnknownHostException
	 *             if the resolution failed
	 * @throws UnsupportedOperationException
	 *             if the operation could not be executed (because of internal
	 *             errors)
	 */
	@Override
	public NetAddress[] getAddressesByName(final String hostname)
			throws UnknownHostException, UnsupportedOperationException
	{
		// check parameter
		if (hostname == null)
		{
			throw new UnknownHostException("hostname=null");
		}

		// action
		try
		{
			final Object inetAddressesObj = lookupAllHostAddrMethod.invoke(inetAddressImpl, hostname);
			if (inetAddressesObj == null)
			{
				// no result
				throw new UnknownHostException("hostname=" + hostname
						+ " could not be resolved");
			}
			else if (inetAddressesObj instanceof byte[][])
			{

				// this seams to be Java 1.5
				final byte[][] inetAddresses = (byte[][]) inetAddressesObj;
				final NetAddress[] result = new NetAddress[inetAddresses.length];
				for (int i = 0; i < inetAddresses.length; i++)
				{
					result[i] = new IpNetAddress(inetAddresses[i]);
				}
				return result;

			}
			else
			{

				// this is Java 1.6 or higher
				final InetAddress[] inetAddresses = (InetAddress[]) inetAddressesObj;
				final NetAddress[] result = new NetAddress[inetAddresses.length];
				for (int i = 0; i < inetAddresses.length; i++)
				{
					result[i] = new IpNetAddress(inetAddresses[i]);
				}
				return result;
			}

		}
		catch (final UnknownHostException e)
		{
			throw e;

		}
		catch (final InvocationTargetException e)
		{
			if (e.getTargetException() instanceof UnknownHostException)
			{
				// re-throw the original exception
				throw (UnknownHostException) e.getTargetException();
			}
			throw new UnsupportedOperationException(
					"resolution failed (1) for hostname=" + hostname, e);

		}
		catch (final Exception e)
		{
			throw new UnsupportedOperationException(
					"resolution failed (2) for hostname=" + hostname, e);
		}
	}

	/**
	 * @see NetAddressNameService#getNames
	 * 
	 * @param ipaddress
	 *            IP address to lookup
	 * @return the host name that matches (array is always of size 1)
	 * @throws UnknownHostException
	 *             if the resolution failed
	 * @throws UnsupportedOperationException
	 *             if the operation could not be executed (because of internal
	 *             errors)
	 */
	@Override
	public String[] getNamesByAddress(final NetAddress ipaddress)
			throws UnknownHostException, UnsupportedOperationException
	{
		if (ipaddress == null)
		{
			throw new UnknownHostException("ipaddress=null");
		}
		if (!(ipaddress instanceof IpNetAddress))
		{
			throw new UnknownHostException(
					"ipaddress is not of type IpNetAddress: " + ipaddress);
		}
		final IpNetAddress ipNetAddress = (IpNetAddress) ipaddress;

		// action
		try
		{
			final String hostname = (String) getHostByAddrMethod.invoke(
					inetAddressImpl, ipNetAddress.getIpaddress());
			if (hostname == null)
			{
				throw new UnknownHostException("ipaddress=" + ipNetAddress
						+ " could not be resolved");
			}
			else
			{
				// the result
				return new String[] { hostname };
			}

		}
		catch (final UnknownHostException e)
		{
			throw e;

		}
		catch (final InvocationTargetException e)
		{
			if (e.getTargetException() instanceof UnknownHostException)
			{
				// the
																		// original
																		// exception
				throw (UnknownHostException) e.getTargetException();
			}
			throw new UnsupportedOperationException(
					"resolution failed (1) ipaddress=" + ipaddress, e);

		}
		catch (final Exception e)
		{
			throw new UnsupportedOperationException(
					"resolution failed (2) ipaddress=" + ipaddress, e);
		}
	}

	/**
	 * @return a singleton instance of this class
	 */
	public static synchronized DefaultIpNetAddressNameService getInstance()
	{
		if (instance == null)
		{
			instance = new DefaultIpNetAddressNameService();
		}

		return instance;
	}
}
