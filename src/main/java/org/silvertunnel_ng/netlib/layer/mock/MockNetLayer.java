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

package org.silvertunnel_ng.netlib.layer.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerStatus;
import org.silvertunnel_ng.netlib.api.NetServerSocket;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.nameservice.mock.NopNetAddressNameService;

/**
 * Mock
 * 
 * @author hapke
 */
public class MockNetLayer implements NetLayer
{
	private final List<MockNetSession> sessionHistory = new ArrayList<MockNetSession>();

	/** parameter for variant 1 */
	private byte[] response;
	/** parameter for variant 1 */
	private boolean allowMultipleSessions;

	/** parameter for variant 2 */
	private List<byte[]> responses;

	/** parameter for variant 3 */
	private Map<NetAddress, byte[]> responsePerDestinationAddress;
	/** parameter for variant 3 */
	private boolean allowMultipleSessionsForOneAddress;
	/*
	 * if all data of response read: wait this time (milliseconds) before "end"
	 * is signaled; 0 = do not wait; -1 = wait endless
	 */
	private final long waitAtEndOfResponseBeforeClosingMs;

	/**
	 * Create the layer (variant 1).
	 * 
	 * @param response
	 *            this is the response that can later be read from the socket
	 * @param allowMultipleSessions
	 *            true=multiple session are allowed (all use the same response)
	 * @param waitAtEndOfResponseBeforeClosingMs
	 *            if all data of response read: wait this time (milliseconds)
	 *            before "end" is signaled; 0 = do not wait; -1 = wait endless
	 */
	public MockNetLayer(final byte[] response, boolean allowMultipleSessions,
			long waitAtEndOfResponseBeforeClosingMs)
	{
		this.response = response;
		this.allowMultipleSessions = allowMultipleSessions;
		this.waitAtEndOfResponseBeforeClosingMs = waitAtEndOfResponseBeforeClosingMs;
	}

	/**
	 * Create a layer (variant 2).
	 * 
	 * @param responses
	 *            each list element represents the response that can be read
	 *            from one socket; response.size()=numberOfAllowedSockets
	 * @param waitAtEndOfResponseBeforeClosingMs
	 *            if all data of response read: wait this time (milliseconds)
	 *            before "end" is signaled; 0 = do not wait; -1 = wait endless
	 */
	public MockNetLayer(final List<byte[]> responses,
			long waitAtEndOfResponseBeforeClosingMs)
	{
		this.responses = responses;
		this.waitAtEndOfResponseBeforeClosingMs = waitAtEndOfResponseBeforeClosingMs;
	}

	/**
	 * Create a layer (variant 3).
	 * 
	 * Different responses are possible for different destination addresses.
	 * 
	 * @param responsePerDestinationAddress
	 *            mapping: destination address->response
	 * @param allowMultipleSessionsForOneAddress
	 *            true=multiple session are allowed for one destination address
	 *            (all use the same response)
	 * @param waitAtEndOfResponseBeforeClosingMs
	 *            if all data of response read: wait this time (milliseconds)
	 *            before "end" is signaled; 0 = do not wait; -1 = wait endless
	 */
	public MockNetLayer(
			final Map<NetAddress, byte[]> responsePerDestinationAddress,
			boolean allowMultipleSessionsForOneAddress,
			long waitAtEndOfResponseBeforeClosingMs)
	{
		this.responsePerDestinationAddress = responsePerDestinationAddress;
		this.allowMultipleSessionsForOneAddress = allowMultipleSessionsForOneAddress;
		this.waitAtEndOfResponseBeforeClosingMs = waitAtEndOfResponseBeforeClosingMs;
	}

	/**
	 * To check after usage of the layer: this method provides the history of
	 * sessions created by this layer object.
	 * 
	 * @return history of sessions created by this layer object
	 */
	public List<MockNetSession> getSessionHistory()
	{
		return sessionHistory;
	}

	/**
	 * To check after usage of the layer: this method provides the history of
	 * the first session created by this layer object.
	 * 
	 * @return the first session; null if no session was created until now
	 */
	public MockNetSession getFirstSessionHistory()
	{
		if (sessionHistory.size() == 0)
		{
			return null;
		}
		else
		{
			return sessionHistory.get(0);
		}
	}

	/** @see NetLayer#createNetSocket(Map, NetAddress, NetAddress) */
	@Override
	public synchronized NetSocket createNetSocket(
			Map<String, Object> localProperties, NetAddress localAddress,
			NetAddress remoteAddress) throws IOException
	{
		MockNetSocket preparedHigherLayerSocket;
		if (response != null)
		{
			// variant 1
			preparedHigherLayerSocket = new MockNetSocket(response,
					waitAtEndOfResponseBeforeClosingMs);
			if (!allowMultipleSessions)
			{
				// cleanup: avoid multiple calls of this method
				response = null;
			}

		}
		else if (responses != null && responses.size() >= 1)
		{
			// variant 2
			preparedHigherLayerSocket = new MockNetSocket(responses.get(0),
					waitAtEndOfResponseBeforeClosingMs);
			// cleanup
			responses.remove(0);

		}
		else if (responsePerDestinationAddress != null
				&& responsePerDestinationAddress.size() >= 1)
		{
			// variant 3
			final byte[] response = responsePerDestinationAddress
					.get(remoteAddress);
			if (response == null)
			{
				throw new IOException(
						"connection could not be established to remoteAddress="
								+ remoteAddress);
			}
			if (!allowMultipleSessionsForOneAddress)
			{
				// cleanup
				responsePerDestinationAddress.remove(remoteAddress);
			}
			preparedHigherLayerSocket = new MockNetSocket(response,
					waitAtEndOfResponseBeforeClosingMs);

		}
		else
		{
			throw new IOException("no more Sockets allowed");
		}

		// create session and socket
		final MockNetSession session = new MockNetSession(localProperties,
				localAddress, remoteAddress, preparedHigherLayerSocket);
		sessionHistory.add(session);
		return session.createHigherLayerNetSocket();
	}

	/** @see NetLayer#createNetServerSocket(Map, NetAddress) */
	@Override
	public NetServerSocket createNetServerSocket(
			Map<String, Object> properties, NetAddress localListenAddress)
	{
		throw new UnsupportedOperationException();
	}

	/** @see NetLayer#getStatus() */
	@Override
	public NetLayerStatus getStatus()
	{
		return NetLayerStatus.READY;
	}

	/** @see NetLayer#waitUntilReady() */
	@Override
	public void waitUntilReady()
	{
		// nothing to do
	}

	/** @see NetLayer#clear() */
	@Override
	public void clear() throws IOException
	{
		// nothing to do
	}

	/** @see NetLayer#getNetAddressNameService */
	@Override
	public NetAddressNameService getNetAddressNameService()
	{
		return NopNetAddressNameService.getInstance();
	}
}
