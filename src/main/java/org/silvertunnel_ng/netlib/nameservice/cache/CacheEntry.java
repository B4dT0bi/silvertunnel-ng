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

package org.silvertunnel_ng.netlib.nameservice.cache;

import java.util.Date;
import java.util.Map;

/**
 * Key + value of the internal storage of class Cache.
 * 
 * @author hapke
 */
class CacheEntry<K, V> implements Map.Entry<K, V>
{
	private K key;
	private V value;

	/** null=expires never */
	private Date expires;

	/**
	 * @param value
	 * @param timeToLiveSeconds
	 */
	public CacheEntry(K key, V value, int timeToLiveSeconds)
	{
		this.key = key;
		this.value = value;
		this.expires = new Date(System.currentTimeMillis()
				+ (1000L * timeToLiveSeconds));
	}

	/**
	 * @param value
	 * @param expires
	 *            null=expires never
	 */
	public CacheEntry(V value, Date expires)
	{
		this.value = value;
		this.expires = expires;
	}

	public boolean isExpired()
	{
		if (expires == null)
		{
			return false;
		}
		else
		{
			return expires.before(new Date());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if (o == null || (!(o instanceof CacheEntry)))
		{
			return false;
		}

		final CacheEntry<K, V> e1 = this;
		final CacheEntry<K, V> e2 = (CacheEntry<K, V>) o;

		return (e1.getKey() == null ? e2.getKey() == null : e1.getKey().equals(
				e2.getKey()))
				&& (e1.getValue() == null ? e2.getValue() == null : e1
						.getValue().equals(e2.getValue()));
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public String toString()
	{
		return "(" + key + "," + value + "," + expires + ")";
	}

	@Override
	public V setValue(V value)
	{
		final V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public V getValue()
	{
		return value;
	}

	public Date getExpires()
	{
		return expires;
	}

	public void setExpires(Date expires)
	{
		this.expires = expires;
	}
}
