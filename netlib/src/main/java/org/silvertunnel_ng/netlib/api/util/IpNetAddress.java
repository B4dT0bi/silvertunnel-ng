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

package org.silvertunnel_ng.netlib.api.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP address.
 * 
 * Used by TcpipNetAddress.
 * 
 * @author hapke
 */
public class IpNetAddress implements NetAddress
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(IpNetAddress.class);

	/** 4 bytes for IPv4 or 16bytes for IPv6. */
	private byte[] ipaddress;

	/** pattern of IP4 address. */
	private static Pattern ip4Pattern;

	/**
	 * Initialize in a way that exceptions get logged.
	 */
	static
	{
		try
		{
			ip4Pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)",
					Pattern.DOTALL + Pattern.CASE_INSENSITIVE
							+ Pattern.UNIX_LINES);
		}
		catch (final Exception e)
		{
			LOG.error("could not initialze class AuthorityKeyCertificate", e);
		}
	}

	/**
	 * Create a new object based on a String.
	 * 
	 * Examples: 127.0.0.1 -&gt; IPv4 address 127.0.0.1 [::1/128] -&gt; IPv6 address
	 * ::1/128 (not yet implemented)
	 * 
	 * @param ipAddressStr
	 *            String to interpret
	 * @throws IllegalArgumentException
	 *             if the argument could not be parsed or is invalid
	 */
	public IpNetAddress(final String ipAddressStr) throws IllegalArgumentException
	{
		// is it a IPv4 address?
		try
		{
			final Matcher m = ip4Pattern.matcher(ipAddressStr);
			if (m.find())
			{
				ipaddress = new byte[4];
				for (int i = 0; i < 4; i++)
				{
					ipaddress[i] = (byte) Integer.parseInt(m.group(i + 1));
				}
			}
		}
		catch (final Exception e)
		{
			throw new IllegalArgumentException("could not parse IPv4 address="
					+ ipAddressStr);
		}
	}

	/**
	 * Create a new object.
	 * 
	 * @param ipaddress
	 *            4 bytes for IPv4 or 16 bytes for IPv6
	 * @throws IllegalArgumentException
	 *             if the argument could not be parsed or is invalid
	 */
	public IpNetAddress(final byte[] ipaddress) throws IllegalArgumentException
	{
		if (ipaddress != null)
		{
			if (ipaddress.length == 4)
			{
				// IPv4
				this.ipaddress = ipaddress;
			}
			else if (ipaddress.length == 16)
			{
				// IPv6
				this.ipaddress = ipaddress;
			}
			else
			{
				throw new IllegalArgumentException(
						"invalid IP address length (" + ipaddress.length
								+ " bytes )");
			}
		}
	}

	/**
	 * Create a new object.
	 * 
	 * @param inetAddress
	 *            4 bytes for IPv4 or 16 bytes for IPv6
	 * @throws IllegalArgumentException
	 *             if the argument could not be parsed or is invalid
	 */
	public IpNetAddress(final InetAddress inetAddress) throws IllegalArgumentException
	{
		if (inetAddress != null)
		{
			if (inetAddress instanceof Inet4Address)
			{
				// IPv4
				this.ipaddress = ((Inet4Address) inetAddress).getAddress();
			}
			else if (ipaddress.length == 16)
			{
				// IPv6
				this.ipaddress = ((Inet6Address) inetAddress).getAddress();
			}
			else
			{
				throw new IllegalArgumentException("invalid inet address=" + inetAddress);
			}
		}
	}

	public byte[] getIpaddress()
	{
		return ipaddress;
	}

	public String getIpaddressAsString()
	{
		if (ipaddress == null)
		{
			return null;
		}
		else if (ipaddress.length == 4)
		{
			return getByteAsNonnegativeInt(ipaddress[0]) + "."
					+ getByteAsNonnegativeInt(ipaddress[1]) + "."
					+ getByteAsNonnegativeInt(ipaddress[2]) + "."
					+ getByteAsNonnegativeInt(ipaddress[3]);
		}
		else
		{
			// IPv6
			// StringBuffer result = new StringBuffer();
			// TODO: implement IPv6 toString
			return ":IPv6:" + ipaddress;
		}
	}

	private int getByteAsNonnegativeInt(final byte b)
	{
		if (b >= 0)
		{
			return b;
		}
		else
		{
			return 256 + b;
		}
	}

	public InetAddress getIpaddressAsInetAddress()
	{
		if (ipaddress == null)
		{
			// no address set
			return null;
		}
		else
		{
			// address set
			try
			{
				return InetAddress.getByAddress(ipaddress);
			}
			catch (final UnknownHostException e)
			{
				LOG.warn("could not convert into InetAddress: {}", toString(), e);
				return null;
			}
		}
	}

	/**
	 * @return a unique id
	 */
	protected String getId()
	{
		return "IpNetAddress(ipaddress=" + getIpaddressAsString() + ")";
	}

	@Override
	public String toString()
	{
		return getId();
	}

	@Override
	public int hashCode()
	{
		return getId().hashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null || !(obj instanceof IpNetAddress))
		{
			return false;
		}

		final IpNetAddress other = (IpNetAddress) obj;
		return getId().equals(other.getId());
	}
}
