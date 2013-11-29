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
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of class RendezvousServiceDescriptor and
 * RendezvousServiceDescriptorService.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class RendezvousServiceDescriptorLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(RendezvousServiceDescriptorLocalTest.class);

	private static final String EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH = "/org/silvertunnel_ng/netlib/layer/tor/directory/example-rendezvous-service-descriptor.txt";
	private static final String EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_INVALID_PATH = "/org/silvertunnel_ng/netlib/layer/tor/directory/example-rendezvous-service-descriptor-wrong-sig.txt";
	private static final String EXAMPLE2_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH = "/org/silvertunnel_ng/netlib/layer/tor/directory/example2-rendezvous-service-descriptor.txt";
	private static final Date EXAMPLE_SD_VALID_DATE = Util.parseUtcTimestamp("2010-03-09 17:00:00");
	private static final Date EXAMPLE_SD_INVALID_DATE = Util.parseUtcTimestamp("2010-03-25 00:30:00");
	private static final Date EXAMPLE2_SD_VALID_DATE = Util.parseUtcTimestamp("2011-02-14 17:25:44");

	// /////////////////////////////////////////////////////
	// test parsing single elements of RendezvousServiceDescriptors
	// /////////////////////////////////////////////////////
	@Test
	public void testGetRendezvousTimePeriod()
	{
		final byte[] rendezvousDescriptorServiceId = { (byte) 143, 1, 2, 3 };
		final Long now = 1188241957L * 1000L;
		final int result = RendezvousServiceDescriptorUtil.getRendezvousTimePeriod(rendezvousDescriptorServiceId, now);
		assertEquals("wrong result of getRendezvousTimePeriod()", 0x000035B9, result);
	}

	@Test
	public void testGetRendezvousTimePeriod_b()
	{
		final byte[] rendezvousDescriptorServiceId = Encoding.parseBase32("duskgytldkxiuqc6");
		final Long now = 0L * 1000L;
		final int result = RendezvousServiceDescriptorUtil.getRendezvousTimePeriod(rendezvousDescriptorServiceId, now);
		assertEquals("wrong result of getRendezvousTimePeriod()", 0, result);
	}

	@Test
	public void testGetRendezvousDescriptorIdBase32()
	{
		final String hiddenServicePermanentIdBase32 = "duskgytldkxiuqc6";
		final int replica = 0;
		final Long now = Util.parseUtcTimestampAsLong("2010-03-02 22:00:00");
		final String result = RendezvousServiceDescriptorUtil.getRendezvousDescriptorIdBase32(hiddenServicePermanentIdBase32, replica, now);
		assertEquals("wrong result of getRendezvousDescriptorIdBase32()", "4gyqu7zqgzn2dkfdyirq2ire4jgnzb24", result);
	}

	@Test
	public void testGetRendezvousDescriptorIdBase32_2()
	{
		final String rendezvousDescriptorServiceIdBase32 = "duskgytldkxiuqc6";
		final int replica = 0;
		final Long now = new Date(0).getTime();
		final String result = RendezvousServiceDescriptorUtil.getRendezvousDescriptorIdBase32(rendezvousDescriptorServiceIdBase32, replica, now);
		assertEquals("wrong result of getRendezvousDescriptorIdBase32()", "txbvqnxzzhy45dbxkjiv4ll56ry7sild", result);
	}

	@Test
	public void testGetRendezvousDescriptorIdBase32_3()
	{
		final String rendezvousDescriptorServiceIdBase32 = "duskgytldkxiuqc6";
		final int replica = 0;
		final Long now = 1188241957L * 1000L;
		final String result = RendezvousServiceDescriptorUtil.getRendezvousDescriptorIdBase32(rendezvousDescriptorServiceIdBase32, replica, now);
		assertEquals("wrong result of getRendezvousDescriptorIdBase32()", "fxvidli3evifukggw66byny7rnwtitdo", result);
	}

	// @Ignore("stable test result cannot be expected here")
	@Test(enabled = false)
	public void testGetRendezvousDescriptorIdBase32_4()
	{
		final String rendezvousDescriptorServiceIdBase32 = "duskgytldkxiuqc6";
		final int replica = 0;
		final Long now = System.currentTimeMillis();
		final String result = RendezvousServiceDescriptorUtil.getRendezvousDescriptorIdBase32(rendezvousDescriptorServiceIdBase32, replica, now);
		assertEquals("wrong result of getRendezvousDescriptorIdBase32()", "[depends-on-the-time]", result);
	}

	// /////////////////////////////////////////////////////
	// test parsing RendezvousServiceDescriptors
	// /////////////////////////////////////////////////////

	@Test
	public void testParseValidRendezvousServiceDescriptor() throws Exception
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance().readFileFromClasspath(EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH);
		final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE_SD_VALID_DATE.getTime());

		// check basics
		assertEquals("wrong version", "2", sd.getVersion());
		assertEquals("wrong publication-time", Util.parseUtcTimestampAsLong("2010-03-09 13:41:53"), sd.getPublicationTime());
		assertEquals("wrong z", "duskgytldkxiuqc6", sd.getZ());

		// check introduction points
		final Collection<SDIntroductionPoint> ips = sd.getIntroductionPoints();
		assertEquals("wrong number introduction points", 3, ips.size());
		final Iterator<SDIntroductionPoint> ipsi = ips.iterator();
		assertEquals("introduction point 1 address", new TcpipNetAddress("192.42.113.248:9001"), ipsi.next().getIpAddressAndOnionPort());
		assertEquals("introduction point 2 address", new TcpipNetAddress("91.143.87.107:443"), ipsi.next().getIpAddressAndOnionPort());
		assertEquals("introduction point 3 address", new TcpipNetAddress("173.74.100.85:9001"), ipsi.next().getIpAddressAndOnionPort());
	}

	@Test
	public void testParseValidRendezvousServiceDescriptorExample2() throws Exception
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance().readFileFromClasspath(EXAMPLE2_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH);
		final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE2_SD_VALID_DATE.getTime());
		LOG.info("sd=" + sd);

		// check basics
		assertEquals("wrong version", "2", sd.getVersion());
		assertEquals("wrong publication-time", EXAMPLE2_SD_VALID_DATE.getTime(), sd.getPublicationTime().longValue());
		assertEquals("wrong z", "4xuwatxuqzfnqjuz", sd.getZ());

		// check introduction points
		final Collection<SDIntroductionPoint> ips = sd.getIntroductionPoints();
		assertEquals("wrong number introduction points", 1, ips.size());
		final Iterator<SDIntroductionPoint> ipsi = ips.iterator();
		assertEquals("introduction point 1 address", new TcpipNetAddress("141.161.20.51:9001"), ipsi.next().getIpAddressAndOnionPort());
	}

	@Test
	public void testParseOutdatedRendezvousServiceDescriptor() throws IOException
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance().readFileFromClasspath(EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH);
		try
		{
			final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE_SD_INVALID_DATE.getTime());
			fail("expected Exception not thrown");

		}
		catch (final TorException e)
		{
			// expected
		}
	}

	@Test
	public void testParseInvalidRendezvousServiceDescriptor() throws IOException
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance()
				.readFileFromClasspath(EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_INVALID_PATH);
		try
		{
			final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE_SD_VALID_DATE.getTime());
			fail("expected Exception not thrown");

		}
		catch (final TorException e)
		{
			// expected
		}
	}

	// /////////////////////////////////////////////////////
	// test formatting RendezvousServiceDescriptors
	// /////////////////////////////////////////////////////

	@Test
	public void testFormatRendezvousServiceDescriptor() throws Exception
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance().readFileFromClasspath(EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH);
		final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE_SD_VALID_DATE.getTime());
		// check basics
		assertEquals("wrong z", "duskgytldkxiuqc6", sd.getZ());

		// check formatting
		final String reformattedRendezvousServiceDescriptorStr = sd.toServiceDescriptorString();
		LOG.info("reformattedRendezvousServiceDescriptorStr=\n" + reformattedRendezvousServiceDescriptorStr);

		// check - but without signature (because the signature cannot be
		// created here because of this missing private key)
		final String expextedRendezvousServiceDescriptorStr = Pattern
				.compile("-----BEGIN SIGNATURE-----.*-----END SIGNATURE-----",
							Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES)
				.matcher(rendezvousServiceDescriptorStr).replaceAll("-----BEGIN SIGNATURE-----\n-----END SIGNATURE-----");
		LOG.info("expextedRendezvousServiceDescriptorStr=" + expextedRendezvousServiceDescriptorStr);
		assertEquals("wrong reformatted RendezvousServiceDescriptorStr", expextedRendezvousServiceDescriptorStr,
						reformattedRendezvousServiceDescriptorStr);
	}

	@Test
	public void testReparsedRendezvousServiceDescriptor() throws Exception
	{
		// read and parse
		final String rendezvousServiceDescriptorStr = FileUtil.getInstance().readFileFromClasspath(EXAMPLE_RENDEZVOUS_SERVICE_DESCRIPTOR_PATH);
		final RendezvousServiceDescriptor sd = new RendezvousServiceDescriptor(rendezvousServiceDescriptorStr, EXAMPLE_SD_VALID_DATE.getTime());
		// check basics
		assertEquals("wrong z", "duskgytldkxiuqc6", sd.getZ());

		final byte[] sdBytes = sd.toByteArray();
		final RendezvousServiceDescriptor sdReparsed = new RendezvousServiceDescriptor(	new String(sdBytes, Util.UTF8),
																						EXAMPLE_SD_VALID_DATE.getTime(),
																						false);
		assertEquals("wrong reformatted RendezvousServiceDescriptorStr", sd.toString(), sdReparsed.toString());
	}
}
