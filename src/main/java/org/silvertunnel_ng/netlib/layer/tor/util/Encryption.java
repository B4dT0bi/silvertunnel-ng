/*
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.jce.provider.JCERSAPrivateKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class contains utility functions concerning encryption.
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */
public class Encryption
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Encryption.class);

	/** digest algorithm used. */
	public static final String DIGEST_ALGORITHM = "SHA-1";
	/** asymetric algorithm. */
	private static final String PK_ALGORITHM = "RSA";

	static
	{
		try
		{
			// install BC, if not already done
			if (Security.getProvider("BC") == null) // TODO : get rid of bouncycastle...
			{
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				// Security.insertProviderAt(new
				// org.bouncycastle.jce.provider.BouncyCastleProvider(),2);
			}
		}
		catch (final Throwable t)
		{
			LOG.error("Cannot initialize class Encryption", t);
		}
	}

	/**
	 * returns the SHA-1 of the input.
	 * 
	 * @param input
	 *            byte array
	 * @return digest value
	 */
	public static byte[] getDigest(final byte[] input)
	{
		return getDigest(DIGEST_ALGORITHM, input);
	}

	/**
	 * returns the digest of the input.
	 * 
	 * @param algorithm
	 *            e.g. "SHA-1"
	 * @param input
	 * @return digest value
	 */
	public static byte[] getDigest(final String algorithm, final byte[] input)
	{
		try
		{
			final MessageDigest md = MessageDigest.getInstance(algorithm);
			// md.reset(); // removed as it should not be needed.
			md.update(input);
			return md.digest();

		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return implementation of the SHA-1 message digest; reset() already
	 *         called
	 */
	public static MessageDigest getMessagesDigest()
	{
		try
		{
			final MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			return md;

		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculate the digest but do not touch md.
	 * 
	 * @param md
	 *            the {@link MessageDigest} to be cloned
	 * @return the digest, calculated with a clone of md
	 */
	public static byte[] intermediateDigest(final MessageDigest md)
	{
		try
		{
			// Make a clone because #digest() will reset the MessageDigest
			// instance
			// and we want to be able to use this class for running digests on
			// circuits
			final MessageDigest mdClone = (MessageDigest) md.clone();
			return mdClone.digest();
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * checks signature of PKCS1-padded SHA1 hash of the input
	 * 
	 * Hint: A different implementation of this method can be found in the svn
	 * history revision<=229.
	 * 
	 * @param signature
	 *            signature to check
	 * @param signingKey
	 *            public key from signing
	 * @param input
	 *            byte array, signature is made over
	 * 
	 * @return true, if the signature is correct
	 * 
	 */
	public static boolean verifySignature(final byte[] signature, final RSAPublicKeyStructure signingKey, final byte[] input)
	{
		final byte[] hash = getDigest(input);

		try
		{
			final RSAKeyParameters myRSAKeyParameters = new RSAKeyParameters(false, signingKey.getModulus(), signingKey.getPublicExponent());

			final PKCS1Encoding pkcsAlg = new PKCS1Encoding(new RSAEngine());
			pkcsAlg.init(false, myRSAKeyParameters);

			final byte[] decryptedSignature = pkcsAlg.processBlock(signature, 0, signature.length);

			return Arrays.equals(hash, decryptedSignature);

		}
		catch (final Exception e)
		{
			LOG.warn("unexpected", e);
			return false;
		}
	}

	/**
	 * checks row signature.
	 * 
	 * @param signature
	 *            signature to check
	 * @param signingKey
	 *            public key from signing
	 * @param data
	 *            byte array, signature is made over
	 * 
	 * @return true, if the signature is correct
	 * 
	 */

	public static boolean verifySignature(final byte[] signature, final PublicKey signingKey, final byte[] data)
	{
		final byte[] dataDigest = getDigest(data);
		return verifySignatureWithHash(signature, signingKey, dataDigest);
	}

	/**
	 * checks row signature.
	 * 
	 * @param signature
	 *            signature to check
	 * @param signingKey
	 *            public key from signing
	 * @param dataDigest
	 *            byte array, the already calculated digest
	 * 
	 * @return true, if the signature is correct
	 * 
	 */

	public static boolean verifySignatureWithHash(final byte[] signature, final PublicKey signingKey, final byte[] dataDigest)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(PK_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, signingKey);
			byte[] decryptedDigest = cipher.doFinal(signature);
			
			if (decryptedDigest != null && dataDigest != null && decryptedDigest.length > dataDigest.length)
			{
				// try to fix bug in security calculation with OpenJDK-6 java
				// web start (ticket #59)
				LOG.warn("verifySignature(): try to fix bug in security calculation with OpenJDK-6 java web start (ticket #59)");
				LOG.warn("verifySignature(): original decryptedDigest=" + Encoding.toHexString(decryptedDigest));
				LOG.warn("verifySignature(): dataDigest              =" + Encoding.toHexString(dataDigest));
				final byte[] fixedDecryptedDigest = new byte[dataDigest.length];
				System.arraycopy(decryptedDigest, decryptedDigest.length - dataDigest.length, fixedDecryptedDigest, 0, dataDigest.length);
				decryptedDigest = fixedDecryptedDigest;
			}

			final boolean verificationSuccessful = Arrays.equals(decryptedDigest, dataDigest);
			if (!verificationSuccessful)
			{
				LOG.info("verifySignature(): decryptedDigest=" + Encoding.toHexString(decryptedDigest));
				LOG.info("verifySignature(): dataDigest     =" + Encoding.toHexString(dataDigest));
			}

			return verificationSuccessful;

		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * sign some data using a private key and PKCS#1 v1.5 padding.
	 * 
	 * @param data
	 *            the data to be signed
	 * @param signingKey
	 *            the key to sign the data
	 * @return a signature
	 */
	public static byte[] signData(final byte[] data, final RSAKeyParameters signingKey)
	{
		try
		{
			final byte[] hash = Encryption.getDigest(data);
			final PKCS1Encoding pkcs1 = new PKCS1Encoding(new RSAEngine());
			pkcs1.init(true, signingKey);
			return pkcs1.processBlock(hash, 0, hash.length);
		}
		catch (final InvalidCipherTextException e)
		{
			LOG.warn("Common.signData(): " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * sign some data using a private key and PKCS#1 v1.5 padding.
	 * 
	 * @param data
	 *            the data to be signed
	 * @param signingKey
	 *            the key to sign the data
	 * @return a signature
	 */
	public static byte[] signData(final byte[] data, final PrivateKey signingKey)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(PK_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, signingKey);
			return cipher.doFinal(getDigest(DIGEST_ALGORITHM, data));

		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** used to encode a signature in PEM. */
	public static String binarySignatureToPEM(final byte[] signature)
	{
		String sigB64 = DatatypeConverter.printBase64Binary(signature);
		final StringBuffer sig = new StringBuffer();

		sig.append("-----BEGIN SIGNATURE-----\n");
		while (sigB64.length() > 64)
		{
			sig.append(sigB64.substring(0, 64) + "\n");
			sigB64 = sigB64.substring(64);
		}
		sig.append(sigB64 + "\n");
		sig.append("-----END SIGNATURE-----\n");
		return sig.toString();
	}

	/**
	 * makes RSA public key from PEM string.
	 * 
	 * @param s
	 *            PEM string that contains the key
	 * @return
	 * @see JCERSAPublicKey
	 */
	public static RSAPublicKey extractPublicRSAKey(final String s)
	{
		RSAPublicKey theKey;
		try
		{
			final PEMReader reader = new PEMReader(new StringReader(s));
			final Object o = reader.readObject();
			reader.close();
			if (!(o instanceof JCERSAPublicKey))
			{
				throw new IOException("Encryption.extractPublicRSAKey: no public key found in string '" + s + "'");
			}
			final JCERSAPublicKey JCEKey = (JCERSAPublicKey) o;
			theKey = getRSAPublicKey(JCEKey.getModulus(), JCEKey.getPublicExponent());

		}
		catch (final Exception e)
		{
			LOG.warn("Encryption.extractPublicRSAKey: Caught exception:" + e.getMessage());
			theKey = null;
		}

		return theKey;
	}

	/**
	 * makes RSA private key from PEM string.
	 * 
	 * @param s
	 *            PEM string that contains the key
	 * @return
	 * @see JCERSAPublicKey
	 */
	public static RSAKeyPair extractRSAKeyPair(final String s)
	{
		RSAKeyPair rsaKeyPair;
		try
		{
			// parse
			final PEMReader reader = new PEMReader(new StringReader(s));
			final Object o = reader.readObject();
			reader.close();
			// check types
			if (!(o instanceof KeyPair))
			{
				throw new IOException("Encryption.extractRSAKeyPair: no private key found in string '" + s + "'");
			}
			final KeyPair keyPair = (KeyPair) o;
			if (!(keyPair.getPrivate() instanceof JCERSAPrivateKey))
			{
				throw new IOException("Encryption.extractRSAKeyPair: no private key found in key pair of string '" + s + "'");
			}
			if (!(keyPair.getPublic() instanceof JCERSAPublicKey))
			{
				throw new IOException("Encryption.extractRSAKeyPair: no public key found in key pair of string '" + s + "'");
			}

			// convert keys and pack them together into a key pair
			final RSAPrivateCrtKey privateKey = (JCERSAPrivateCrtKey) keyPair.getPrivate();
			LOG.debug("JCEPrivateKey={}", privateKey);
			final RSAPublicKey publicKey = (JCERSAPublicKey) keyPair.getPublic();
			rsaKeyPair = new RSAKeyPair(publicKey, privateKey);

		}
		catch (final Exception e)
		{
			LOG.warn("Encryption.extractPrivateRSAKey: Caught exception:" + e.getMessage());
			rsaKeyPair = null;
		}

		return rsaKeyPair;
	}

	/**
	 * Converts RSA private key to PEM string.
	 * 
	 * @param rsaKeyPair
	 * 
	 * @return PEM string
	 */
	public static String getPEMStringFromRSAKeyPair(final RSAKeyPair rsaKeyPair)
	{
		final StringWriter pemStrWriter = new StringWriter();
		final PEMWriter pemWriter = new PEMWriter(pemStrWriter);
		try
		{
			final KeyPair keyPair = new KeyPair(rsaKeyPair.getPublic(), rsaKeyPair.getPrivate());
			// pemWriter.writeObject(keyPair);
			pemWriter.writeObject(keyPair.getPrivate());
			// pemWriter.flush();
			pemWriter.close();

		}
		catch (final IOException e)
		{
			LOG.warn("Caught exception:" + e.getMessage());
			return "";
		}

		return pemStrWriter.toString();
	}

	/**
	 * Create a key based on the parameters.
	 * 
	 * @param modulus
	 * @param publicExponent
	 * @return the key
	 */
	public static RSAPublicKey getRSAPublicKey(final BigInteger modulus, final BigInteger publicExponent)
	{
		try
		{
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a key based on the parameters.
	 * 
	 * @param modulus
	 * @param privateExponent
	 * @return the key
	 */
	public static RSAPrivateKey getRSAPrivateKey(final BigInteger modulus, final BigInteger privateExponent)
	{
		try
		{
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
		}
		catch (final GeneralSecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * makes RSA public key from bin byte array.
	 * 
	 * @param b
	 *            byte array that contains the key
	 * @return
	 * @see JCERSAPublicKey
	 */
	public static RSAPublicKey extractBinaryRSAKey(final byte[] b)
	{
		RSAPublicKey theKey;

		try
		{
			final ASN1InputStream ais = new ASN1InputStream(b);
			final Object asnObject = ais.readObject();
			final ASN1Sequence sequence = (ASN1Sequence) asnObject;
			final RSAPublicKeyStructure tempKey = new RSAPublicKeyStructure(sequence);
			theKey = getRSAPublicKey(tempKey.getModulus(), tempKey.getPublicExponent());
			ais.close();
		}
		catch (final IOException e)
		{
			LOG.warn("Caught exception:" + e.getMessage());
			theKey = null;
		}

		return theKey;
	}

	/**
	 * copy from one format to another.
	 */
	public static RSAPublicKey getRSAPublicKey(final JCERSAPublicKey jpub)
	{
		return getRSAPublicKey(jpub.getModulus(), jpub.getPublicExponent());
	}

	/**
	 * converts a RSAPublicKey into PKCS1-encoding (ASN.1).
	 * 
	 * @param pubKeyStruct
	 * @see JCERSAPublicKey
	 * @return PKCS1-encoded RSA PUBLIC KEY
	 */
	public static byte[] getPKCS1EncodingFromRSAPublicKey(final RSAPublicKey pubKeyStruct)
	{
		try
		{
			final RSAPublicKeyStructure myKey = new RSAPublicKeyStructure(pubKeyStruct.getModulus(), pubKeyStruct.getPublicExponent());
			final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			final ASN1OutputStream aOut = new ASN1OutputStream(bOut);
			aOut.writeObject(myKey.toASN1Object());
			aOut.close();
			return bOut.toByteArray();
		}
		catch (final Exception e)
		{
			return null;
		}
	}

	/**
	 * converts a JCERSAPublicKey into PEM/PKCS1-encoding.
	 * 
	 * @param rsaPublicKey
	 * @see RSAPublicKeyStructure
	 * @return PEM-encoded RSA PUBLIC KEY
	 */
	public static String getPEMStringFromRSAPublicKey(final RSAPublicKey rsaPublicKey)
	{

		// mrk: this was awful to program. Remeber: There are two entirely
		// different
		// standard formats for rsa public keys. Bouncy castle does only support
		// the
		// one we can't use for TOR directories.

		final StringBuffer tmpDirSigningKey = new StringBuffer();

		try
		{

			tmpDirSigningKey.append("-----BEGIN RSA PUBLIC KEY-----\n");

			final byte[] base64Encoding = Base64.encode(getPKCS1EncodingFromRSAPublicKey(rsaPublicKey));
			for (int i = 0; i < base64Encoding.length; i++)
			{
				tmpDirSigningKey.append((char) base64Encoding[i]);
				if (((i + 1) % 64) == 0)
				{
					tmpDirSigningKey.append("\n");
				}
			}
			tmpDirSigningKey.append("\n");

			tmpDirSigningKey.append("-----END RSA PUBLIC KEY-----\n");
		}
		catch (final Exception e)
		{
			return null;
		}

		return tmpDirSigningKey.toString();
	}

	/**
	 * encrypt data with asymmetric key. create asymmetrical encrypted data:<br>
	 * <ul>
	 * <li>OAEP padding [42 bytes] (RSA-encrypted)
	 * <li>Symmetric key [16 bytes] FIXME: we assume that we ALWAYS need this
	 * <li>First part of data [70 bytes]
	 * <li>Second part of data [x-70 bytes] (Symmetrically encrypted)
	 * <ul>
	 * encrypt and store in result
	 * 
	 * @param pub
	 * @param symmetricKey
	 *            AES key
	 * @param data
	 *            to be encrypted, needs currently to be at least 70 bytes long
	 * @return the first half of the key exchange, ready to be send to the other
	 *         partner
	 */
	public static byte[] asymEncrypt(final RSAPublicKey pub, final byte[] symmetricKey, final byte[] data) throws TorException
	{
		if (data == null)
		{
			throw new NullPointerException("can't encrypt NULL data");
		}

		HybridEncryption hybridEncryption = new HybridEncryption();
		final byte[] result = hybridEncryption.encrypt(data, pub, symmetricKey);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Encryption.asymEncrypt(): unencrypted payload:\n" + Encoding.toHexString(data, 100));
			LOG.debug("Encryption.asymEncrypt(): encrypted payload:\n" + Encoding.toHexString(result, 100));
		}
		return result;
	}

	/**
	 * decrypt data with asymmetric key. create asymmetrically encrypted data:<br>
	 * <ul>
	 * <li>OAEP padding [42 bytes] (RSA-encrypted)
	 * <li>Symmetric key [16 bytes]
	 * <li>First part of data [70 bytes]
	 * <li>Second part of data [x-70 bytes] (Symmetrically encrypted)
	 * <ul>
	 * encrypt and store in result
	 * 
	 * @param priv
	 *            key to use for decryption
	 * @param data
	 *            to be decrypted, needs currently to be at least 70 bytes long
	 * @return raw data
	 */
	public static byte[] asymDecrypt(final RSAPrivateKey priv, final byte[] data) throws TorException
	{

		if (data == null)
		{
			throw new NullPointerException("can't encrypt NULL data");
		}
		if (data.length < 70)
		{
			throw new TorException("input array too short");
		}

		try
		{
			int encryptedBytes = 0;

			// init OAEP
			final OAEPEncoding oaep = new OAEPEncoding(new RSAEngine());
			oaep.init(false, new RSAKeyParameters(true, priv.getModulus(), priv.getPrivateExponent()));
			// apply RSA+OAEP
			encryptedBytes = oaep.getInputBlockSize();
			final byte[] oaepInput = new byte[encryptedBytes];
			System.arraycopy(data, 0, oaepInput, 0, encryptedBytes);
			final byte[] part1 = oaep.decodeBlock(oaepInput, 0, encryptedBytes);

			// extract symmetric key
			final byte[] symmetricKey = new byte[16];
			System.arraycopy(part1, 0, symmetricKey, 0, 16);
			// init AES
			final AESCounterMode aes = new AESCounterMode(symmetricKey);
			// apply AES
			final byte[] aesInput = new byte[data.length - encryptedBytes];
			System.arraycopy(data, encryptedBytes, aesInput, 0, aesInput.length);
			final byte[] part2 = aes.processStream(aesInput);

			// replace unencrypted data
			final byte[] result = new byte[part1.length - 16 + part2.length];
			System.arraycopy(part1, 16, result, 0, part1.length - 16);
			System.arraycopy(part2, 0, result, part1.length - 16, part2.length);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Encryption.asymDecrypt():: encrypted payload:\n" + Encoding.toHexString(data, 100));
				LOG.debug("Encryption.asymDecrypt():: unencrypted payload:\n" + Encoding.toHexString(result, 100));
			}

			return result;

		}
		catch (final InvalidCipherTextException e)
		{
			LOG.error("Encryption.asymDecrypt(): can't decrypt cipher text:" + e.getMessage());
			throw new TorException("Encryption.asymDecrypt(): InvalidCipherTextException:" + e.getMessage());
		}
	}

	private static final int KEY_STRENGTH = 1024;
	private static final int KEY_CERTAINTY = 80; // use 112 for strength=2048

	/**
	 * Create a fresh RSA key pair.
	 * 
	 * @return a new RSAKeyPair
	 */
	public static RSAKeyPair createNewRSAKeyPair()
	{
		try
		{
			// Generate a 1024-bit RSA key pair
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(KEY_STRENGTH);
			final KeyPair keypair = keyGen.genKeyPair();
			final RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keypair.getPrivate();
			final RSAPublicKey publicKey = (RSAPublicKey) keypair.getPublic();

			LOG.debug("privateKey={}", privateKey);
			LOG.debug("publicKey={}", publicKey);

			return new RSAKeyPair(publicKey, privateKey);

		}
		catch (final NoSuchAlgorithmException e)
		{
			LOG.error("Could not create new key pair", e);
			throw new RuntimeException(e);
		}
	}
}
