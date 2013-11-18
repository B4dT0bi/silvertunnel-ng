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

package org.silvertunnel_ng.netlib.layer.tor.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.layer.tor.common.TorX509TrustManager;
import org.testng.annotations.Test;

/**
 * Test of TorX509TrustManager.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class TorX509TrustManagerLocalTest
{
	/** */
	private static Pattern cnPattern = TorX509TrustManager.cnPattern;

	@Test
	public void testCnPattern1()
	{
		test("CN=www.abcd.de, XY=xyz", "www.abcd.de");
	}

	@Test
	public void testCnPattern2()
	{
		test("CN=www.foo.de", "www.foo.de");
	}

	@Test
	public void testCnPattern3()
	{
		test("AB=bar, CN=www.abcd.de, XY=xyz", "www.abcd.de");
	}

	private void test(final String dnName, final String expectedDnMatch)
	{
		final Matcher dnNameMatch = cnPattern.matcher(dnName);
		assertEquals(true, dnNameMatch.matches());
		assertEquals(expectedDnMatch, dnNameMatch.group(1));
	}
}
