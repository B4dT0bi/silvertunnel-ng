/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.Serializable;
import java.net.PasswordAuthentication;

/**
 * AuthCacheValue: interface to minimise exposure to authentication cache for
 * external users (ie. plugin)
 * 
 * @author Michael McMahon
 */

public abstract class AuthCacheValue implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8021676947097690390L;

	public enum Type
	{
		Proxy, Server
	};

	/**
	 * Caches authentication info entered by user. See cacheKey()
	 */
	protected static AuthCache cache = new AuthCacheImpl();

	public static void setAuthCache(final AuthCache map)
	{
		cache = map;
	}

	/* Package private ctor to prevent extension outside package */

	AuthCacheValue()
	{
	}

	abstract Type getAuthType();

	/**
	 * @return name of server/proxy.
	 */
	abstract String getHost();

	/**
	 * @return portnumber of server/proxy.
	 */
	abstract int getPort();

	/**
	 * @return realm of authentication if known.
	 */
	abstract String getRealm();

	/**
	 * @return root path of realm or the request path if the root is not known yet.
	 */
	abstract String getPath();

	/**
	 * @return http or https.
	 */
	abstract String getProtocolScheme();

	/**
	 * @return the credentials associated with this authentication.
	 */
	abstract PasswordAuthentication credentials();
}
