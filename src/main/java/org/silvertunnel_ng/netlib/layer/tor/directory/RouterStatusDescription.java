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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;

/**
 * used to store server descriptors from a dir-spec v2 network status document.
 * 
 * @author hapke
 */
public class RouterStatusDescription
{
	/** nickname of the router. */
	private String nickname;
	/** {@link Fingerprint} of the router. */
	private Fingerprint fingerprint;
	private byte[] digestDescriptor;
	private Date lastPublication;
	/** IP address of the router. */
	private String ip;
	/** OR port and Dir Port. */
	private int orPort, dirPort;
	private String flags;
	private SecureRandom rnd = new SecureRandom();

	/**
	 * we have to judge from the server's flags which of the both should be
	 * downloaded rather than the other. MAYBE one or both of them are already
	 * in the HasMap this.torServers, but we can't rely on that.<br>
	 * The flags are stored in the member variable "flags" and are currently:<br>
	 * <tt>Authority, Exit, Fast, Guard, Named, Stable, Running, Valid, V2Dir</tt>
	 * 
	 * @param other
	 *            the other descriptor, to which we compare this descriptor
	 * @return true, if this one is better to download
	 */
	public boolean isBetterThan(final RouterStatusDescription other)
	{
		// do a fixed prioritizing: Running, Authority, Exit, Guard, Fast,
		// Stable, Valid
		if ((flags.indexOf("Running") >= 0) && (other.flags.indexOf("Running") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Running") >= 0) && (flags.indexOf("Running") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Authority") >= 0) && (other.flags.indexOf("Authority") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Authority") >= 0) && (flags.indexOf("Authority") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Exit") >= 0) && (other.flags.indexOf("Exit") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Exit") >= 0) && (flags.indexOf("Exit") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Guard") >= 0) && (other.flags.indexOf("Guard") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Guard") >= 0) && (flags.indexOf("Guard") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Fast") >= 0) && (other.flags.indexOf("Fast") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Fast") >= 0) && (flags.indexOf("Fast") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Stable") >= 0) && (other.flags.indexOf("Stable") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Stable") >= 0) && (flags.indexOf("Stable") < 0))
		{
			return false;
		}
		if ((flags.indexOf("Valid") >= 0) && (other.flags.indexOf("Valid") < 0))
		{
			return true;
		}
		if ((other.flags.indexOf("Valid") >= 0) && (flags.indexOf("Valid") < 0))
		{
			return false;
		}
		// finally - all (important) flags seem to be equal..
		// download the one, that is fresher?
		if (lastPublication.compareTo(other.lastPublication) < 0)
		{
			return true;
		}
		if (lastPublication.compareTo(other.lastPublication) > 0)
		{
			return false;
		}
		// choose by random
		if (rnd != null)
		{
			return rnd.nextBoolean();
		}

		// say no, because experience tells that dir-servers tend to list
		// important stuff first
		return false;
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public Fingerprint getFingerprint()
	{
		return fingerprint;
	}

	public void setFingerprint(final String fingerprint)
	{
		this.fingerprint = new FingerprintImpl(fingerprint);
	}

	public void setFingerprint(final byte[] fingerprint)
	{
		this.fingerprint = new FingerprintImpl(fingerprint);
	}

	public void setFingerprint(final Fingerprint fingerprint)
	{
		this.fingerprint = fingerprint;
	}

	public byte[] getDigestDescriptor()
	{
		return digestDescriptor;
	}

	public void setDigestDescriptor(final String digestDescriptorBase64)
	{
		String base64 = digestDescriptorBase64;
		while (base64.length() % 4 != 0)
		{
			base64 += "=";
		}
		setDigestDescriptor(DatatypeConverter.parseBase64Binary(base64));
	}

	public void setDigestDescriptor(final byte[] digestDescriptor)
	{
		this.digestDescriptor = digestDescriptor;
	}

	public Date getLastPublication()
	{
		return lastPublication;
	}

	public void setLastPublication(final Date lastPublication)
	{
		this.lastPublication = lastPublication;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getOrPort()
	{
		return orPort;
	}

	public void setOrPort(int orPort)
	{
		this.orPort = orPort;
	}

	public int getDirPort()
	{
		return dirPort;
	}

	public void setDirPort(int dirPort)
	{
		this.dirPort = dirPort;
	}

	public String getFlags()
	{
		return flags;
	}

	public void setFlags(String flags)
	{
		this.flags = flags;
	}

	public SecureRandom getRnd()
	{
		return rnd;
	}

	public void setRnd(SecureRandom rnd)
	{
		this.rnd = rnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "RouterStatusDescription [nickname=" + nickname
				+ ", fingerprint=" + fingerprint + ", digestDescriptor="
				+ Arrays.toString(digestDescriptor) + ", lastPublication="
				+ lastPublication + ", ip=" + ip + ", orPort=" + orPort
				+ ", dirPort=" + dirPort + ", flags=" + flags + ", rnd=" + rnd
				+ "]";
	}
}
