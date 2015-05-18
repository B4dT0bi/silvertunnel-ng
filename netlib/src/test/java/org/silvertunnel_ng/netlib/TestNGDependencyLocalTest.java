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

package org.silvertunnel_ng.netlib;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test the TestNg features which are used to remove JExample.
 * 
 * @author Tobias Boese
 */
public final class TestNGDependencyLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TestNGDependencyLocalTest.class);

	private static int counter = 1;

	/** should be executed as 1st test case. */
	@Test
	public void testA()
	{
		LOG.info("I'm here: testA()/expected to be 1st test case");
		assertEquals("wrong order/counter", 1, counter++);
	}

	/** should be executed as 3rd test case. */
	@Test(dependsOnMethods = {"testC" })
	public void testB()
	{
		LOG.info("I'm here: testB()/expected to be 3rd test case");
		assertEquals("wrong order/counter", 3, counter++);
	}

	/** should be executed as 2nd test case. */
	@Test(dependsOnMethods = {"testA" })
	public void testC()
	{
		LOG.info("I'm here: testC()/expected to be 2nd test case");
		assertEquals("wrong order/counter", 2, counter++);
	}

	/** should be executed as 4th/last test case. */
	@Test(dependsOnMethods = {"testB" })
	public void testD()
	{
		LOG.info("I'm here: testD()/expected to be 4th/last test case");
		assertEquals("wrong order/counter", 4, counter++);
	}
}
