/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;

/**
 * Abstract kind of TCPStream.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public interface Stream
{
	/**
	 * for internal usage.
	 * 
	 * @param force
	 *            if set to true, just destroy the object, without sending
	 *            END-CELLs and stuff
	 */
	void close(boolean force);

	void setId(int id);

	int getId();

	/**
	 * @return is this {@link Stream} closed?
	 */
	boolean isClosed();

	/**
	 * @return timestamp of the last sent cell.
	 */
	long getLastCellSentDate();

	/**
	 * @return get {@link Circuit} where this stream is attached to.
	 */
	Circuit getCircuit();

	/**
	 * Send a {@link Cell} using this {@link Stream}.
	 * @param cell the {@link Cell} to be sent.
	 * @throws TorException
	 */
	void sendCell(Cell cell) throws TorException;

	/**
	 * Handle the received cell.
	 * @param cell the {@link Cell}
	 */
	void processCell(Cell cell) throws TorException;
}
