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
package org.silvertunnel_ng.netlib;


import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import jdepend.framework.JDepend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * JUnit test to to enforce dependency rules.
 * 
 * More examples: http://clarkware.com/software/JDepend.html#junit
 * 
 * @author hapke
 */
public class DependencyLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DependencyLocalTest.class);
	private static JDepend jdepend;

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		jdepend = new JDepend();
		jdepend.addDirectory("target/classes");
	}

	/**
	 * Tests that a package dependency cycle does not exist for any of the
	 * analyzed packages.
	 */
	@Test(timeOut = 20000)
	public void testDependencyCyclesAllPackages()
	{
		final Collection<?> packages = jdepend.analyze();
		LOG.info("nr of scanned packages : " + packages.size());
		assertEquals(
				"Cycles exist, for details run: \"ant clean build build-test jdepend\", packages="
						+ packages, false, jdepend.containsCycles());
	}
}
