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

package org.silvertunnel_ng.netlib.layer.tor;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.RSAKeyPair;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layer over Tor network: tunnels (TCP/IP) network traffic through the Tor
 * anonymity network.
 * 
 * @author hapke
 */
public final class TorNetLayerUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorNetLayerUtil.class);

	private static final String FILENAME_HOSTNAME = "hostname";
	private static final String FILENAME_PRIVATE_KEY = "private_key";

	private static TorNetLayerUtil instance = new TorNetLayerUtil();

	/**
	 * @return singleton instance
	 */
	public static TorNetLayerUtil getInstance()
	{
		return instance;
	}
	/** */
	protected TorNetLayerUtil()
	{
	}

	/**
	 * Read the private key (and implicitly the public key) of a hidden service
	 * from files in a directory structured in Tor's standard hidden service
	 * directory layout: [directoryPath]/hostname (file that contains the public
	 * .onion hostname of the hidden service) [directoryPath]/private_key (file
	 * that contains the private hidden service key in PEM ASCII format).
	 * 
	 * @param directory
	 *            path of the directory; not null
	 * @param checkHostname
	 *            false=ignore hostname (i.e. hostname will be derived from
	 *            private key); true=check the correct content of the hostname
	 *            file; throw an UnknownHostException if the hostname does not
	 *            belong to the private key
	 * @return the TorHiddenServicePrivateNetAddress of the service
	 * @throws UnknownHostException
	 *             if checkHostnameFile=true and the ostname does not belong to
	 *             the private key
	 * @throws IOException
	 *             in the case of an error
	 */
	public TorHiddenServicePrivateNetAddress readTorHiddenServicePrivateNetAddressFromFiles(final File directory, 
	                                                                                        final boolean checkHostname) 
						throws UnknownHostException,
							   IOException
	{
		// read private key
		final File privateKeyFile = new File(directory, FILENAME_PRIVATE_KEY);
		final String privateKeyStr = FileUtil.readFile(privateKeyFile);

		// read hostname
		final File hostnameFile = new File(directory, FILENAME_HOSTNAME);
		final String hostnameStr = FileUtil.readFile(hostnameFile);

		// do the rest
		return parseTorHiddenServicePrivateNetAddressFromStrings(privateKeyStr, hostnameStr, checkHostname);
	}

	/**
	 * Parse private key (and implicitly the public key) of a hidden service
	 * from PEM string.
	 * 
	 * @param privateKeyPEMStr
	 *            private hidden service key in PEM ASCII format
	 * @param hostnameStr
	 *            the public .onion hostname of the hidden service
	 * @param checkHostname
	 *            false=ignore hostname (i.e. hostname will be derived from
	 *            private key); true=check the correct content of the hostname
	 *            file; throw an UnknownHostException if the hostname does not
	 *            belong to the private key
	 * @return the TorHiddenServicePrivateNetAddress of the service
	 * @throws UnknownHostException
	 * @throws IOException
	 *             in the case of an error
	 */
	public TorHiddenServicePrivateNetAddress parseTorHiddenServicePrivateNetAddressFromStrings(
			final String privateKeyPEMStr, 
			final String hostnameStr, 
			final boolean checkHostname)
			throws UnknownHostException, IOException
	{
		// create result
		final RSAKeyPair keyPair = Encryption.extractRSAKeyPair(privateKeyPEMStr);
		final TorHiddenServicePrivateNetAddress result = new TorHiddenServicePrivateNetAddress(keyPair.getPublic(), keyPair.getPrivate());

		// check hostnameStr parameter
		final String hostnameLowerStr = (hostnameStr == null) ? null : hostnameStr.toLowerCase();
		final boolean isHostnameOK = result.getPublicOnionHostname().equals(hostnameLowerStr);
		if (!isHostnameOK)
		{
			final String msg = "hostname=" + hostnameStr
					+ " does not belong to hidden service - \""
					+ result.getPublicOnionHostname()
					+ "\" was expected with PEM=" + privateKeyPEMStr;
			if (checkHostname)
			{
				throw new UnknownHostException(msg);
			}
			else
			{
				if (hostnameStr != null)
				{
					LOG.debug(msg);
				}
			}
		}

		return result;
	}

	/**
	 * Write private key (and implicitly the public key) of a hidden service
	 * from files in a directory structured in Tor's standard hidden service
	 * directory layout: [directoryPath]/hostname (file that contains the public
	 * .onion hostname of the hidden service) [directoryPath]/private_key (file
	 * that contains the private hidden service key in PEM ASCII format)
	 * 
	 * To check the created private key file, the openssl command line tool can
	 * be used: openssl rsa -noout -text -in [directoryPath]/private_key must
	 * show a valid private key without error messages.
	 * 
	 * @param directory
	 *            path of the directory; not null
	 * @param netAddress
	 *            netAddress with the private key
	 * @throws IOException
	 *             in the case of an error
	 */
	public void writeTorHiddenServicePrivateNetAddressToFiles(final File directory,
	                                                          final TorHiddenServicePrivateNetAddress netAddress) 
	                                                        		  throws IOException
	{
		// write private key
		final String pemStr = Encryption.getPEMStringFromRSAKeyPair(new RSAKeyPair(netAddress.getPublicKey(), 
		                                                                           netAddress.getPrivateKey()));
		final File privateKeyFile = new File(directory, FILENAME_PRIVATE_KEY);
		FileUtil.writeFile(privateKeyFile, pemStr);

		// write hostname
		final File hostnameFile = new File(directory, FILENAME_HOSTNAME);
		FileUtil.writeFile(hostnameFile, netAddress.getPublicOnionHostname());
	}

	/**
	 * Create private and public key for a new Tor hidden service.
	 * 
	 * The result should be stored on a persistent media before use, to be able to
	 * start the same hidden service on the same address after a restart.
	 * 
	 * @return new private and public key
	 */
	public TorHiddenServicePrivateNetAddress createNewTorHiddenServicePrivateNetAddress()
	{
		final RSAKeyPair keyPair = Encryption.createNewRSAKeyPair();
		return new TorHiddenServicePrivateNetAddress(keyPair.getPublic(), keyPair.getPrivate());
	}
}
