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
package org.silvertunnel_ng.netlib.tool;

import java.nio.ByteBuffer;

/**
 * this class is used for converting data types to byte arrays and back.
 * 
 * @author Tobias Boese
 * 
 */
public final class ByteUtils
{
	/**
	 * an internal buffer for long.
	 */
	private static ByteBuffer longBuffer = ByteBuffer.allocate(8);
	/**
	 * an internal buffer for int.
	 */
	private static ByteBuffer intBuffer = ByteBuffer.allocate(4);

	/** */
	private ByteUtils()
	{
	}

	/**
	 * Convert a long into a byte array.
	 * 
	 * @param value
	 *            the long value
	 * @return a byte array containing the long
	 */
	public static byte[] longToBytes(final long value)
	{
		longBuffer.clear();
		longBuffer.putLong(value);
		return longBuffer.array();
	}

	/**
	 * Convert a byte array to a long value.
	 * 
	 * @param value
	 *            the byte array
	 * @return a long
	 */
	public static long bytesToLong(final byte[] value)
	{
		return bytesToLong(value, 0);
	}

	/**
	 * Convert a byte array to a long value.
	 * 
	 * @param value
	 *            the byte array
	 * @param offset
	 *            the offset of the long in the byte array.
	 * @return a long
	 */
	public static long bytesToLong(final byte[] value, final int offset)
	{
		longBuffer.clear();
		longBuffer.put(value, offset, 8);
		longBuffer.flip();
		return longBuffer.getLong();
	}

	/**
	 * Convert an int into a byte array.
	 * 
	 * @param value
	 *            the int value
	 * @return a byte array containing the int
	 */
	public static byte[] intToBytes(final int value)
	{
		intBuffer.clear();
		intBuffer.putInt(value);
		return intBuffer.array();
	}

	/**
	 * Converts a byte array to a int value.
	 * 
	 * @param value
	 *            the byte array
	 * @param offset
	 *            the offset of the int in the byte array
	 * @return an int
	 */
	public static int bytesToInt(final byte[] value, final int offset)
	{
		intBuffer.clear();
		intBuffer.put(value, offset, 4);
		intBuffer.flip();
		return intBuffer.getInt();
	}
}
