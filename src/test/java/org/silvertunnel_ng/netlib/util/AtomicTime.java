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

package org.silvertunnel_ng.netlib.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads the atomic time from a atomic time server.
 * 
 * Original is coming from : http://www.rgagnon.com/javadetails/java-0589.html
 * 
 * @author Tobias Boese
 * 
 */
public class AtomicTime
{
	/** class logger. */
	private static final Logger LOG = LoggerFactory.getLogger(AtomicTime.class);
	
	private static final String ATOMIC_TIME_SERVER = "time-a.nist.gov";

	public static final long getTimeDiffinMillis() throws IOException
	{
		// Send request
		final DatagramSocket socket = new DatagramSocket();
		final InetAddress address = InetAddress.getByName(ATOMIC_TIME_SERVER);
		final byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
				123);

		// Set the transmit timestamp *just* before sending the packet
		// ToDo: Does this actually improve performance or not?
		NtpMessage.encodeTimestamp(packet.getData(), 40,
				(System.currentTimeMillis() / 1000.0) + 2208988800.0);

		socket.send(packet);

		// Get response
		LOG.info("NTP request sent, waiting for response...\n");
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);

		// Immediately record the incoming timestamp
		final double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

		// Process response
		final NtpMessage msg = new NtpMessage(packet.getData());

		// Corrected, according to RFC2030 errata
		final double roundTripDelay = (destinationTimestamp - msg.getOriginateTimestamp())
				- (msg.getTransmitTimestamp() - msg.getReceiveTimestamp());

		final double localClockOffset = ((msg.getReceiveTimestamp() - msg.getOriginateTimestamp()) 
				 					   + (msg.getTransmitTimestamp() - destinationTimestamp)) / 2;

		// Display response
		LOG.info("NTP server: " + ATOMIC_TIME_SERVER);
		LOG.info(msg.toString());

		LOG.info("Dest. timestamp:     " + NtpMessage.timestampToString(destinationTimestamp));

		LOG.info("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");

		LOG.info("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset * 1000)	+ " ms");

		socket.close();
		return (long) (localClockOffset * 1000);
	}
}
