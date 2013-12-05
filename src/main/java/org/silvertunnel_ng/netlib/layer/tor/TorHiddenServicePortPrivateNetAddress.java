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

package org.silvertunnel_ng.netlib.layer.tor;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Address information of a hidden service needed to create such service (server
 * side).
 * 
 * Inclusive private key of the hidden service. Inclusive port number.
 * 
 * Used by TorNetLayer.
 * 
 * @author hapke
 */
public class TorHiddenServicePortPrivateNetAddress implements NetAddress
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorHiddenServicePortPrivateNetAddress.class);

	/** private and public key. */
	private final TorHiddenServicePrivateNetAddress torHiddenServicePrivateNetAddress;
	private final int port;

	private static final int MIN_PORT = 0;
	private static final int MAX_PORT = 65535;

	/**
	 * Create a new object.
	 * 
	 * @param torHiddenServicePrivateNetAddress
	 *            public and private key
	 * @param port
	 *            port number
	 */
	public TorHiddenServicePortPrivateNetAddress(final TorHiddenServicePrivateNetAddress torHiddenServicePrivateNetAddress, final int port)
	{
		this.torHiddenServicePrivateNetAddress = torHiddenServicePrivateNetAddress;
		this.port = port;

		checkThis();
	}

	/**
	 * Called form constructor.
	 * 
	 * @throws IllegalArgumentException
	 */
	private void checkThis() throws IllegalArgumentException
	{
		if (port < MIN_PORT || port > MAX_PORT)
		{
			throw new IllegalArgumentException("port=" + port + " is out of range");
		}
	}

	/**
	 * @return 20 bytes long hash value
	 */
	public byte[] getPublicKeyHash()
	{
		return torHiddenServicePrivateNetAddress.getPublicKeyHash();
	}

	/**
	 * @return host name of the hidden service, like "duskgytldkxiuqc6.onion",
	 *         derived from the hidden service public key.
	 */
	public String getPublicOnionHostname()
	{
		return torHiddenServicePrivateNetAddress.getPublicOnionHostname();
	}

	/**
	 * @return public address to access this hidden service
	 */
	public TcpipNetAddress getPublicTcpipNetAddress()
	{
		return new TcpipNetAddress(getPublicOnionHostname(), getPort());
	}

	/**
	 * @return a unique id
	 */
	protected String getId()
	{
		return "TorHiddenServicePrivateNetAddress(hostname="
				+ getPublicOnionHostname() + ",port=" + port + ")";
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
		if (obj == null || !(obj instanceof TorHiddenServicePortPrivateNetAddress))
		{
			return false;
		}

		final TorHiddenServicePortPrivateNetAddress other = (TorHiddenServicePortPrivateNetAddress) obj;
		return getId().equals(other.getId());
	}

	// /////////////////////////////////////////////////////
	// generated getters
	// /////////////////////////////////////////////////////

	public TorHiddenServicePrivateNetAddress getTorHiddenServicePrivateNetAddress()
	{
		return torHiddenServicePrivateNetAddress;
	}

	public int getPort()
	{
		return port;
	}
}
