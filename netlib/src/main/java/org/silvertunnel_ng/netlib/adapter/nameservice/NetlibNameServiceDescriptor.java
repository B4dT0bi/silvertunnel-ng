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

import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.mock.NopNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.redirect.SwitchingNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

/**
 * Constructs the NameServiceNetlibJava5/6 and NameServiceNetlibAdapter and
 * returns references to it. Intended for use by java.net.InetAddress static
 * initializer.
 * 
 * This class assigns the name of this name service provider: "dns" -&gt;
 * "NetlibNameService" -- which is "dns,NetlibNameService".
 * 
 * Thanks to Roman Kuzmik for its article
 *
 * link http://rkuzmik.blogspot.com/2006/08/local-managed-dns-java_11.html
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class NetlibNameServiceDescriptor implements NameServiceDescriptor
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NetlibNameServiceDescriptor.class);

	public static final String DNS_PROVIDER_NAME = "NetlibNameService";

	/** NetlibNameService instance. */
	private static NameService nameService;

	private static SwitchingNetAddressNameService switchingNetAddressNameService;

	/**
	 * Create and store a NetlibNameService instance.
	 */
	static
	{
		try
		{
			LOG.info("NetlibNameServiceDescriptor#static called");

			NetAddressNameService firstNetAddressNameService = null;
			final String firstNetAddressNameServiceName = System.getProperty("org.silvertunnel_ng.netlib.nameservice");
			if (firstNetAddressNameServiceName != null)
			{
				try
				{
					firstNetAddressNameService = (NetAddressNameService) Class
							.forName(firstNetAddressNameServiceName)
							.getConstructor().newInstance();
				}
				catch (final Exception e)
				{
					LOG.warn("could not instantiate org.silvertunnel_ng.netlib.nameservice={}"
							, firstNetAddressNameServiceName
							, e);
				}
			}
			if (firstNetAddressNameService == null)
			{
				// use default
				firstNetAddressNameService = new NopNetAddressNameService();
			}

			// create switcher to be able to change the
			// NetAddressNameServiceName later
			switchingNetAddressNameService = new SwitchingNetAddressNameService(firstNetAddressNameService);

			// initialize name service provider
			// if (JavaVersion.getJavaVersion()==JavaVersion.JAVA_1_5) {
			// adapter compatible with Java 1.5
			// nameService = new NameServiceNetlibJava5(new
			// NameServiceNetlibAdapter(switchingNetAddressNameService));
			// } else {
			// adapter compatible with Java 1.6 or higher
			nameService = new NameServiceNetlibJava6(new NameServiceNetlibAdapter(switchingNetAddressNameService));
			// }

		}
		catch (final Throwable t)
		{
			LOG.error("NetlibNameServiceDescriptor initialization failed", t);
		}
	}

	/**
	 * Intended for use by java.net.InetAddress static initializer.
	 * 
	 * @return "dns"
	 */
	@Override
	public String getType()
	{
		LOG.debug("NetlibNameServiceDescriptor.getType() called");
		return "dns";
	}

	/**
	 * Intended for use by java.net.InetAddress static initializer.
	 * 
	 * @return name of the NetlibNameService
	 */
	@Override
	public String getProviderName()
	{
		LOG.debug("NetlibNameServiceDescriptor.getProviderName() called");
		return DNS_PROVIDER_NAME;
	}

	/**
	 * Intended for use by java.net.InetAddress static initializer.
	 * 
	 * @return a reference to the name service provider that was already created
	 *         as class load time
	 */
	@Override
	public NameService createNameService()
	{
		LOG.debug("NetlibNameServiceDescriptor.createNameService() called");
		return nameService;
	}

	/**
	 * @return the SwitchingNetAddressNameService to be able to switch the lower
	 *         NetAddressNameService
	 */
	public static SwitchingNetAddressNameService getSwitchingNetAddressNameService()
	{
		return switchingNetAddressNameService;
	}

}
