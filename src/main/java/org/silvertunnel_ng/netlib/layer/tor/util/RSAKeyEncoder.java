/*
 * Copied from Orchid aka JTor.
 */
package org.silvertunnel_ng.netlib.layer.tor.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.util.ASN1Parser.ASN1BitString;
import org.silvertunnel_ng.netlib.layer.tor.util.ASN1Parser.ASN1Integer;
import org.silvertunnel_ng.netlib.layer.tor.util.ASN1Parser.ASN1Object;
import org.silvertunnel_ng.netlib.layer.tor.util.ASN1Parser.ASN1Sequence;

public class RSAKeyEncoder
{
	private final static String HEADER = "-----BEGIN RSA PUBLIC KEY-----";
	private final static String FOOTER = "-----END RSA PUBLIC KEY-----";

	private final ASN1Parser asn1Parser = new ASN1Parser();

	/**
	 * Parse a PKCS1 PEM encoded RSA public key into the modulus/exponent
	 * components and construct a new RSAPublicKey.
	 * 
	 * @param pem
	 *            The PEM encoded string to parse.
	 * @return a new RSAPublicKey
	 * 
	 * @throws GeneralSecurityException
	 *             If an error occurs while parsing the pem argument or creating
	 *             the RSA key.
	 */
	public RSAPublicKey parsePEMPublicKey(final String pem) throws GeneralSecurityException
	{
		try
		{
			byte[] bs = decodeAsciiArmoredPEM(pem);
			ByteBuffer data = ByteBuffer.wrap(bs);
			final ASN1Object ob = asn1Parser.parseASN1(data);
			final List<ASN1Object> seq = asn1ObjectToSequence(ob, 2);
			final BigInteger modulus = asn1ObjectToBigInt(seq.get(0));
			final BigInteger exponent = asn1ObjectToBigInt(seq.get(1));
			return createKeyFromModulusAndExponent(modulus, exponent);
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidKeyException(e);
		}
	}

	private RSAPublicKey createKeyFromModulusAndExponent(final BigInteger modulus, final BigInteger exponent) throws GeneralSecurityException
	{
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory fac = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) fac.generatePublic(spec);
	}

	/**
	 * Return the PKCS1 encoded representation of the specified RSAPublicKey.
	 * Since the primary encoding format for RSA public keys is X.509
	 * SubjectPublicKeyInfo, this needs to be converted to PKCS1 by extracting
	 * the needed field.
	 * 
	 * @param publicKey
	 *            The RSA public key to encode.
	 * @return The PKCS1 encoded representation of the publicKey argument
	 */
	public byte[] getPKCS1Encoded(final RSAPublicKey publicKey)
	{
		return extractPKCS1KeyFromSubjectPublicKeyInfo(publicKey.getEncoded());
	}

	/*
	 * SubjectPublicKeyInfo encoding looks like this:
	 * 
	 * SEQUENCE { SEQUENCE { OBJECT IDENTIFIER rsaEncryption (1 2 840 113549 1 1
	 * 1) NULL } BIT STRING (encapsulating) { <-- contains PKCS1 encoded key
	 * SEQUENCE { INTEGER (modulus) INTEGER (exponent) } } }
	 * 
	 * See: http://www.jensign.com/JavaScience/dotnet/JKeyNet/index.html
	 */
	private byte[] extractPKCS1KeyFromSubjectPublicKeyInfo(final byte[] input)
	{
		final ASN1Object ob = asn1Parser.parseASN1(ByteBuffer.wrap(input));
		final List<ASN1Object> seq = asn1ObjectToSequence(ob, 2);
		return asn1ObjectToBitString(seq.get(1));
	}

	private BigInteger asn1ObjectToBigInt(final ASN1Object ob)
	{
		if (!(ob instanceof ASN1Integer))
		{
			throw new IllegalArgumentException();
		}
		final ASN1Integer n = (ASN1Integer) ob;
		return n.getValue();
	}

	private List<ASN1Object> asn1ObjectToSequence(final ASN1Object ob, final int expectedSize)
	{
		if (ob instanceof ASN1Sequence)
		{
			final ASN1Sequence seq = (ASN1Sequence) ob;
			if (seq.getItems().size() != expectedSize)
			{
				throw new IllegalArgumentException();
			}
			return seq.getItems();
		}
		throw new IllegalArgumentException("ASN1Object not of type ASN1Sequence. ASN1Object : " + ob);
	}

	private byte[] asn1ObjectToBitString(final ASN1Object ob)
	{
		if (!(ob instanceof ASN1BitString))
		{
			throw new IllegalArgumentException();
		}
		final ASN1BitString bitstring = (ASN1BitString) ob;
		return bitstring.getBytes();
	}
	private byte[] decodeAsciiArmoredPEM(final String pem) 
	{
		final String trimmed = removeDelimiters(pem);
		return DatatypeConverter.parseBase64Binary(trimmed);
	}
	
	private String removeDelimiters(final String pem) 
	{
		final int headerIdx = pem.indexOf(HEADER);
		final int footerIdx = pem.indexOf(FOOTER);
		if (headerIdx == -1 || footerIdx == -1 || footerIdx <= headerIdx) 
		{
			return pem;
		}
		return pem.substring(headerIdx + HEADER.length(), footerIdx);
	}
}
