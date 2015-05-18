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

package org.silvertunnel_ng.netlib.layer.tor.api;

/**
 * A finger print (a HASH_LEN-byte of asn1 encoded public key) of an identity
 * key or signing key of a router or directory server.
 * 
 * An object is read only.
 * 
 * @author hapke
 */
public interface Fingerprint extends Comparable<Fingerprint>
{
	String getHex();

	String getHexWithSpaces();

	/**
	 * @return a copy of the internally byte array
	 */
	byte[] getBytes();

	@Override
	String toString();

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);

	/**
	 * @param other
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	int compareTo(Fingerprint other);

	/**
	 * Clone, but do not throw an exception.
	 */
	Fingerprint cloneReliable() throws RuntimeException;
}
