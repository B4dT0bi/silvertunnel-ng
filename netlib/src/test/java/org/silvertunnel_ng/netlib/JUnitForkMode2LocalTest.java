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



package org.silvertunnel_ng.netlib;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * JUnit test to test the JUnit execution environment test isolation: Each test
 * case class should be executed in a separate JVM. This will be enforced by ant
 * task junit/batchtest with fork="yes" and (default) forkmode="perTest".
 * 
 * The test scenario consists of two test case classes:
 * 
 * @see org.silvertunnel_ng.netlib.JUnitForkMode1LocalTest
 * @see org.silvertunnel_ng.netlib.JUnitForkMode2LocalTest
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class JUnitForkMode2LocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(JUnitForkMode2LocalTest.class);

	@Test
	public void testThatImRunningInMyOwnJVM()
	{
		LOG.info("run testThatImRunningInMyOwnJVM()");
		assertEquals(
				"wrong value of variable \"alreadyExecuted\", i.e. test cases classes are NOT executed in separate JVM (but they should!)",
				false, JUnitForkMode1LocalTest.alreadyExecuted);
		JUnitForkMode1LocalTest.alreadyExecuted = true;
	}
}
