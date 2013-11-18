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

package org.silvertunnel_ng.netlib.layer.tor.util;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 * Test cryptography logic.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class EncodingLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(EncodingLocalTest.class);

	/** any data for testing. */
	private static final byte[] EXAMPLE_DATA = { -11, 22, -33, 44, -55, 66,
			-77, 88, -99 };

	@Test
	public void testToBase32()
	{
		final String result = Encoding.toBase32(EXAMPLE_DATA);
		assertEquals("wrong toBase32() result", "6uln6lgjikzvrhi", result);
	}

	@Test
	public void testParseBase32()
	{
		final String base32 = Encoding.toBase32(EXAMPLE_DATA);
		final byte[] result = Encoding.parseBase32(base32);
		assertEquals("wrong parseBase32() result",
				Arrays.toString(EXAMPLE_DATA), Arrays.toString(result));
	}

	@Test
	public void testIntTo4ByteArray1()
	{
		final int value = 0xfc00ee11;
		final byte[] result = Encoding.intToNByteArray(value, 4);
		assertEquals(
				"wrong intToNByteArray() (1) result",
				Arrays.toString(new byte[] { (byte) 0xfc, (byte) 0x00,
						(byte) 0xee, (byte) 0x11 }), Arrays.toString(result));
	}

	@Test
	public void testIntTo4ByteArray2()
	{
		final int value = 0x11fc00ee;
		final byte[] result = Encoding.intToNByteArray(value, 4);
		assertEquals(
				"wrong intToNByteArray() (2) result",
				Arrays.toString(new byte[] { (byte) 0x11, (byte) 0xfc,
						(byte) 0x00, (byte) 0xee }), Arrays.toString(result));
	}
}
