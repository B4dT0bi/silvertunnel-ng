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

package org.silvertunnel_ng.netlib.layer.tor.util;

/**
 * this exception is used to throw Tor-related exceptions.
 * 
 * @author Lexi Pimenidis
 * @author Tobias Boese
 */
public class TorException extends Exception
{


	/** */
	private static final long serialVersionUID = 597691078427388360L;
	
	/**
	 * A TorException.
	 */
	public TorException()
	{
		super();
	}

	/**
	 * TorException with detail Message.
	 * @param message the detail message
	 */
	public TorException(final String message)
	{
		super(message);
	}

	/**
	 * TorException with root cause.
	 * @param cause the root cause
	 */
	public TorException(final Throwable cause)
	{
		super(cause);
	}
	/**
	 * TorException with detail message and root cause.
	 * @param message the detail message
	 * @param cause the root cause
	 */
	public TorException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
