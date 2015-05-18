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

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple helper methods to use RendezvousServiceDescriptor.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class RendezvousServiceDescriptorUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(RendezvousServiceDescriptorUtil.class);

	/** one day in seconds. */
	private static final int TIMEPERIOD_V2_DESC_VALIDITY_SECONDS = 24 * 60 * 60;

	/**
	 * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/
	 * rend-spec.txt - chapter 1.2
	 * 
	 * @param hiddenServicePermanentIdBase32
	 *            base32 encoded z (length 16 chars), also known as permanent
	 *            id, usually left part of the .onion domain
	 * @param now
	 *            current time
	 * @return base32 encoded descriptorId of z (length 32 chars) and more; not
	 *         null
	 */
	public static RendezvousServiceDescriptorKeyValues getRendezvousDescriptorId(final String hiddenServicePermanentIdBase32,
																				 final int replica,
																				 final Long now)
	{
		final RendezvousServiceDescriptorKeyValues result = new RendezvousServiceDescriptorKeyValues();

		// shared secret between hidden service and its client: currently not
		// used
		final byte[] descriptorCookie = null;

		// calculate current time-period
		final byte[] hiddenServicePermanentId = Encoding.parseBase32(hiddenServicePermanentIdBase32);

		result.setTimePeriod(RendezvousServiceDescriptorUtil.getRendezvousTimePeriod(hiddenServicePermanentId, now));

		// calculate secret-id-part = h(time-period + descriptorCookie +
		// replica)
		result.setSecretIdPart(RendezvousServiceDescriptorUtil.getRendezvousSecretIdPart(result.getTimePeriod(), descriptorCookie, replica));

		// calculate descriptor ID
		final byte[] unhashedDescriptorId = ByteArrayUtil.concatByteArrays(hiddenServicePermanentId, result.getSecretIdPart());
		if (hiddenServicePermanentId.length != 10)
		{
			LOG.warn("wrong length of hiddenServicePermanentId=" + Arrays.toString(hiddenServicePermanentId));
		}
		result.setDescriptorId(Encryption.getDigest(unhashedDescriptorId));

		return result;
	}

	/**
	 * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/
	 * rend-spec.txt - chapter 1.2
	 * 
	 * @param hiddenServicePermanentIdBase32
	 *            base32 encoded z (length 16 chars), also known as permanent
	 *            id, usually left part of the .onion domain
	 * @param now
	 *            current time
	 * @return base32 encoded descriptorId of z (length 32 chars)
	 */
	public static String getRendezvousDescriptorIdBase32(final String hiddenServicePermanentIdBase32, 
	                                                     final int replica, 
	                                                     final Long now)
	{
		return Encoding.toBase32(getRendezvousDescriptorId(hiddenServicePermanentIdBase32, replica, now).getDescriptorId());
	}

	/**
	 * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/
	 * rend-spec.txt - chapter 1.2 time-period = (current-time +
	 * permanent-id-highest-byte * 86400 / 256) / 86400
	 * 
	 * @param hiddenServicePermanentId
	 *            also known as permanent id
	 * @param now
	 *            current time
	 * @return timeperiod
	 */
	public static int getRendezvousTimePeriod(final byte[] hiddenServicePermanentId, final Long now)
	{
		final int nowInSeconds = (int) (now / 1000L);
		// get the correct unsigned byte value (Java treats all bytes as signed)
		final int serviceIdHighestByte = (256 + hiddenServicePermanentId[0]) % 256;
		final int result = (nowInSeconds + (serviceIdHighestByte * RendezvousServiceDescriptorUtil.TIMEPERIOD_V2_DESC_VALIDITY_SECONDS / 256))
				/ RendezvousServiceDescriptorUtil.TIMEPERIOD_V2_DESC_VALIDITY_SECONDS;
		return result;
	}

	/**
	 * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/
	 * rend-spec.txt - chapter 1.2
	 * 
	 * @param timePeriod
	 * @param descriptorCookieBytes
	 *            can be null
	 * @param replica
	 * @return h(timePeriod + escriptorCookie + replica)
	 */
	public static byte[] getRendezvousSecretIdPart(final int timePeriod, byte[] descriptorCookieBytes, final int replica)
	{
		// convert input to byte arrays
		final int BYTES4 = 4;
		final byte[] timePeriodBytes = Encoding.intToNByteArray(timePeriod, BYTES4);
		if (descriptorCookieBytes == null)
		{
			descriptorCookieBytes = new byte[0];
		}
		final int BYTES1 = 1;
		final byte[] replicaBytes = Encoding.intToNByteArray(replica, BYTES1);

		// calculate digest
		final byte[] allBytes = ByteArrayUtil.concatByteArrays(timePeriodBytes, descriptorCookieBytes, replicaBytes);
		return Encryption.getDigest(allBytes);
	}

	/**
	 * Calculate z of domain z.onion as specified in See
	 * http://gitweb.torproject
	 * .org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter
	 * "1.5. Alice receives a z.onion address"
	 * 
	 * @return z hiddenServicePermanentIdBase32
	 */
	public static String calculateZFromPublicKey(final RSAPublicKey publicKey)
	{
		final byte[] publicKeyHash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(publicKey));
		final byte[] zBytes = new byte[10];
		System.arraycopy(publicKeyHash, 0, zBytes, 0, 10);
		return Encoding.toBase32(zBytes);
	}
}
