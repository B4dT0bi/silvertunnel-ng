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

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Random;

import org.silvertunnel_ng.netlib.tool.DynByteBuffer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Tobias Boese
 *
 */
public final class DynByteBufferTest
{
	/** the {@link DynByteBuffer} object. */
	private DynByteBuffer buffer;
	/**
	 * Initialize the {@link DynByteBuffer}.
	 */
	@BeforeMethod
	public void setUp()
	{
		buffer = new DynByteBuffer();
	}
	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#DynByteBuffer()}.
	 */
	@Test
	public void testDynByteBuffer()
	{
		assertNotNull(buffer);
		assertTrue(buffer.getSize() > 0);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#DynByteBuffer(int)}.
	 */
	@Test
	public void testDynByteBufferInt()
	{
		buffer = new DynByteBuffer(1337);
		assertNotNull(buffer);
		assertTrue(buffer.getSize() == 1337);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(byte)}.
	 */
	@Test
	public void testAppendByte()
	{
		int oldSize = buffer.getSize();
		byte [] testByte = {66};
		buffer.append(testByte[0]);
		assertEquals(testByte.length, buffer.getLength());
		assertEquals(oldSize, buffer.getSize());
		byte [] result = buffer.toArray();
		assertTrue(result.length == testByte.length);
		assertArrayEquals(testByte, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(byte[])}.
	 */
	@Test
	public void testAppendByteArray()
	{
		int oldSize = buffer.getSize();
		byte [] testByte = {1, 3, 3, 7};
		buffer.append(testByte, false);
		assertEquals(testByte.length, buffer.getLength());
		assertEquals(oldSize, buffer.getSize());
		byte [] result = buffer.toArray();
		assertTrue(result.length == testByte.length);
		assertArrayEquals(testByte, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(byte[])}.
	 */
	@Test
	public void testAppendByteArrayBig()
	{
		int oldSize = buffer.getSize();
		byte [] testByte = new byte [oldSize * 3];
		Random random = new Random();
		random.nextBytes(testByte);
		buffer.append(testByte, false);
		assertEquals(testByte.length, buffer.getLength());
		assertTrue(oldSize < buffer.getSize());
		byte [] result = buffer.toArray();
		assertTrue(result.length == testByte.length);
		assertArrayEquals(testByte, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(byte[], int)}.
	 */
	@Test
	public void testAppendByteArrayInt()
	{
		byte [] testByte = new byte [20];
		Random random = new Random();
		random.nextBytes(testByte);

		byte [] expected = new byte [15];
		for (int i = 0; i < expected.length; i++)
		{
			expected[i] = testByte[i + 5];
		}
		
		buffer.append(testByte, 5);
		
		assertEquals(expected.length, buffer.getLength());
		byte [] result = buffer.toArray();
		assertTrue(result.length == expected.length);
		assertArrayEquals(expected, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(byte[], int, int)}.
	 */
	@Test
	public void testAppendByteArrayIntInt()
	{
		byte [] testByte = new byte [20];
		Random random = new Random();
		random.nextBytes(testByte);

		byte [] expected = new byte [10];
		for (int i = 0; i < expected.length; i++)
		{
			expected[i] = testByte[i + 5];
		}
		
		buffer.append(testByte, 5, 10);
		
		assertEquals(expected.length, buffer.getLength());
		byte [] result = buffer.toArray();
		assertTrue(result.length == expected.length);
		assertArrayEquals(expected, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#toArray()}.
	 */
	@Test
	public void testToArray()
	{
		byte [] result = buffer.toArray();
		assertEquals(0, result.length);
		
		byte [] test = new byte [120];
		Random random = new Random();
		random.nextBytes(test);
		buffer.append(test, false);
		assertTrue(buffer.getSize() > 100);
		buffer.init();
		assertEquals(0, buffer.getLength());
		byte [] test2 = new byte [20];
		random.nextBytes(test2);
		buffer.append(test2, false);
		assertEquals(test2.length, buffer.getLength());
		assertArrayEquals(test2, buffer.toArray());
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#init()}.
	 */
	@Test
	public void testInit()
	{
		byte [] test = new byte [120];
		Random random = new Random();
		random.nextBytes(test);
		buffer.append(test, false);
		assertTrue(buffer.getSize() > 100);
		buffer.init();
		assertEquals(0, buffer.getLength());
		byte [] test2 = new byte [20];
		random.nextBytes(test2);
		buffer.append(test2, false);
		assertEquals(test2.length, buffer.getLength());
		assertArrayEquals(test2, buffer.toArray());
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(long)}.
	 */
	@Test
	public void testAppendLong()
	{
		long testValue = 1337;
		byte [] expected = {0, 0, 0, 0, 0, 0, 5, 57};
		buffer.append(testValue);
		assertEquals(8, buffer.getLength());
		assertArrayEquals(expected, buffer.toArray());
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#append(java.lang.String)}.
	 */
	@Test
	public void testAppendString()
	{
		String testString = "Dies ist ein Test!";
		byte [] expected = {0, 0, 0, 18, 68, 105, 101, 115, 32, 105, 115, 116, 32, 101, 105, 110, 32, 84, 101, 115, 116, 33};
		buffer.append(testString);
		byte [] result = buffer.toArray();
		assertArrayEquals(expected, result);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#getSize()}.
	 */
	@Test
	public void testGetSize()
	{
		buffer = new DynByteBuffer(100);
		assertEquals(100, buffer.getSize());
		byte [] test = new byte [120];
		Random random = new Random();
		random.nextBytes(test);
		buffer.append(test, false);
		assertTrue(buffer.getSize() > 100);
	}

	/**
	 * Test method for {@link de.badtobi.secureim.helper.DynByteBuffer#getLength()}.
	 */
	@Test
	public void testGetLength()
	{
		byte [] test = new byte [120];
		Random random = new Random();
		random.nextBytes(test);
		buffer.append(test, false);
		assertEquals(test.length, buffer.getLength());
	}

}
