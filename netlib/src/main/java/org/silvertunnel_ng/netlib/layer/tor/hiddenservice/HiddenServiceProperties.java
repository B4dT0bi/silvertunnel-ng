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
package org.silvertunnel_ng.netlib.layer.tor.hiddenservice;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.Set;

import org.silvertunnel_ng.netlib.layer.tor.directory.SDIntroductionPoint;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.RSAKeyPair;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * contains all properties for a hidden service.
 */
public class HiddenServiceProperties
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HiddenServiceProperties.class);

	private RSAPublicKey pub;
	private RSAPrivateKey priv;
	private byte[] pubKeyHash;
	private int port;
	private Set<SDIntroductionPoint> introPoints;
	private int minimumNumberOfIntroPoints;

	/** constructor for first initialization or use-once-services. */
	public HiddenServiceProperties(final int port, final RSAKeyPair keyPair) throws TorException
	{
		init(port, new HashSet<SDIntroductionPoint>(), keyPair);
	}

	/** constructor for first initialization or use-once-services. */
	public HiddenServiceProperties(final int port,
	                               final Set<SDIntroductionPoint> introPoints, 
	                               final RSAKeyPair keyPair)
			throws TorException
	{
		init(port, introPoints, keyPair);
	}

	/**
	 * initializes hidden service and service descriptor private void init(int
	 * port, HiddenServiceRequestHandler handler, Set<SDIntroductionPoint>
	 * introPoints) throws TorException { this.port = port; this.handler =
	 * handler; this.introPoints = introPoints; minimumNumberOfIntroPoints = 3;
	 * try { // generates a new random key pair on every start. KeyPairGenerator
	 * generator = KeyPairGenerator.getInstance("RSA", "BC");
	 * generator.initialize(1024, new SecureRandom()); KeyPair keypair =
	 * generator.generateKeyPair(); pub = (RSAPublicKey) keypair.getPublic();
	 * priv = (RSAPrivateKey) keypair.getPrivate(); // precalc-hash of public
	 * key pubKeyHash =
	 * Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(pub));
	 * 
	 * } catch (NoSuchProviderException e) {
	 * LOG.error("HiddenServiceProperties: Caught exception: " +
	 * e.getMessage()); } catch (NoSuchAlgorithmException e) {
	 * LOG.error("HiddenServiceProperties: Caught exception: " +
	 * e.getMessage()); } }
	 */

	private void init(final int port, 
	                  final Set<SDIntroductionPoint> introPoints,
	                  final RSAKeyPair keyPair) throws TorException
	{
		this.port = port;
		this.introPoints = introPoints;
		minimumNumberOfIntroPoints = 3;

		pub = keyPair.getPublic();
		priv = keyPair.getPrivate();
		// precalc-hash of public key
		pubKeyHash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(pub));
	}

	/** constructor for saved configuration of hidden services. */
	public HiddenServiceProperties(final String filename) throws IOException
	{
		// FIXME: implement
		throw new IOException("not implemented yet");
	}

	/** writes all informations to a file. */
	void writeToFile(final String filename) throws IOException
	{
		// FIXME: implement
		throw new IOException("not implemented yet");
	}

	public void addIntroPoint(final SDIntroductionPoint introPoint)
	{
		introPoints.add(introPoint);
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public RSAPublicKey getPublicKey()
	{
		return pub;
	}

	public RSAPrivateKey getPrivateKey()
	{
		return priv;
	}

	public byte[] getPubKeyHash()
	{
		return pubKeyHash;
	}

	public int getPort()
	{
		return port;
	}

	public Set<SDIntroductionPoint> getIntroPoints()
	{
		return introPoints;
	}

	public int getNumberOfIntroPoints()
	{
		return introPoints.size();
	}

	public int getMinimumNumberOfIntroPoints()
	{
		return minimumNumberOfIntroPoints;
	}
}
