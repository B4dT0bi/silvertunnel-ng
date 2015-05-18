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

package org.silvertunnel_ng.netlib.layer.mock;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * JUnit test to test closing the MockNetLayer.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class ClosingMockNetLayerLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ClosingMockNetLayerLocalTest.class);

	private volatile boolean threadClosedIS;

	@Test(timeOut = 5000)
	public void testClosingMockByteArrayInputStream() throws Exception
	{
		final byte[] response = new byte[] { 1 };
		final long waitAtTheEndMs = 10000;
		final MockByteArrayInputStream is = new MockByteArrayInputStream(
				response, waitAtTheEndMs);

		// read first byte
		int b = is.read();
		assertEquals("wrong first byte", 1, b);

		// start thread to close the MockByteArrayInputStream after 1 second
		threadClosedIS = false;
		final Thread t = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (final InterruptedException e)
				{
					LOG.info("Thread interrupted");
				}
				threadClosedIS = true;
				is.close();
			}
		};
		t.start();

		// the thread must not be finished
		assertEquals(
				"wrong state threadClosedIS, i.e. the Thread was too fast?",
				false, threadClosedIS);

		// read the second byte: this must block until close
		b = is.read();
		assertEquals("wrong second byte", -1, b);

		// the thread must already be finished
		assertEquals(
				"wrong state threadClosedIS, i.e. the Thread did not yet close the MockByteArrayInputStream",
				true, threadClosedIS);
	}
}
