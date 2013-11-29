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
 * this cell is used to establish introduction point.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Boese
 */
public class CellRelayRendezvous1 extends CellRelay
{
	/**
	 * Create a new Cell of type Relay_Rendezvous1.
	 * 
	 * @param circuit the Circuit to be used for sending/receiving the Cell
	 * @param cookie the rendezvous cookie
	 * @param dhY the Y part of Diffie-Hellman
	 * @param keyHandshake the keyHandshake to verify that DH worked
	 */
	public CellRelayRendezvous1(final Circuit circuit, 
			             final byte[] cookie, 
			             final byte[] dhY, 
			             final byte[] keyHandshake)
	{
		super(circuit, RELAY_RENDEZVOUS1);
		// copy to payload
		System.arraycopy(cookie, 0, data, 0, cookie.length);
		System.arraycopy(dhY, 0, data, cookie.length, dhY.length);
		System.arraycopy(keyHandshake, 0, data, cookie.length + dhY.length, keyHandshake.length);
		setLength(cookie.length + dhY.length + keyHandshake.length);
	}
}
