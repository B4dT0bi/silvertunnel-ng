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
package org.silvertunnel_ng.netlib.util;

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
		synchronized (longBuffer)
		{
			longBuffer.clear();
			longBuffer.putLong(value);
			return longBuffer.array();			
		}
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
		synchronized (longBuffer) 
		{
			longBuffer.clear();
			longBuffer.put(value, offset, 8);
			longBuffer.flip();
			return longBuffer.getLong();
		}
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
		synchronized (intBuffer) 
		{
			intBuffer.clear();
			intBuffer.putInt(value);
			return intBuffer.array();
		}
	}

	/**
	 * Converts a byte array to an int value.
	 * 
	 * @param value
	 *            the byte array
	 * @param offset
	 *            the offset of the int in the byte array
	 * @return an int
	 */
	public static int bytesToInt(final byte[] value, final int offset)
	{
		synchronized (intBuffer) 
		{
			intBuffer.clear();
			intBuffer.put(value, offset, 4);
			intBuffer.flip();
			return intBuffer.getInt();
		}
	}
	/**
	 * Extract the Booleans from a byte. 
	 * (which have been saved with getByteFromBooleans)
	 * 
	 * @param data the byte containing the information about the Booleans
	 * @return an array of 4 Booleans
	 */
	public static Boolean [] getBooleansFromByte(final byte data)
	{
		final int tmp = (int) (data & 0xff);
		final Boolean [] result = new Boolean[4];
		result[0] = (tmp & 0x03) == 0x02 ? null : (tmp & 0x03) == 0x01;
		result[1] = (tmp & 0x0c) == 0x08 ? null : (tmp & 0x0c) == 0x04;
		result[2] = (tmp & 0x30) == 0x20 ? null : (tmp & 0x30) == 0x10;
		result[3] = (tmp & 0xc0) == 0x80 ? null : (tmp & 0xc0) == 0x40;
		return result;
	}
	/**
	 * Convert max. 4 Boolean values to a byte.
	 * 
	 * For saving 1 Boolean value 2 bits are used.
	 * 00 = false
	 * 01 = true
	 * 10 = null
	 * 
	 * @param value (mandatory) Boolean value
	 * @param optValues (optional) Boolean values (max. 3)
	 * @return a byte
	 */
	public static byte getByteFromBooleans(final Boolean value, final Boolean ... optValues)
	{
		int result = 0;
		if (value == null)
		{
			result += 0x02; // 0b00000010
		}
		else if (value)
		{
			result += 0x01; // 0b00000001
		}
		if (optValues.length < 1 || optValues[0] == null)
		{
			result += 0x08; // 0b00001000
		}
		else if (optValues[0])
		{
			result += 0x04; // 0b00000100 
		}
		if (optValues.length < 2 || optValues[1] == null)
		{
			result += 0x20; // 0b00100000
		}
		else if (optValues[1])
		{
			result += 0x10; // 0b00010000
		}
		if (optValues.length < 3 || optValues[2] == null)
		{
			result += 0x80; // 0b10000000
		}
		else if (optValues[2])
		{
			result += 0x40; // 0b01000000
		}
		return (byte) result;
	}
}
