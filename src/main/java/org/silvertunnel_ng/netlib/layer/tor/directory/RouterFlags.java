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
package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.tool.ByteUtils;


/**
 * RouterFlags are used for determining the Status of the router (eg. Running, Stable, Fast, etc)
 * 
 * @author Tobias Boese
 */
public final class RouterFlags
{
	/** 
	 * flag Running. 
	 * "Running" if the router is currently usable
	 */
	private Boolean running = null;
	/** 
	 * flag Authority.
	 * "Authority" if the router is a directory authority.
	 */
	private Boolean authority = null;
	/**
	 * "Exit" if the router is more useful for building general-purpose exit circuits than for relay circuits.  
	 * The path building algorithm uses this flag; see path-spec.txt.
	 */
	private Boolean exit = null;
	/**
	 * "BadExit" if the router is believed to be useless as an exit node 
	 * (because its ISP censors it, because it is behind a restrictive proxy, or for some similar reason).
	 */
	private Boolean badExit = null;
	/**
	 * "BadDirectory" if the router is believed to be useless as a directory cache 
	 * (because its directory port isn't working, its bandwidth is always throttled, or for some similar reason).
	 */
	private Boolean badDirectory = null;
	/**
	 * "Fast" if the router is suitable for high-bandwidth circuits.
	 */
	private Boolean fast = null;
	/**
	 * "Guard" if the router is suitable for use as an entry guard.
	 */
	private Boolean guard = null;
	/**
	 * "HSDir" if the router is considered a v2 hidden service directory.
	 */
	private Boolean hSDir = null;
	/**
	 *  "Named" if the router's identity-nickname mapping is canonical, and this authority binds names.
	 */
	private Boolean named = null;
	/**
	 * "Stable" if the router is suitable for long-lived circuits.
	 */
	private Boolean stable = null;
	/**
	 * "Unnamed" if another router has bound the name used by this router, and this authority binds names.
	 */
	private Boolean unnamed = null;
	/** 
	 * "Valid" if the router has been 'validated'.
	 */
	private Boolean valid = null;
	/**
	 * "V2Dir" if the router implements the v2 directory protocol.
	 */
	private Boolean v2Dir = null;
	
	/**
	 * Standard Constructor.
	 * All values are set to null.
	 */
	public RouterFlags()
	{
	}
	/**
	 * Initialize all values with the given initial value.
	 * @param initialValue the initial value used for setting all members.
	 */
	public RouterFlags(final Boolean initialValue)
	{
		running = initialValue;
		exit = initialValue;
		authority = initialValue;
		fast = initialValue;
		guard = initialValue;
		stable = initialValue;
		named = initialValue;
		unnamed = initialValue;
		v2Dir = initialValue;
		valid = initialValue;
		hSDir = initialValue;
		badDirectory = initialValue;
		badExit = initialValue;
	}
	/**
	 * Set the flags by parsing a given string.
	 * @param flags the String which contains the set flags. All other flags are set to false.
	 */
	public RouterFlags(final String flags)
	{
		setAllFlags(flags);
	}
	/**
	 * Set the flags by parsing a given byte array.
	 * @param data the byte array which contains the flags.
	 */
	public RouterFlags(final byte [] data)
	{
		Boolean [] flags1 = ByteUtils.getBooleansFromByte(data[0]);
		running = flags1[0];
		exit = flags1[1];
		authority = flags1[2];
		fast = flags1[3];
		Boolean [] flags2 = ByteUtils.getBooleansFromByte(data[1]);
		guard = flags2[0];
		stable = flags2[1];
		named = flags2[2];
		unnamed = flags2[3];
		Boolean [] flags3 = ByteUtils.getBooleansFromByte(data[2]);
		v2Dir = flags3[0];
		valid = flags3[1];
		hSDir = flags3[2];
		badDirectory = flags3[3];
		Boolean [] flags4 = ByteUtils.getBooleansFromByte(data[3]);
		badExit = flags4[0];
	}
	/**
	 * Set all flags by given flags as string.
	 * @param flags the flags as string
	 */
	public void setAllFlags(final String flags)
	{
		running = flags.contains(IDENTIFIER_RUNNING);
		exit = flags.contains(IDENTIFIER_EXIT);
		authority = flags.contains(IDENTIFIER_AUTHORITY);
		fast = flags.contains(IDENTIFIER_FAST);
		guard = flags.contains(IDENTIFIER_GUARD);
		stable = flags.contains(IDENTIFIER_STABLE);
		named = flags.contains(IDENTIFIER_NAMED);
		unnamed = flags.contains(IDENTIFIER_UNNAMED);
		v2Dir = flags.contains(IDENTIFIER_DIRECTORY);
		valid = flags.contains(IDENTIFIER_VALID);
		hSDir = flags.contains(IDENTIFIER_HIDDENSERVICE_DIRECTORY);
		badDirectory = flags.contains(IDENTIFIER_BAD_DIRECTORY);
		badExit = flags.contains(IDENTIFIER_BAD_EXIT);
	}
	/**
	 * Convert the RouterFlags to a byte array.
	 * @return a byte array containing the RouterFlag information.
	 */
	protected byte [] toByteArray()
	{
		byte [] result = new byte[4];
		result[0] = ByteUtils.getByteFromBooleans(running, exit, authority, fast);
		result[1] = ByteUtils.getByteFromBooleans(guard, stable, named, unnamed);
		result[2] = ByteUtils.getByteFromBooleans(v2Dir, valid, hSDir, badDirectory);
		result[3] = ByteUtils.getByteFromBooleans(badExit);
		return result;
	}
	/**
	 * @return the running as Boolean (can also be null)
	 */
	public Boolean getRunning()
	{
		return running;
	}
	/**
	 * @return the running as boolean (will return false if not set)
	 */
	public boolean isRunning()
	{
		return running == null ? false : running;
	}
	/**
	 * @param running the running to set
	 */
	public void setRunning(final Boolean running)
	{
		this.running = running;
	}
	/**
	 * @return the authority as Boolean (can also be null if not set)
	 */
	public Boolean getAuthority()
	{
		return authority;
	}
	/**
	 * @return the authority as boolean (will return false if not set)
	 */
	public boolean isAuthority()
	{
		return authority == null ? false : authority;
	}
	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(final Boolean authority)
	{
		this.authority = authority;
	}
	/**
	 * @return the exit as Boolean (can be null)
	 */
	public Boolean getExit()
	{
		return exit;
	}
	/**
	 * @return the exit as boolean (will return false if not set)
	 */
	public boolean isExit()
	{
		return exit == null ? false : exit;
	}
	/**
	 * @param exit the exit to set
	 */
	public void setExit(final Boolean exit)
	{
		this.exit = exit;
	}
	/**
	 * @return the badExit as Boolean (can be null)
	 */
	public Boolean getBadExit()
	{
		return badExit;
	}
	/**
	 * @return the badExit as boolean (will return false if not set)
	 */
	public boolean isBadExit()
	{
		return badExit == null ? false : badExit;
	}
	/**
	 * @param badExit the badExit to set
	 */
	public void setBadExit(final Boolean badExit)
	{
		this.badExit = badExit;
	}
	/**
	 * @return the badDirectory as Boolean (can be null)
	 */
	public Boolean getBadDirectory()
	{
		return badDirectory;
	}
	/**
	 * @return the badDirectory as boolean (will return false if not set)
	 */
	public boolean isBadDirectory()
	{
		return badDirectory == null ? false : badDirectory;
	}
	/**
	 * @param badDirectory the badDirectory to set
	 */
	public void setBadDirectory(final Boolean badDirectory)
	{
		this.badDirectory = badDirectory;
	}
	/**
	 * @return the fast as Boolean (can be null)
	 */
	public Boolean getFast()
	{
		return fast;
	}
	/**
	 * @return the fast as boolean (will return false if not set)
	 */
	public boolean isFast()
	{
		return fast == null ? false : fast;
	}
	/**
	 * @param fast the fast to set
	 */
	public void setFast(final Boolean fast)
	{
		this.fast = fast;
	}
	/**
	 * @return the guard as Boolean (can be null)
	 */
	public Boolean getGuard()
	{
		return guard;
	}
	/**
	 * @return the guard as boolean (will return false if not set)
	 */
	public boolean isGuard()
	{
		return guard == null ? false : guard;
	}
	/**
	 * @param guard the guard to set
	 */
	public void setGuard(final Boolean guard)
	{
		this.guard = guard;
	}
	/**
	 * @return the hSDir as Boolean (can be null)
	 */
	public Boolean getHSDir()
	{
		return hSDir;
	}
	/**
	 * @return the hSDir as boolean (will return false if not set)
	 */
	public boolean isHSDir()
	{
		return hSDir == null ? false : hSDir;
	}
	/**
	 * @param hSDir the hSDir to set
	 */
	public void setHSDir(final Boolean hSDir)
	{
		this.hSDir = hSDir;
	}
	/**
	 * @return the named as Boolean (can be null)
	 */
	public Boolean getNamed()
	{
		return named;
	}
	/**
	 * @return the named as boolean (will return false if not set)
	 */
	public boolean isNamed()
	{
		return named == null ? false : named;
	}
	/**
	 * @param named the named to set
	 */
	public void setNamed(final Boolean named)
	{
		this.named = named;
	}
	/**
	 * @return the stable as Boolean (can be null)
	 */
	public Boolean getStable()
	{
		return stable;
	}
	/**
	 * @return the stable as boolean (will return false if not set)
	 */
	public boolean isStable()
	{
		return stable == null ? false : stable;
	}
	/**
	 * @param stable the stable to set
	 */
	public void setStable(final Boolean stable)
	{
		this.stable = stable;
	}
	/**
	 * @return the unnamed as Boolean (can be null)
	 */
	public Boolean getUnnamed()
	{
		return unnamed;
	}
	/**
	 * @return the unnamed as boolean (will return false if not set)
	 */
	public boolean isUnnamed()
	{
		return unnamed == null ? false : unnamed;
	}
	/**
	 * @param unnamed the unnamed to set
	 */
	public void setUnnamed(final Boolean unnamed)
	{
		this.unnamed = unnamed;
	}
	/**
	 * @return the valid as Boolean (can be null)
	 */
	public Boolean getValid()
	{
		return valid;
	}
	/**
	 * @return the valid as boolean (will return false if not set)
	 */
	public boolean isValid()
	{
		return valid == null ? false : valid;
	}
	/**
	 * @param valid the valid to set
	 */
	public void setValid(final Boolean valid)
	{
		this.valid = valid;
	}
	/**
	 * @return the v2Dir as Boolean (can be null)
	 */
	public Boolean getV2Dir()
	{
		return v2Dir;
	}
	/**
	 * @return the v2Dir as boolean (will return false if not set)
	 */
	public boolean isV2Dir()
	{
		return v2Dir == null ? false : v2Dir;
	}
	/**
	 * @param v2Dir the v2Dir to set
	 */
	public void setV2Dir(final Boolean v2Dir)
	{
		this.v2Dir = v2Dir;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authority == null) ? 0 : authority.hashCode());
		result = prime * result + ((badDirectory == null) ? 0 : badDirectory.hashCode());
		result = prime * result + ((badExit == null) ? 0 : badExit.hashCode());
		result = prime * result + ((exit == null) ? 0 : exit.hashCode());
		result = prime * result + ((fast == null) ? 0 : fast.hashCode());
		result = prime * result + ((guard == null) ? 0 : guard.hashCode());
		result = prime * result + ((hSDir == null) ? 0 : hSDir.hashCode());
		result = prime * result + ((named == null) ? 0 : named.hashCode());
		result = prime * result + ((running == null) ? 0 : running.hashCode());
		result = prime * result + ((stable == null) ? 0 : stable.hashCode());
		result = prime * result + ((unnamed == null) ? 0 : unnamed.hashCode());
		result = prime * result + ((v2Dir == null) ? 0 : v2Dir.hashCode());
		result = prime * result + ((valid == null) ? 0 : valid.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof RouterFlags))
		{
			return false;
		}
		RouterFlags other = (RouterFlags) obj;
		if (!equals(authority, other.authority))
		{
			return false;
		}
		if (!equals(badDirectory, other.badDirectory))
		{
			return false;
		}
		if (!equals(badExit, other.badExit))
		{
			return false;
		}
		if (!equals(exit, other.exit))
		{
			return false;
		}
		if (!equals(fast, other.fast))
		{
			return false;
		}
		if (!equals(guard, other.guard))
		{
			return false;
		}
		if (!equals(hSDir, other.hSDir))
		{
			return false;
		}
		if (!equals(named, other.named))
		{
			return false;
		}
		if (!equals(running, other.running))
		{
			return false;
		}
		if (!equals(stable, other.stable))
		{
			return false;
		}
		if (!equals(unnamed, other.unnamed))
		{
			return false;
		}
		if (!equals(valid, other.valid))
		{
			return false;
		}
		if (!equals(v2Dir, other.v2Dir))
		{
			return false;
		}
		return true;
	}
	private boolean equals(final Boolean bool1, final Boolean bool2)
	{
		if (bool1 == null && bool2 == null)
		{
			return true;
		}
		else
		{
			if (bool1 == null || bool2 == null)
			{
				return false;
			}
			return bool1.equals(bool2);
		}
	}
	/**
	 * Check if the current router flags are matching the other flags.
	 * @param ruleFlags {@link RouterFlags} object which should be treated as a rule
	 * @return true if this {@link RouterFlags} matches the given rule
	 */
	protected boolean match(final RouterFlags ruleFlags)
	{
		if (!match(ruleFlags.getExit(), getExit()))
		{
			return false;
		}
		if (!match(ruleFlags.getStable(), getStable()))
		{
			return false;
		}
		if (!match(ruleFlags.getFast(), getFast()))
		{
			return false;
		}
		if (!match(ruleFlags.getGuard(), getGuard()))
		{
			return false;
		}
		if (!match(ruleFlags.getHSDir(), getHSDir()))
		{
			return false;
		}
		if (!match(ruleFlags.getNamed(), getNamed()))
		{
			return false;
		}
		if (!match(ruleFlags.getUnnamed(), getUnnamed()))
		{
			return false;
		}
		if (!match(ruleFlags.getValid(), getValid()))
		{
			return false;
		}
		if (!match(ruleFlags.getV2Dir(), getV2Dir()))
		{
			return false;
		}
		if (!match(ruleFlags.getRunning(), getRunning()))
		{
			return false;
		}
		return true;
	}
	/**
	 * Check if the given rule matches the given value.
	 * @param rule if rule is null then the value is ignored.
	 * @param value the value to be checked against the rule
	 * @return true if value matches with rule
	 */
	private boolean match(final Boolean rule, final Boolean value)
	{
		if (rule != null && rule != value)
		{
			return false;
		}
		return true;
	}
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		if (isAuthority())
		{
			buffer.append(IDENTIFIER_AUTHORITY);
			buffer.append(' ');
		}
		if (isBadDirectory())
		{
			buffer.append(IDENTIFIER_BAD_DIRECTORY);
			buffer.append(' ');
		}
		if (isBadExit())
		{
			buffer.append(IDENTIFIER_BAD_EXIT);
			buffer.append(' ');
		}
		if (isExit())
		{
			buffer.append(IDENTIFIER_EXIT);
			buffer.append(' ');
		}
		if (isFast())
		{
			buffer.append(IDENTIFIER_FAST);
			buffer.append(' ');
		}
		if (isGuard())
		{
			buffer.append(IDENTIFIER_GUARD);
			buffer.append(' ');
		}
		if (isHSDir())
		{
			buffer.append(IDENTIFIER_HIDDENSERVICE_DIRECTORY);
			buffer.append(' ');
		}
		if (isNamed())
		{
			buffer.append(IDENTIFIER_NAMED);
			buffer.append(' ');
		}
		if (isStable())
		{
			buffer.append(IDENTIFIER_STABLE);
			buffer.append(' ');
		}
		if (isRunning())
		{
			buffer.append(IDENTIFIER_RUNNING);
			buffer.append(' ');
		}
		if (isUnnamed())
		{
			buffer.append(IDENTIFIER_UNNAMED);
			buffer.append(' ');
		}
		if (isValid())
		{
			buffer.append(IDENTIFIER_VALID);
			buffer.append(' ');
		}
		if (isV2Dir())
		{
			buffer.append(IDENTIFIER_DIRECTORY);
			buffer.append(' ');
		}
		return buffer.toString().trim();
	}
	/** Identifier for Authority. */
	private static final String IDENTIFIER_AUTHORITY = "Authority";
	/** Identifier for Running. */
	private static final String IDENTIFIER_RUNNING = "Running";
	/** Identifier for Exit. */
	private static final String IDENTIFIER_EXIT = "Exit";
	/** Identifier for Fast. */
	private static final String IDENTIFIER_FAST = "Fast";
	/** Identifier for Guard. */
	private static final String IDENTIFIER_GUARD = "Guard";
	/** Identifier for Stable. */
	private static final String IDENTIFIER_STABLE = "Stable";
	/** Identifier for Named. */
	private static final String IDENTIFIER_NAMED = "Named";
	/** Identifier for Unnamed. */
	private static final String IDENTIFIER_UNNAMED = "Unnamed";
	/** Identifier for V2Dir. */
	private static final String IDENTIFIER_DIRECTORY = "V2Dir";
	/** Identifier for Valid. */
	private static final String IDENTIFIER_VALID = "Valid";
	/** Identifier for HSDir. */
	private static final String IDENTIFIER_HIDDENSERVICE_DIRECTORY = "HSDir";
	/** Identifier for BadDirectory. */
	private static final String IDENTIFIER_BAD_DIRECTORY = "BadDirectory";
	/** Identifier for BadExit. */
	private static final String IDENTIFIER_BAD_EXIT = "BadExit";
}
