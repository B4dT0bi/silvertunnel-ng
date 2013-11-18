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

package org.silvertunnel_ng.netlib.api;

/**
 * Interface which just holds IDs of all {@link NetLayer}s.
 * 
 * @author hapke
 * @author Tobias Boese
 *
 */
public enum NetLayerIDs
{
	/** No-Operation {@link NetLayer}. */
	NOP("nop"),
	
	/** TCP/IP {@link NetLayer}. */
	TCPIP("tcpip"),
	
	/** TLS over TCP/IP {@link NetLayer}. */
	TLS_OVER_TCPIP("tls_over_tcpip"),
	
	/** SOCKS over TCP/IP {@link NetLayer}. */
	SOCKS_OVER_TCPIP("socks_over_tcpip"),
	
	/** TOR over TLS over TCP/IP {@link NetLayer}. */
	TOR_OVER_TLS_OVER_TCPIP("tor_over_tls_over_tcpip"),
	
	/** TOR {@link NetLayer}. */
	TOR(TOR_OVER_TLS_OVER_TCPIP.getValue()),
	
	/** TOR over TLS over TCP/IP {@link NetLayer}. */
	SOCKS_OVER_TOR_OVER_TLS_OVER_TCPIP("socks_over_tor_over_tls_over_tcpip"),
	
	/** TOR over TLS over TCP/IP {@link NetLayer}. */
	SOCKS_OVER_TOR(SOCKS_OVER_TOR_OVER_TLS_OVER_TCPIP.getValue()),
	
	/** MOCK {@link NetLayer}. (used for JUnit tests)*/
	MOCK("mock"),
	
	/** modify_over_tcpip {@link NetLayer}. (used for JUnit tests)*/
	MODIFY_OVER_TCPIP("modify_over_tcpip"),
	
	/** modify_over_echo {@link NetLayer}. (used for JUnit tests)*/
	MODIFY_OVER_ECHO("modify_over_echo"),
	
	/** not used. just for error handling.*/
	UNKNOWN("unknown");
	
	/** internal String representation. */
	private String internalValue;
	
	private NetLayerIDs(final String value)
	{
		internalValue = value;
	}
	/**
	 * @return the internal {@link String} representation.
	 */
	public String getValue()
	{
		return internalValue;
	}
	/**
	 * Get a {@link NetLayerIDs} by a given {@link String}.
	 * @param value the {@link String}
	 * @return a {@link NetLayerIDs} or UNKNOWN when not found
	 */
	public static NetLayerIDs getByValue(final String value)
	{
		for (NetLayerIDs netLayer : NetLayerIDs.values())
		{
			if (value.equals(netLayer.getValue()))
			{
				return netLayer;
			}
		}
		return UNKNOWN;
	}
}
