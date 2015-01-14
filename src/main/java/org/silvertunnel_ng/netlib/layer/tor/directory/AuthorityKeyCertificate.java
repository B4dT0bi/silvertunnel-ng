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
package org.silvertunnel_ng.netlib.layer.tor.directory;

import java.io.UnsupportedEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel_ng.netlib.util.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object a hold authority dir server key.
 * 
 * @author hapke
 */
public class AuthorityKeyCertificate implements Cloneable
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(AuthorityKeyCertificate.class);

	/**
	 * The raw authority key certificate which has been handed to us. In the
	 * normal case we just return this stored String.
	 */
	private final String authorityKeyCertificateStr;
	/** pattern of a authorityKeyCertificate. */
	private static Pattern pattern;

	/** "fingerprint" = "v3ident" = digest of the authority's dirIdentityKey. */
	private final Fingerprint dirIdentityKeyDigest;
	private final Date dirKeyPublished;
	private final Date dirKeyExpires;
	private final RSAPublicKey dirIdentityKey;
	private final RSAPublicKey dirSigningKey;
	private final Fingerprint dirSigningKeyDigest;

	/**
	 * Initialize in a way that exceptions get logged.
	 */
	static
	{
		try
		{
			pattern = Pattern
					.compile(
							"^(dir-key-certificate-version 3\n"
									+ "fingerprint (\\w+)\n"
									+ "dir-key-published ([0-9: \\-]+)\n"
									+ "dir-key-expires ([0-9: \\-]+)\n"
									+ "dir-identity-key\n(-----BEGIN RSA PUBLIC KEY.*?END RSA PUBLIC KEY-----)\n"
									+ "dir-signing-key\n(-----BEGIN RSA PUBLIC KEY.*?END RSA PUBLIC KEY-----)\n"
									+ "(dir-key-crosscert\n-----BEGIN ID SIGNATURE-----(.*?)-----END ID SIGNATURE-----\n){0,1}"
									+ "dir-key-certification\n)-----BEGIN SIGNATURE-----(.*?)-----END SIGNATURE-----",
							Pattern.DOTALL + Pattern.MULTILINE
									+ Pattern.CASE_INSENSITIVE
									+ Pattern.UNIX_LINES);
		}
		catch (final Exception e)
		{
			LOG.error("could not initialze class AuthorityKeyCertificate", e);
		}
	}

	/**
	 * Extracts all relevant information from the authority key certificate and
	 * saves it in the member variables.
	 * 
	 * @param authorityKeyCertificateStr
	 *            string encoded authority dir key certificate version 3
	 */
	public AuthorityKeyCertificate(String authorityKeyCertificateStr)
			throws TorException
	{
		this.authorityKeyCertificateStr = authorityKeyCertificateStr;

		// parse the authorityKeyCertificateStr
		final Matcher m = pattern.matcher(authorityKeyCertificateStr);
		m.find();

		// parse fingerprint
		final String fingerprintStr = m.group(2);
		dirIdentityKeyDigest = new FingerprintImpl(
				DatatypeConverter.parseHexBinary(fingerprintStr));

		// parse dates
		final String dirKeyPublishedStr = m.group(3);
		dirKeyPublished = Util.parseUtcTimestamp(dirKeyPublishedStr);
		final String dirKeyExpiresStr = m.group(4);
		dirKeyExpires = Util.parseUtcTimestamp(dirKeyExpiresStr);

		// parse keys
		String dirIdentityKeyStr = m.group(5);
		dirIdentityKey = Encryption.extractPublicRSAKey(dirIdentityKeyStr);
		final String dirSigningKeyStr = m.group(6);
		dirSigningKey = Encryption.extractPublicRSAKey(dirSigningKeyStr);
		dirSigningKeyDigest = new FingerprintImpl(
				Encryption.getDigest(Encryption
						.getPKCS1EncodingFromRSAPublicKey(dirSigningKey)));

		// verify identity-key against fingerprint
		try
		{
			final byte[] dirIdentityKeyPkcs = Encryption
					.getPKCS1EncodingFromRSAPublicKey(dirIdentityKey);
			final byte[] dirIdentityKeyHash = Encryption
					.getDigest(dirIdentityKeyPkcs);
			if (!new FingerprintImpl(dirIdentityKeyHash)
					.equals(dirIdentityKeyDigest))
			{
				throw new TorException("dirIdentityKey hash("
						+ new FingerprintImpl(dirIdentityKeyHash)
						+ ")!=fingerprint(" + dirIdentityKeyDigest + ")");
			}
		}
		catch (final TorException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			LOG.warn(
					"error while verify identity-key against fingerprint", e);
			throw new TorException(
					"error while verify identity-key against fingerprint: " + e);
		}

		// check the validity of the signature (and skip dir-key-crosscert)
		final String dirKeyCertificationStr = m.group(9);
		while (dirIdentityKeyStr.length() % 4 != 0)
		{
			dirIdentityKeyStr += "=";
		}
		final byte[] dirKeyCertification = DatatypeConverter
				.parseBase64Binary(dirKeyCertificationStr);
		final String signedDataStr = m.group(1);
		byte[] signedData = null;
		try
		{
			signedData = signedDataStr.getBytes(Util.UTF8);
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.warn("unexpected", e);
		}
		if (!Encryption.verifySignature(dirKeyCertification, dirIdentityKey,
				signedData))
		{
			throw new TorException(
					"dirKeyCertification check failed for fingerprint="
							+ dirIdentityKeyDigest);
		}
	}

	/**
	 * used for debugging purposes.
	 */
	@Override
	public String toString()
	{
		return "AuthorityKeyCertificate(" + "fingerprint="
				+ dirIdentityKeyDigest + ",dirKeyPublished="
				+ Util.formatUtcTimestamp(dirKeyPublished) + ",dirKeyExpires="
				+ Util.formatUtcTimestamp(dirKeyExpires) + ",dirIdentityKey="
				+ dirIdentityKey + ",dirSigningKey=" + dirSigningKey + ")";
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	public String getAuthorityKeyCertificateStr()
	{
		return authorityKeyCertificateStr;
	}

	/**
	 * @return fingerprint
	 */
	public Fingerprint getDirIdentityKeyDigest()
	{
		return dirIdentityKeyDigest;
	}

	public Date getDirKeyPublished()
	{
		return dirKeyPublished;
	}

	public Date getDirKeyExpires()
	{
		return dirKeyExpires;
	}

	public RSAPublicKey getDirIdentityKey()
	{
		return dirIdentityKey;
	}

	public RSAPublicKey getDirSigningKey()
	{
		return dirSigningKey;
	}

	public Fingerprint getDirSigningKeyDigest()
	{
		return dirSigningKeyDigest;
	}

}
