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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for handling (local)properties.
 * 
 * @author hapke
 */
public class PropertiesUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);
	private static final String LIST_SEPARATOR = ",";

	/**
	 * Try to return an Object.
	 * 
	 * @param properties
	 *            can be null
	 * @param key
	 *            not null
	 * @param defaultValue
	 *            can be null
	 * @return
	 */
	public static Object getAsObject(Map<String, Object> properties,
			String key, Object defaultValue)
	{
		if (properties == null)
		{
			return defaultValue;
		}

		return properties.get(key);
	}

	/**
	 * Try to return a String[]. If the value is of a different type then try to
	 * parse it as comma separated String.
	 * 
	 * @param properties
	 *            can be null
	 * @param key
	 *            not null
	 * @param defaultValue
	 *            can be null
	 * @return
	 */
	public static String[] getAsStringArray(Map<String, Object> properties,
			final String key, final String[] defaultValue)
	{
		if (properties == null)
		{
			return defaultValue;
		}

		final Object value = properties.get(key);

		// process property value
		if (value == null)
		{
			return defaultValue;
		}
		if (value instanceof String[])
		{
			return (String[]) value;
		}
		return value.toString().split(LIST_SEPARATOR);
	}

	/**
	 * Try to return a String.
	 * 
	 * @param properties
	 *            can be null
	 * @param key
	 *            not null
	 * @param defaultValue
	 *            can be null
	 * @return
	 */
	public static String getAsString(Map<String, Object> properties,
			String key, String defaultValue)
	{
		if (properties == null)
		{
			return defaultValue;
		}

		final Object value = properties.get(key);

		// process property value
		if (value == null)
		{
			return defaultValue;
		}
		return value.toString();
	}

	/**
	 * Try to return a Long.
	 * 
	 * @param properties
	 *            can be null
	 * @param key
	 *            not null
	 * @param defaultValue
	 *            can be null
	 * @return
	 */
	public static Long getAsLong(Map<String, Object> properties, final String key,
			final Long defaultValue)
	{
		if (properties == null)
		{
			return defaultValue;
		}

		final Object obj = properties.get(key);
		if (obj instanceof Long)
		{
			return (Long) obj;
		}
		else if (obj instanceof Integer)
		{
			return ((Integer) obj).longValue();
		}
		else if (obj instanceof Short)
		{
			return ((Short) obj).longValue();
		}
		else if (obj instanceof Byte)
		{
			return ((Byte) obj).longValue();
		}
		else if (obj instanceof String)
		{
			try
			{
				final String s = (String) obj;
				return Long.valueOf(s);
			}
			catch (final Exception e)
			{
				// ignore it
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}

		return defaultValue;
	}

	/**
	 * Try to return a Integer.
	 * 
	 * @param properties
	 *            can be null
	 * @param key
	 *            not null
	 * @param defaultValue
	 *            can be null
	 * @return
	 */
	public static Integer getAsInteger(Map<String, Object> properties,
			String key, final Integer defaultValue)
	{
		if (properties == null)
		{
			return defaultValue;
		}

		final Object obj = properties.get(key);
		if (obj instanceof Integer)
		{
			return (Integer) obj;
		}
		else if (obj instanceof Long)
		{
			return ((Long) obj).intValue();
		}
		else if (obj instanceof Short)
		{
			return ((Short) obj).intValue();
		}
		else if (obj instanceof Byte)
		{
			return ((Byte) obj).intValue();
		}
		else if (obj instanceof String)
		{
			try
			{
				final String s = (String) obj;
				return Integer.valueOf(s);
			}
			catch (final Exception e)
			{
				// ignore it
				LOG.debug("got Exception : {}", e.getMessage(), e);
			}
		}

		return defaultValue;
	}

	/*
	 * TODO: check how to implement this correctly public static <T> T
	 * getAsType(Map<String,Object> properties, String key, T defaultValue) {
	 * Object value = properties.get(key);
	 * 
	 * if (value==null) { return defaultValue; }
	 * 
	 * try { return (T)value; } }
	 */
}
