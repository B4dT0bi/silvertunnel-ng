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

import java.io.IOException;
import java.security.Security;
import java.util.Date;

import org.silvertunnel_ng.netlib.util.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test of DirectoryConsensus.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class DirectoryConsensusLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryConsensusLocalTest.class);

	private static final String EXAMPLE_CONSENSUS_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-consensus.txt";
	private static final String EXAMPLE_CONSENSUS_WITHOUT_SIGNATURES_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-consensus-without-signatures.txt";
	private static final String EXAMPLE_CONSENSUS_WITH_INVALID_SIGNATURES_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-consensus-with-invalid-signatures.txt";

	private static final Date EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE = Util.parseUtcTimestamp("2015-05-20 23:30:00");
	private static final Date EXAMPLE_CONSENSUS_INVALID_DATE = Util.parseUtcTimestamp("2010-01-26 00:30:00");

	private static final String EXAMPLE_AUTHORITY_KEYS_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-authority-keys.txt";

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		// install BC, if not already done
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
		{
			Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
		}
	}

	/**
	 * Test parsing of a valid consensus document.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParsingValidConsensus() throws Exception
	{
		// read and parse
		final String directoryConsensusStr = FileUtil.readFileFromClasspath(EXAMPLE_CONSENSUS_PATH);
		final DirectoryConsensus consensus = new DirectoryConsensus(
				directoryConsensusStr,
				getAllExampleAuthorityKeys(EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE),
				EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE);

		// check result
		assertEquals("invalid number of parsed entries", 6289, consensus
				.getFingerprintsNetworkStatusDescriptors().size());

		// check that a specific entry was parsed, with fingerprint
		// hex=7BE6 83E6 5D48 1413 21C5 ED92 F075 C553 64AC 7123/base64=
		// e+aD5l1IFBMhxe2S8HXFU2SscSM SGRqxFrX6FqxIeOhCxZ1/wnIHvw )
		final Fingerprint fingerprint = new FingerprintImpl(
				DatatypeConverter.parseHexBinary("7BE683E65D48141321C5ED92F075C55364AC7123"));
		final RouterStatusDescription desc = consensus
				.getFingerprintsNetworkStatusDescriptors().get(fingerprint);
		assertEquals("one specific result router entry: wrong fingerprint",
				fingerprint, desc.getFingerprint());
		assertEquals("one specific result router entry: wrong nick name",
				"dannenberg", desc.getNickname());
		assertEquals("one specific result router entry: wrong IP address",
				"193.23.244.244", desc.getIp());

		// check the validity checks
		assertTrue(
				"invalid freshUntil=" + consensus.getFreshUntil(),
				consensus.getFreshUntil().before(
						EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE));
		assertTrue("wrong result for needsToBeRefreshed with date="
				+ EXAMPLE_CONSENSUS_INVALID_DATE,
				consensus.needsToBeRefreshed(EXAMPLE_CONSENSUS_INVALID_DATE));
	}

	/**
	 * Test parsing of an invalid consensus document: the document contains no
	 * signatures
	 * 
	 * @throws Exception
	 */
	@Test (expectedExceptions = TorException.class)
	public void testParsingConsensusWithoutSignatures() throws Exception
	{
		// read
		final String directoryConsensusStr = FileUtil.readFileFromClasspath(EXAMPLE_CONSENSUS_WITHOUT_SIGNATURES_PATH);
		// parse
		new DirectoryConsensus(
					directoryConsensusStr,
					getAllExampleAuthorityKeys(EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE),
					EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE);

		// check result
		fail("parsing the consesus was expected to fail");
	}

	/**
	 * Test parsing of an invalid consensus document: the document contains
	 * multiple, but identical (valid) signatures
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParsingConsensusWithInvalidSignatures() throws Exception
	{
		// read
		final String directoryConsensusStr = FileUtil.readFileFromClasspath(EXAMPLE_CONSENSUS_WITH_INVALID_SIGNATURES_PATH);
		try
		{
			// parse
			new DirectoryConsensus(
					directoryConsensusStr,
					getAllExampleAuthorityKeys(EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE),
					EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE);

			// check result
			fail("parsing the consensus was expected to fail");

		}
		catch (final TorException e)
		{
			// expected
			LOG.info("expected exception: " + e);
		}
	}

	/**
	 * Test parsing of an invalid consensus document: the document was
	 * manipulated, i.e. all signature checks must fail
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParsingManipulatedConsensus() throws Exception
	{
		// read and manipulate
		final String validDirectoryConsensusStr = FileUtil.readFileFromClasspath(EXAMPLE_CONSENSUS_PATH);
		final String manipulatedDirectoryConsensusStr = validDirectoryConsensusStr
				.replace("valid-until 2010-01-26 00:00:00",
						 "valid-until 2010-01-26 00:00:01");

		try
		{
			// parse
			new DirectoryConsensus(
					manipulatedDirectoryConsensusStr,
					getAllExampleAuthorityKeys(EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE),
					EXAMPLE_CONSENSUS_VALID_BUT_UNFRESH_DATE);

			// check result
			fail("parsing the consensus was expected to fail");

		}
		catch (final TorException e)
		{
			// expected
			LOG.info("expected exception: " + e);
		}
	}

	// /////////////////////////////////////////////////////
	// helper method(s)
	// /////////////////////////////////////////////////////

	private AuthorityKeyCertificates getAllExampleAuthorityKeys(final Date currentDate) throws TorException, IOException
	{
		final String allCertsStr = FileUtil.readFileFromClasspath(EXAMPLE_AUTHORITY_KEYS_PATH);
		final AuthorityKeyCertificates allCerts = new AuthorityKeyCertificates(allCertsStr, currentDate);
		return allCerts;
	}
}
