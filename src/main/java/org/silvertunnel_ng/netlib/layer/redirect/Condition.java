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

package org.silvertunnel_ng.netlib.layer.redirect;

import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.util.IpNetAddress;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;

/**
 * Pattern-NetLayer pair used to configured ConditionalNetLayer.
 * 
 * @author hapke
 */
public class Condition
{
	private final Pattern pattern;
	private final NetLayer netLayer;

	/**
	 * Create a condition that exactly maps the provided IP address to the
	 * provided NetLayer.
	 * 
	 * @param ipNetAddress
	 * @param netLayer
	 */
	public Condition(IpNetAddress ipNetAddress, NetLayer netLayer)
	{
		this(ipNetAddress.getIpaddressAsString(), netLayer);
	}

	/**
	 * Create a condition that exactly maps the String (IP address or host name)
	 * to the provided NetLayer.
	 * 
	 * @param hostnameOrIpAddress
	 * @param netLayer
	 */
	public Condition(String hostnameOrIpAddress, NetLayer netLayer)
	{
		this(Pattern.compile("^" + Pattern.quote(hostnameOrIpAddress)
				+ ":(\\d{1,5})$"), netLayer);
	}

	/**
	 * Create a condition that exactly maps the provided TCP/IP
	 * address/name/port to the provided NetLayer.
	 * 
	 * @param tcpipNetAddress
	 * @param netLayer
	 */
	public Condition(TcpipNetAddress tcpipNetAddress, NetLayer netLayer)
	{
		this(Pattern.compile("^" + "(("
				+ Pattern.quote(tcpipNetAddress.getIpaddressAndPort()) + ")|"
				+ " (" + Pattern.quote(tcpipNetAddress.getHostnameAndPort())
				+ "))" + "$"), netLayer);
	}

	/**
	 * @param pattern
	 * @param netLayer
	 */
	public Condition(Pattern pattern, NetLayer netLayer)
	{
		this.pattern = pattern;
		this.netLayer = netLayer;
	}

	@Override
	public String toString()
	{
		return pattern + "-" + netLayer;
	}

	// /////////////////////////////////////////////////////
	// generated getters
	// /////////////////////////////////////////////////////

	public Pattern getPattern()
	{
		return pattern;
	}

	public NetLayer getNetLayer()
	{
		return netLayer;
	}
}
