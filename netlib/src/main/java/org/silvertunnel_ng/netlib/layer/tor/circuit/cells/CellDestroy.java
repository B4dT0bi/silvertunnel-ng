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

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;

/**
 * used to create a DESTROY cell.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Boese
 */
public class CellDestroy extends Cell
{
	/** used for a nicer debugging output. */
	private static final String[] REASON_TO_STRING = { "none", "end-circ tor-protocol",
			"internal", "requested", "hibernating", "resourcelimit",
			"connectfailed", "or_identity", "or_conn_closed", "finished",
			"timeout", "destroyed", "nosuchservice", "tor protocol violation"};

	public static final byte REASON_NONE = 0;
	public static final byte REASON_END_CIRC_TOR_PROTOCOL = 1;
	public static final byte REASON_INTERNAL = 2;
	public static final byte REASON_REQUESTED = 3;
	public static final byte REASON_HIBERNATING = 4;
	public static final byte REASON_RESOURCELIMIT = 5;
	public static final byte REASON_CONNECTFAILED = 6;
	public static final byte REASON_OR_IDENTITY = 7;
	public static final byte REASON_OR_CONN_CLOSED = 8;
	public static final byte REASON_FINISHED = 9;
	public static final byte REASON_TIMEOUT = 10;
	public static final byte REASON_DESTROYED = 11;
	public static final byte REASON_NO_SUCH_SERVICE = 12;
	public static final byte REASON_TOR_PROTOCOL = 13;
	/**
	 * creates a DESTROY-CELL.
	 * 
	 * @param c
	 *            the circuit that is to be torn down
	 */
	public CellDestroy(final Circuit c)
	{
		super(c, Cell.CELL_DESTROY);
	}

	/**
	 * Convert the ReasonId to a string. (used for debugging)
	 * @param reason the reason id
	 * @return a human readable interpretation of the reason
	 */
	public static String getReason(final int reason)
	{
		if (reason < 0 || reason >= REASON_TO_STRING.length)
		{
			return "[" + reason + "]";
		}
		return REASON_TO_STRING[reason];
	}

	/**
	 * @return the reason from the payload as string
	 */
	public String getReason()
	{
		return getReason(payload[0]);
	}

}
