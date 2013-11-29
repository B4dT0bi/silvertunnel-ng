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
 * this cell is used to make anonymous DNS-lookups
 * 
 * @author Lexi Pimenidis
 */
public class CellRelayResolve extends CellRelay
{
	public CellRelayResolve(Stream s, String hostname)
	{
		super(s, CellRelay.RELAY_RESOLVE);
		// data...
		final byte[] host = hostname.getBytes();
		System.arraycopy(host, 0, data, 0, host.length);
		// length
		setLength(host.length);
	}
}
