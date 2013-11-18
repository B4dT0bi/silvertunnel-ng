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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;

import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.testng.annotations.Test;

/**
 * Testing the {@link RouterImpl} class.
 * 
 * @author Tobias Boese
 * 
 */
public final class RouterImplLocalTest
{
	/**
	 * Example descriptor from router : chaoscomputerclub27.
	 */
	private static final String EXAMPLE_SERVER_DESCRIPTOR_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-router-descriptor.txt";
	/**
	 * Example descriptors from all routers.
	 */
	private static final String EXAMPLE_SERVER_DESCRIPTORS_PATH = "/org/silvertunnel_ng/netlib/layer/tor/example-router-descriptors.txt";

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#RouterImpl(org.silvertunnel_ng.netlib.layer.tor.common.TorConfig, java.lang.String)}
	 * .
	 * 
	 * @throws IOException
	 * @throws TorException
	 */
	@Test
	public void testRouterImplTorConfigString() throws TorException, IOException
	{
		String descriptor = FileUtil.getInstance().readFileFromClasspath(EXAMPLE_SERVER_DESCRIPTOR_PATH);
		RouterImpl testObject = new RouterImpl(TorConfig.getInstance(), descriptor);
		assertNotNull(testObject);
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#RouterImpl(org.silvertunnel_ng.netlib.layer.tor.common.TorConfig, java.lang.String, java.net.InetAddress, int, int, org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint, org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint)}
	 * .
	 */
	@Test(enabled = false)
	public void testRouterImplTorConfigStringInetAddressIntIntFingerprintFingerprint()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#updateServerStatus(boolean, boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testUpdateServerStatusBooleanBoolean()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#updateServerStatus(java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testUpdateServerStatusString()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#regularExpression()}
	 * .
	 */
	@Test(enabled = false)
	public void testRegularExpression()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#cloneReliable()}
	 * .
	 */
	@Test(enabled = false)
	public void testCloneReliable()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#parseRouterDescriptors(org.silvertunnel_ng.netlib.layer.tor.common.TorConfig, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testParseRouterDescriptors()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#renderRouterDescriptor()}
	 * .
	 */
	@Test(enabled = false)
	public void testRenderRouterDescriptor()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getRefinedRankingIndex(float)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetRefinedRankingIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#punishRanking()}
	 * .
	 */
	@Test(enabled = false)
	public void testPunishRanking()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#exitPolicyAccepts(java.net.InetAddress, int)}
	 * .
	 */
	@Test(enabled = false)
	public void testExitPolicyAccepts()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirServer()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirServer()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isExitNode()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsExitNode()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#toString()}
	 * .
	 */
	@Test(enabled = false)
	public void testToString()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#toLongString()}
	 * .
	 */
	@Test(enabled = false)
	public void testToLongString()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isValid()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsValid()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getDirAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetDirAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getOrAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOrAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getNickname()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetNickname()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getHostname()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetHostname()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getCountryCode()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetCountryCode()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getOrPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOrPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getSocksPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetSocksPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getDirPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetDirPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getBandwidthAvg()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthAvg()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getBandwidthBurst()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthBurst()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getBandwidthObserved()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthObserved()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getPlatform()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetPlatform()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getPublished()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetPublished()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getFingerprint()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetFingerprint()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getV3Ident()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetV3Ident()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getUptime()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetUptime()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getOnionKey()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOnionKey()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getSigningKey()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetSigningKey()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getExitpolicy()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetExitpolicy()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getContact()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetContact()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getFamily()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetFamily()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getValidUntil()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetValidUntil()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getLastUpdate()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetLastUpdate()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Authority()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Authority()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Authority(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Authority()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Exit()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Exit()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Exit(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Exit()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Fast()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Fast()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Fast(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Fast()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Guard()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Guard()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Guard(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Guard()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Named()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Named()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Named(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Named()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Stable()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Stable()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Stable(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Stable()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Running()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Running()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Running(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Running()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2Valid()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Valid()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2Valid(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2Valid()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2V2dir()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2V2dir()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setDirv2V2dir(boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetDirv2V2dir()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#isDirv2HSDir()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2HSDir()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getRankingIndex()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetRankingIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#setRankingIndex(float)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetRankingIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getHighBandwidth()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetHighBandwidth()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl#getRouterDescriptor()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetRouterDescriptor()
	{
		fail("Not yet implemented");
	}

}
