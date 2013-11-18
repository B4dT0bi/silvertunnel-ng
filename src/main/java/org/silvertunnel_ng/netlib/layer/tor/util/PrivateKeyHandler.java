/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel_ng.netlib.layer.tor.util;

import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Date;

import javax.net.ssl.X509KeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * manages private keys for:
 * <ul>
 * <li>identity
 * <li>onion routing
 * <li>hidden services
 * </ul>
 * 
 * @author Lexi Pimenidis
 */
public class PrivateKeyHandler implements X509KeyManager
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(PrivateKeyHandler.class);

	private KeyPair keypair;

	/**
	 * generates a new random key pair on every start.
	 */
	public PrivateKeyHandler()
	{
		try
		{
			final KeyPairGenerator generator = KeyPairGenerator.getInstance(
					"RSA", "BC");
			generator.initialize(1024, new SecureRandom());
			keypair = generator.generateKeyPair();
		}
		catch (final Exception e)
		{
			LOG.error("PrivateKeyHandler: Caught exception: " + e.getMessage());
		}
	}

	public KeyPair getIdentity()
	{
		return keypair;
	}

	@Override
	public String chooseClientAlias(final String[] keyType,
			final Principal[] issuers, Socket socket)
	{
		return Util.MYNAME;
	}

	@Override
	public PrivateKey getPrivateKey(final String alias)
	{
		return keypair.getPrivate();
	}

	@Override
	public String[] getClientAliases(final String keyType,
			final Principal[] issuers)
	{
		final String[] s = new String[1];
		s[0] = "TorJava";
		return s;
	}

	@Override
	public java.security.cert.X509Certificate[] getCertificateChain(
			final String alias)
	{
		try
		{
			final org.bouncycastle.x509.X509V3CertificateGenerator generator = new org.bouncycastle.x509.X509V3CertificateGenerator();
			generator.reset();
			generator.setSerialNumber(BigInteger.valueOf(42));
			generator.setNotBefore(new Date(
					System.currentTimeMillis() - 24L * 3600 * 1000));
			generator.setNotAfter(new Date(System.currentTimeMillis() + 365L
					* 24 * 3600 * 1000));
			generator.setIssuerDN(new org.bouncycastle.asn1.x509.X509Name("CN="
					+ Util.MYNAME));
			generator.setSubjectDN(new org.bouncycastle.asn1.x509.X509Name(
					"CN=" + Util.MYNAME));
			generator.setPublicKey(keypair.getPublic());
			generator.setSignatureAlgorithm("SHA1WITHRSA");
			final java.security.cert.X509Certificate x509 = generator.generate(
					keypair.getPrivate(), "BC");
			final java.security.cert.X509Certificate[] x509s = new java.security.cert.X509Certificate[2];

			// send the same certificate twice works fine with the default
			// implementation of tor!
			// myself:
			x509s[0] = x509;
			// a certificate for myself:
			x509s[1] = x509;

			return x509s;
		}
		catch (final Exception e)
		{
			LOG.error("Caught exception: " + e.getMessage());
		}
		return null;
	}

	@Override
	public String chooseServerAlias(final String keyType,
			final Principal[] issuers, Socket socket)
	{
		return null;
	}

	@Override
	public String[] getServerAliases(final String keyType,
			final Principal[] issuers)
	{
		return null;
	}
}
