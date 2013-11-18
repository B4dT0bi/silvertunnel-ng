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
package org.silvertunnel_ng.netlib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helperclass for dealing with Systemproperties.
 * 
 * @author Tobias Boese
 */
public final class SystemPropertiesHelper
{
	private SystemPropertiesHelper()
	{
		
	}
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SystemPropertiesHelper.class);
	/**
	 * Read a system property as integer.
	 * 
	 * @param key
	 *            key to be searched
	 * @param defaultValue
	 *            the default value if property is not found
	 * @return the system property as integer; defaultValue is the system
	 *         property is not set or not parse-able
	 */
	public static int getSystemProperty(final String key, final int defaultValue)
	{
		final String value = System.getProperty(key);
		if (value == null)
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(value);
		}
		catch (final Exception e)
		{
			// value could not be parsed
			return defaultValue;
		}
	}

	/**
	 * Read a system property as boolean.
	 * 
	 * @param key
	 *            key to be searched
	 * @param defaultValue
	 *            the default value if property is not found
	 * @return the system property as integer; defaultValue is the system
	 *         property is not set or not parse-able
	 */
	public static boolean getSystemProperty(final String key, final boolean defaultValue)
	{
		String value = System.getProperty(key);
		if (value == null)
		{
			return defaultValue;
		}
		try
		{
			final int tmp = Integer.parseInt(value);
			if (tmp == 0 || tmp == 1)
			{
				return tmp == 1;
			}
			else
			{
				LOG.warn("incorrect value (" + value + ") for " + key
						+ " possible values are : 0,1,true,false,y,n");
				return defaultValue;
			}
		}
		catch (final Exception e)
		{
			value = value.toLowerCase();
			if (value.charAt(0) == 'f')
			{
				return false;
			}
			else if (value.charAt(0) == 'n')
			{
				return false;
			}
			else if (value.charAt(0) == 't')
			{
				return true;
			}
			else if (value.charAt(0) == 'y')
			{
				return true;
			}
			LOG.warn("incorrect value (" + value + ") for " + key
					+ " possible values are : 0,1,true,false,y,n");
			return defaultValue;
		}
	}
}
