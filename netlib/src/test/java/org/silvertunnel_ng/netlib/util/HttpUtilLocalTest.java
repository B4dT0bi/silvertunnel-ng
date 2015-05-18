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

package org.silvertunnel_ng.netlib.util;

import static org.testng.AssertJUnit.assertEquals;

import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.testng.annotations.Test;

/**
 * Test of class HttpUtil.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class HttpUtilLocalTest
{

	@Test(timeOut = 1000)
	public void testdecodeChunkedHttpResponse1() throws Exception
	{
		final String chunked = "14\n\rHalloHalloHalloHallo\n\r0\n\r";
		final String expectedUnchunked = "HalloHalloHalloHallo";

		HttpUtil.getInstance();
		assertEquals("wrong result", expectedUnchunked, new String(HttpUtil
				.decodeChunkedHttpResponse(chunked.getBytes(Util.UTF8))));
	}

	@Test(timeOut = 1000)
	public void testdecodeChunkedHttpResponse2() throws Exception
	{
		final String chunked = "9\nHallo\nIhr\n1\n\n3\nda!\n0\n";
		final String expectedUnchunked = "Hallo\nIhr\nda!";

		HttpUtil.getInstance();
		assertEquals("wrong result", expectedUnchunked, new String(HttpUtil
				.decodeChunkedHttpResponse(chunked.getBytes(Util.UTF8))));
	}
}
