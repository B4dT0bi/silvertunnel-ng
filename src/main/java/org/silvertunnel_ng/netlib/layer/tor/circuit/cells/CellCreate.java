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

import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * used to create a CREATE cell.
 * 
 * @author Lexi Pimenidis
 */
public class CellCreate extends Cell
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(CellCreate.class);

	/**
	 * creates a CREATE-CELL.
	 * 
	 * @param circuit
	 *            the circuit that is to be build with this cell
	 */
	public CellCreate(final Circuit circuit) throws TorException
	{
		super(circuit, Cell.CELL_CREATE);
		// create DH-exchange:
		final byte[] data = new byte[144];
		// OAEP padding [42 bytes] (RSA-encrypted)
		// (gets added automatically)
		// Symmetric key [16 bytes]
		System.arraycopy(circuit.getRouteNodes()[0].getSymmetricKeyForCreate(),
				0, data, 0, 16);
		// First part of g^x [70 bytes]
		// Second part of g^x [58 bytes] (Symmetrically encrypted)
		System.arraycopy(circuit.getRouteNodes()[0].getDhXBytes(), 0, data, 16,
				128);
		// encrypt and store result in payload
		final byte[] temp = circuit.getRouteNodes()[0].asymEncrypt(data);

		if (payload.length < temp.length)
		{
			LOG.warn("encrypted data longer than max payload length.\n possible unwanted truncation.");
			System.arraycopy(temp, 0, payload, 0, payload.length);
		}
		else
		{
			System.arraycopy(temp, 0, payload, 0, temp.length);
		}
	}
}
