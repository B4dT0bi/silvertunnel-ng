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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import java.util.Arrays;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.util.DatatypeConverter;

/**
 * used to store server descriptors from a dir-spec v2 network status document.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class RouterStatusDescription
{
	/** nickname of the router. */
	private String nickname;
	/** {@link Fingerprint} of the router. */
	private Fingerprint fingerprint;
	/** "Digest" is a hash of its most recent descriptor as signed (that is, not including the signature), encoded in base64. */
	private byte [] digestDescriptor;
	/**
	 * "Publication" is the publication time of its most recent descriptor.
	 */
	private long lastPublication;
	/** IP address of the router. */
	private String ip;
	/** OR port and Dir Port. */
	private int orPort, dirPort;
	/** Router flags. (stable, valid, running, etc)*/
	private RouterFlags routerFlags;

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	/**
	 * Get the Routers nickname.
	 * @return the nickname of the router as string
	 */
	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(final String nickname)
	{
		this.nickname = nickname;
	}

	public Fingerprint getFingerprint()
	{
		return fingerprint;
	}

	public void setFingerprint(final String fingerprint)
	{
		this.fingerprint = new FingerprintImpl(fingerprint);
	}

	public void setFingerprint(final byte[] fingerprint)
	{
		this.fingerprint = new FingerprintImpl(fingerprint);
	}

	public void setFingerprint(final Fingerprint fingerprint)
	{
		this.fingerprint = fingerprint;
	}

	public byte[] getDigestDescriptor()
	{
		return digestDescriptor;
	}

	/**
	 * Get the descriptor digest in hex notation.
	 * @return a string containing the hexnotation of the descriptor digest
	 */
	public String getDigestDescriptorAsHex()
	{
		return DatatypeConverter.printHexBinary(digestDescriptor).toUpperCase();
	}

	/**
	 * Set the descriptor digest by passing the base64 encoded value.
	 * 
	 * @param digestDescriptorBase64 the descriptor digest in base64 encoding
	 */
	public void setDigestDescriptor(final String digestDescriptorBase64)
	{
		String base64 = digestDescriptorBase64;
		while (base64.length() % 4 != 0)
		{
			base64 += "=";
		}
		setDigestDescriptor(DatatypeConverter.parseBase64Binary(base64));
	}

	public void setDigestDescriptor(final byte[] digestDescriptor)
	{
		this.digestDescriptor = digestDescriptor;
	}
	
	public long getLastPublication()
	{
		return lastPublication;
	}

	public void setLastPublication(final long lastPublication)
	{
		this.lastPublication = lastPublication;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(final String ip)
	{
		this.ip = ip;
	}

	public int getOrPort()
	{
		return orPort;
	}

	public void setOrPort(final int orPort)
	{
		this.orPort = orPort;
	}

	/**
	 * Get Directory Port of relay. 0 if none is set.
	 * @return the directory port of this router (int)
	 */
	public int getDirPort()
	{
		return dirPort;
	}

	/**
	 * Get Directory Port of relay. 0 if none is set.
	 * @param dirPort the directory port of this router (int)
	 */
	public void setDirPort(final int dirPort)
	{
		this.dirPort = dirPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "RouterStatusDescription [nickname=" + nickname
				+ ", fingerprint=" + fingerprint + ", digestDescriptor="
				+ Arrays.toString(digestDescriptor) + ", lastPublication="
				+ lastPublication + ", ip=" + ip + ", orPort=" + orPort
				+ ", dirPort=" + dirPort + "]";
	}

	/**
	 * @return the routerFlags
	 */
	public RouterFlags getRouterFlags()
	{
		return routerFlags;
	}

	/**
	 * @param routerFlags the routerFlags to set
	 */
	public void setRouterFlags(final RouterFlags routerFlags)
	{
		this.routerFlags = routerFlags;
	}
	/**
	 * @param routerFlags the routerFlags to set
	 */
	public void setRouterFlags(final String routerFlags)
	{
		this.routerFlags = new RouterFlags(routerFlags);
	}
}
