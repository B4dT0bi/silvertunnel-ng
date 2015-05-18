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

package org.silvertunnel_ng.netlib.util;

/**
 * Simple API to store a String to ... and retrieve it later from ... .
 * 
 * It works like a Map.
 * 
 * Key may only contain lower case Latin letters (a-z), digits (0-9) or
 * underscore (_) or minus (-) or dot (.).
 * 
 * The value is a not null String of ASCII characters; non-ASCII characters are
 * not guaranteed to be stored correctly.
 * 
 * @author hapke
 */
public interface StringStorage
{
	/**
	 * Store a value.
	 * 
	 * @param key
	 *            a valid key (see interface doc for details)
	 * @param value
	 *            a not null ASCII String; non-ASCII characters are not
	 *            guaranteed to be stored correctly
	 * @throws IllegalArgumentException
	 */
	public void put(String key, String value) throws IllegalArgumentException;

	/**
	 * Retrieve a value.
	 * 
	 * @param key
	 *            a valid key (see interface doc for details)
	 * @return the values; null if no value found
	 */
	public String get(String key);
}
