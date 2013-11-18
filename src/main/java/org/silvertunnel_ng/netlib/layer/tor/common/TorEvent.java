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
package org.silvertunnel_ng.netlib.layer.tor.common;

/**
 * Used as a hook for other applications to receive notifications in case of
 * certain events inside tor.
 */
public class TorEvent
{
	public static final int GENERAL = 0;
	public static final int CIRCUIT_BUILD = 10;
	public static final int CIRCUIT_CLOSED = 11;
	public static final int STREAM_BUILD = 20;
	public static final int STREAM_CLOSED = 21;

	private final String description;
	private final int type;
	private final Object cause;

	public TorEvent(final int type, final Object o, final String description)
	{
		this.description = description;
		this.cause = o;
		this.type = type;
	}

	public String getDescription()
	{
		return this.description;
	}

	public int getType()
	{
		return this.type;
	}

	public Object getObject()
	{
		return this.cause;
	}
}
