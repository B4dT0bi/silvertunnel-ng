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

package org.silvertunnel_ng.netlib.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to handle input streams, output streams and byte arrays.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class ByteArrayUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ByteArrayUtil.class);

	private static final char SPECIAL_CHAR = '?';

	/**
	 * Interpret the byte array as chars as far as possible.
	 * 
	 * @param byteArray the byte array which should be converted
	 * @return a human readable string 
	 */
	public static String showAsString(final byte[] byteArray)
	{
		final StringBuffer result = new StringBuffer(byteArray.length);
		for (int i = 0; i < byteArray.length; i++)
		{
			result.append(asChar(byteArray[i]));
		}
		return result.toString();
	}

	/**
	 * Interpret the byte array as chars as far as possible.
	 * 
	 * @param byteArray the byte array which should be converted.
	 * @return a string showing the content of the given byte array
	 */
	public static String showAsStringDetails(final byte[] byteArray)
	{
		final StringBuffer result = new StringBuffer(byteArray.length);
		for (int i = 0; i < byteArray.length; i++)
		{
			result.append(asCharDetail(byteArray[i]));
		}
		return result.toString();
	}

	/**
	 * Get parameters as byte[].
	 */
	public static byte[] getByteArray(int... bytes)
	{
		final byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
		{
			result[i] = (byte) bytes[i];
		}
		return result;
	}

	/**
	 * This method creates a byte[] that can e.b. be used for testing with
	 * MockNetLayer.
	 * 
	 * The returned byte[] consist of (prefix as UTF-8 bytes)+(middle sequence
	 * 0x00 0x01 ... with numOfBytesInTheMiddle bytes)+(suffix as UTF-8 bytes)
	 * 
	 * @param prefixStr
	 *            not null
	 * @param numOfBytesInTheMiddle
	 *            &gt;=0
	 * @param suffixStr
	 *            not null
	 * @return the described byte array; not null
	 */
	public static byte[] getByteArray(final String prefixStr,
			final int numOfBytesInTheMiddle, final String suffixStr)
	{
		try
		{
			// create the three parts
			final byte[] prefix = prefixStr.getBytes(Util.UTF8);
			final byte[] suffix = suffixStr.getBytes(Util.UTF8);
			final byte[] middle = new byte[numOfBytesInTheMiddle];
			for (int i = 0; i < middle.length; i++)
			{
				middle[i] = (byte) i;
			}

			// concat the parts
			final byte[] result = new byte[prefix.length + middle.length + suffix.length];
			System.arraycopy(prefix, 0, result, 0, prefix.length);
			System.arraycopy(middle, 0, result, prefix.length, middle.length);
			System.arraycopy(suffix, 0, result, prefix.length + middle.length, suffix.length);
			return result;

		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("", e);
		}
		return new byte[0];
	}

	/**
	 * @param b
	 * @return b as printable char; or as ? if not printable
	 */
	public static char asChar(final byte b)
	{
		if (b < ' ' || (b & 0xFF) > 127)
		{
			return SPECIAL_CHAR;
		}
		else
		{
			return (char) b;
		}
	}

	/**
	 * See also: BufferedLogger.log().
	 * 
	 * @param b
	 * @return b as printable char; or as ?XX if not printable where XX is the
	 *         hex code
	 */
	public static String asCharDetail(final byte b)
	{
		if (b < ' ' || (b & 0xFF) > 127)
		{
			// add hex value (always two digits)
			final String hex = Integer.toHexString(b & 0xFF);
			if (hex.length() < 2)
			{
				return SPECIAL_CHAR + "0" + hex;
			}
			else
			{
				return SPECIAL_CHAR + hex;
			}
		}
		else
		{
			return Character.toString((char) b);
		}
	}

	/**
	 * Read data from is until the buffer is full or the stream is closed.
	 * 
	 * @param maxResultSize
	 * @param is
	 * @return the bytes read (length&lt;=maxResultSize).
	 */
	public static byte[] readDataFromInputStream(final int maxResultSize,
			final InputStream is) throws IOException
	{
		final byte[] tempResultBuffer = new byte[maxResultSize];

		int len = 0;
		do
		{
			if (len >= tempResultBuffer.length)
			{
				// LOG.info("result buffer is full");
				break;
			}
			final int lastLen = is.read(tempResultBuffer, len,
					tempResultBuffer.length - len);
			if (lastLen < 0)
			{
				// LOG.info("end of result stream");
				break;
			}
			len += lastLen;
		}
		while (true);

		// copy to result buffer
		final byte[] result = new byte[len];
		System.arraycopy(tempResultBuffer, 0, result, 0, len);

		return result;
	}

	/**
	 * Concatenate given byte arrays to one byte array.
	 * @param input 1..x byte array which should be concatenated
	 * @return a concatenated byte array containing all the inputs
	 */
	public static byte[] concatByteArrays(final byte[]... input)
	{
		// determine full length
		int len = 0;
		for (int i = 0; i < input.length; i++)
		{
			len += input[i].length;
		}
		final byte[] result = new byte[len];

		// copy single byte arrays
		int pos = 0;
		for (int i = 0; i < input.length; i++)
		{
			System.arraycopy(input[i], 0, result, pos, input[i].length);
			pos += input[i].length;
		}

		return result;
	}
}
