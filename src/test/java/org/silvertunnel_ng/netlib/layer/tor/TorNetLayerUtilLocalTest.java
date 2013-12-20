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

package org.silvertunnel_ng.netlib.layer.tor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;

import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test that reading system properties in TorConfic static constructor works.
 * 
 * These tests are not executed by default.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class TorNetLayerUtilLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorNetLayerUtilLocalTest.class);

	private static final String TEMPDIR = System.getProperty("java.io.tmpdir");
	private static final String TEST_SUB_DIR_NAME = "testHiddenServiceDir";

	private static TorNetLayerUtil torNetLayerUtil = TorNetLayerUtil.getInstance();

	public static final String EXAMPLE_PRIVATE_KEY_PEM_PATH = "/org/silvertunnel_ng/netlib/layer/tor/util/example-private-key-PEM.txt";
	public static final String EXAMPLE_ONION_DOMAIN_DERIVED_FROM_PRIVATE_KEY = "4xuwatxuqzfnqjuz";

	@Test(timeOut = 5000)
	public void testCreationOfNewHiddenSericePrivateNetAddresses() throws Exception
	{
		// 1st creation
		final TorHiddenServicePrivateNetAddress netAddress1 = torNetLayerUtil.createNewTorHiddenServicePrivateNetAddress();
		LOG.info("new hidden service netAddress1=" + netAddress1);
		assertNotNull("invalid netAddress1=null", netAddress1);
		LOG.debug("new hidden service netAddress1.getPrivateKey()=" + netAddress1.getPrivateKey());

		// 2nd creation
		final TorHiddenServicePrivateNetAddress netAddress2 = torNetLayerUtil.createNewTorHiddenServicePrivateNetAddress();
		LOG.info("new hidden service netAddress2=" + netAddress2);
		assertNotNull("invalid netAddress2=null", netAddress2);
		LOG.debug("new hidden service netAddress2.getPrivateKey()=" + netAddress1.getPrivateKey());

		// check that both hidden services are different
		assertFalse("new netAddress1=new netAddress2", netAddress1.equals(netAddress2));
	}

	@Test(timeOut = 5000)
	public void testParsingTorsOriginalHiddenSericePrivateNetAddressInfo() throws Exception
	{
		// read the Strings
		final String originalTorPrivateKeyPEMStr = FileUtil.readFileFromClasspath(EXAMPLE_PRIVATE_KEY_PEM_PATH);
		LOG.info("originalTorPrivateKeyPEMStr=" + originalTorPrivateKeyPEMStr);
		final String originalTorHostnameStr = EXAMPLE_ONION_DOMAIN_DERIVED_FROM_PRIVATE_KEY + ".onion";

		// parse and check
		final boolean checkHostname = true;
		final TorHiddenServicePrivateNetAddress netAddress = torNetLayerUtil
				.parseTorHiddenServicePrivateNetAddressFromStrings(
						originalTorPrivateKeyPEMStr, originalTorHostnameStr,
						checkHostname);

		// show result
		LOG.info("netAddress=" + netAddress);
		LOG.debug("netAddress.getPrivateKey()=" + netAddress.getPrivateKey());
	}

	@Test(timeOut = 5000)
	public void testWritingAndReadingHiddenSericePrivateNetAddressInfo() throws Exception
	{
		// create new NetAddress
		final TorHiddenServicePrivateNetAddress netAddressOriginal = torNetLayerUtil.createNewTorHiddenServicePrivateNetAddress();
		LOG.info("new hidden service netAddressOriginal=" + netAddressOriginal);
		LOG.debug("new hidden service netAddressOriginal=" + netAddressOriginal.toStringDetails());

		// prepare directory
		final File directory = new File(TEMPDIR, TEST_SUB_DIR_NAME);
		directory.mkdir();

		// write to directory
		torNetLayerUtil.writeTorHiddenServicePrivateNetAddressToFiles(directory, netAddressOriginal);

		// read from directory
		final TorHiddenServicePrivateNetAddress netAddressRead = torNetLayerUtil
				.readTorHiddenServicePrivateNetAddressFromFiles(directory, true);
		LOG.info("new hidden service netAddressRead=" + netAddressRead);
		LOG.debug("new hidden service netAddressRead=" + netAddressRead.toStringDetails());

		// check result
		assertEquals("TorHiddenServicePrivateNetAddress changed after writing and reading",
				netAddressOriginal, netAddressRead);
	}
}
