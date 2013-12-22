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
 * @author Tobias Boese
 */
public final class RouterStatusDescription
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
	/** 
	 * flag Running. 
	 * "Running" if the router is currently usable
	 */
	private boolean running = false;
	/** 
	 * flag Authority.
	 * "Authority" if the router is a directory authority.
	 */
	private boolean authority = false;
	/**
	 * "Exit" if the router is more useful for building general-purpose exit circuits than for relay circuits.  
	 * The path building algorithm uses this flag; see path-spec.txt.
	 */
	private boolean exit = false;
	/**
	 * "BadExit" if the router is believed to be useless as an exit node 
	 * (because its ISP censors it, because it is behind a restrictive proxy, or for some similar reason).
	 */
	private boolean badExit = false;
	/**
	 * "BadDirectory" if the router is believed to be useless as a directory cache 
	 * (because its directory port isn't working, its bandwidth is always throttled, or for some similar reason).
	 */
	private boolean badDirectory = false;
	/**
	 * "Fast" if the router is suitable for high-bandwidth circuits.
	 */
	private boolean fast = false;
	/**
	 * "Guard" if the router is suitable for use as an entry guard.
	 */
	private boolean guard = false;
	/**
	 * "HSDir" if the router is considered a v2 hidden service directory.
	 */
	private boolean HSDir = false;
	/**
	 *  "Named" if the router's identity-nickname mapping is canonical, and this authority binds names.
	 */
	private boolean named = false;
	/**
	 * "Stable" if the router is suitable for long-lived circuits.
	 */
	private boolean stable = false;
	/**
	 * "Unnamed" if another router has bound the name used by this router, and this authority binds names.
	 */
	private boolean unnamed = false;
	/** 
	 * "Valid" if the router has been 'validated'.
	 */
	private boolean valid = false;
	/**
	 * "V2Dir" if the router implements the v2 directory protocol.
	 */
	private boolean v2Dir = false;
	
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
		// do a fixed prioritizing: Running, Authority, Exit, Guard, Fast, Stable, Valid
		if (running && !other.running)
		{
			return true;
		}
		if (other.running && !running)
		{
			return false;
		}
		if (authority && !other.authority)
		{
			return true;
		}
		if (other.authority && !authority)
		{
			return false;
		}
		if (exit && !other.exit)
		{
			return true;
		}
		if (other.exit && !exit)
		{
			return false;
		}
		if (guard && !other.guard)
		{
			return true;
		}
		if (other.guard && !guard)
		{
			return false;
		}
		if (fast && !other.fast)
		{
			return true;
		}
		if (!other.fast && !fast)
		{
			return false;
		}
		if (stable && !other.stable)
		{
			return true;
		}
		if (other.stable && !stable)
		{
			return false;
		}
		if (valid && !other.valid)
		{
			return true;
		}
		if (other.valid && !valid)
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

	public void setNickname(final String nickname)
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

	public void setIp(final String ip)
	{
		this.ip = ip;
	}

	public int getOrPort()
	{
		return orPort;
	}

	public void setOrPort(final int orPort)
	{
		this.orPort = orPort;
	}

	public int getDirPort()
	{
		return dirPort;
	}

	public void setDirPort(final int dirPort)
	{
		this.dirPort = dirPort;
	}

	public void setFlags(final String flags)
	{
		running = flags.contains("Running");
		exit = flags.contains("Exit");
		authority = flags.contains("Authority");
		fast = flags.contains("Fast");
		guard = flags.contains("Guard");
		stable = flags.contains("Stable");
		named = flags.contains("Named");
		unnamed = flags.contains("Unnamed");
		v2Dir = flags.contains("V2Dir");
		valid = flags.contains("Valid");
		HSDir = flags.contains("HSDir");
		badDirectory = flags.contains("BadDirectory");
		badExit = flags.contains("BadExit");
	}

	/**
	 * @return the running
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * @return the authority
	 */
	public boolean isAuthority()
	{
		return authority;
	}

	/**
	 * @return the exit
	 */
	public boolean isExit()
	{
		return exit;
	}

	/**
	 * @return the badExit
	 */
	public boolean isBadExit()
	{
		return badExit;
	}

	/**
	 * @return the badDirectory
	 */
	public boolean isBadDirectory()
	{
		return badDirectory;
	}

	/**
	 * @return the fast
	 */
	public boolean isFast()
	{
		return fast;
	}

	/**
	 * @return the guard
	 */
	public boolean isGuard()
	{
		return guard;
	}

	/**
	 * @return the hSDir
	 */
	public boolean isHSDir()
	{
		return HSDir;
	}

	/**
	 * @return the named
	 */
	public boolean isNamed()
	{
		return named;
	}

	/**
	 * @return the stable
	 */
	public boolean isStable()
	{
		return stable;
	}

	/**
	 * @return the unnamed
	 */
	public boolean isUnnamed()
	{
		return unnamed;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * @return the v2Dir
	 */
	public boolean isV2Dir()
	{
		return v2Dir;
	}

	/**
	 * @return the rnd
	 */
	public SecureRandom getRnd()
	{
		return rnd;
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
				+ ", dirPort=" + dirPort + "]";
	}
}
