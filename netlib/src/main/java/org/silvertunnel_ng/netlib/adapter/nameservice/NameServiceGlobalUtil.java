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

package org.silvertunnel_ng.netlib.adapter.nameservice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.cache.CachingNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.mock.NopNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.net.spi.nameservice.NameService;

/**
 * This class allows modification of the JVM global socket handling.
 * 
 * This class contains Java version specific code and does maybe not always
 * work! Detailed description:
 * http://sourceforge.net/apps/trac/silvertunnel/wiki
 * /Netlib+Name+Service+Adapter
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class NameServiceGlobalUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NameServiceGlobalUtil.class);

	private static boolean initialized = false;
	private static boolean initializedWithSuccess = false;

	/**
	 * time to circumvent caching (maximum of networkaddress.cache.ttl and
	 * networkaddress.cache.negative.ttl) in milliseconds
	 * 
	 * 10 seconds + a bit because Windows JVMs need a bit more.
	 */
	private static final long CACHE_TIMEOUT_MILLIS = 11000;

	/**
	 * Initialize the NameService of class java.net.InetAddress.
	 * 
	 * This method call influences the complete Java JVM.
	 * 
	 * This method can be called multiple times without any problems, but it
	 * must be called before the first usage/before the class initialization of
	 * class java.net.InetAddress.
	 * 
	 * If this method cannot be called before initialization of class
	 * java.net.InetAddress we have a (first quality) way: try to call this
	 * method in the static initializer of your calling class.
	 * 
	 * If this method cannot be called before initialization of class
	 * java.net.InetAddress we have a (second quality) alternative: set the
	 * following system properties when starting the JVM (e.g. with use of
	 * "-Dkey=value" command line arguments):
	 * sun.net.spi.nameservice.provider.1=dns,NetlibNameService (properties to
	 * disable name service caching,set TTL to 0) Disabling the name service
	 * caching is needed to be able to change the lower NetAddressNameService.
	 * 
	 * The first lower NetAddressNameService is NopNetAddressNameService and NOT
	 * {@link DefaultIpNetAddressNameService} +
	 * {@link CachingNetAddressNameService} i.e. all name service requests will
	 * fail (and NOT behave, from the user perspective, as before calling this
	 * method).
	 * 
	 * @throws IllegalStateException
	 *             if the method call came too late after JVM start, i.e. class
	 *             java.net.InetAddress was already initialized before calling
	 *             this method.
	 */
	public static synchronized void initNameService()
			throws IllegalStateException
	{
		// already initialized?
		if (initialized)
		{
			// yes: set NopNetlibNameService if not already set
			if (!isNopNetAddressNameServiceInstalled())
			{
				setIpNetAddressNameService(NopNetAddressNameService.getInstance());
			}
			LOG.debug("initialized");
			
		}
		else
		{
			// no: initialize now

			// specify that the DNS will be provided by Netlib
			System.setProperty("sun.net.spi.nameservice.provider.1", "dns,"
					+ NetlibNameServiceDescriptor.DNS_PROVIDER_NAME);

			// disable caching as good as possible - needed to be able to switch
			// name service implementations:
			System.setProperty("sun.net.inetaddr.ttl", "0");
			System.setProperty("sun.net.inetaddr.negative.ttl", "0");
			// in most Java/JRE environments negative.ttl cannot be changed
			// because of an higher priority of entries in file
			// jre/lib/security/java.security:
			// #networkaddress.cache.ttl=-1
			// networkaddress.cache.negative.ttl=10
			// better would be entry in file jre/lib/security/java.security:
			// networkaddress.cache.ttl=0 and
			// networkaddress.cache.negative.ttl=0
			// or no entry at all in this file

			/*
			 * Currently, we do NOT specify which NetAddressNameService will be
			 * used first (class must have default constructor without
			 * arguments), Example would be:
			 * System.setProperty("org.silvertunnel_ng.netlib.nameservice",
			 * "org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService"
			 * );
			 * 
			 * Instead, we omit this system property and use
			 * org.silvertunnel_ng.
			 * org.silvertunnel_ng.netlib.nameservice.mock.NopNetAddressNameService
			 */
			//System.setProperty(
			//		"org.silvertunnel_ng.netlib.nameservice",
			//		"org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService");
			System.setProperty(
					"org.silvertunnel_ng.netlib.nameservice",
					"org.silvertunnel_ng.netlib.nameservice.mock.NopNetAddressNameService");
			// update status
			initialized = true;
		}

		// check that java.net.InetAddress has not be initialized yet (without
		// (Nop)NetlibNameService)
		initializedWithSuccess = isNopNetAddressNameServiceInstalled();
		if (initializedWithSuccess)
		{
			// success
			LOG.info("Installation of NameService adapter with NopNetAddressNameService was successful");
		}
		else
		{
			// the normal way didnt worked. -> try the hard way ;)
			initNameServiceHardway();
			initializedWithSuccess = isNopNetAddressNameServiceInstalled();
			if (initializedWithSuccess)
			{
				// success
				LOG.info("Installation of NameService adapter with NopNetAddressNameService was successful (hard way)");
			}
			else
			{
				// error
				final String msg = "Installation of NameService adapter with NopNetAddressNameService failed: "
						+ "probably the method NameServiceGlobalUtil.initNameService() is called too late, "
						+ "i.e. after first usage of java.net.InetAddress";
				LOG.error(msg);
				throw new IllegalStateException(msg);
			}
		}
	}
	/** save old nameservices to restore old status. */
	private static List<NameService> oldNameServices;
	/**
	 * Try to reset the values in InetAddress.nameServices the hard way.
	 */
	private static void initNameServiceHardway()
	{
		try
		{
			Field field = InetAddress.class.getDeclaredField("nameServices");
			field.setAccessible(true);
			oldNameServices = (List<NameService>) field.get(null);

			// get name service if provided and requested
			String provider = null;
			String propPrefix = "sun.net.spi.nameservice.provider.";
			int n = 1;
			List<NameService> nameServices = new ArrayList<NameService>();
			provider = System.getProperty(propPrefix + n);
			while (provider != null)
			{
				Method m = InetAddress.class.getDeclaredMethod("createNSProvider", String.class);
				m.setAccessible(true);
				NameService ns = (NameService) m.invoke(null, provider);
				if (ns != null)
				{
					nameServices.add(ns);
				}

				n++;
				provider = System.getProperty(propPrefix + n);
			}

			// if not designate any name services provider,
			// create a default one
			if (nameServices.size() == 0)
			{
				Method m = InetAddress.class.getDeclaredMethod("createNSProvider", String.class);
				m.setAccessible(true);
				NameService ns = (NameService) m.invoke(null, "default");
				nameServices.add(ns);
			}
			field.set(null, nameServices);
			
			// resetting the cache policy to NEVER
			Field fieldCache = sun.net.InetAddressCachePolicy.class.getDeclaredField("cachePolicy");
			fieldCache.setAccessible(true);
			fieldCache.set(null, 0); // TODO : save old value and reset it

			// resetting the cache policy for negative founds to NEVER
			Field fieldCacheNeg = sun.net.InetAddressCachePolicy.class.getDeclaredField("negativeCachePolicy");
			fieldCacheNeg.setAccessible(true);
			fieldCacheNeg.set(null, 0); // TODO : save old value and reset it
			
		}
		catch (Exception exception)
		{
			LOG.debug("Hardway init doesnt work. got Exception : {}", exception, exception);
		}
	}
	/**
	 * If the InetAddress class was modified by initNameServiceHardway we can try to reset the old values.
	 */
	public static void resetInetAddress()
	{
		if (oldNameServices != null)
		{
			try
			{
				Field field = InetAddress.class.getDeclaredField("nameServices");
				field.setAccessible(true);
				field.set(null, oldNameServices);
			}
			catch (Exception exception)
			{
				LOG.warn("Could not reset InetAddress due to exception", exception);
			}
		}
	}
	/**
	 * @return true = installation of NameService adapter with
	 *         NopNetAddressNameService was successful; false= installation was
	 *         not successful
	 */
	public static boolean isNopNetAddressNameServiceInstalled()
	{
		// test name service
		try
		{
			// try to use Java standard way of DNS resolution
			final InetAddress[] address = InetAddress.getAllByName("dnstest.silvertunnel-ng.org"); // if this resolves to 1.2.3.4 it is wrong
			return false;
		}
		catch (final UnknownHostException e)
		{
			// this is expected
		}

		try
		{
			final InetAddress[] address = InetAddress.getAllByName(NopNetAddressNameService.CHECKER_NAME);

			// check the expected result
			if (address == null)
			{
				LOG.error("InetAddress.getAllByName() returned null as address (but this is wrong)");
				return false;
			}
			else if (address.length != 1)
			{
				LOG.error("InetAddress.getAllByName() returned array of wrong size={}", address.length);
				return false;
			}
			else if (Arrays.equals(address[0].getAddress(), NopNetAddressNameService.CHECKER_IP[0].getIpaddress()))
			{
				// correct return value
				return true;
			}
			else
			{
				LOG.error("InetAddress.getAllByName() returned wrong IP address={}", Arrays.toString(address[0].getAddress()));
				return false;
			}
		}
		catch (final Exception e)
		{
			LOG.error("InetAddress.getAllByName() throwed unexpected excpetion={}", e, e);
			return false;
		}
	}

	/**
	 * Set a new NetAddressNameService be used as/by the java.net.InetAddress.
	 * 
	 * This method call influences the complete Java JVM.
	 * 
	 * @param lowerNetAddressNameService
	 *            the new service implementation; not not forget to embed it
	 *            into a CachingNetAddressNameService (usually we want caching
	 *            here to avoid performance problems)
	 * @throws IllegalStateException
	 *             if initSocketImplFactory() was not called before calling this
	 *             method
	 */
	public static synchronized void setIpNetAddressNameService(final NetAddressNameService lowerNetAddressNameService)
			throws IllegalStateException
	{
		if (!initialized)
		{
			throw new IllegalStateException(
					"initNameService() must be called first (but was not)");
		}

		// action
		NetlibNameServiceDescriptor.getSwitchingNetAddressNameService()
				.setLowerNetAddressNameService(lowerNetAddressNameService);
	}

	/**
	 * @return number of milliseconds to wait after a lower service switch until
	 *         the new lower service is completely active
	 */
	public static long getCacheTimeoutMillis()
	{
		return CACHE_TIMEOUT_MILLIS;
	}
	/**
	 * @return is the {@link DefaultIpNetAddressNameService} active?
	 */
	public static boolean isDefaultIpNetAddressNameServiceActive()
	{
		return NetlibNameServiceDescriptor.getSwitchingNetAddressNameService()
										  .getLowerNetAddressNameServiceClass()
										  .equals(DefaultIpNetAddressNameService.class.getCanonicalName());
	}
	/**
	 * activate the {@link DefaultIpNetAddressNameService}.
	 */
	public static void activateDefaultIpNetAddressNameService()
	{
		if (!initialized)
		{
			initNameService();
		}
		setIpNetAddressNameService(DefaultIpNetAddressNameService.getInstance());
	}
}
