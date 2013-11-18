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

package org.silvertunnel_ng.netlib.layer.tor.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtilLocalTest;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptorUtil;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test class Parsing.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class EncryptionLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(EncryptionLocalTest.class);

	@Test(timeOut = 5000)
	public void testExtractRSAKeyPair() throws Exception
	{
		// parse private key from PEM
		final String privateKeyPEM = FileUtil.getInstance()
				.readFileFromClasspath(
						TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH);
		final RSAKeyPair keyPair = Encryption.extractRSAKeyPair(privateKeyPEM);
		assertNotNull("could not parse prive key from PEM format", keyPair);

		// check the the public part of the key is as expected
		final String z = RendezvousServiceDescriptorUtil
				.calculateZFromPublicKey(keyPair.getPublic());
		assertEquals(
				"public part of the parted key pair does not create the correct z value",
				TorNetLayerUtilLocalTest.EXAMPLE_ONION_DOMAIN_DERIVED_FROM_PRIVATE_KEY,
				z);
	}

	@Test(timeOut = 5000)
	public void testFormattingPrivateKeyAsPEM() throws Exception
	{
		// read one private key as PEM to have an example key
		String privateKeyPEM = FileUtil.getInstance().readFileFromClasspath(
				TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH);
		final RSAKeyPair keyPair = Encryption.extractRSAKeyPair(privateKeyPEM);

		LOG.info("keyPair=" + keyPair);

		// convert private key to PEM and compare with original PEM
		String newPrivateKeyPEM = Encryption
				.getPEMStringFromRSAKeyPair(keyPair);
		final RSAKeyPair newKeyPair = Encryption
				.extractRSAKeyPair(newPrivateKeyPEM);

		LOG.info("\n\nnewKeyPair=" + newKeyPair);

		// replace operation system specific line seperators
		privateKeyPEM = privateKeyPEM.replaceAll("\\r\\n", "\n").replaceAll(
				"\\r", "\n");
		newPrivateKeyPEM = newPrivateKeyPEM.replaceAll("\\r\\n", "\n")
				.replaceAll("\\r", "\n");

		// check the the conversion PEM -> keyPair -> PEM did not change the
		// content
		assertEquals("wrong private key", keyPair.getPrivate(),
				newKeyPair.getPrivate());
		assertEquals("wrong public key", keyPair.getPublic(),
				newKeyPair.getPublic());
		assertEquals("wrong private key PEM string", privateKeyPEM,
				newPrivateKeyPEM);
	}
}
