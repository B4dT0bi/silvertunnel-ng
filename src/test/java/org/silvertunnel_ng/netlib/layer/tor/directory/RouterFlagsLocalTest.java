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

import org.testng.annotations.Test;


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
		// all members should be null in its initial state
		assertNull(flags.getAuthority());
		assertNull(flags.getBadDirectory());
		assertNull(flags.getBadExit());
		assertNull(flags.getExit());
		assertNull(flags.getFast());
		assertNull(flags.getGuard());
		assertNull(flags.getHSDir());
		assertNull(flags.getHibernating());
		assertNull(flags.getNamed());
		assertNull(flags.getRunning());
		assertNull(flags.getStable());
		assertNull(flags.getUnnamed());
		assertNull(flags.getV2Dir());
		assertNull(flags.getValid());
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
		assertNotNull(flags.getAuthority());
		assertNotNull(flags.getBadDirectory());
		assertNotNull(flags.getBadExit());
		assertNotNull(flags.getExit());
		assertNotNull(flags.getFast());
		assertNotNull(flags.getGuard());
		assertNotNull(flags.getHSDir());
		assertNotNull(flags.getHibernating());
		assertNotNull(flags.getNamed());
		assertNotNull(flags.getRunning());
		assertNotNull(flags.getStable());
		assertNotNull(flags.getUnnamed());
		assertNotNull(flags.getV2Dir());
		assertNotNull(flags.getValid());
		assertTrue(flags.getAuthority());
		assertTrue(flags.getBadDirectory());
		assertTrue(flags.getBadExit());
		assertTrue(flags.getExit());
		assertTrue(flags.getFast());
		assertTrue(flags.getGuard());
		assertTrue(flags.getHSDir());
		assertTrue(flags.getHibernating());
		assertTrue(flags.getNamed());
		assertTrue(flags.getRunning());
		assertTrue(flags.getStable());
		assertTrue(flags.getUnnamed());
		assertTrue(flags.getV2Dir());
		assertTrue(flags.getValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(Boolean)}.
	 */
	@Test
	public void testRouterFlagsFalse()
	{
		RouterFlags flags = new RouterFlags(false);
		assertNotNull(flags);
		assertNotNull(flags.getAuthority());
		assertNotNull(flags.getBadDirectory());
		assertNotNull(flags.getBadExit());
		assertNotNull(flags.getExit());
		assertNotNull(flags.getFast());
		assertNotNull(flags.getGuard());
		assertNotNull(flags.getHSDir());
		assertNotNull(flags.getHibernating());
		assertNotNull(flags.getNamed());
		assertNotNull(flags.getRunning());
		assertNotNull(flags.getStable());
		assertNotNull(flags.getUnnamed());
		assertNotNull(flags.getV2Dir());
		assertNotNull(flags.getValid());
		assertFalse(flags.getAuthority());
		assertFalse(flags.getBadDirectory());
		assertFalse(flags.getBadExit());
		assertFalse(flags.getExit());
		assertFalse(flags.getFast());
		assertFalse(flags.getGuard());
		assertFalse(flags.getHSDir());
		assertFalse(flags.getHibernating());
		assertFalse(flags.getNamed());
		assertFalse(flags.getRunning());
		assertFalse(flags.getStable());
		assertFalse(flags.getUnnamed());
		assertFalse(flags.getV2Dir());
		assertFalse(flags.getValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringExit()
	{
		RouterFlags flagExit = new RouterFlags("Exit");
		assertNotNull(flagExit);
		assertNotNull(flagExit.getAuthority());
		assertNotNull(flagExit.getBadDirectory());
		assertNotNull(flagExit.getBadExit());
		assertNotNull(flagExit.getExit());
		assertNotNull(flagExit.getFast());
		assertNotNull(flagExit.getGuard());
		assertNotNull(flagExit.getHSDir());
		assertNotNull(flagExit.getNamed());
		assertNotNull(flagExit.getRunning());
		assertNotNull(flagExit.getStable());
		assertNotNull(flagExit.getUnnamed());
		assertNotNull(flagExit.getV2Dir());
		assertNotNull(flagExit.getValid());
		assertNull(flagExit.getHibernating());
		assertFalse(flagExit.getAuthority());
		assertFalse(flagExit.getBadDirectory());
		assertFalse(flagExit.getBadExit());
		assertTrue(flagExit.getExit());
		assertFalse(flagExit.getFast());
		assertFalse(flagExit.getGuard());
		assertFalse(flagExit.getHSDir());
		assertFalse(flagExit.getNamed());
		assertFalse(flagExit.getRunning());
		assertFalse(flagExit.getStable());
		assertFalse(flagExit.getUnnamed());
		assertFalse(flagExit.getV2Dir());
		assertFalse(flagExit.getValid());
	}
	
	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringAuthority()
	{
		RouterFlags flag = new RouterFlags("Authority");
		assertNotNull(flag);
		assertNotNull(flag.getAuthority());
		assertNotNull(flag.getBadDirectory());
		assertNotNull(flag.getBadExit());
		assertNotNull(flag.getExit());
		assertNotNull(flag.getFast());
		assertNotNull(flag.getGuard());
		assertNotNull(flag.getHSDir());
		assertNotNull(flag.getNamed());
		assertNotNull(flag.getRunning());
		assertNotNull(flag.getStable());
		assertNotNull(flag.getUnnamed());
		assertNotNull(flag.getV2Dir());
		assertNotNull(flag.getValid());
		assertNull(flag.getHibernating());
		assertTrue(flag.getAuthority());
		assertFalse(flag.getBadDirectory());
		assertFalse(flag.getBadExit());
		assertFalse(flag.getExit());
		assertFalse(flag.getFast());
		assertFalse(flag.getGuard());
		assertFalse(flag.getHSDir());
		assertFalse(flag.getNamed());
		assertFalse(flag.getRunning());
		assertFalse(flag.getStable());
		assertFalse(flag.getUnnamed());
		assertFalse(flag.getV2Dir());
		assertFalse(flag.getValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(java.lang.String)}.
	 */
	@Test
	public void testRouterFlagsStringUnknown()
	{
		RouterFlags flag = new RouterFlags("Unknown");
		assertNotNull(flag);
		assertNotNull(flag.getAuthority());
		assertNotNull(flag.getBadDirectory());
		assertNotNull(flag.getBadExit());
		assertNotNull(flag.getExit());
		assertNotNull(flag.getFast());
		assertNotNull(flag.getGuard());
		assertNotNull(flag.getHSDir());
		assertNotNull(flag.getNamed());
		assertNotNull(flag.getRunning());
		assertNotNull(flag.getStable());
		assertNotNull(flag.getUnnamed());
		assertNotNull(flag.getV2Dir());
		assertNotNull(flag.getValid());
		assertNull(flag.getHibernating());
		assertFalse(flag.getAuthority());
		assertFalse(flag.getBadDirectory());
		assertFalse(flag.getBadExit());
		assertFalse(flag.getExit());
		assertFalse(flag.getFast());
		assertFalse(flag.getGuard());
		assertFalse(flag.getHSDir());
		assertFalse(flag.getNamed());
		assertFalse(flag.getRunning());
		assertFalse(flag.getStable());
		assertFalse(flag.getUnnamed());
		assertFalse(flag.getV2Dir());
		assertFalse(flag.getValid());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags#RouterFlags(byte[])}.
	 */
	@Test
	public void testRouterFlagsByteArrayNull()
	{
		RouterFlags flags = new RouterFlags();
		assertNotNull(flags);
		// all members should be null in its initial state
		assertNull(flags.getAuthority());
		assertNull(flags.getBadDirectory());
		assertNull(flags.getBadExit());
		assertNull(flags.getExit());
		assertNull(flags.getFast());
		assertNull(flags.getGuard());
		assertNull(flags.getHSDir());
		assertNull(flags.getHibernating());
		assertNull(flags.getNamed());
		assertNull(flags.getRunning());
		assertNull(flags.getStable());
		assertNull(flags.getUnnamed());
		assertNull(flags.getV2Dir());
		assertNull(flags.getValid());
		
		byte [] tmp = flags.toByteArray();
		
		RouterFlags flags2 = new RouterFlags(tmp);
		assertNotNull(flags2);
		// all members should be null
		assertNull(flags2.getAuthority());
		assertNull(flags2.getBadDirectory());
		assertNull(flags2.getBadExit());
		assertNull(flags2.getExit());
		assertNull(flags2.getFast());
		assertNull(flags2.getGuard());
		assertNull(flags2.getHSDir());
		assertNull(flags2.getHibernating());
		assertNull(flags2.getNamed());
		assertNull(flags2.getRunning());
		assertNull(flags2.getStable());
		assertNull(flags2.getUnnamed());
		assertNull(flags2.getV2Dir());
		assertNull(flags2.getValid());
		
		assertEquals(flags, flags2);
	}
	/**
	 * Testing all possibilities of all flags.
	 */
	@Test
	public void testAllFlags()
	{
		Boolean [] list = new Boolean [14]; // we have 14 flags to test
		shiftBoolean(list, 0);
		while (!isNull(list))
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
			
			RouterFlags flags2 = new RouterFlags(flags1.toByteArray());
			assertEquals(flags1, flags2);
			shiftBoolean(list, 0);
		}
	}
	/**
	 * Check if the given array of Booleans is null.
	 * @param list the array of Booleans to be checked
	 * @return true if all elements are null
	 */
	private boolean isNull(final Boolean [] list)
	{
		for (int i = 0; i < list.length; i++)
		{
			if (list[i] != null)
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
	private void shiftBoolean(final Boolean [] list, final int pos)
	{
		if (list[pos] == null)
		{
			list[pos] = false;
		}
		else if (!list[pos])
		{
			list[pos] = true;
		}
		else
		{
			list[pos] = null;
			if (pos + 1 < list.length)
			{
				shiftBoolean(list, pos + 1);
			}
		}
	}
}
