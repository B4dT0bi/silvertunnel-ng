/*
 * Copyright 2004-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package org.silvertunnel_ng.netlib.adapter.url.impl.net.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class allows for centralized access to Networking properties.
 * Default values are loaded from the file jre/lib/net.properties
 *
 *
 * @author Jean-Christophe Collet
 *
 */

public class NetProperties
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NetProperties.class);
	private static Properties props = new Properties();
	static
	{
		AccessController.doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				loadDefaultProperties();
				return null;
			}
		});
	}

	private NetProperties()
	{
	};

	/*
	 * Loads the default networking system properties the file is in
	 * jre/lib/net.properties
	 */
	private static void loadDefaultProperties()
	{
		String fname = System.getProperty("java.home");
		if (fname == null)
		{
			throw new Error("Can't find java.home ??");
		}
		try
		{
			File f = new File(fname, "lib");
			f = new File(f, "net.properties");
			fname = f.getCanonicalPath();
			final InputStream in = new FileInputStream(fname);
			final BufferedInputStream bin = new BufferedInputStream(in);
			props.load(bin);
			bin.close();
		}
		catch (final Exception e)
		{
			// Do nothing. We couldn't find or access the file
			// so we won't have default properties...
			LOG.debug("got Exception : {}", e.getMessage(), e);
		}
	}

	/**
	 * Get a networking system property. If no system property was defined
	 * returns the default value, if it exists, otherwise returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the property name.
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPropertiesAccess</code> method doesn't allow
	 *             access to the system properties.
	 * @return the <code>String</code> value for the property, or
	 *         <code>null</code>
	 */
	public static String get(String key)
	{
		final String def = props.getProperty(key);
		try
		{
			return System.getProperty(key, def);
		}
		catch (final IllegalArgumentException e)
		{
			LOG.debug("got IllegalArgumentException : {}", e.getMessage(), e);
		}
		catch (final NullPointerException e)
		{
			LOG.debug("got NullPointerException : {}", e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Get an Integer networking system property. If no system property was
	 * defined returns the default value, if it exists, otherwise returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the property name.
	 * @param defval
	 *            the default value to use if the property is not found
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPropertiesAccess</code> method doesn't allow
	 *             access to the system properties.
	 * @return the <code>Integer</code> value for the property, or
	 *         <code>null</code>
	 */
	public static Integer getInteger(final String key, final int defval)
	{
		String val = null;

		try
		{
			val = System.getProperty(key, props.getProperty(key));
		}
		catch (final IllegalArgumentException e)
		{
			LOG.debug("got IllegalArgumentException : {}", e.getMessage(), e);
		}
		catch (final NullPointerException e)
		{
			LOG.debug("got NullPointerException : {}", e.getMessage(), e);
		}

		if (val != null)
		{
			try
			{
				return Integer.decode(val);
			}
			catch (final NumberFormatException ex)
			{
				LOG.debug("got NumberFormatException : {}", ex.getMessage(), ex);
			}
		}
		return Integer.valueOf(defval);
	}

	/**
	 * Get a Boolean networking system property. If no system property was
	 * defined returns the default value, if it exists, otherwise returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the property name.
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPropertiesAccess</code> method doesn't allow
	 *             access to the system properties.
	 * @return the <code>Boolean</code> value for the property, or
	 *         <code>null</code>
	 */
	public static Boolean getBoolean(final String key)
	{
		String val = null;

		try
		{
			val = System.getProperty(key, props.getProperty(key));
		}
		catch (final IllegalArgumentException e)
		{
			LOG.debug("got IllegalArgumentException : {}", e.getMessage(), e);
		}
		catch (final NullPointerException e)
		{
			LOG.debug("got NullPointerException : {}", e.getMessage(), e);
		}

		if (val != null)
		{
			try
			{
				return Boolean.valueOf(val);
			}
			catch (final NumberFormatException ex)
			{
				LOG.debug("got NumberFormatException : {}", ex.getMessage(), ex);
			}
		}
		return null;
	}

}
