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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Random;

import org.silvertunnel_ng.netlib.util.ByteUtils;
import org.testng.annotations.Test;

/**
 * Unittest for ByteUtils class.
 * 
 * @author Tobias Boese
 */
public final class ByteUtilsLocalTest 
{

	/**
	 * Test method for {@link ByteUtils#longToBytes(long)}.
	 */
	@Test
	public void testLongToBytes() 
	{
		System.out.println(Arrays.toString(ByteUtils.longToBytes(2010201020102010L)));
		System.out.println(Arrays.toString(ByteUtils.longToBytes(Long.MAX_VALUE)));
		assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, ByteUtils.longToBytes(0L));
		assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 5, 57}, ByteUtils.longToBytes(1337L));
		assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 20, 64}, ByteUtils.longToBytes(5184L));
		assertArrayEquals(new byte[] {0, 0, 0, 0, 1, 80, 46, 112}, ByteUtils.longToBytes(22031984L));
		assertArrayEquals(new byte[] {0, 0, 0, 0, 1, 50, -69, 122}, ByteUtils.longToBytes(20102010L));
		assertArrayEquals(new byte[] {0, 7, 36, 68, 101, -70, -11, 122}, ByteUtils.longToBytes(2010201020102010L));
		assertArrayEquals(new byte[] {127, -1, -1, -1, -1, -1, -1, -1}, ByteUtils.longToBytes(Long.MAX_VALUE));
	}

	/**
	 * Test method for {@link ByteUtils#bytesToLong(byte[])}.
	 */
	@Test
	public void testBytesToLongByteArray() 
	{
		assertEquals(0L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}));
		assertEquals(1337L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 5, 57}));
		assertEquals(5184L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 20, 64}));
		assertEquals(22031984L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 1, 80, 46, 112}));
		assertEquals(20102010L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 1, 50, -69, 122}));
		assertEquals(2010201020102010L, ByteUtils.bytesToLong(new byte[] {0, 7, 36, 68, 101, -70, -11, 122}));
		assertEquals(Long.MAX_VALUE, ByteUtils.bytesToLong(new byte[] {127, -1, -1, -1, -1, -1, -1, -1}));
	}

	/**
	 * Test method for {@link ByteUtils#bytesToLong(byte[], int)}.
	 */
	@Test
	public void testBytesToLongByteArrayInt() 
	{
		assertEquals(0L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, 0));
		assertEquals(1337L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 5, 57}, 0));
		assertEquals(5184L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 0, 0, 20, 64}, 0));
		assertEquals(22031984L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 1, 80, 46, 112}, 0));
		assertEquals(20102010L, ByteUtils.bytesToLong(new byte[] {0, 0, 0, 0, 1, 50, -69, 122}, 0));
		assertEquals(2010201020102010L, ByteUtils.bytesToLong(new byte[] {0, 7, 36, 68, 101, -70, -11, 122}, 0));
		assertEquals(Long.MAX_VALUE, ByteUtils.bytesToLong(new byte[] {127, -1, -1, -1, -1, -1, -1, -1}, 0));

		assertEquals(0L, ByteUtils.bytesToLong(new byte[] {9, 0, 0, 0, 0, 0, 0, 0, 0}, 1));
		assertEquals(1337L, ByteUtils.bytesToLong(new byte[] {9, 0, 0, 0, 0, 0, 0, 5, 57}, 1));
		assertEquals(5184L, ByteUtils.bytesToLong(new byte[] {9, 0, 0, 0, 0, 0, 0, 20, 64}, 1));
		assertEquals(22031984L, ByteUtils.bytesToLong(new byte[] {9, 0, 0, 0, 0, 1, 80, 46, 112}, 1));
		assertEquals(20102010L, ByteUtils.bytesToLong(new byte[] {9, 0, 0, 0, 0, 1, 50, -69, 122}, 1));
		assertEquals(2010201020102010L, ByteUtils.bytesToLong(new byte[] {9, 0, 7, 36, 68, 101, -70, -11, 122}, 1));
		assertEquals(Long.MAX_VALUE, ByteUtils.bytesToLong(new byte[] {9, 127, -1, -1, -1, -1, -1, -1, -1}, 1));
	}
	/**
	 * Testing conversion from long to byte to long with randomly chosen longs.
	 */
	@Test
	public void testRandomLongValues()
	{
		Random random = new Random();
		for (int i = 0; i < 10000; i++)
		{
			long expected = random.nextLong();
			byte [] tmp = ByteUtils.longToBytes(expected);
			assertEquals(expected, ByteUtils.bytesToLong(tmp));
		}
	}
	/**
	 * Testing conversion from int to byte to int with randomly chosen ints.
	 */
	@Test
	public void testRandomIntValues()
	{
		Random random = new Random();
		for (int i = 0; i < 10000; i++)
		{
			int expected = random.nextInt();
			byte [] tmp = ByteUtils.intToBytes(expected);
			assertEquals(expected, ByteUtils.bytesToInt(tmp, 0));
		}
	}
	/**
	 * Test method for {@link ByteUtils#intToBytes(int)}.
	 */
	@Test
	public void testIntToBytes() 
	{
		assertArrayEquals(new byte[] {0, 0, 0, 0}, ByteUtils.intToBytes(0));
		assertArrayEquals(new byte[] {0, 0, 5, 57}, ByteUtils.intToBytes(1337));
		assertArrayEquals(new byte[] {0, 0, 20, 64}, ByteUtils.intToBytes(5184));
		assertArrayEquals(new byte[] {1, 80, 46, 112}, ByteUtils.intToBytes(22031984));
		assertArrayEquals(new byte[] {1, 50, -69, 122}, ByteUtils.intToBytes(20102010));
		assertArrayEquals(new byte[] {127, -1, -1, -1}, ByteUtils.intToBytes(Integer.MAX_VALUE));
	}

	/**
	 * Test method for {@link ByteUtils#bytesToInt(byte[], int)}.
	 */
	@Test
	public void testBytesToInt() 
	{
		assertEquals(0, ByteUtils.bytesToInt(new byte[] {0, 0, 0, 0}, 0));
		assertEquals(1337, ByteUtils.bytesToInt(new byte[] {0, 0, 5, 57}, 0));
		assertEquals(5184, ByteUtils.bytesToInt(new byte[] {0, 0, 20, 64}, 0));
		assertEquals(22031984, ByteUtils.bytesToInt(new byte[] {1, 80, 46, 112}, 0));
		assertEquals(20102010, ByteUtils.bytesToInt(new byte[] {1, 50, -69, 122}, 0));
		assertEquals(Integer.MAX_VALUE, ByteUtils.bytesToInt(new byte[] {127, -1, -1, -1}, 0));

		assertEquals(0, ByteUtils.bytesToInt(new byte[] {9, 0, 0, 0, 0}, 1));
		assertEquals(1337, ByteUtils.bytesToInt(new byte[] {9, 0, 0, 5, 57}, 1));
		assertEquals(5184, ByteUtils.bytesToInt(new byte[] {9, 0, 0, 20, 64}, 1));
		assertEquals(22031984, ByteUtils.bytesToInt(new byte[] {9, 1, 80, 46, 112}, 1));
		assertEquals(20102010, ByteUtils.bytesToInt(new byte[] {9, 1, 50, -69, 122}, 1));
		assertEquals(Integer.MAX_VALUE, ByteUtils.bytesToInt(new byte[] {9, 127, -1, -1, -1}, 1));
	}

	/**
	 * Test method for {@link ByteUtils#getBooleansFromByte(byte)}.
	 */
	@Test
	public void testGetBooleansFromByte() 
	{
		assertArrayEquals(new Boolean[] {null, null, null, null}, ByteUtils.getBooleansFromByte((byte) 170));
		assertArrayEquals(new Boolean[] {true, null, null, null}, ByteUtils.getBooleansFromByte((byte) 169));
		assertArrayEquals(new Boolean[] {false, null, null, null}, ByteUtils.getBooleansFromByte((byte) 168));
		assertArrayEquals(new Boolean[] {null, true, null, null}, ByteUtils.getBooleansFromByte((byte) 166));
		assertArrayEquals(new Boolean[] {true, true, null, null}, ByteUtils.getBooleansFromByte((byte) 165));
		assertArrayEquals(new Boolean[] {false, true, null, null}, ByteUtils.getBooleansFromByte((byte) 164));
		assertArrayEquals(new Boolean[] {null, false, null, null}, ByteUtils.getBooleansFromByte((byte) 162));
		assertArrayEquals(new Boolean[] {true, false, null, null}, ByteUtils.getBooleansFromByte((byte) 161));
		assertArrayEquals(new Boolean[] {false, false, null, null}, ByteUtils.getBooleansFromByte((byte) 160));
		assertArrayEquals(new Boolean[] {null, null, true, null}, ByteUtils.getBooleansFromByte((byte) 154));
		assertArrayEquals(new Boolean[] {true, null, true, null}, ByteUtils.getBooleansFromByte((byte) 153));
		assertArrayEquals(new Boolean[] {false, null, true, null}, ByteUtils.getBooleansFromByte((byte) 152));
		assertArrayEquals(new Boolean[] {null, true, true, null}, ByteUtils.getBooleansFromByte((byte) 150));
		assertArrayEquals(new Boolean[] {true, true, true, null}, ByteUtils.getBooleansFromByte((byte) 149));
		assertArrayEquals(new Boolean[] {false, true, true, null}, ByteUtils.getBooleansFromByte((byte) 148));
		assertArrayEquals(new Boolean[] {null, false, true, null}, ByteUtils.getBooleansFromByte((byte) 146));
		assertArrayEquals(new Boolean[] {true, false, true, null}, ByteUtils.getBooleansFromByte((byte) 145));
		assertArrayEquals(new Boolean[] {false, false, true, null}, ByteUtils.getBooleansFromByte((byte) 144));
		assertArrayEquals(new Boolean[] {null, null, false, null}, ByteUtils.getBooleansFromByte((byte) 138));
		assertArrayEquals(new Boolean[] {true, null, false, null}, ByteUtils.getBooleansFromByte((byte) 137));
		assertArrayEquals(new Boolean[] {false, null, false, null}, ByteUtils.getBooleansFromByte((byte) 136));
		assertArrayEquals(new Boolean[] {null, true, false, null}, ByteUtils.getBooleansFromByte((byte) 134));
		assertArrayEquals(new Boolean[] {true, true, false, null}, ByteUtils.getBooleansFromByte((byte) 133));
		assertArrayEquals(new Boolean[] {false, true, false, null}, ByteUtils.getBooleansFromByte((byte) 132));
		assertArrayEquals(new Boolean[] {null, false, false, null}, ByteUtils.getBooleansFromByte((byte) 130));
		assertArrayEquals(new Boolean[] {true, false, false, null}, ByteUtils.getBooleansFromByte((byte) 129));
		assertArrayEquals(new Boolean[] {false, false, false, null}, ByteUtils.getBooleansFromByte((byte) 128));
		assertArrayEquals(new Boolean[] {null, null, null, true}, ByteUtils.getBooleansFromByte((byte) 106));
		assertArrayEquals(new Boolean[] {true, null, null, true}, ByteUtils.getBooleansFromByte((byte) 105));
		assertArrayEquals(new Boolean[] {false, null, null, true}, ByteUtils.getBooleansFromByte((byte) 104));
		assertArrayEquals(new Boolean[] {null, true, null, true}, ByteUtils.getBooleansFromByte((byte) 102));
		assertArrayEquals(new Boolean[] {true, true, null, true}, ByteUtils.getBooleansFromByte((byte) 101));
		assertArrayEquals(new Boolean[] {false, true, null, true}, ByteUtils.getBooleansFromByte((byte) 100));
		assertArrayEquals(new Boolean[] {null, false, null, true}, ByteUtils.getBooleansFromByte((byte) 98));
		assertArrayEquals(new Boolean[] {true, false, null, true}, ByteUtils.getBooleansFromByte((byte) 97));
		assertArrayEquals(new Boolean[] {false, false, null, true}, ByteUtils.getBooleansFromByte((byte) 96));
		assertArrayEquals(new Boolean[] {null, null, true, true}, ByteUtils.getBooleansFromByte((byte) 90));
		assertArrayEquals(new Boolean[] {true, null, true, true}, ByteUtils.getBooleansFromByte((byte) 89));
		assertArrayEquals(new Boolean[] {false, null, true, true}, ByteUtils.getBooleansFromByte((byte) 88));
		assertArrayEquals(new Boolean[] {null, true, true, true}, ByteUtils.getBooleansFromByte((byte) 86));
		assertArrayEquals(new Boolean[] {true, true, true, true}, ByteUtils.getBooleansFromByte((byte) 85));
		assertArrayEquals(new Boolean[] {false, true, true, true}, ByteUtils.getBooleansFromByte((byte) 84));
		assertArrayEquals(new Boolean[] {null, false, true, true}, ByteUtils.getBooleansFromByte((byte) 82));
		assertArrayEquals(new Boolean[] {true, false, true, true}, ByteUtils.getBooleansFromByte((byte) 81));
		assertArrayEquals(new Boolean[] {false, false, true, true}, ByteUtils.getBooleansFromByte((byte) 80));
		assertArrayEquals(new Boolean[] {null, null, false, true}, ByteUtils.getBooleansFromByte((byte) 74));
		assertArrayEquals(new Boolean[] {true, null, false, true}, ByteUtils.getBooleansFromByte((byte) 73));
		assertArrayEquals(new Boolean[] {false, null, false, true}, ByteUtils.getBooleansFromByte((byte) 72));
		assertArrayEquals(new Boolean[] {null, true, false, true}, ByteUtils.getBooleansFromByte((byte) 70));
		assertArrayEquals(new Boolean[] {true, true, false, true}, ByteUtils.getBooleansFromByte((byte) 69));
		assertArrayEquals(new Boolean[] {false, true, false, true}, ByteUtils.getBooleansFromByte((byte) 68));
		assertArrayEquals(new Boolean[] {null, false, false, true}, ByteUtils.getBooleansFromByte((byte) 66));
		assertArrayEquals(new Boolean[] {true, false, false, true}, ByteUtils.getBooleansFromByte((byte) 65));
		assertArrayEquals(new Boolean[] {false, false, false, true}, ByteUtils.getBooleansFromByte((byte) 64));
		assertArrayEquals(new Boolean[] {null, null, null, false}, ByteUtils.getBooleansFromByte((byte) 42));
		assertArrayEquals(new Boolean[] {true, null, null, false}, ByteUtils.getBooleansFromByte((byte) 41));
		assertArrayEquals(new Boolean[] {false, null, null, false}, ByteUtils.getBooleansFromByte((byte) 40));
		assertArrayEquals(new Boolean[] {null, true, null, false}, ByteUtils.getBooleansFromByte((byte) 38));
		assertArrayEquals(new Boolean[] {true, true, null, false}, ByteUtils.getBooleansFromByte((byte) 37));
		assertArrayEquals(new Boolean[] {false, true, null, false}, ByteUtils.getBooleansFromByte((byte) 36));
		assertArrayEquals(new Boolean[] {null, false, null, false}, ByteUtils.getBooleansFromByte((byte) 34));
		assertArrayEquals(new Boolean[] {true, false, null, false}, ByteUtils.getBooleansFromByte((byte) 33));
		assertArrayEquals(new Boolean[] {false, false, null, false}, ByteUtils.getBooleansFromByte((byte) 32));
		assertArrayEquals(new Boolean[] {null, null, true, false}, ByteUtils.getBooleansFromByte((byte) 26));
		assertArrayEquals(new Boolean[] {true, null, true, false}, ByteUtils.getBooleansFromByte((byte) 25));
		assertArrayEquals(new Boolean[] {false, null, true, false}, ByteUtils.getBooleansFromByte((byte) 24));
		assertArrayEquals(new Boolean[] {null, true, true, false}, ByteUtils.getBooleansFromByte((byte) 22));
		assertArrayEquals(new Boolean[] {true, true, true, false}, ByteUtils.getBooleansFromByte((byte) 21));
		assertArrayEquals(new Boolean[] {false, true, true, false}, ByteUtils.getBooleansFromByte((byte) 20));
		assertArrayEquals(new Boolean[] {null, false, true, false}, ByteUtils.getBooleansFromByte((byte) 18));
		assertArrayEquals(new Boolean[] {true, false, true, false}, ByteUtils.getBooleansFromByte((byte) 17));
		assertArrayEquals(new Boolean[] {false, false, true, false}, ByteUtils.getBooleansFromByte((byte) 16));
		assertArrayEquals(new Boolean[] {null, null, false, false}, ByteUtils.getBooleansFromByte((byte) 10));
		assertArrayEquals(new Boolean[] {true, null, false, false}, ByteUtils.getBooleansFromByte((byte) 9));
		assertArrayEquals(new Boolean[] {false, null, false, false}, ByteUtils.getBooleansFromByte((byte) 8));
		assertArrayEquals(new Boolean[] {null, true, false, false}, ByteUtils.getBooleansFromByte((byte) 6));
		assertArrayEquals(new Boolean[] {true, true, false, false}, ByteUtils.getBooleansFromByte((byte) 5));
		assertArrayEquals(new Boolean[] {false, true, false, false}, ByteUtils.getBooleansFromByte((byte) 4));
		assertArrayEquals(new Boolean[] {null, false, false, false}, ByteUtils.getBooleansFromByte((byte) 2));
		assertArrayEquals(new Boolean[] {true, false, false, false}, ByteUtils.getBooleansFromByte((byte) 1));
		assertArrayEquals(new Boolean[] {false, false, false, false}, ByteUtils.getBooleansFromByte((byte) 0));
	}

	/**
	 * Test method for {@link ByteUtils#getByteFromBooleans(java.lang.Boolean, java.lang.Boolean[])}.
	 */
	@Test
	public void testGetByteFromBooleans() 
	{
		assertEquals((byte) 170, ByteUtils.getByteFromBooleans(null, null, null, null));
		assertEquals((byte) 169, ByteUtils.getByteFromBooleans(true, null, null, null));
		assertEquals((byte) 168, ByteUtils.getByteFromBooleans(false, null, null, null));
		assertEquals((byte) 166, ByteUtils.getByteFromBooleans(null, true, null, null));
		assertEquals((byte) 165, ByteUtils.getByteFromBooleans(true, true, null, null));
		assertEquals((byte) 164, ByteUtils.getByteFromBooleans(false, true, null, null));
		assertEquals((byte) 162, ByteUtils.getByteFromBooleans(null, false, null, null));
		assertEquals((byte) 161, ByteUtils.getByteFromBooleans(true, false, null, null));
		assertEquals((byte) 160, ByteUtils.getByteFromBooleans(false, false, null, null));
		assertEquals((byte) 154, ByteUtils.getByteFromBooleans(null, null, true, null));
		assertEquals((byte) 153, ByteUtils.getByteFromBooleans(true, null, true, null));
		assertEquals((byte) 152, ByteUtils.getByteFromBooleans(false, null, true, null));
		assertEquals((byte) 150, ByteUtils.getByteFromBooleans(null, true, true, null));
		assertEquals((byte) 149, ByteUtils.getByteFromBooleans(true, true, true, null));
		assertEquals((byte) 148, ByteUtils.getByteFromBooleans(false, true, true, null));
		assertEquals((byte) 146, ByteUtils.getByteFromBooleans(null, false, true, null));
		assertEquals((byte) 145, ByteUtils.getByteFromBooleans(true, false, true, null));
		assertEquals((byte) 144, ByteUtils.getByteFromBooleans(false, false, true, null));
		assertEquals((byte) 138, ByteUtils.getByteFromBooleans(null, null, false, null));
		assertEquals((byte) 137, ByteUtils.getByteFromBooleans(true, null, false, null));
		assertEquals((byte) 136, ByteUtils.getByteFromBooleans(false, null, false, null));
		assertEquals((byte) 134, ByteUtils.getByteFromBooleans(null, true, false, null));
		assertEquals((byte) 133, ByteUtils.getByteFromBooleans(true, true, false, null));
		assertEquals((byte) 132, ByteUtils.getByteFromBooleans(false, true, false, null));
		assertEquals((byte) 130, ByteUtils.getByteFromBooleans(null, false, false, null));
		assertEquals((byte) 129, ByteUtils.getByteFromBooleans(true, false, false, null));
		assertEquals((byte) 128, ByteUtils.getByteFromBooleans(false, false, false, null));
		assertEquals((byte) 106, ByteUtils.getByteFromBooleans(null, null, null, true));
		assertEquals((byte) 105, ByteUtils.getByteFromBooleans(true, null, null, true));
		assertEquals((byte) 104, ByteUtils.getByteFromBooleans(false, null, null, true));
		assertEquals((byte) 102, ByteUtils.getByteFromBooleans(null, true, null, true));
		assertEquals((byte) 101, ByteUtils.getByteFromBooleans(true, true, null, true));
		assertEquals((byte) 100, ByteUtils.getByteFromBooleans(false, true, null, true));
		assertEquals((byte) 98, ByteUtils.getByteFromBooleans(null, false, null, true));
		assertEquals((byte) 97, ByteUtils.getByteFromBooleans(true, false, null, true));
		assertEquals((byte) 96, ByteUtils.getByteFromBooleans(false, false, null, true));
		assertEquals((byte) 90, ByteUtils.getByteFromBooleans(null, null, true, true));
		assertEquals((byte) 89, ByteUtils.getByteFromBooleans(true, null, true, true));
		assertEquals((byte) 88, ByteUtils.getByteFromBooleans(false, null, true, true));
		assertEquals((byte) 86, ByteUtils.getByteFromBooleans(null, true, true, true));
		assertEquals((byte) 85, ByteUtils.getByteFromBooleans(true, true, true, true));
		assertEquals((byte) 84, ByteUtils.getByteFromBooleans(false, true, true, true));
		assertEquals((byte) 82, ByteUtils.getByteFromBooleans(null, false, true, true));
		assertEquals((byte) 81, ByteUtils.getByteFromBooleans(true, false, true, true));
		assertEquals((byte) 80, ByteUtils.getByteFromBooleans(false, false, true, true));
		assertEquals((byte) 74, ByteUtils.getByteFromBooleans(null, null, false, true));
		assertEquals((byte) 73, ByteUtils.getByteFromBooleans(true, null, false, true));
		assertEquals((byte) 72, ByteUtils.getByteFromBooleans(false, null, false, true));
		assertEquals((byte) 70, ByteUtils.getByteFromBooleans(null, true, false, true));
		assertEquals((byte) 69, ByteUtils.getByteFromBooleans(true, true, false, true));
		assertEquals((byte) 68, ByteUtils.getByteFromBooleans(false, true, false, true));
		assertEquals((byte) 66, ByteUtils.getByteFromBooleans(null, false, false, true));
		assertEquals((byte) 65, ByteUtils.getByteFromBooleans(true, false, false, true));
		assertEquals((byte) 64, ByteUtils.getByteFromBooleans(false, false, false, true));
		assertEquals((byte) 42, ByteUtils.getByteFromBooleans(null, null, null, false));
		assertEquals((byte) 41, ByteUtils.getByteFromBooleans(true, null, null, false));
		assertEquals((byte) 40, ByteUtils.getByteFromBooleans(false, null, null, false));
		assertEquals((byte) 38, ByteUtils.getByteFromBooleans(null, true, null, false));
		assertEquals((byte) 37, ByteUtils.getByteFromBooleans(true, true, null, false));
		assertEquals((byte) 36, ByteUtils.getByteFromBooleans(false, true, null, false));
		assertEquals((byte) 34, ByteUtils.getByteFromBooleans(null, false, null, false));
		assertEquals((byte) 33, ByteUtils.getByteFromBooleans(true, false, null, false));
		assertEquals((byte) 32, ByteUtils.getByteFromBooleans(false, false, null, false));
		assertEquals((byte) 26, ByteUtils.getByteFromBooleans(null, null, true, false));
		assertEquals((byte) 25, ByteUtils.getByteFromBooleans(true, null, true, false));
		assertEquals((byte) 24, ByteUtils.getByteFromBooleans(false, null, true, false));
		assertEquals((byte) 22, ByteUtils.getByteFromBooleans(null, true, true, false));
		assertEquals((byte) 21, ByteUtils.getByteFromBooleans(true, true, true, false));
		assertEquals((byte) 20, ByteUtils.getByteFromBooleans(false, true, true, false));
		assertEquals((byte) 18, ByteUtils.getByteFromBooleans(null, false, true, false));
		assertEquals((byte) 17, ByteUtils.getByteFromBooleans(true, false, true, false));
		assertEquals((byte) 16, ByteUtils.getByteFromBooleans(false, false, true, false));
		assertEquals((byte) 10, ByteUtils.getByteFromBooleans(null, null, false, false));
		assertEquals((byte) 9, ByteUtils.getByteFromBooleans(true, null, false, false));
		assertEquals((byte) 8, ByteUtils.getByteFromBooleans(false, null, false, false));
		assertEquals((byte) 6, ByteUtils.getByteFromBooleans(null, true, false, false));
		assertEquals((byte) 5, ByteUtils.getByteFromBooleans(true, true, false, false));
		assertEquals((byte) 4, ByteUtils.getByteFromBooleans(false, true, false, false));
		assertEquals((byte) 2, ByteUtils.getByteFromBooleans(null, false, false, false));
		assertEquals((byte) 1, ByteUtils.getByteFromBooleans(true, false, false, false));
		assertEquals((byte) 0, ByteUtils.getByteFromBooleans(false, false, false, false));
	}
}
