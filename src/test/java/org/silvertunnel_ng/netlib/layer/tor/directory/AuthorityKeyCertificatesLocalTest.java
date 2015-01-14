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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.silvertunnel_ng.netlib.util.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of AuthorityKeyCertificate and AuthorityKeyCertificates.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class AuthorityKeyCertificatesLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(AuthorityKeyCertificatesLocalTest.class);

	private static final Date DATE_20091231 = Util.parseUtcTimestamp("2009-12-31 00:00:00");

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		// install BC, if not already done
		if (Security.getProvider("BC") == null)
		{
			Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
		}
	}

	/**
	 * Test that parsing of the first certificate of
	 * /org/silvertunnel/netlib/layer/tor/example-authority-keys.txt is
	 * successful.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSingleCertificateSuccessfulParsing() throws Exception
	{
		// parse certificate
		final String allCertsStr = FileUtil.readFileFromClasspath("/org/silvertunnel_ng/netlib/layer/tor/example-authority-keys.txt");
		final AuthorityKeyCertificate firstCert = new AuthorityKeyCertificate(
				allCertsStr);

		// check result
		final String expectedFingerprint = "ED03BB616EB2F60BEC80151114BB25CEF515B226";
		assertEquals("wrong fingerprint", expectedFingerprint, firstCert.getDirIdentityKeyDigest().getHex());
		assertEquals("wrong dirKeyPublished", "2010-01-19 13:48:46", Util.formatUtcTimestamp(firstCert.getDirKeyPublished()));
		assertEquals("wrong dirKeyExpires", "2011-01-19 13:48:46", Util.formatUtcTimestamp(firstCert.getDirKeyExpires()));
		LOG.debug("dirSigningKey=" + Encryption.getPEMStringFromRSAPublicKey(firstCert.getDirSigningKey()));
	}

	/**
	 * Test that the modified certificate in
	 * /org/silvertunnel/netlib/layer/tor/example-authority-key-corrupted.txt is
	 * invalid/raises an exception.
	 * 
	 * @throws Exception
	 */
	@Test (expectedExceptions = TorException.class)
	public void testSingeCertificateSignatureCheck() throws Exception
	{
		// parse certificate, must throw TorException
		final String allCertsStr = FileUtil.readFileFromClasspath("/org/silvertunnel_ng/netlib/layer/tor/example-authority-key-corrupted.txt");
		new AuthorityKeyCertificate(allCertsStr);
		fail("expected TorException not thrown");
	}

	/**
	 * Test that parsing of the all certificates of
	 * /org/silvertunnel/netlib/layer/tor/example-authority-keys.txt is
	 * successful.
	 * 
	 * Inclusive test of skipping outdated certificates.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAllCertificatesSuccessfulParsing() throws Exception
	{
		// parse all certificates
		final String allCertsStr = FileUtil.readFileFromClasspath("/org/silvertunnel_ng/netlib/layer/tor/example-authority-keys.txt");
		final AuthorityKeyCertificates allCerts = new AuthorityKeyCertificates(allCertsStr, DATE_20091231);
		final Map<Fingerprint, AuthorityKeyCertificate> all = allCerts.getAuthorityKeyCertificates();

		// check result
		assertEquals("list: invalid number of certificates in allCerts", 7, all.size());
		final String[] allExpectedFingerprintStrs = new String[] {
				"ED03BB616EB2F60BEC80151114BB25CEF515B226",
				"E8A9C45EDE6D711294FADF8E7951F4DE6CA56B58",
				"80550987E1D626E3EBA5E5E75A458DE0626D088C",
				"585769C78764D58426B8B52B6651A5A71137189A",
				"14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4",
				"27B6B5996C426270A5C95488AA5BCEB6BCC86956",
				"D586D18309DED4CD6D57C18FDB97EFA96D330566", };
		for (final String oneExpectedFingerprintStr : allExpectedFingerprintStrs)
		{
			assertTrue("fingerprint missing in result: "
					+ oneExpectedFingerprintStr,
					null != all.get(new FingerprintImpl(DatatypeConverter
							.parseHexBinary(oneExpectedFingerprintStr))));
		}
	}

	/**
	 * Test that the privately created certificate in
	 * /org/silvertunnel/netlib/layer/tor/unauthorized-authority-key.txt can be
	 * parsed and is valid, but it will not be accepted because the fingerprint
	 * is not authorized.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSkippingOfCertificatedWithUnauthorizedFingerprint() throws Exception
	{
		//
		// test preparation: check the test certificate (to avoid
		// misinterpretation of the test result)
		//

		// parse the (unauthorized) certificate, i.e. check that the certificate
		// is valid
		final String allCertsStr = FileUtil.readFileFromClasspath("/org/silvertunnel_ng/netlib/layer/tor/unauthorized-authority-key.txt");
		final AuthorityKeyCertificate firstCert = new AuthorityKeyCertificate(allCertsStr);
		assertEquals("test preparation: wrong fingerprint",
				"C14378EB4ED0B8D6F449F3982298279A44D4D2A0", firstCert.getDirIdentityKeyDigest().getHex());

		// try parse the (unauthorized) certificate as (single-element) list,
		// the fingerprint will be handled as authorized
		final Collection<Fingerprint> patchedAuthorizedAuthorityKeyIdentityKeys = new ArrayList<Fingerprint>();
		patchedAuthorizedAuthorityKeyIdentityKeys
				.add(new FingerprintImpl(
						DatatypeConverter
								.parseHexBinary("80550987E1D626E3EBA5E5E75A458DE0626D088C")));
		AuthorityKeyCertificates allCerts = new AuthorityKeyCertificates(
				allCertsStr, DATE_20091231,
				patchedAuthorizedAuthorityKeyIdentityKeys);
		assertEquals(
				"test preparation: invalid number of certificates in allCerts",
				0, allCerts.getAuthorityKeyCertificates().size());

		//
		// execute the test
		//

		// try parse the (unauthorized) certificate as (single-element) list
		allCerts = new AuthorityKeyCertificates(allCertsStr, DATE_20091231);
		assertEquals("invalid number of certificates in allCerts", 0, allCerts.getAuthorityKeyCertificates().size());
	}
}
