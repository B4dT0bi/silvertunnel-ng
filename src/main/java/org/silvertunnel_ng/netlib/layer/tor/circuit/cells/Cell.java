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
package org.silvertunnel_ng.netlib.layer.tor.circuit.cells;

import java.io.IOException;
import java.io.InputStream;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * most general form of the cells in the Tor protocol. Should not be used on its
 * own.
 * 
 * @author Lexi Pimenidis
 * @author Vinh Pham
 */
public class Cell
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Cell.class);

	/** Padding cell. */
	public static final int CELL_PADDING = 0;
	/** Create a circuit. */
	public static final int CELL_CREATE = 1;
	/** Acknowledge create. */
	public static final int CELL_CREATED = 2;
	/** End-to-end data. */
	public static final int CELL_RELAY = 3;
	/** Stop using a circuit. */
	public static final int CELL_DESTROY = 4;
	/** Create a circuit, no PK. */
	public static final int CELL_CREATE_FAST = 5;
	/** Circuit created, no PK. */
	public static final int CELL_CREATED_FAST = 6;
	/** End-to-end data. limited. */
	public static final int CELL_RELAY_EARLY = 9;
	static final int CELL_TOTAL_SIZE = 512;
	static final int CELL_CIRCID_SIZE = 2;
	static final int CELL_COMMAND_SIZE = 1;
	static final int CELL_PAYLOAD_SIZE = 509;
	static final int CELL_CIRCID_POS = 0;
	static final int CELL_COMMAND_POS = CELL_CIRCID_POS + CELL_CIRCID_SIZE;
	static final int CELL_PAYLOAD_POS = CELL_COMMAND_POS + CELL_COMMAND_SIZE;

	private static final String[] TYPE_TO_STRING = { "padding", "create",
			"created", "relay", "destroy", "create-fast", "created-fast", "", "", "relay-early" };

	private int circuitId;
	private byte command;
	protected byte[] payload;
	/** Circuit for sending data or circuit that needs to be created. */
	protected Circuit outCircuit;

	/**
	 * initialize cell for sending.
	 * 
	 * @param outCircuit
	 *            a circuit that is used to send some data on, or that needs ot
	 *            be created
	 * @param command
	 *            the purpose of this cell
	 */
	Cell(final Circuit outCircuit, final int command)
	{
		// payload is all zeros because java does this for us.
		this.payload = new byte[Cell.CELL_PAYLOAD_SIZE];

		this.circuitId = outCircuit.getId();
		this.command = Encoding.intToNByteArray(command, 1)[0];
		this.outCircuit = outCircuit;
	}

	/**
	 * initialize cell from data<br>
	 * Attention: this.outCircuit is not set!
	 * 
	 * @param data
	 *            a raw cell. 512 bytes long.
	 */
	Cell(final byte[] data) throws NullPointerException
	{
		initFromData(data);
	}

	/**
	 * initialize cell from stream Attention: this.outCircuit is not set!
	 * 
	 * @param in
	 *            the input stream from a TLS-line to read the data from
	 */
	public Cell(final InputStream in) throws IOException
	{
		if (in == null)
		{
			throw new IOException("null as input stream given");
		}
		final byte[] data = new byte[Cell.CELL_TOTAL_SIZE];
		int filled = 0;
		while (filled < data.length)
		{
			final int n = in.read(data, filled, data.length - filled);
			if (n < 0)
			{
				throw new IOException("Cell.<init>: reached EOF");
			}
			filled += n;
			// TODO: check if this is OK:
			Thread.yield();
		}
		initFromData(data);
	}

	/**
	 * this function decodes an incoming cell from the raw bytes into the given
	 * data structures of the Cell-Class.
	 * 
	 * @param data
	 *            a raw cell. 512 bytes long.
	 */
	private void initFromData(final byte[] data) throws NullPointerException
	{
		if (data == null)
		{
			throw new NullPointerException("no data given");
		}
		this.payload = new byte[Cell.CELL_PAYLOAD_SIZE];

		this.circuitId = Encoding.byteArrayToInt(data, Cell.CELL_CIRCID_POS, Cell.CELL_CIRCID_SIZE);
		this.command = data[Cell.CELL_COMMAND_POS];
		System.arraycopy(data, Cell.CELL_PAYLOAD_POS, this.payload, 0, payload.length);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Cell.initFromData: " + toString("Received "));
		}
	}

	/**
	 * concat all data to a single byte-array. This function is used to finally
	 * transmit the cell over a line.
	 */
	public byte[] toByteArray()
	{
		final byte[] buff = new byte[Cell.CELL_TOTAL_SIZE];

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Cell.toByteArray(): " + toString("Sending "));
		}

		System.arraycopy(
				Encoding.intToNByteArray(this.circuitId, Cell.CELL_CIRCID_SIZE),
				0, buff, Cell.CELL_CIRCID_POS, Cell.CELL_CIRCID_SIZE);

		buff[Cell.CELL_COMMAND_POS] = this.command;
		System.arraycopy(this.payload, 0, buff, CELL_PAYLOAD_POS, this.payload.length);

		return buff;
	}

	/** wrapper for toString(String description). */
	@Override
	public String toString()
	{
		return toString("");
	}

	/** used for debugging output. */
	private String toString(final String description)
	{
		return description + "cell for circuit " + getCircuitId()
				+ " with command " + type() + ". Payload:\n"
				+ Encoding.toHexString(payload, 100);
	}

	public static String type(final int t)
	{
		if (t >= 0 && t < TYPE_TO_STRING.length)
		{
			return TYPE_TO_STRING[t];
		}
		else
		{
			return "[" + t + "]";
		}
	}

	public String type()
	{
		return type(command);
	}

	// /////////////////////////////////////////////////////
	// getter and setters
	// /////////////////////////////////////////////////////
	/** @return is this a padding cell? */
	public final boolean isTypePadding()
	{
		return this.command == CELL_PADDING;
	}

	/** @return is this a created cell? */
	public final boolean isTypeCreated()
	{
		return this.command == CELL_CREATED;
	}

	/** @return is this a relay cell? */
	public final boolean isTypeRelay()
	{
		return this.command == CELL_RELAY;
	}

	/** @return is this a destroy cell? */
	public final boolean isTypeDestroy()
	{
		return this.command == CELL_DESTROY;
	}

	public int getCircuitId()
	{
		return circuitId;
	}

	public byte getCommand()
	{
		return command;
	}

	public void setCommand(final byte command)
	{
		this.command = command;
	}

	public byte[] getPayload()
	{
		return payload;
	}

	public void setPayload(final byte[] payload)
	{
		this.payload = payload;
	}

	public Circuit getOutCircuit()
	{
		return outCircuit;
	}

	public void setOutCircuit(final Circuit outCircuit)
	{
		this.outCircuit = outCircuit;
	}
}
