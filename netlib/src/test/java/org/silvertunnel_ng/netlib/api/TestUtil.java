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

package org.silvertunnel_ng.netlib.api;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.layer.mock.MockNetSession;

public class TestUtil
{

	/**
	 * 
	 * @param msg
	 * @param mockNetSession
	 * @param expectedReceivedBytes
	 * @param expectedNetAddress
	 * @throws IOException
	 */
	public static void assertMockNetLayerSavedData(String msg,
			MockNetSession mockNetSession, byte[] expectedReceivedBytes,
			NetAddress expectedNetAddress) throws Exception
	{
		// check received bytes
		final byte[] actualReceivedBytes = mockNetSession
				.getHigherLayerNetSocket().getByteArrayOutputStream()
				.toByteArray();
		assertEquals(msg + " (wrong bytes)",
				Arrays.toString(expectedReceivedBytes),
				Arrays.toString(actualReceivedBytes));

		// check address
		final NetAddress actualNetAddress = mockNetSession
				.getProvidedRemoteAddress();
		assertEquals(msg + " (wrong address)", expectedNetAddress,
				actualNetAddress);

	}

	/**
	 * Wait/block until the mockNetSession received the specified number of
	 * bytes.
	 * 
	 * @param mockNetSession
	 * @param minimumNumberOfReceivedBytes
	 */
	public static void waitUntilMinimumNumberOfReceivedBytes(
			MockNetSession mockNetSession, int minimumNumberOfReceivedBytes)
			throws Exception
	{
		while (mockNetSession.getHigherLayerNetSocket()
				.getByteArrayOutputStream().size() < minimumNumberOfReceivedBytes)
		{
			Thread.sleep(100);
		}
	}
}
