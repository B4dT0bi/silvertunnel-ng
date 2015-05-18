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
package com.maxmind.geoip.util;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;


/**
 * JUnit test to test class InMemoryRandomAccessFile.
 * 
 * @author hapke
 */
public class InMemoryRandomAccessFileLocalTest
{
	@Test
	public void testRead() throws Exception
	{
		// initialize and test constructor
		final byte[] dataSource = { 2, 4, 6, 8, 10 };
		final int maxSize = 10;
		final InMemoryRandomAccessFile f = new InMemoryRandomAccessFile(
				new ByteArrayInputStream(dataSource), maxSize);

		// 1st attempt
		f.seek(1);
		assertEquals("wrong 1st byte", 4, f.read());
		assertEquals("wrong 1st position", 2L, f.getFilePointer());

		// 2nd attempt
		final byte[] buffer = new byte[2];
		final byte[] expectedBuffer = { 6, 8 };
		f.read(buffer);
		assertEquals("wrong 2nd+3rd bytes", Arrays.toString(expectedBuffer),
				Arrays.toString(buffer));
		assertEquals("wrong 2nd position", 4L, f.getFilePointer());
	}
}
