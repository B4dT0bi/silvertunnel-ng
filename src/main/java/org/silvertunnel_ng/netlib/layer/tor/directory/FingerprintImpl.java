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

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.util.Parsing;

/**
 * A finger print (a HASH_LEN-byte of asn1 encoded public key) of an identity
 * key or signing key of a router or directory server.
 * 
 * An object is read only.
 * 
 * @author hapke
 */
public final class FingerprintImpl implements Fingerprint, Cloneable, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1621113144294310736L;
	/** internal byte array containing the data of the fingerprint. */
	private byte[] bytes;
	/** cache of result of getHex(). */
	private String hexCache;

	/**
	 * Create an {@link Fingerprint} object by using the BASE64 encoded data.
	 * @param identityKeyBase64 the BASE64 encoded data
	 */
	public FingerprintImpl(final String identityKeyBase64)
	{
		String b64fp = identityKeyBase64;
		while (b64fp.length() % 4 != 0)
		{
			b64fp += "=";
		}
		setIdentityKey(DatatypeConverter.parseBase64Binary(b64fp));
	}
	
	/**
	 * Create an {@link Fingerprint} object by using the data directly from a byte array.
	 * @param identityKey the byte array containing the identyKey
	 */
	public FingerprintImpl(final byte[] identityKey)
	{
		setIdentityKey(identityKey);
	}

	private void setIdentityKey(final byte[] identityKey)
	{
		// check parameter
		if (identityKey == null)
		{
			throw new NullPointerException();
		}
		if (identityKey.length < 4)
		{
			throw new IllegalArgumentException("invalid array length="
					+ identityKey.length);
		}

		// save value
		this.bytes = identityKey;
	}

	/**
	 * @return the fingerprint in hex notation.
	 */
	@Override
	public String getHex()
	{
		if (hexCache == null)
		{
			hexCache = Parsing.renderFingerprint(bytes, false);
		}
		return hexCache;
	}

	/**
	 * @return the fingerprint in hex notation with spaces.
	 */
	@Override
	public String getHexWithSpaces()
	{
		return Parsing.renderFingerprint(bytes, true);
	}

	/**
	 * @return a copy of the internal byte array
	 */
	@Override
	public byte[] getBytes()
	{
		final byte[] result = new byte[bytes.length];
		System.arraycopy(bytes, 0, result, 0, bytes.length);
		return result;
	}

	@Override
	public String toString()
	{
		return "fingerprintHexWithSpaces=" + getHexWithSpaces();
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(bytes);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof FingerprintImpl))
		{
			return false;
		}
		final FingerprintImpl o = (FingerprintImpl) obj;
		return Arrays.equals(this.bytes, o.bytes);
	}

	/**
	 * @param other
	 *            the other object
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(final Fingerprint other)
	{
		// performance optimization should be possible if necessary:
		return getHex().compareTo(other.getHex());
	}

	/**
	 * Clone, but do not throw an exception.
	 * 
	 * @return a cloned {@link Fingerprint} object
	 * @throws RuntimeException
	 *             when cloning is not supported
	 */
	@Override
	public Fingerprint cloneReliable() throws RuntimeException
	{
		try
		{
			return (Fingerprint) clone();
		}
		catch (final CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
