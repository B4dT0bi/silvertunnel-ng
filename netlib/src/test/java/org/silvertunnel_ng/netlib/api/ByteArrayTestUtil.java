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

package org.silvertunnel_ng.netlib.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to handle input streams, output streams and byte arrays.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class ByteArrayTestUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ByteArrayTestUtil.class);

	/**
	 * Read expectedResponse.length number of bytes from responseIS and compare
	 * it with the expectedResponse.
	 * 
	 * @param log
	 *            if null: do not log
	 * @param msg
	 * @param expectedResponse
	 * @param actualResponseIS
	 * @throws IOException
	 */
	public static void assertByteArrayFromInputStream(Logger log, String msg,
			byte[] expectedResponse, InputStream actualResponseIS)
			throws IOException
	{
		// read the expected number of bytes
		final byte[] response = new byte[expectedResponse.length];
		final int expLen = expectedResponse.length;
		for (int i = 0; i < expLen; i++)
		{
			response[i] = (byte) actualResponseIS.read();
			if (log != null)
			{
				log.info("  read response[" + i + "/" + (expLen - 1) + "]="
						+ response[i] + "('"
						+ ByteArrayUtil.asChar(response[i]) + "') , expected: "
						+ expectedResponse[i] + "('"
						+ ByteArrayUtil.asChar(expectedResponse[i]) + "')");
			}
		}

		ByteArrayTestUtil.assertEquals("wrong response", expectedResponse,
				response);
	}

	public static void assertEquals(String msg, byte[] expected, byte[] actual)
	{
		final String expectedStr = Arrays.toString(expected);
		final String actualStr = Arrays.toString(actual);
		org.testng.AssertJUnit.assertEquals(msg, expectedStr, actualStr);
	}

}
