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

import java.util.BitSet;


/**
 * RouterFlags are used for determining the Status of the router (eg. Running, Stable, Fast, etc)
 *
 * @author Tobias Boese
 */
public final class RouterFlags {
    /**
     * flag Running.
     * "Running" if the router is currently usable
     */
    private static int INDEX_RUNNING = 0;
    /**
     * flag Authority.
     * "Authority" if the router is a directory INDEX_AUTHORITY.
     */
    private static int INDEX_AUTHORITY = 1;
    /**
     * "Exit" if the router is more useful for building general-purpose INDEX_EXIT circuits than for relay circuits.
     * The path building algorithm uses this flag; see path-spec.txt.
     */
    private static int INDEX_EXIT = 2;
    /**
     * "BadExit" if the router is believed to be useless as an INDEX_EXIT node
     * (because its ISP censors it, because it is behind a restrictive proxy, or for some similar reason).
     */
    private static int INDEX_BAD_EXIT = 3;
    /**
     * "BadDirectory" if the router is believed to be useless as a directory cache
     * (because its directory port isn't working, its bandwidth is always throttled, or for some similar reason).
     */
    private static int INDEX_BAD_DIRECTORY = 4;
    /**
     * "Fast" if the router is suitable for high-bandwidth circuits.
     */
    private static int INDEX_FAST = 5;
    /**
     * "Guard" if the router is suitable for use as an entry INDEX_GUARD.
     */
    private static int INDEX_GUARD = 6;
    /**
     * "HSDir" if the router is considered a v2 hidden service directory.
     */
    private static int INDEX_HIDDENSERVICE_DIRECTORY = 7;
    /**
     * "Named" if the router's identity-nickname mapping is canonical, and this INDEX_AUTHORITY binds names.
     */
    private static int INDEX_NAMED = 8;
    /**
     * "Stable" if the router is suitable for long-lived circuits.
     */
    private static int INDEX_STABLE = 9;
    /**
     * "Unnamed" if another router has bound the name used by this router, and this INDEX_AUTHORITY binds names.
     */
    private static int INDEX_UNNAMED = 10;
    /**
     * "Valid" if the router has been 'validated'.
     */
    private static int INDEX_VALID = 11;
    /**
     * "V2Dir" if the router implements the v2 directory protocol.
     */
    private static int INDEX_V2DIR = 12;
    /**
     * If the Router is currently INDEX_HIBERNATING we should not use it.
     */
    private static int INDEX_HIBERNATING = 13;

    private BitSet value = new BitSet(14);

    /**
     * Standard Constructor.
     * All values are set to null.
     */
    public RouterFlags() {
        value.set(0, 14, false);
    }

    /**
     * Initialize all values with the given initial value.
     *
     * @param initialValue the initial value used for setting all members.
     */
    public RouterFlags(final Boolean initialValue) {
        value.set(0, 14, initialValue);
    }

    /**
     * Set the flags by parsing a given string.
     *
     * @param flags the String which contains the set flags. All other flags are set to false.
     */
    public RouterFlags(final String flags) {
        setAllFlags(flags);
    }

    /**
     * Set the flags by parsing a given byte array.
     *
     * @param data the byte array which contains the flags.
     */
    public RouterFlags(final byte[] data) {
        Boolean[] flags1 = ByteUtils.getBooleansFromByte(data[0]);
        value.set(INDEX_RUNNING, flags1[0]);
        value.set(INDEX_EXIT, flags1[1]);
        value.set(INDEX_AUTHORITY, flags1[2]);
        value.set(INDEX_FAST, flags1[3]);
        Boolean[] flags2 = ByteUtils.getBooleansFromByte(data[1]);
        value.set(INDEX_GUARD, flags2[0]);
        value.set(INDEX_STABLE, flags2[1]);
        value.set(INDEX_NAMED, flags2[2]);
        value.set(INDEX_UNNAMED, flags2[3]);
        Boolean[] flags3 = ByteUtils.getBooleansFromByte(data[2]);
        value.set(INDEX_V2DIR, flags3[0]);
        value.set(INDEX_VALID, flags3[1]);
        value.set(INDEX_HIDDENSERVICE_DIRECTORY, flags3[2]);
        value.set(INDEX_BAD_DIRECTORY, flags3[3]);
        Boolean[] flags4 = ByteUtils.getBooleansFromByte(data[3]);
        value.set(INDEX_BAD_EXIT, flags4[0]);
        value.set(INDEX_HIBERNATING, flags4[1]);
    }

    /**
     * Set all flags by given flags as string.
     *
     * @param flags the flags as string
     */
    public void setAllFlags(final String flags) {
        value.set(INDEX_RUNNING, flags.contains(IDENTIFIER_RUNNING));
        value.set(INDEX_EXIT, flags.contains(IDENTIFIER_EXIT));
        value.set(INDEX_AUTHORITY, flags.contains(IDENTIFIER_AUTHORITY));
        value.set(INDEX_FAST, flags.contains(IDENTIFIER_FAST));
        value.set(INDEX_GUARD, flags.contains(IDENTIFIER_GUARD));
        value.set(INDEX_STABLE, flags.contains(IDENTIFIER_STABLE));
        value.set(INDEX_NAMED, flags.contains(IDENTIFIER_NAMED));
        value.set(INDEX_UNNAMED, flags.contains(IDENTIFIER_UNNAMED));
        value.set(INDEX_V2DIR, flags.contains(IDENTIFIER_DIRECTORY));
        value.set(INDEX_VALID, flags.contains(IDENTIFIER_VALID));
        value.set(INDEX_HIDDENSERVICE_DIRECTORY, flags.contains(IDENTIFIER_HIDDENSERVICE_DIRECTORY));
        value.set(INDEX_BAD_DIRECTORY, flags.contains(IDENTIFIER_BAD_DIRECTORY));
        value.set(INDEX_BAD_EXIT, flags.contains(IDENTIFIER_BAD_EXIT));
    }

    /**
     * Convert the RouterFlags to a byte array.
     *
     * @return a byte array containing the RouterFlag information.
     */
    protected byte[] toByteArray() {
        byte[] result = new byte[4];
        result[0] = ByteUtils.getByteFromBooleans(isRunning(), isExit(), isAuthority(), isFast());
        result[1] = ByteUtils.getByteFromBooleans(isGuard(), isStable(), isNamed(), isUnnamed());
        result[2] = ByteUtils.getByteFromBooleans(isV2Dir(), isValid(), isHSDir(), isBadDirectory());
        result[3] = ByteUtils.getByteFromBooleans(isBadExit(), isHibernating());
        return result;
    }

    /**
     * @return the INDEX_RUNNING as boolean (will return false if not set)
     */
    public boolean isRunning() {
        return value.get(INDEX_RUNNING);
    }

    /**
     * @param running the INDEX_RUNNING to set
     */
    public void setRunning(final Boolean running) {
        value.set(INDEX_RUNNING, running);
    }

    /**
     * @return is the router currently INDEX_HIBERNATING? (will return false if not set)
     */
    public boolean isHibernating() {
        return value.get(INDEX_HIBERNATING);
    }

    /**
     * @param hibernating the INDEX_HIBERNATING to set
     */
    public void setHibernating(final Boolean hibernating) {
        value.set(INDEX_HIBERNATING, hibernating);
    }

    /**
     * @return the INDEX_AUTHORITY as boolean (will return false if not set)
     */
    public boolean isAuthority() {
        return value.get(INDEX_AUTHORITY);
    }

    /**
     * @param authority the INDEX_AUTHORITY to set
     */
    public void setAuthority(final Boolean authority) {
        value.set(INDEX_AUTHORITY, authority);
    }

    /**
     * @return the INDEX_EXIT as boolean (will return false if not set)
     */
    public boolean isExit() {
        return value.get(INDEX_EXIT);
    }

    /**
     * @param exit the INDEX_EXIT to set
     */
    public void setExit(final Boolean exit) {
        value.set(INDEX_EXIT, exit);
    }

    /**
     * @return the INDEX_BAD_EXIT as boolean (will return false if not set)
     */
    public boolean isBadExit() {
        return value.get(INDEX_BAD_EXIT);
    }

    /**
     * @param badExit the INDEX_BAD_EXIT to set
     */
    public void setBadExit(final Boolean badExit) {
        value.set(INDEX_BAD_EXIT, badExit);
    }

    /**
     * @return the INDEX_BAD_DIRECTORY as boolean (will return false if not set)
     */
    public boolean isBadDirectory() {
        return value.get(INDEX_BAD_DIRECTORY);
    }

    /**
     * @param badDirectory the INDEX_BAD_DIRECTORY to set
     */
    public void setBadDirectory(final Boolean badDirectory) {
        value.set(INDEX_BAD_DIRECTORY, badDirectory);
    }

    /**
     * @return the INDEX_FAST as boolean (will return false if not set)
     */
    public boolean isFast() {
        return value.get(INDEX_FAST);
    }

    /**
     * @param fast the INDEX_FAST to set
     */
    public void setFast(final Boolean fast) {
        value.set(INDEX_FAST, fast);
    }

    /**
     * @return the INDEX_GUARD as boolean (will return false if not set)
     */
    public boolean isGuard() {
        return value.get(INDEX_GUARD);
    }

    /**
     * @param guard the INDEX_GUARD to set
     */
    public void setGuard(final Boolean guard) {
        value.set(INDEX_GUARD, guard);
    }

    /**
     * @return the INDEX_HIDDENSERVICE_DIRECTORY as boolean (will return false if not set)
     */
    public boolean isHSDir() {
        return value.get(INDEX_HIDDENSERVICE_DIRECTORY);
    }

    /**
     * @param hSDir the INDEX_HIDDENSERVICE_DIRECTORY to set
     */
    public void setHSDir(final Boolean hSDir) {
        value.set(INDEX_HIDDENSERVICE_DIRECTORY, hSDir);
    }

    /**
     * @return the INDEX_NAMED as boolean (will return false if not set)
     */
    public boolean isNamed() {
        return value.get(INDEX_NAMED);
    }

    /**
     * @param named the INDEX_NAMED to set
     */
    public void setNamed(final Boolean named) {
        value.set(INDEX_NAMED, named);
    }

    /**
     * @return the INDEX_STABLE as boolean (will return false if not set)
     */
    public boolean isStable() {
        return value.get(INDEX_STABLE);
    }

    /**
     * @param stable the INDEX_STABLE to set
     */
    public void setStable(final Boolean stable) {
        value.set(INDEX_STABLE, stable);
    }

    /**
     * @return the INDEX_UNNAMED as boolean (will return false if not set)
     */
    public boolean isUnnamed() {
        return value.get(INDEX_UNNAMED);
    }

    /**
     * @param unnamed the INDEX_UNNAMED to set
     */
    public void setUnnamed(final Boolean unnamed) {
        value.set(INDEX_UNNAMED, unnamed);
    }

    /**
     * @return the INDEX_VALID as boolean (will return false if not set)
     */
    public boolean isValid() {
        return value.get(INDEX_VALID);
    }

    /**
     * @param valid the INDEX_VALID to set
     */
    public void setValid(final Boolean valid) {
        value.set(INDEX_VALID, valid);
    }

    /**
     * @return the INDEX_V2DIR as boolean (will return false if not set)
     */
    public boolean isV2Dir() {
        return value.get(INDEX_V2DIR);
    }

    /**
     * @param v2Dir the INDEX_V2DIR to set
     */
    public void setV2Dir(final Boolean v2Dir) {
        value.set(INDEX_V2DIR, v2Dir);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouterFlags that = (RouterFlags) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Check if the current router flags are matching the other flags.
     *
     * @param ruleFlags {@link RouterFlags} object which should be treated as a rule
     * @return true if this {@link RouterFlags} matches the given rule
     */
    protected boolean match(final RouterFlags ruleFlags) {
        String [] flags = ruleFlags.toString().split(" ");
        String myFlags = toString();
        for (String flag : flags) {
            if (!myFlags.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (isAuthority()) {
            buffer.append(IDENTIFIER_AUTHORITY);
            buffer.append(' ');
        }
        if (isBadDirectory()) {
            buffer.append(IDENTIFIER_BAD_DIRECTORY);
            buffer.append(' ');
        }
        if (isBadExit()) {
            buffer.append(IDENTIFIER_BAD_EXIT);
            buffer.append(' ');
        }
        if (isExit()) {
            buffer.append(IDENTIFIER_EXIT);
            buffer.append(' ');
        }
        if (isFast()) {
            buffer.append(IDENTIFIER_FAST);
            buffer.append(' ');
        }
        if (isGuard()) {
            buffer.append(IDENTIFIER_GUARD);
            buffer.append(' ');
        }
        if (isHibernating()) {
            buffer.append(IDENTIFIER_HIBERNATING);
            buffer.append(' ');
        }
        if (isHSDir()) {
            buffer.append(IDENTIFIER_HIDDENSERVICE_DIRECTORY);
            buffer.append(' ');
        }
        if (isNamed()) {
            buffer.append(IDENTIFIER_NAMED);
            buffer.append(' ');
        }
        if (isStable()) {
            buffer.append(IDENTIFIER_STABLE);
            buffer.append(' ');
        }
        if (isRunning()) {
            buffer.append(IDENTIFIER_RUNNING);
            buffer.append(' ');
        }
        if (isUnnamed()) {
            buffer.append(IDENTIFIER_UNNAMED);
            buffer.append(' ');
        }
        if (isValid()) {
            buffer.append(IDENTIFIER_VALID);
            buffer.append(' ');
        }
        if (isV2Dir()) {
            buffer.append(IDENTIFIER_DIRECTORY);
            buffer.append(' ');
        }
        return buffer.toString().trim();
    }

    /**
     * Identifier for Authority.
     */
    private static final String IDENTIFIER_AUTHORITY = "Authority";
    /**
     * Identifier for Running.
     */
    private static final String IDENTIFIER_RUNNING = "Running";
    /**
     * Identifier for Exit.
     */
    private static final String IDENTIFIER_EXIT = "Exit";
    /**
     * Identifier for Fast.
     */
    private static final String IDENTIFIER_FAST = "Fast";
    /**
     * Identifier for Guard.
     */
    private static final String IDENTIFIER_GUARD = "Guard";
    /**
     * Identifier for Stable.
     */
    private static final String IDENTIFIER_STABLE = "Stable";
    /**
     * Identifier for Named.
     */
    private static final String IDENTIFIER_NAMED = "Named";
    /**
     * Identifier for Unnamed.
     */
    private static final String IDENTIFIER_UNNAMED = "Unnamed";
    /**
     * Identifier for V2Dir.
     */
    private static final String IDENTIFIER_DIRECTORY = "V2Dir";
    /**
     * Identifier for Valid.
     */
    private static final String IDENTIFIER_VALID = "Valid";
    /**
     * Identifier for HSDir.
     */
    private static final String IDENTIFIER_HIDDENSERVICE_DIRECTORY = "HSDir";
    /**
     * Identifier for Hibernating.
     */
    private static final String IDENTIFIER_HIBERNATING = "Hibernating";
    /**
     * Identifier for BadDirectory.
     */
    private static final String IDENTIFIER_BAD_DIRECTORY = "BadDirectory";
    /**
     * Identifier for BadExit.
     */
    private static final String IDENTIFIER_BAD_EXIT = "BadExit";
}
