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

package org.silvertunnel_ng.netlib.api;

/**
 * An object represents the state of a NetLayer.
 * 
 * It can be used to retrieve the status of a NetLayer and display it to the
 * user in a proper way.
 * 
 * @author hapke
 */
public class NetLayerStatus implements Comparable<NetLayerStatus>
{
	public static final NetLayerStatus NEW = new NetLayerStatus("New or clean",
			0.0);
	public static final NetLayerStatus READY = new NetLayerStatus(
			"Ready for use", 1.0);
	// hint: we intentionally do not have a state "CLOSED"

	/** English name of this status. */
	private final String name;
	/** indicator: between 0 (absolutely not ready) and 1 (ready for use). */
	private final double readyIndicator;

	/**
	 * Create a new NetLayerStatus instance.
	 * 
	 * @param name
	 *            English name of this status, not null
	 * @param readyIndicator
	 *            between 0 (absolutely not ready) and 1 (ready for use)
	 */
	protected NetLayerStatus(final String name, final double readyIndicator)
	{
		this.name = name;
		this.readyIndicator = readyIndicator;
	}

	@Override
	public String toString()
	{
		return this.name + " (" + ((int) (100 * readyIndicator)) + "%)";
	}

	@Override
	public int hashCode()
	{
		return (int) (readyIndicator * Integer.MAX_VALUE);
	}

	@Override
	public boolean equals(final Object o)
	{
		// simple checks
		if (o == null || !(o instanceof NetLayerStatus))
		{
			return false;
		}

		final NetLayerStatus obj = (NetLayerStatus) o;
		return this.name.equals(obj.name)
				&& this.readyIndicator == obj.readyIndicator;
	}

	@Override
	public int compareTo(final NetLayerStatus other)
	{
		return new Double(readyIndicator).compareTo(new Double(other
				.getReadyIndicator()));
	}

	// /////////////////////////////////////////////////////
	// generated getters
	// /////////////////////////////////////////////////////

	/**
	 * @return English name of this status
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return indicator: between 0 (absolutely not ready) and 1 (ready for use)
	 */
	public double getReadyIndicator()
	{
		return readyIndicator;
	}
}
