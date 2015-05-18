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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

/**
 * Object to hold circuit live status data.
 * 
 * @author hapke
 */
public class CircuitsStatus
{
	/** all circuits. */
	private int circuitsTotal = 0;
	/** circuits that are building up, or that are established. */
	private int circuitsAlive = 0;
	/** established, but not already closed. */
	private int circuitsEstablished = 0;
	/** closing down. */
	private int circuitsClosed = 0;

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	public int getCircuitsTotal()
	{
		return circuitsTotal;
	}

	public void setCircuitsTotal(int circuitsTotal)
	{
		this.circuitsTotal = circuitsTotal;
	}

	public int getCircuitsAlive()
	{
		return circuitsAlive;
	}

	public void setCircuitsAlive(int circuitsAlive)
	{
		this.circuitsAlive = circuitsAlive;
	}

	public int getCircuitsEstablished()
	{
		return circuitsEstablished;
	}

	public void setCircuitsEstablished(int circuitsEstablished)
	{
		this.circuitsEstablished = circuitsEstablished;
	}

	public int getCircuitsClosed()
	{
		return circuitsClosed;
	}

	public void setCircuitsClosed(int circuitsClosed)
	{
		this.circuitsClosed = circuitsClosed;
	}
}
