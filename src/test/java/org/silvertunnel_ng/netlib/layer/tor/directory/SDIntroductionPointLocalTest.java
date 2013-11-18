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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of class SDIntroductionPoint.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SDIntroductionPointLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SDIntroductionPointLocalTest.class);

	private static final String EXAMPLE_INTRODUCTION_POINTS_DESCRIPTOR_PATH = "/org/silvertunnel_ng/netlib/layer/tor/directory/example-introduction-points-descriptor.txt";

	@Test(timeOut = 2000)
	public void testParseValidIntrductionPointsDescriptor() throws Exception
	{
		// read and parse
		final String introductionPointsStr = FileUtil.getInstance()
				.readFileFromClasspath(
						EXAMPLE_INTRODUCTION_POINTS_DESCRIPTOR_PATH);
		final Collection<SDIntroductionPoint> ips = SDIntroductionPoint
				.parseMultipleIntroductionPoints(introductionPointsStr);

		// check introduction points
		assertEquals("wrong number introduction points", 3, ips.size());
		final Iterator<SDIntroductionPoint> ipsi = ips.iterator();
		assertEquals("introduction point 1 address", new TcpipNetAddress(
				"192.42.113.248:9001"), ipsi.next().getIpAddressAndOnionPort());
		assertEquals("introduction point 2 address", new TcpipNetAddress(
				"91.143.87.107:443"), ipsi.next().getIpAddressAndOnionPort());
		assertEquals("introduction point 3 address", new TcpipNetAddress(
				"173.74.100.85:9001"), ipsi.next().getIpAddressAndOnionPort());
	}

	@Test(timeOut = 2000)
	public void testFormatIntrductionPointsDescriptor() throws Exception
	{
		// read, parse and simple check
		final String introductionPointsStr = FileUtil.getInstance()
				.readFileFromClasspath(
						EXAMPLE_INTRODUCTION_POINTS_DESCRIPTOR_PATH);
		final Collection<SDIntroductionPoint> ips = SDIntroductionPoint
				.parseMultipleIntroductionPoints(introductionPointsStr);
		assertEquals("wrong number introduction points", 3, ips.size());

		// format to String
		final String reformattedIntroductionPointsStr = SDIntroductionPoint
				.formatMultipleIntroductionPoints(ips);
		assertEquals("wrong reformatted introductionPointsStr",
				introductionPointsStr, reformattedIntroductionPointsStr);
	}
}
