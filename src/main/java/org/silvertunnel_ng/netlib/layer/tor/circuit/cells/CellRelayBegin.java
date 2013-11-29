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
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;

/**
 * sends a BEGIN cell, needed to establish a tcp-stream
 * 
 * @author Lexi Pimenidis
 */
public class CellRelayBegin extends CellRelay
{
	/**
	 * constructor to build a BEGIN-CELL
	 * 
	 * @param s
	 *            the stream that will carry the cell and the following data
	 * @param sp
	 *            contains the host and port to connect to
	 */
	public CellRelayBegin(Stream s, TCPStreamProperties sp)
	{
		// init a new Relay-cell
		super(s, RELAY_BEGIN);

		// ADDRESS | ':' | PORT | [00]
		byte[] host;
		if (sp.isAddrResolved())
		{
			// insert IP-adress in dotted-quad format, if resolved
			final StringBuffer sb = new StringBuffer();
			final byte[] a = sp.getAddr().getAddress();
			for (int i = 0; i < 4; ++i)
			{
				if (i > 0)
				{
					sb.append('.');
				}
				sb.append((a[i]) & 0xff);
			}

			host = sb.toString().getBytes();
		}
		else
		{
			// otherwise let exit-point resolve name itself
			host = sp.getHostname().getBytes();
		}

		System.arraycopy(host, 0, data, 0, host.length);
		// set ':'
		getData()[host.length] = ':';
		// set port
		final byte[] port = Integer.valueOf(sp.getPort()).toString().getBytes();
		System.arraycopy(port, 0, getData(), host.length + 1, port.length);
		// set length
		setLength(host.length + 1 + port.length + 1);
	}
}
