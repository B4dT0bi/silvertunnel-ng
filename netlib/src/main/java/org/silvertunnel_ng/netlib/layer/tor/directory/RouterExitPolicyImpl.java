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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.layer.tor.api.RouterExitPolicy;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.tool.ConvenientStreamReader;
import org.silvertunnel_ng.netlib.tool.ConvenientStreamWriter;

import java.io.IOException;

/**
 * Compound data structure for storing exit policies.
 * 
 * An object is read-only.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class RouterExitPolicyImpl implements RouterExitPolicy, Cloneable
{
	/** if false: reject. */
	private final boolean accept;
	private final long ip;
	private final long netmask;
	private final int loPort;
	private final int hiPort;

	public RouterExitPolicyImpl(final boolean accept, 
	                            final long ip, 
	                            final long netmask,
	                            final int loPort, 
	                            final int hiPort)
	{
		this.accept = accept;
		this.ip = ip;
		this.netmask = netmask;
		this.loPort = loPort;
		this.hiPort = hiPort;
	}

	/**
	 * Clone, but do not throw an exception.
	 */
	@Override
	public RouterExitPolicy cloneReliable() throws RuntimeException
	{
		try
		{
			return (RouterExitPolicy) clone();
		}
		catch (final CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		return accept + " " + Encoding.toHex(ip) + "/"
				+ Encoding.toHex(netmask) + ":" + loPort + "-" + hiPort;
	}

	// /////////////////////////////////////////////////////
	// generated getters
	// /////////////////////////////////////////////////////

	@Override
	public boolean isAccept()
	{
		return accept;
	}

	@Override
	public long getIp()
	{
		return ip;
	}

	@Override
	public long getNetmask()
	{
		return netmask;
	}

	@Override
	public int getLoPort()
	{
		return loPort;
	}

	@Override
	public int getHiPort()
	{
		return hiPort;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (accept ? 1231 : 1237);
		result = prime * result + hiPort;
		result = prime * result + (int) (ip ^ (ip >>> 32));
		result = prime * result + loPort;
		result = prime * result + (int) (netmask ^ (netmask >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof RouterExitPolicyImpl))
		{
			return false;
		}
		RouterExitPolicyImpl other = (RouterExitPolicyImpl) obj;
		if (accept != other.accept)
		{
			return false;
		}
		if (hiPort != other.hiPort)
		{
			return false;
		}
		if (ip != other.ip)
		{
			return false;
		}
		if (loPort != other.loPort)
		{
			return false;
		}
		if (netmask != other.netmask)
		{
			return false;
		}
		return true;
	}
	/**
	 * Create a new object out of a byte array.
	 * @param data the byte array containing the binary representation of a {@link RouterExitPolicyImpl} object
	 * @return a new {@link RouterExitPolicyImpl} object containing the information from the byte array
	 */
	protected static RouterExitPolicyImpl parseFrom(final ConvenientStreamReader data) throws IOException {
		RouterExitPolicyImpl result = new RouterExitPolicyImpl(data.readBoolean(),
															   data.readLong(),
															   data.readLong(),
															   data.readInt(),
															   data.readInt());
		return result;
	}
	/**
	 * Serialize all members into a stream.
	 */
	public void save(final ConvenientStreamWriter convenientStreamWriter) throws IOException {
        convenientStreamWriter.writeBoolean(accept);
        convenientStreamWriter.writeLong(ip);
        convenientStreamWriter.writeLong(netmask);
        convenientStreamWriter.writeInt(loPort);
        convenientStreamWriter.writeInt(hiPort);
	}
}
