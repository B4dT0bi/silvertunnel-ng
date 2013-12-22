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
	 * Testdata which should be checked against the lookupservice.
	 * 
	 * Format : xx.iii.iii.iii.iii
	 * xx = country code (uppercase)
	 * iii = octet of ip
	 * delimiter = .
	 */
	private static final String [] TESTDATA = {"IT.151.38.39.114"
	                                         , "DE.77.186.10.253"
	                                         , "US.12.25.205.51"
	                                         , "US.64.81.104.131"
	                                         , "CO.200.21.225.82"
	                                         , "RU.37.146.131.149"
	                                         , "US.173.213.78.126"
	                                         , "SE.5.34.241.111"
	                                         , "US.107.10.169.198"};
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

		for (String ipTest : TESTDATA)
		{
			final String [] tmp = ipTest.split("\\.");
			final byte octet1 = (byte) Integer.parseInt(tmp[1]);
			final byte octet2 = (byte) Integer.parseInt(tmp[2]);
			final byte octet3 = (byte) Integer.parseInt(tmp[3]);
			final byte octet4 = (byte) Integer.parseInt(tmp[4]);
			
			final String countryFromLs = ls.getCountry(new byte[] {octet1, octet2, octet3, octet4});
			assertEquals("wrong country for ip " + tmp[1] + "." + tmp[2] + "." + tmp[3] + "." + tmp[4], tmp[0], countryFromLs);
		}

		ls.close();
	}
}
