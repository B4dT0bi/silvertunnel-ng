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

package org.silvertunnel_ng.netlib.layer.redirect;

import static org.testng.AssertJUnit.fail;

import java.io.IOException;

import org.silvertunnel_ng.netlib.api.ByteArrayTestUtil;
import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.TestUtil;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.mock.MockNetLayer;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test SwitchingNetLayer.
 * 
 * In detail: switch from one lower layer mock to another lower layer mock.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class SwitchingLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SwitchingLocalTest.class);

	private byte[] USER_DATA_REQUEST;

	// 1st lower layer/1st request
	private byte[] USER_DATA_RESPONSE1;
	private final NetAddress remoteNetAddressOfRequest1 = new TcpipNetAddress(
			"host1:1001");

	// 2nd lower layer/2nd request
	private byte[] USER_DATA_RESPONSE2;
	private final NetAddress remoteNetAddressOfRequest2 = new TcpipNetAddress(
			"host1:1001");

	private SwitchingNetLayer switchingNetLayer;
	private MockNetLayer lowerNetLayer1;
	private MockNetLayer lowerNetLayer2;

	public SwitchingLocalTest()
	{
		try
		{
			USER_DATA_REQUEST = ByteArrayUtil.getByteArray(
					"Das ist mein Request", 5000, "\u00e0.");
			USER_DATA_RESPONSE1 = ByteArrayUtil.getByteArray(
					"Hier ist\n\nmeine erste Antwort\n fuer heute", 3001,
					"\u00e0.");
			USER_DATA_RESPONSE2 = ByteArrayUtil.getByteArray(
					"Hier ist\n\nmeine zweite Antwort\n fuer heute", 3002,
					"\u00e0.");

		}
		catch (final Exception e)
		{
			LOG.error("unexpected during construction", e);
		}
	}

	@BeforeMethod
	public void setUp() throws Exception
	{
		// initialize lower layers
		final long WAIT_ENDLESS = -1;
		lowerNetLayer1 = new MockNetLayer(USER_DATA_RESPONSE1, false, WAIT_ENDLESS);
		lowerNetLayer2 = new MockNetLayer(USER_DATA_RESPONSE2, false, WAIT_ENDLESS);

		// initialize switchingNetLayer with 1st lower layer
		switchingNetLayer = new SwitchingNetLayer(lowerNetLayer1);
	}

	/**
	 * Create and test a connection (expected to be with 1st lower layer). Then
	 * switch the lower layer. Then create and test a connection (expected to be
	 * with 2nd lower layer).
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 2000)
	public void testSocketSwitchBetweenTwoConnections() throws Exception
	{
		// create and test a connection (expected to be with 1st lower layer)
		NetSocket netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest1);
		requestResponseHelp(netSocket, remoteNetAddressOfRequest1,
				USER_DATA_REQUEST, USER_DATA_RESPONSE1, lowerNetLayer1);

		// switch the lower layer
		switchingNetLayer.setLowerNetLayer(lowerNetLayer2, true);

		// create and test a connection (expected to be with 2nd lower layer)
		netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest2);
		requestResponseHelp(netSocket, remoteNetAddressOfRequest2,
				USER_DATA_REQUEST, USER_DATA_RESPONSE2, lowerNetLayer2);
	}

	/**
	 * Create 1st connection (expected to be with 1st lower layer). Then switch
	 * the lower layer with closeAllOpenConnectionsImmediately=true Then use and
	 * test 1st connection (expected to fail) Then create and test a connection
	 * (expected to be with 2nd lower layer).
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 2000)
	public void testSocketSwitchDuringConnection_with_closeAllOpenConnectionsImmediately()
			throws Exception
	{
		// create 1st connection (expected to be with 1st lower layer)
		NetSocket netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest1);

		// switch the lower layer
		switchingNetLayer.setLowerNetLayer(lowerNetLayer2, true);

		// use and test 1st connection (expected to fail)
		try
		{
			requestResponseHelp(netSocket, remoteNetAddressOfRequest1,
					USER_DATA_REQUEST, USER_DATA_RESPONSE1, lowerNetLayer1);
			fail("expected IOException after switch not thrown");
		}
		catch (final IOException e)
		{
			// expected because: netSocket should be closed after switch with
			// closeAllOpenConnectionsImmediately=true
		}

		// create and test a connection (expected to be with 2nd lower layer)
		netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest2);
		requestResponseHelp(netSocket, remoteNetAddressOfRequest2,
				USER_DATA_REQUEST, USER_DATA_RESPONSE2, lowerNetLayer2);
	}

	/**
	 * Create 1st connection (expected to be with 1st lower layer). Then switch
	 * the lower layer with closeAllOpenConnectionsImmediately=false Then use
	 * and test 1st connection (expected to be with 1st lower layer) Then create
	 * and test a connection (expected to be with 2nd lower layer).
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = 2000)
	public void testSocketSwitchDuringConnection_without_closeAllOpenConnectionsImmediately()
			throws Exception
	{
		// create 1st connection (expected to be with 1st lower layer)
		NetSocket netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest1);

		// switch the lower layer
		switchingNetLayer.setLowerNetLayer(lowerNetLayer2, false);

		// use and test 1st connection (expected to be with 1st lower layer)
		requestResponseHelp(netSocket, remoteNetAddressOfRequest1,
				USER_DATA_REQUEST, USER_DATA_RESPONSE1, lowerNetLayer1);

		// create and test a connection (expected to be with 2nd lower layer)
		netSocket = switchingNetLayer.createNetSocket(null, null,
				remoteNetAddressOfRequest2);
		requestResponseHelp(netSocket, remoteNetAddressOfRequest2,
				USER_DATA_REQUEST, USER_DATA_RESPONSE2, lowerNetLayer2);
	}

	/**
	 * Execute a request and check the response.
	 * 
	 * @param netLayerOfRequest
	 * @param remoteNetAddressOfRequest
	 * @param userDataRequest
	 * @param expectedUserDataResponse
	 * @param expectedMockNetLayer
	 * @throws Exception
	 */
	protected void requestResponseHelp(NetSocket netSocket,
			NetAddress remoteNetAddressOfRequest, byte[] userDataRequest,
			byte[] expectedUserDataResponse, MockNetLayer expectedMockNetLayer)
			throws Exception
	{
		// send user data to remote side
		netSocket.getOutputStream().write(userDataRequest);
		netSocket.getOutputStream().flush();

		// receive and check user data from remote side
		ByteArrayTestUtil.assertByteArrayFromInputStream(null,
				"wrong user data response", expectedUserDataResponse,
				netSocket.getInputStream());

		// check user data received by the remote side (mock)
		TestUtil.waitUntilMinimumNumberOfReceivedBytes(
				expectedMockNetLayer.getFirstSessionHistory(),
				userDataRequest.length);
		netSocket.close();
		final NetAddress expectedNetAddress = remoteNetAddressOfRequest;
		TestUtil.assertMockNetLayerSavedData("wrong data received by mock",
				expectedMockNetLayer.getFirstSessionHistory(), userDataRequest,
				expectedNetAddress);
	}
}
