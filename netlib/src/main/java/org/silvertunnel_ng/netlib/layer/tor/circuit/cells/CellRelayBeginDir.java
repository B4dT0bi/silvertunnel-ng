/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
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

import org.silvertunnel_ng.netlib.layer.tor.circuit.Stream;


/**
 * sends a BEGIN_DIR cell, needed to establish a directory-stream.
 * 
 * @author Tobias Boese
 */
public class CellRelayBeginDir extends CellRelay
{
	/**
	 * constructor to build a BEGIN_DIR-CELL.
	 * 
	 * @param stream
	 *            the stream that will carry the cell and the following data
	 */
	public CellRelayBeginDir(final Stream stream)
	{
		// init a new Relay-cell
		super(stream, RELAY_BEGIN_DIR);
	}
}
