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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.silvertunnel_ng.netlib.tool.ConvenientStreamReader;
import org.silvertunnel_ng.netlib.tool.ConvenientStreamWriter;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Local test of the class {@link RouterFlags}.
 * 
 * @author Tobias Boese
 */
public final class RouterFlagsLocalTest
{

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags()}.
	 */
	@Test
	public void testRouterFlags()
	{
		RouterFlags flags = new RouterFlags();
		assertNotNull(flags);
		assertFalse(flags.isAuthority());
		assertFalse(flags.isBadDirectory());
		assertFalse(flags.isBadExit());
		assertFalse(flags.isExit());
		assertFalse(flags.isFast());
		assertFalse(flags.isGuard());
		assertFalse(flags.isHSDir());
		assertFalse(flags.isHibernating());
		assertFalse(flags.isNamed());
		assertFalse(flags.isRunning());
		assertFalse(flags.isStable());
		assertFalse(flags.isUnnamed());
		assertFalse(flags.isV2Dir());
		assertFalse(flags.isValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(Boolean)}.
	 */
	@Test
	public void testRouterFlagsTrue()
	{
		RouterFlags flags = new RouterFlags(true);
		assertNotNull(flags);
		assertTrue(flags.isAuthority());
		assertTrue(flags.isBadDirectory());
		assertTrue(flags.isBadExit());
		assertTrue(flags.isExit());
		assertTrue(flags.isFast());
		assertTrue(flags.isGuard());
		assertTrue(flags.isHSDir());
		assertTrue(flags.isHibernating());
		assertTrue(flags.isNamed());
		assertTrue(flags.isRunning());
		assertTrue(flags.isStable());
		assertTrue(flags.isUnnamed());
		assertTrue(flags.isV2Dir());
		assertTrue(flags.isValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(Boolean)}.
	 */
	@Test
	public void testRouterFlagsFalse()
	{
		RouterFlags flags = new RouterFlags(false);
		assertNotNull(flags);
        assertFalse(flags.isAuthority());
        assertFalse(flags.isBadDirectory());
        assertFalse(flags.isBadExit());
        assertFalse(flags.isExit());
        assertFalse(flags.isFast());
        assertFalse(flags.isGuard());
        assertFalse(flags.isHSDir());
        assertFalse(flags.isHibernating());
        assertFalse(flags.isNamed());
        assertFalse(flags.isRunning());
        assertFalse(flags.isStable());
        assertFalse(flags.isUnnamed());
        assertFalse(flags.isV2Dir());
        assertFalse(flags.isValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringExit()
	{
		RouterFlags flagExit = new RouterFlags("Exit");
		assertNotNull(flagExit);
		assertFalse(flagExit.isHibernating());
		assertFalse(flagExit.isAuthority());
		assertFalse(flagExit.isBadDirectory());
		assertFalse(flagExit.isBadExit());
		assertTrue(flagExit.isExit());
		assertFalse(flagExit.isFast());
		assertFalse(flagExit.isGuard());
		assertFalse(flagExit.isHSDir());
		assertFalse(flagExit.isNamed());
		assertFalse(flagExit.isRunning());
		assertFalse(flagExit.isStable());
		assertFalse(flagExit.isUnnamed());
		assertFalse(flagExit.isV2Dir());
		assertFalse(flagExit.isValid());
	}
	
	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringAuthority()
	{
		RouterFlags flag = new RouterFlags("Authority");
		assertNotNull(flag);
		assertTrue(flag.isAuthority());
		assertFalse(flag.isBadDirectory());
		assertFalse(flag.isBadExit());
		assertFalse(flag.isExit());
		assertFalse(flag.isFast());
		assertFalse(flag.isGuard());
		assertFalse(flag.isHSDir());
		assertFalse(flag.isNamed());
		assertFalse(flag.isRunning());
		assertFalse(flag.isStable());
		assertFalse(flag.isUnnamed());
		assertFalse(flag.isV2Dir());
		assertFalse(flag.isValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringUnknown()
	{
		RouterFlags flag = new RouterFlags("Unknown");
		assertNotNull(flag);
		assertFalse(flag.isAuthority());
		assertFalse(flag.isBadDirectory());
		assertFalse(flag.isBadExit());
		assertFalse(flag.isExit());
		assertFalse(flag.isFast());
		assertFalse(flag.isGuard());
		assertFalse(flag.isHSDir());
		assertFalse(flag.isNamed());
		assertFalse(flag.isRunning());
		assertFalse(flag.isStable());
		assertFalse(flag.isUnnamed());
		assertFalse(flag.isV2Dir());
		assertFalse(flag.isValid());
	}

	/**
	 * Testing all possibilities of all flags.
	 */
	@Test
	public void testAllFlags() throws IOException {
		boolean [] list = new boolean [14]; // we have 14 flags to test
		shiftBoolean(list, 0);
		while (!isFalse(list))
		{
			RouterFlags flags1 = new RouterFlags();
			flags1.setAuthority(list[0]);
			flags1.setBadDirectory(list[1]);
			flags1.setBadExit(list[2]);
			flags1.setExit(list[3]);
			flags1.setFast(list[4]);
			flags1.setGuard(list[5]);
			flags1.setHSDir(list[6]);
			flags1.setNamed(list[7]);
			flags1.setRunning(list[8]);
			flags1.setStable(list[9]);
			flags1.setUnnamed(list[10]);
			flags1.setV2Dir(list[11]);
			flags1.setValid(list[12]);
			flags1.setHibernating(list[13]);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ConvenientStreamWriter convenientStreamWriter = new ConvenientStreamWriter(byteArrayOutputStream);
            flags1.save(convenientStreamWriter);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ConvenientStreamReader convenientStreamReader = new ConvenientStreamReader(byteArrayInputStream);
			RouterFlags flags2 = new RouterFlags(convenientStreamReader);
			assertEquals(flags1, flags2);
			shiftBoolean(list, 0);
		}
	}

    /**
     * Testing the matching functionality.
     */
    @Test
    public void testMatch() {
        RouterFlags flags1 = new RouterFlags();
        flags1.setExit(true);
        flags1.setFast(true);
        flags1.setStable(true);

        RouterFlags match = new RouterFlags();
        match.setStable(true);

        assertTrue(flags1.match(match));

        match.setAuthority(true);

        assertFalse(flags1.match(match));
    }
	/**
	 * Check if the given array of Booleans is null.
	 * @param list the array of Booleans to be checked
	 * @return true if all elements are null
	 */
	private boolean isFalse(final boolean [] list)
	{
		for (int i = 0; i < list.length; i++)
		{
			if (list[i])
			{
				return false;
			}
		}
		return true;
	}
	/**
	 * Shifting the Booleans.
	 * null, null -> false, null -> true, null -> null, false -> false, false -> true, false -> null, true -> ...
	 * @param list the array of Booleans which should ne shifted
	 * @param pos the position which should be shifted
	 */
	private void shiftBoolean(final boolean [] list, final int pos)
	{
		if (list[pos] == false)
		{
			list[pos] = true;
		}
		else
		{
			list[pos] = false;
			if (pos + 1 < list.length)
			{
				shiftBoolean(list, pos + 1);
			}
		}
	}
}
