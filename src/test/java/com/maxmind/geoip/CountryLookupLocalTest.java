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

package com.maxmind.geoip;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import org.testng.annotations.Test;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;

/**
 * Test com.maxmind.geoip.LookupService with reading database from input stream.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class CountryLookupLocalTest
{
	/**
	 * Test if the DB lookup works for known IP<->Country constellations.
	 * @throws IOException when GeoIP Database cannot be found
	 */
	@Test
	public void testGetCountry() throws IOException
	{
		final LookupService ls = new LookupService(getClass()
				.getResourceAsStream(TorConfig.TOR_GEOIPCITY_PATH),
				TorConfig.TOR_GEOIPCITY_MAX_FILE_SIZE);

		assertEquals("wrong country 1", "IT",
				ls.getCountry(new byte[] { (byte) 151, 38, 39, 114 }).getCode());
		assertEquals("wrong country 2", "DE",
				ls.getCountry(new byte[] { 77, (byte) 186, 10, (byte) 253 }).getCode());
		assertEquals("wrong country 3", "US",
				ls.getCountry(new byte[] { 12, 25, (byte) 205, 51 }).getCode());
		assertEquals("wrong country 4", "US",
				ls.getCountry(new byte[] { 64, 81, 104, (byte) 131 }).getCode());
		assertEquals("wrong country 5", "CO",
				ls.getCountry(new byte[] { (byte) 200, 21, (byte) 225, 82 }).getCode());

		ls.close();
	}
}
