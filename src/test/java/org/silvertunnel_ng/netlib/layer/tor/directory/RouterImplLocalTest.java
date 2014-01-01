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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.tool.ByteUtils;
import org.silvertunnel_ng.netlib.tool.DynByteBuffer;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.testng.annotations.BeforeClass;
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

	private String descriptor;
	private String descriptors;

	@BeforeClass
	public void setUp() throws IOException
	{
		descriptor = FileUtil.readFileFromClasspath(EXAMPLE_SERVER_DESCRIPTOR_PATH);
		descriptors = FileUtil.readFileFromClasspath(EXAMPLE_SERVER_DESCRIPTORS_PATH);		
	}
	/**
	 * Test method for
	 * {@link RouterImpl#RouterImpl(String)}
	 * .
	 * 
	 * @throws IOException
	 * @throws TorException
	 */
	@Test
	public void testRouterImplTorConfigString() throws TorException, IOException
	{
		final RouterImpl testObject = new RouterImpl(descriptor);
		assertNotNull("parsing the descriptor didnt worked (should not return null)", testObject);
		assertEquals("wrong contact info", "J. Random Hacker <anonymizer@ccc.de>", testObject.getContact());
		assertEquals("wrong countrycode", "AT", testObject.getCountryCode());
		assertEquals("wrong nickname", "chaoscomputerclub27", testObject.getNickname());
		assertEquals("wrong tor version", "Tor 0.2.4.17-rc on Linux", testObject.getPlatform());
		assertTrue("wrong hidden service dir setting", testObject.isDirv2HSDir());
		assertEquals("wrong ip address", "77.244.254.227", testObject.getHostname());
		assertEquals("wrong or port", 443, testObject.getOrPort());
		assertEquals("wrong socks port", 0, testObject.getSocksPort());
		assertEquals("wrong dir port", 80, testObject.getDirPort());
		assertEquals("wrong fingerprint",
		             new FingerprintImpl(DatatypeConverter.parseHexBinary("EEC954FB78B4FE48C6783FC3CB2E8562092890B8")),
		             testObject.getFingerprint());
		assertEquals("wrong uptime", 5637742, testObject.getUptime());
		assertEquals("wrong number of family members", 11, testObject.getFamily().size());
		assertTrue("familymember 1 (11a0239fc6668705f68842811318b669c636f86e) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("11a0239fc6668705f68842811318b669c636f86e"))));
		assertTrue("familymember 2 (659df6537d605feab3b77e58e75342d704f0a799) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("659df6537d605feab3b77e58e75342d704f0a799"))));
		assertTrue("familymember 3 (71e78e9b961d5e25f1a16fccd15e81aa3b36cb93) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("71e78e9b961d5e25f1a16fccd15e81aa3b36cb93"))));
		assertTrue("familymember 4 (7610bbd3f5bb67284eee8476721ae6109dc29bea) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("7610bbd3f5bb67284eee8476721ae6109dc29bea"))));
		assertTrue("familymember 5 (7BE683E65D48141321C5ED92F075C55364AC7123) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("7BE683E65D48141321C5ED92F075C55364AC7123"))));
		assertTrue("familymember 6 (92d151a8219cc742de7e0eaeb6d18faf9793ba79) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("92d151a8219cc742de7e0eaeb6d18faf9793ba79"))));
		assertTrue("familymember 7 (9e9fad3187c9911b71849e0e63f35c7cd41faaa3) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("9e9fad3187c9911b71849e0e63f35c7cd41faaa3"))));
		assertTrue("familymember 8 (a9c039a5fd02fca06303dcfaabe25c5912c63b26) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("a9c039a5fd02fca06303dcfaabe25c5912c63b26"))));
		assertTrue("familymember 9 (d5edc74f2fb81e6ac1a8eba56448f71ddfaa4ae5) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("d5edc74f2fb81e6ac1a8eba56448f71ddfaa4ae5"))));
		assertTrue("familymember 10 (fbadb0598b2fe16aa1a078187620ec4c2df08451) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("fbadb0598b2fe16aa1a078187620ec4c2df08451"))));
		assertTrue("familymember 11 (fdba46e69d2dfa3fe165eeb84325e90b0b29bf07) not found", 
		           testObject.getFamily().contains(
		                          new FingerprintImpl(DatatypeConverter.parseHexBinary("fdba46e69d2dfa3fe165eeb84325e90b0b29bf07"))));
		assertEquals(1073741824, testObject.getBandwidthAvg());
		assertEquals(1258291200, testObject.getBandwidthBurst());
		assertEquals(4887529, testObject.getBandwidthObserved());
		assertFalse(testObject.isDirv2Authority());
		assertFalse(testObject.isDirv2Exit());
		assertFalse(testObject.isDirv2Fast());
		assertFalse(testObject.isDirv2Guard());
		assertFalse(testObject.isDirv2Named());
		assertFalse(testObject.isDirv2Running());
		assertFalse(testObject.isDirv2Stable());
		assertFalse(testObject.isDirv2V2dir());
		assertFalse(testObject.isDirv2Valid());
	}
	/**
	 * Test method if it is possible to write a parsed Router to a file using own method.
	 * @throws TorException 
	 * @throws IOException 
	 */
	@Test
	public void testWriteRouterToFile() throws TorException, IOException
	{
		final RouterImpl testObject = new RouterImpl(descriptor);
		FileOutputStream fileOutputStream = new FileOutputStream("router.test");
		fileOutputStream.write(testObject.toByteArray());
		fileOutputStream.close();
		
		FileInputStream fileInputStream = new FileInputStream("router.test");
		DynByteBuffer buffer = new DynByteBuffer(fileInputStream);
		RouterImpl testObject2 = new RouterImpl(buffer);
		assertEquals(testObject, testObject2);
	}
	/**
	 * Test method if it is possible to write a parsed Router to a file using {@link ObjectOutputStream}.
	 * @throws TorException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testWriteRoutersToFile() throws TorException, IOException, ClassNotFoundException
	{
		Directory directory = new Directory(null, null, null);
		final Map<Fingerprint, RouterImpl> allrouters = directory.parseRouterDescriptors(descriptors);
		assertNotNull(allrouters);
		assertFalse(allrouters.isEmpty());
		assertEquals(4648, allrouters.size());

		FileOutputStream fileOutputStream = new FileOutputStream("routers.test");
		fileOutputStream.write(ByteUtils.intToBytes(allrouters.size()));
		for (RouterImpl router : allrouters.values())
		{
			fileOutputStream.write(router.toByteArray());
		}
		fileOutputStream.close();
		
		final Map<Fingerprint, RouterImpl> allrouters2 = new HashMap<Fingerprint, RouterImpl>();
		FileInputStream fileInputStream = new FileInputStream("routers.test");
		DynByteBuffer buffer = new DynByteBuffer(fileInputStream);
		int count = buffer.getNextInt();
		for (int i = 0; i < count; i++)
		{
			RouterImpl router = new RouterImpl(buffer);
			allrouters2.put(router.getFingerprint(), router);
		}
		assertEquals(allrouters, allrouters2);
	}

	/**
	 * Test method for
	 * {@link RouterImpl#cloneReliable()}
	 * .
	 */
	@Test(enabled = false)
	public void testCloneReliable()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link Directory#parseRouterDescriptors(String)}
	 * .
	 * @throws IOException when loading the example_server_descriptors from classpath didnt worked
	 */
	@Test
	public void testParseRouterDescriptors() throws IOException
	{
		Directory directory = new Directory(null, null, null);
		final Map<Fingerprint, RouterImpl> allrouters = directory.parseRouterDescriptors(descriptors);
		assertNotNull(allrouters);
		assertFalse(allrouters.isEmpty());
		assertEquals(4648, allrouters.size());
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getRefinedRankingIndex(float)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetRefinedRankingIndex()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#punishRanking()}
	 * .
	 */
	@Test(enabled = false)
	public void testPunishRanking()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#exitPolicyAccepts(java.net.InetAddress, int)}
	 * .
	 */
	@Test(enabled = false)
	public void testExitPolicyAccepts()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirServer()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirServer()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isExitNode()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsExitNode()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#toString()}
	 * .
	 */
	@Test(enabled = false)
	public void testToString()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#toLongString()}
	 * .
	 */
	@Test(enabled = false)
	public void testToLongString()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isValid()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsValid()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getDirAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetDirAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getOrAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOrAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getNickname()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetNickname()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getHostname()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetHostname()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getAddress()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetAddress()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getCountryCode()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetCountryCode()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getOrPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOrPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getSocksPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetSocksPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getDirPort()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetDirPort()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getBandwidthAvg()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthAvg()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getBandwidthBurst()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthBurst()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getBandwidthObserved()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetBandwidthObserved()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getPlatform()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetPlatform()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getPublished()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetPublished()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getFingerprint()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetFingerprint()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getV3Ident()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetV3Ident()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getUptime()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetUptime()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getOnionKey()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetOnionKey()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getSigningKey()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetSigningKey()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getContact()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetContact()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getFamily()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetFamily()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getValidUntil()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetValidUntil()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getLastUpdate()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetLastUpdate()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Authority()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Authority()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Exit()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Exit()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Fast()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Fast()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Guard()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Guard()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Named()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Named()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Stable()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Stable()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Running()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Running()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2Valid()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2Valid()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2V2dir()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2V2dir()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#isDirv2HSDir()}
	 * .
	 */
	@Test(enabled = false)
	public void testIsDirv2HSDir()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link RouterImpl#getRankingIndex()}
	 * .
	 */
	@Test(enabled = false)
	public void testGetRankingIndex()
	{
		fail("Not yet implemented");
	}
}
