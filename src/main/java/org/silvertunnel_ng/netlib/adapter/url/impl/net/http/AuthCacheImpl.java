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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Michael McMahon
 */

public class AuthCacheImpl implements AuthCache
{
	HashMap hashtable;

	public AuthCacheImpl()
	{
		hashtable = new HashMap();
	}

	public void setMap(HashMap map)
	{
		hashtable = map;
	}

	// put a value in map according to primary key + secondary key which
	// is the path field of AuthenticationInfo

	@Override
	public synchronized void put(final String pkey, final AuthCacheValue value)
	{
		LinkedList list = (LinkedList) hashtable.get(pkey);
		final String skey = value.getPath();
		if (list == null)
		{
			list = new LinkedList();
			hashtable.put(pkey, list);
		}
		// Check if the path already exists or a super-set of it exists
		final ListIterator iter = list.listIterator();
		while (iter.hasNext())
		{
			final AuthenticationInfo inf = (AuthenticationInfo) iter.next();
			if (inf.path == null || inf.path.startsWith(skey))
			{
				iter.remove();
			}
		}
		iter.add(value);
	}

	// get a value from map checking both primary
	// and secondary (urlpath) key

	@Override
	public synchronized AuthCacheValue get(final String pkey, String skey)
	{
		final AuthenticationInfo result = null;
		final LinkedList list = (LinkedList) hashtable.get(pkey);
		if (list == null || list.size() == 0)
		{
			return null;
		}
		if (skey == null)
		{
			return (AuthenticationInfo) list.get(0); // list should contain only one element
		}
		final ListIterator iter = list.listIterator();
		while (iter.hasNext())
		{
			final AuthenticationInfo inf = (AuthenticationInfo) iter.next();
			if (skey.startsWith(inf.path))
			{
				return inf;
			}
		}
		return null;
	}

	@Override
	public synchronized void remove(final String pkey, final AuthCacheValue entry)
	{
		final LinkedList list = (LinkedList) hashtable.get(pkey);
		if (list == null)
		{
			return;
		}
		if (entry == null)
		{
			list.clear();
			return;
		}
		final ListIterator iter = list.listIterator();
		while (iter.hasNext())
		{
			final AuthenticationInfo inf = (AuthenticationInfo) iter.next();
			if (entry.equals(inf))
			{
				iter.remove();
			}
		}
	}
}
