/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
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

package org.silvertunnel_ng.netlib.layer.tor.circuit.cells;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Stream;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the general form of a RELAY cell in the Tor Protocol. This class also calls
 * the crypto- functions in Node.java to decode an onion, if encrypted data is
 * received.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Boese
 */
public class CellRelay extends Cell
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(CellRelay.class);

	public static final int RELAY_BEGIN = 1;
	public static final int RELAY_DATA = 2;
	public static final int RELAY_END = 3;
	public static final int RELAY_CONNECTED = 4;
	public static final int RELAY_SENDME = 5;
	public static final int RELAY_EXTEND = 6;
	public static final int RELAY_EXTENDED = 7;
	public static final int RELAY_TRUNCATE = 8;
	public static final int RELAY_TRUNCATED = 9;
	public static final int RELAY_DROP = 10;
	public static final int RELAY_RESOLVE = 11;
	public static final int RELAY_RESOLVED = 12;
	public static final int RELAY_BEGIN_DIR = 13;
	public static final int RELAY_ESTABLISH_INTRO = 32;
	public static final int RELAY_ESTABLISH_RENDEZVOUS = 33;
	public static final int RELAY_INTRODUCE1 = 34;
	public static final int RELAY_INTRODUCE2 = 35;
	public static final int RELAY_RENDEZVOUS1 = 36;
	public static final int RELAY_RENDEZVOUS2 = 37;
	public static final int RELAY_INTRO_ESTABLISHED = 38;
	public static final int RELAY_RENDEZVOUS_ESTABLISHED = 39;
	public static final int RELAY_COMMAND_INTRODUCE_ACK = 40;

	public static final int RELAY_COMMAND_SIZE = 1;
	public static final int RELAY_RECOGNIZED_SIZE = 2;
	public static final int RELAY_STREAMID_SIZE = 2;
	public static final int RELAY_DIGEST_SIZE = 4;
	public static final int RELAY_LENGTH_SIZE = 2;
	public static final int RELAY_DATA_SIZE = 498;
	public static final int RELAY_TOTAL_SIZE = CELL_PAYLOAD_SIZE;
	public static final int RELAY_COMMAND_POS = 0;
	public static final int RELAY_RECOGNIZED_POS = RELAY_COMMAND_POS + RELAY_COMMAND_SIZE;
	public static final int RELAY_STREAMID_POS = RELAY_RECOGNIZED_POS + RELAY_RECOGNIZED_SIZE;
	public static final int RELAY_DIGEST_POS = RELAY_STREAMID_POS + RELAY_STREAMID_SIZE;
	public static final int RELAY_LENGTH_POS = RELAY_DIGEST_POS + RELAY_DIGEST_SIZE;
	public static final int RELAY_DATA_POS = RELAY_LENGTH_POS + RELAY_LENGTH_SIZE;

	/** used for a nicer debugging output. */
	private static final String[] COMMAND_TO_STRING = { "zero", "begin",
			"data", "end", "connected", "sendme", "extend", "extended",
			"truncate", "truncated", "drop", "resolv", "resolved", 
			"RELAY_BEGIN_DIR",	"[14]", "[15]", "[16]", "[17]", "[18]", "[19]", "[20]",
			"[21]", "[22]", "[23]", "[24]", "[25]", "[26]", "[27]", "[28]", "[29]", "[30]",
			"[31]", 
			"RELAY_COMMAND_ESTABLISH_INTRO", 
			"RELAY_COMMAND_ESTABLISH_RENDEZVOUS", 
			"RELAY_COMMAND_INTRODUCE1", 
			"RELAY_COMMAND_INTRODUCE2", 
			"RELAY_COMMAND_RENDEZVOUS1", 
			"RELAY_COMMAND_RENDEZVOUS2", 
			"RELAY_COMMAND_INTRO_ESTABLISHED", 
			"RELAY_COMMAND_RENDEZVOUS_ESTABLISHED", 
			"RELAY_COMMAND_INTRODUCE_ACK"};

	/** used for a nicer debugging output. */
	private static final String[] REASON_TO_STRING = { "none", "misc",
			"resolve failed", "connect refused", "exit policy", "destroy",
			"done", "timeout", "(unallocated - see spec)", "hibernating",
			"internal", "resource limit", "connection reset",
			"tor protocol violation" };

	/** used for a nicer debugging output. */
	private static final String[] TRUNCATED_REASON_TO_STRING = { "none",
			"protocol", "internal", "requested", "hibernating",
			"resourcelimit", "connectfailed", "or_identity", "or_conn_closed",
			"finished", "timeout", "destroyed", "nosuchservice" };

	private byte relayCommand;
	/** 16 bit unsigned integer. */
	private int streamId;
	private byte[] digest = new byte[4];
	/** 16 bit unsigned integer. */
	private int length;
	protected byte[] data = new byte[498];
	/**
	 * set to a value from 0 to outCircuit.routeEstablished-1 to address a
	 * special router in the chain, default is the last one.
	 */
	private int addressedRouterInCircuit = -1;

	/**
	 * constructor. used for EXTEND-cells and SENDME-cells
	 */
	CellRelay(final Circuit c, final int relayCommand)
	{
		super(c, Cell.CELL_RELAY);
		this.relayCommand = (byte) relayCommand;
	}
	/**
	 * constructor. used for EXTEND-cells and SENDME-cells
	 */
	CellRelay(final Circuit c, final int cellType, final int relayCommand)
	{
		super(c, cellType);
		this.relayCommand = (byte) relayCommand;
	}

	/**
	 * initialize cell. used by RELAY_BEGIN-cells
	 */
	CellRelay(final Stream s, final int relayCommand)
	{
		super(s.getCircuit(), Cell.CELL_RELAY);
		this.streamId = s.getId();
		this.relayCommand = (byte) relayCommand;
	}

	/**
	 * initialize cell. used by RELAY_BEGIN-cells
	 */
	CellRelay(final Stream s, final int cellType, final int relayCommand)
	{
		super(s.getCircuit(), cellType);
		this.streamId = s.getId();
		this.relayCommand = (byte) relayCommand;
	}

	/**
	 * initialize from received data.
	 */
	CellRelay(final byte[] data) throws TorException
	{
		super(data);
		initFromData();
	}

	/**
	 * initialize from main Cell-type.
	 */
	public CellRelay(final Circuit circ, final Cell cell) throws TorException
	{
		super(cell.toByteArray()); // TODO: inefficient at max! - but currently
									// working
		this.outCircuit = circ;
		initFromData();
	}

	/**
	 * init cell from stream.
	 */
	CellRelay(final InputStream in) throws IOException, TorException
	{
		super(in);
		initFromData();
	}

	/**
	 * decrypts an onion and checks digests and stuff. input is taken from the
	 * parent class' payload.
	 */
	void initFromData() throws TorException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("CellRelay.initFromData() for "
					+ outCircuit.getRouteEstablished() + " layers");
		}
		// decrypt forwards, take keys from route
		int encryptingRouter;
		boolean digestVerified = false;
		if (outCircuit.getRouteEstablished() == 0)
		{
			LOG.warn("CellRelay.initFromData() for zero layers on "
					+ outCircuit.toString());
		}
		for (encryptingRouter = 0; encryptingRouter <= outCircuit
				.getRouteEstablished(); ++encryptingRouter)
		{
			// check if no decryption has lead to a recognized cell
			if (encryptingRouter == outCircuit.getRouteEstablished())
			{
				throw new TorException(
						"relay cell not recognized, possibly due to decryption errors? on "
								+ outCircuit.toString());
			}
			// decrypt payload
			outCircuit.getRouteNodes()[encryptingRouter].symDecrypt(payload);
			if (LOG.isDebugEnabled())
			{
				LOG.info("CellRelay.initFromDate with encryptingRouter="
						+ encryptingRouter + " has decrypted payload="
						+ ByteArrayUtil.showAsStringDetails(payload));
			}

			// if recognized and digest is correct, then stop decrypting
			if ((payload[CellRelay.RELAY_RECOGNIZED_POS] == 0)
					&& (payload[CellRelay.RELAY_RECOGNIZED_POS + 1] == 0))
			{
				// check digest.

				// save digest
				System.arraycopy(payload, CellRelay.RELAY_DIGEST_POS, digest,
						0, CellRelay.RELAY_DIGEST_SIZE);
				// set to ZERO
				payload[CellRelay.RELAY_DIGEST_POS] = 0;
				payload[CellRelay.RELAY_DIGEST_POS + 1] = 0;
				payload[CellRelay.RELAY_DIGEST_POS + 2] = 0;
				payload[CellRelay.RELAY_DIGEST_POS + 3] = 0;
				// calculate digest
				final byte[] digestCalc = outCircuit.getRouteNodes()[encryptingRouter]
						.calcBackwardDigest(payload);
				// restore digest
				System.arraycopy(digest, 0, payload,
						CellRelay.RELAY_DIGEST_POS, CellRelay.RELAY_DIGEST_SIZE);
				// check digest
				if ((digest[0] == digestCalc[0])
						&& (digest[1] == digestCalc[1])
						&& (digest[2] == digestCalc[2])
						&& (digest[3] == digestCalc[3]))
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("CellRelay.initFromData(): backward digest from "
								+ outCircuit.getRouteNodes()[encryptingRouter]
										.getRouter().getNickname() + " is OK");
					}
					digestVerified = true;
					break;
				}
				else
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("didn't verified digest="
								+ Encoding.toHexString(digest)
								+ ", digestCalc="
								+ Encoding.toHexString(digestCalc));
					}
				}
			}
		}
		// check if digest verified
		if (!digestVerified)
		{
			LOG.warn("CellRelay.initFromData(): Received "
					+ Encoding.toHexString(digest)
					+ " as backward digest but couldn't verify");
			throw new TorException("wrong digest");
		}

		// copy data from payload
		relayCommand = payload[CellRelay.RELAY_COMMAND_POS];
		streamId = Encoding.byteArrayToInt(payload,
				CellRelay.RELAY_STREAMID_POS, CellRelay.RELAY_STREAMID_SIZE);
		length = Encoding.byteArrayToInt(payload, CellRelay.RELAY_LENGTH_POS,
				CellRelay.RELAY_LENGTH_SIZE);
		System.arraycopy(payload, CellRelay.RELAY_DATA_POS, data, 0,
				CellRelay.RELAY_DATA_SIZE);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("CellRelay.initFromData(): " + toString());
		}
	}

	/**
	 * set to a value from 0 to outCircuit.routeEstablished-1 to address a
	 * special router in the chain, default is the last one.
	 */
	boolean setAddressedRouter(int router)
	{
		if ((router > -1) && (router < outCircuit.getRouteEstablished()))
		{
			addressedRouterInCircuit = router;
			return true;
		}
		return false;
	}

	/**
	 * prepares the meta-data, such that the cell can be transmitted. encrypts
	 * an onion.
	 * 
	 * @return the data ready for sending
	 */
	@Override
	public
	byte[] toByteArray()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("CellRelay.toByteArray() for " + outCircuit.getRouteEstablished() + " layers");
		}
		// put everything in payload
		payload[CellRelay.RELAY_COMMAND_POS] = relayCommand;
		System.arraycopy(Encoding.intToNByteArray(streamId, CellRelay.RELAY_STREAMID_SIZE), 
		                 0, 
		                 payload,
		                 CellRelay.RELAY_STREAMID_POS, 
		                 CellRelay.RELAY_STREAMID_SIZE);
		System.arraycopy(Encoding.intToNByteArray(length, CellRelay.RELAY_LENGTH_SIZE),
		                 0, 
		                 payload, 
		                 CellRelay.RELAY_LENGTH_POS,
		                 CellRelay.RELAY_LENGTH_SIZE);
		System.arraycopy(data, 0, payload, CellRelay.RELAY_DATA_POS, CellRelay.RELAY_DATA_SIZE);
		// calculate digest and insert it
		int i0 = outCircuit.getRouteEstablished() - 1;
		if (addressedRouterInCircuit >= 0)
		{
			i0 = addressedRouterInCircuit;
		}
		digest = outCircuit.getRouteNodes()[i0].calcForwardDigest(payload);
		System.arraycopy(digest, 0, payload, CellRelay.RELAY_DIGEST_POS, CellRelay.RELAY_DIGEST_SIZE);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("CellRelay.toByteArray(): " + toString());
		}
		// encrypt backwards, take keys from route
		for (int i = i0; i >= 0; --i)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("CellRelay.toByteArray with encryptingRouter=" + i
						+ " has unencrypted payload="
						+ ByteArrayUtil.showAsStringDetails(payload));
			}
			outCircuit.getRouteNodes()[i].symEncrypt(payload);
		}
		// create the byte array to be send over TLS
		return super.toByteArray();
	}

	/**
	 * for debugging and stuff.
	 */
	private static String getReasonForClosing(final int reason)
	{
		if ((reason < 0) || (reason >= REASON_TO_STRING.length))
		{
			return "[" + reason + "]";
		}
		return REASON_TO_STRING[reason];
	}

	public String getReasonForClosing()
	{
		return getReasonForClosing(data[0]);
	}

	/**
	 * for debugging and stuff.
	 */
	private static String getReasonForTruncated(final int reason)
	{
		if ((reason < 0) || (reason >= TRUNCATED_REASON_TO_STRING.length))
		{
			return "[" + reason + "]";
		}
		return TRUNCATED_REASON_TO_STRING[reason];
	}

	public String getReasonForTruncated()
	{
		return getReasonForTruncated(data[0]);
	}

	/**
	 * @return the current relay command as String.
	 */
	public String getRelayCommandAsString()
	{
		return getRelayCommandAsString(relayCommand);
	}

	/**
	 * Gets the String representation of the relay command.
	 * 
	 * @param cmd
	 *            the relay command as small int
	 * @return a String-representation of the command
	 */
	public static String getRelayCommandAsString(final int cmd)
	{
		if ((cmd < COMMAND_TO_STRING.length) && (cmd >= 0))
		{
			return COMMAND_TO_STRING[cmd];
		}
		else
		{
			return "[" + cmd + "]";
		}
	}

	/**
	 * used for debugging.
	 */
	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();

		// main header
		sb.append("Relay cell for circuit " + getCircuitId() + "/stream "
				+ streamId + " with command " + getRelayCommandAsString()
				+ ".\n");
		// is the cell not recognized?
		if (Encoding.byteArrayToInt(payload, 1, 2) != 0)
		{
			sb.append("  Recognized    " + Encoding.toHexString(payload, 100, 1, 2) + "\n");
			sb.append("  DigestID      " + Encoding.toHexString(digest) + "\n");
		}
		// display connection
		if (isTypeBegin())
		{
			final byte[] host = new byte[length - 1];
			System.arraycopy(data, 0, host, 0, length - 1);
			sb.append("  Connecting to: " + new String(host) + "\n");

		}
		else if (isTypeEnd())
		{
			// display reason for end, if given
			sb.append("  End reason: " + getReasonForClosing() + "\n");

		}
		else if (isTypeConnected() && length >= 4)
		{
			// display connection
			final byte[] ip = new byte[4];
			System.arraycopy(data, 0, ip, 0, 4);
			try
			{
				sb.append("  Connected to: " + InetAddress.getByAddress(ip).toString() + "\n");
			}
			catch (final UnknownHostException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("got UnknownHostException : ip = "
						+ Arrays.toString(ip) 
						+ " exception : " + e + " "
						+ e.getMessage(), e);
				}
			}

		}
		else if ((length > 0) && (relayCommand != 6) && (relayCommand != 7))
		{
			// display data field, if there is data AND data is not encrypted
			sb.append("  Data (" + length + " bytes)\n"	+ Encoding.toHexString(data, 100, 0, length) + "\n");
		}

		return sb.toString();
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public boolean isTypeBegin()
	{
		return relayCommand == RELAY_BEGIN;
	}

	public boolean isTypeData()
	{
		return relayCommand == RELAY_DATA;
	}

	public boolean isTypeEnd()
	{
		return relayCommand == RELAY_END;
	}

	boolean isTypeConnected()
	{
		return relayCommand == RELAY_CONNECTED;
	}

	/**
	 * @return is this {@link CellRelay} a RELAY_SENDME cell?
	 */
	public final boolean isTypeSendme()
	{
		return relayCommand == RELAY_SENDME;
	}

	boolean isTypeExtend()
	{
		return relayCommand == RELAY_EXTEND;
	}

	boolean isTypeExtended()
	{
		return relayCommand == RELAY_EXTENDED;
	}

	boolean isTypeTruncate()
	{
		return relayCommand == RELAY_TRUNCATE;
	}

	public boolean isTypeTruncated()
	{
		return relayCommand == RELAY_TRUNCATED;
	}

	boolean isTypeDrop()
	{
		return relayCommand == RELAY_DROP;
	}

	boolean isTypeResolve()
	{
		return relayCommand == RELAY_RESOLVED;
	}

	boolean isTypeResolved()
	{
		return relayCommand == RELAY_RESOLVED;
	}

	boolean isTypeEstablishedRendezvous()
	{
		return relayCommand == RELAY_RENDEZVOUS_ESTABLISHED;
	}

	boolean isTypeIntroduceACK()
	{
		return relayCommand == RELAY_COMMAND_INTRODUCE_ACK;
	}

	boolean isTypeRendezvous2()
	{
		return relayCommand == RELAY_RENDEZVOUS2;
	}

	public boolean isTypeIntroduce2()
	{
		return relayCommand == RELAY_INTRODUCE2;
	}

	public byte getRelayCommand()
	{
		return relayCommand;
	}

	public void setRelayCommand(byte relayCommand)
	{
		this.relayCommand = relayCommand;
	}

	public int getStreamId()
	{
		return streamId;
	}

	public void setStreamId(int streamId)
	{
		this.streamId = streamId;
	}

	public byte[] getDigest()
	{
		return digest;
	}

	public void setDigest(byte[] digest)
	{
		this.digest = digest;
	}
	/**
	 * @return Get length of payload.
	 */
	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}
}
