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

package org.silvertunnel_ng.netlib.layer.tor.util;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

/**
 * This class is a simple holder for an RSA key pair (a public key and a private
 * key). It does not enforce any security, and, when initialized, should be
 * treated like a RSAPrivateKey.
 * 
 * @author hapke
 */
public class RSAKeyPair
{
	private final RSAPublicKey publicKey;
	private final RSAPrivateCrtKey privateKey;

	public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateCrtKey privateKey)
	{
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@Override
	public String toString()
	{
		return publicKey + "\n" + privateKey;
	}

	public RSAPublicKey getPublic()
	{
		return publicKey;
	}

	public RSAPrivateCrtKey getPrivate()
	{
		return privateKey;
	}
}
