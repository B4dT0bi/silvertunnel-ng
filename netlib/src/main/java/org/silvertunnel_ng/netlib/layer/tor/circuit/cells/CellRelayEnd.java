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

import org.silvertunnel_ng.netlib.layer.tor.circuit.Stream;

/**
 * sends an END cell, needed to close a tcp-stream.
 * 
 * @author Lexi Pimenidis
 */
public class CellRelayEnd extends CellRelay
{
	/**
	 * constructor to build a ENDCELL.
	 * 
	 * @param s
	 *            the stream that shall be closed
	 * @param reason
	 *            a reason
	 */
	public CellRelayEnd(final Stream s, final byte reason)
	{
		// initialize a new Relay-cell
		super(s, CellRelay.RELAY_END);

		// set length
		setLength(1);
		data[0] = reason;
	}
}
