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
package org.silvertunnel_ng.netlib.layer.tor.common;

import static org.testng.AssertJUnit.*;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.testng.annotations.Test;

/**
 * Test the {@link TCPStreamProperties} class.
 * 
 * @author Tobias Boese
 */
public final class TCPStreamPropertiesLocalTest
{

	/**
	 * Test method for {@link TCPStreamProperties#TCPStreamProperties(String, int)}.
	 */
	@Test(enabled = false)
	public void testTCPStreamPropertiesStringInt()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#TCPStreamProperties(InetAddress, int)}.
	 */
	@Test(enabled = false)
	public void testTCPStreamPropertiesInetAddressInt()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#TCPStreamProperties(TcpipNetAddress)}.
	 */
	@Test(enabled = false)
	public void testTCPStreamPropertiesTcpipNetAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#TCPStreamProperties()}.
	 */
	@Test(enabled = false)
	public void testTCPStreamProperties()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setCustomRoute(Fingerprint[])}.
	 */
	@Test(enabled = false)
	public void testSetCustomRoute()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getCustomExitpoint()}.
	 */
	@Test(enabled = false)
	public void testGetCustomExitpoint()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setCustomExitpoint(Fingerprint)}.
	 */
	@Test(enabled = false)
	public void testSetCustomExitpoint()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getProposedRouteFingerprints()}.
	 */
	@Test(enabled = false)
	public void testGetProposedRouteFingerprints()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getDestination()}.
	 */
	@Test(enabled = false)
	public void testGetDestination()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getRankingInfluenceIndex()}.
	 */
	@Test(enabled = false)
	public void testGetRankingInfluenceIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setRankingInfluenceIndex(float)}.
	 */
	@Test(enabled = false)
	public void testSetRankingInfluenceIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setMinRouteLength(int)}.
	 */
	@Test
	public void testSetMinRouteLength()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(TorConfig.getRouteMinLength(), streamProperties.getMinRouteLength());
		streamProperties.setMinRouteLength(55);
		assertEquals(55, streamProperties.getMinRouteLength());
	}

	/**
	 * Test method for {@link TCPStreamProperties#setMaxRouteLength(int)}.
	 */
	@Test
	public void testSetMaxRouteLength()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(TorConfig.getRouteMaxLength(), streamProperties.getMaxRouteLength());
		streamProperties.setMaxRouteLength(55);
		assertEquals(55, streamProperties.getMaxRouteLength());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getMinRouteLength()}.
	 */
	@Test
	public void testGetMinRouteLength()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(TorConfig.getRouteMinLength(), streamProperties.getMinRouteLength());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getMaxRouteLength()}.
	 */
	@Test
	public void testGetMaxRouteLength()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(TorConfig.getRouteMaxLength(), streamProperties.getMaxRouteLength());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getHostname()}.
	 * When no hostname and no ip is set it should return null.
	 */
	@Test
	public void testGetHostnameNullNull()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertNull(streamProperties.getHostname());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getHostname()}.
	 * When a hostname and no ip is set it should return the hostname.
	 */
	@Test
	public void testGetHostnameNotNull()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties("myhost", 22);
		assertNotNull(streamProperties.getHostname());
		assertEquals("myhost", streamProperties.getHostname());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getHostname()}.
	 * When no hostname but a ip is set it should return the ip as hostname.
	 */
	@Test
	public void testGetHostnameAsIP()
	{
		TcpipNetAddress address = new TcpipNetAddress(new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4}, 22);
		TCPStreamProperties streamProperties = new TCPStreamProperties(address);
		assertNotNull(streamProperties.getHostname());
		assertEquals("1.2.3.4", streamProperties.getHostname());
	}

	/**
	 * Test method for {@link TCPStreamProperties#setAddr(java.net.InetAddress)}.
	 */
	@Test(enabled = false)
	public void testSetAddr()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getAddr()}.
	 */
	@Test(enabled = false)
	public void testGetAddr()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isAddrResolved()}.
	 */
	@Test(enabled = false)
	public void testIsAddrResolved()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setAddrResolved(boolean)}.
	 */
	@Test(enabled = false)
	public void testSetAddrResolved()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isUntrustedExitAllowed()}.
	 */
	@Test(enabled = false)
	public void testIsUntrustedExitAllowed()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setUntrustedExitAllowed(boolean)}.
	 */
	@Test(enabled = false)
	public void testSetUntrustedExitAllowed()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isNonGuardEntryAllowed()}.
	 */
	@Test(enabled = false)
	public void testIsNonGuardEntryAllowed()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setNonGuardEntryAllowed(boolean)}.
	 */
	@Test(enabled = false)
	public void testSetNonGuardEntryAllowed()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isExitPolicyRequired()}.
	 */
	@Test(enabled = false)
	public void testIsExitPolicyRequired()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setExitPolicyRequired(boolean)}.
	 */
	@Test(enabled = false)
	public void testSetExitPolicyRequired()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getPort()}.
	 */
	@Test
	public void testGetPort()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(0, streamProperties.getPort());
		TcpipNetAddress address = new TcpipNetAddress("myhost", 22);
		streamProperties = new TCPStreamProperties(address);
		assertEquals(22, streamProperties.getPort());
	}

	/**
	 * Test method for {@link TCPStreamProperties#setPort(int)}.
	 */
	@Test
	public void testSetPort()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertEquals(0, streamProperties.getPort());
		streamProperties.setPort(22);
		assertEquals(22, streamProperties.getPort());
	}

	/**
	 * Test method for {@link TCPStreamProperties#getConnectRetries()}.
	 */
	@Test(enabled = false)
	public void testGetConnectRetries()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setConnectRetries(int)}.
	 */
	@Test(enabled = false)
	public void testSetConnectRetries()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#getRouteFingerprints()}.
	 */
	@Test(enabled = false)
	public void testGetRouteFingerprints()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isFastRoute()}.
	 */
	@Test(enabled = false)
	public void testIsFastRoute()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#setFastRoute(boolean)}.
	 */
	@Test(enabled = false)
	public void testSetFastRoute()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link TCPStreamProperties#isStableRoute()}.
	 * Check the default setting for stable route.
	 */
	@Test
	public void testIsStableRouteDefault()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertFalse(streamProperties.isStableRoute());
	}

	/**
	 * List of "long-lived" ports listed in path-spec 2.2. a circuit needs to be
	 * "stable" for these ports. 
	 */
	private static final int[] LONG_LIVED_PORTS = { 21, 22, 706, 1863, 5050, 5190, 5222, 5223, 6667, 6697, 8300 };

	/**
	 * Test method for {@link TCPStreamProperties#isStableRoute()}.
	 * Check if the stable route flag is set to true for the "long-lived" ports.
	 */
	@Test
	public void testIsStableRouteLongLivedPort()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertFalse(streamProperties.isStableRoute());
		for (int tmpPort : LONG_LIVED_PORTS)
		{
			streamProperties.setPort(tmpPort);
			assertTrue(streamProperties.isStableRoute());
			streamProperties.setPort(0);
			assertFalse(streamProperties.isStableRoute());
		}
	}

	/**
	 * Test method for {@link TCPStreamProperties#setStableRoute(boolean)}.
	 */
	@Test
	public void testSetStableRoute()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertFalse(streamProperties.isStableRoute());
		streamProperties.setStableRoute(true);
		assertTrue(streamProperties.isStableRoute());
		streamProperties.setStableRoute(false);
		assertFalse(streamProperties.isStableRoute());
	}

	/**
	 * Test method for {@link TCPStreamProperties#isConnectToDirServer()}.
	 */
	@Test
	public void testIsConnectToDirServer()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertFalse(streamProperties.isConnectToDirServer());
	}

	/**
	 * Test method for {@link TCPStreamProperties#setConnectToDirServer(boolean)}.
	 */
	@Test
	public void testSetConnectToDirServer()
	{
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		assertFalse(streamProperties.isConnectToDirServer());
		streamProperties.setConnectToDirServer(true);
		assertTrue(streamProperties.isConnectToDirServer());
		streamProperties.setConnectToDirServer(false);
		assertFalse(streamProperties.isConnectToDirServer());
	}
}
