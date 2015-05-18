/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2013 silvertunnel.org
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
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.silvertunnel_ng.netlib.util.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test of AuthorityServer.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class AuthorityServersLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(AuthorityServersLocalTest.class);

	/**
	 * Test AuthorityServers.getAuthorityIpAndPorts().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAuthorityIpAndPorts() throws Exception
	{
		// action
		final Collection<String> all = AuthorityServers
				.getAuthorityIpAndPorts();
		LOG.info("AuthorityServers.getAuthorityIpAndPorts().size()="
				+ all.size());

		// check size
		assertEquals("wrong size", 10, all.size());

		// check data
		final String example1 = "193.23.244.244:80";
		assertTrue("does not contain " + example1, all.contains(example1));
		final String example2 = "171.25.193.9:443";
		assertTrue("does not contain " + example2, all.contains(example2));
	}

	/**
	 * Test AuthorityServers.getAuthorityDirIdentityKeyDigests().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAuthorizedAuthorityKeyIdentityKeys() throws Exception
	{
		// action
		final Collection<Fingerprint> all = AuthorityServers
				.getAuthorityDirIdentityKeyDigests();
		LOG.info("AuthorityServers.getAuthorityDirIdentityKeyDigests().size()="
				+ all.size());

		// check result
		final String example1 = "585769C78764D58426B8B52B6651A5A71137189A";
		assertTrue("does not contain " + example1,
				all.contains(new FingerprintImpl(DatatypeConverter
						.parseHexBinary(example1))));
		final String example2 = "D586D18309DED4CD6D57C18FDB97EFA96D330566";
		assertTrue("does not contain " + example2,
				all.contains(new FingerprintImpl(DatatypeConverter
						.parseHexBinary(example2))));
	}

	/**
	 * Test AuthorityServers.getAuthorityRouters().
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAuthorityRouters() throws Exception
	{
		// action
		final Collection<Router> all = AuthorityServers
				.getAuthorityRouters();

		// check size
		assertEquals("wrong size", 10, all.size());

		// check the 1st element
		final Iterator<Router> iter = all.iterator();
		Router r = iter.next();
		assertEquals("wrong 1st element: wrong nickname", "moria1",
				r.getNickname());
		assertEquals("wrong 1st element: wrong address", "128.31.0.39", r
				.getAddress().getHostAddress());
		assertEquals("wrong 1st element: wrong dirPort", 9131, r.getDirPort());
		assertEquals(
				"wrong 1st element: wrong fingerprint",
				new FingerprintImpl(
						DatatypeConverter
								.parseHexBinary("9695DFC35FFEB861329B9F1AB04C46397020CE31")),
				r.getFingerprint());

		// check the 3rd element
		r = iter.next();
		r = iter.next();
		assertEquals("wrong 3rd element: wrong nickname", "dizum",
				r.getNickname());
		assertEquals("wrong 3rd element: wrong address", "194.109.206.212", r
				.getAddress().getHostAddress());
		assertEquals("wrong 3rd element: wrong dirPort", 80, r.getDirPort());
		assertEquals(
				"wrong 3rd element: wrong fingerprint",
				new FingerprintImpl(
						DatatypeConverter
								.parseHexBinary("7EA6EAD6FD83083C538F44038BBFA077587DD755")),
				r.getFingerprint());
	}
}
