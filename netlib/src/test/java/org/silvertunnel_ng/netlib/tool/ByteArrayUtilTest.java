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

import static org.testng.AssertJUnit.*;

import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Testing the {@link ByteArrayUtil} class.
 * @author Tobias Boese
 *
 */
public final class ByteArrayUtilTest 
{
	/** byte array which contains all values from 0 - 255. */
	private final byte [] testArray = new byte [256];
	/** expected show Detail result. */
	private static final String SHOW_AS_STRING_EXPECTED = "??????????????????????"
														+ "?????????? !\"#$%&'()*"
														+ "+,-./0123456789:;<=>?@"
														+ "ABCDEFGHIJKLMNOPQRSTUV"
														+ "WXYZ[\\]^_`abcdefghijk"
														+ "lmnopqrstuvwxyz{|}~??"
														+ "??????????????????????"
														+ "??????????????????????"
														+ "??????????????????????"
														+ "??????????????????????"
														+ "??????????????????????"
														+ "????????????????";
	private static final String SHOW_AS_STRING_DETAIL_EXPECTED = "?00?01?02?03?04"
														+ "?05?06?07?08?09?0a?0b?0c"
														+ "?0d?0e?0f?10?11?12?13?14"
														+ "?15?16?17?18?19?1a?1b?1c"
														+ "?1d?1e?1f !\"#$%&'()*+,-"
														+ "./0123456789:;<=>?@ABCDE"
														+ "FGHIJKLMNOPQRSTUVWXYZ[\\"
														+ "]^_`abcdefghijklmnopqrst"
														+ "uvwxyz{|}~?80?81?82?83?84"
														+ "?85?86?87?88?89?8a?8b?8c?8d"
														+ "?8e?8f?90?91?92?93?94?95?96"
														+ "?97?98?99?9a?9b?9c?9d?9e?9f"
														+ "?a0?a1?a2?a3?a4?a5?a6?a7?a8"
														+ "?a9?aa?ab?ac?ad?ae?af?b0?b1"
														+ "?b2?b3?b4?b5?b6?b7?b8?b9?ba"
														+ "?bb?bc?bd?be?bf?c0?c1?c2?c3"
														+ "?c4?c5?c6?c7?c8?c9?ca?cb?cc"
														+ "?cd?ce?cf?d0?d1?d2?d3?d4?d5"
														+ "?d6?d7?d8?d9?da?db?dc?dd?de"
														+ "?df?e0?e1?e2?e3?e4?e5?e6?e7"
														+ "?e8?e9?ea?eb?ec?ed?ee?ef?f0"
														+ "?f1?f2?f3?f4?f5?f6?f7?f8?f9"
														+ "?fa?fb?fc?fd?fe?ff";
	/**
	 * Setup method for creating the test array.
	 */
	@BeforeClass
	public void setUp()
	{
		for (int i = 0; i < testArray.length; i++)
		{
			testArray[i] = (byte) i;
		}
	}
	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#showAsString(byte[])}.
	 */
	@Test
	public void testShowAsString() 
	{
		assertEquals(SHOW_AS_STRING_EXPECTED, ByteArrayUtil.showAsString(testArray));
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#showAsStringDetails(byte[])}.
	 */
	@Test
	public void testShowAsStringDetails() 
	{
		assertEquals(SHOW_AS_STRING_DETAIL_EXPECTED, ByteArrayUtil.showAsStringDetails(testArray));
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#getByteArray(int[])}.
	 */
	@Test(enabled = false)
	public void testGetByteArrayIntArray() 
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#getByteArray(java.lang.String, int, java.lang.String)}.
	 */
	@Test(enabled = false)
	public void testGetByteArrayStringIntString() 
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#asChar(byte)}.
	 */
	@Test
	public void testAsChar() 
	{
		final byte [] expected = SHOW_AS_STRING_EXPECTED.getBytes();
		for (int i = 0; i < 255; i++)
		{
			assertEquals(expected[i], ByteArrayUtil.asChar(testArray[i]));
		}
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#asCharDetail(byte)}.
	 */
	@Test(enabled = false)
	public void testAsCharDetail() 
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#readDataFromInputStream(int, java.io.InputStream)}.
	 */
	@Test(enabled = false)
	public void testReadDataFromInputStream() 
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.util.ByteArrayUtil#concatByteArrays(byte[][])}.
	 */
	@Test(enabled = false)
	public void testConcatByteArrays() 
	{
		fail("Not yet implemented");
	}

}
