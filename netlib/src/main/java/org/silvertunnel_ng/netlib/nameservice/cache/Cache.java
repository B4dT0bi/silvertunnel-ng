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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a very simple, general purpose cache implementation.
 * 
 * The idea is to be compatible with/a subset of JCache JSR 107 and to be
 * compatible with java.util.Map.
 * 
 * @author hapke
 */
public class Cache<K, V> implements Map<K, V>
{
	/** the stored elements. */
	private final Map<K, CacheEntry<K, V>> storage;

	/** configuration parameter. */
	private final int timeToLiveSeconds;
	/** configuration parameter. */
	private final int maxElements;

	/** constructor argument limit. */
	private static final int MIN_MAX_ELEMENTS = 1;

	/**
	 * Create a new cache instance.
	 * 
	 * @param timeToLiveSeconds
	 *            &lt;0 means unlimited time to live, 0 means no caching
	 * @param maxElements
	 *            &gt;=1
	 */
	public Cache(final int maxElements, final int timeToLiveSeconds)
	{
		if (timeToLiveSeconds < 0)
		{
			this.timeToLiveSeconds = Integer.MAX_VALUE;
		}
		else
		{
			this.timeToLiveSeconds = timeToLiveSeconds;
		}
		if (maxElements < MIN_MAX_ELEMENTS)
		{
			throw new IllegalArgumentException("invalid maxElements="
					+ maxElements);
		}
		this.maxElements = maxElements;

		storage = new HashMap<K, CacheEntry<K, V>>(maxElements);
	}

	@Override
	public synchronized void clear()
	{
		storage.clear();
	}

	@Override
	public synchronized boolean containsKey(final Object key)
	{
		final V v = get(key);
		return v != null;
	}

	@Override
	public synchronized boolean containsValue(final Object value)
	{
		return values().contains(value);
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, V>> entrySet()
	{
		final Set<java.util.Map.Entry<K, V>> entries = new HashSet<java.util.Map.Entry<K, V>>(
				storage.size());

		for (final K key : storage.keySet())
		{
			final CacheEntry<K, V> cacheValue = storage.get(key);
			if (cacheValue != null)
			{
				entries.add(cacheValue);
			}
		}

		return entries;
	}

	@Override
	public synchronized V get(final Object key)
	{
		if (timeToLiveSeconds == 0)
		{
			return null; // do not cache
		}

		final CacheEntry<K, V> value = storage.get(key);
		if (value == null)
		{
			// no entry found
			return null;
		}
		else if (value.isExpired())
		{
			// expired entry found
			storage.remove(key);
			return null;
		}
		else
		{
			// valid entry found
			return value.getValue();
		}
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return storage.isEmpty();
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return storage.keySet();
	}

	@Override
	public synchronized V put(final K key, final V value)
	{
		if (timeToLiveSeconds == 0)
		{
			return null; // do not cache
		}

		ensureThatAtLeastOneMoreEntryCanBePutted();

		final CacheEntry<K, V> valueNew = new CacheEntry<K, V>(key, value,
				timeToLiveSeconds);
		final CacheEntry<K, V> valueOld = storage.put(key, valueNew);
		return (valueOld == null) ? null : valueOld.getValue();
	}

	@Override
	public synchronized void putAll(final Map<? extends K, ? extends V> m)
	{
		for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public synchronized V remove(final Object key)
	{
		final CacheEntry<K, V> v = storage.remove(key);
		return (v == null) ? null : v.getValue();
	}

	@Override
	public synchronized int size()
	{
		removeExpiredEntries();
		return storage.size();
	}

	@Override
	public synchronized Collection<V> values()
	{
		final Collection<V> values = new ArrayList<V>(storage.size());

		for (final K key : storage.keySet())
		{
			final CacheEntry<K, V> value = storage.get(key);
			if (value != null)
			{
				values.add(value.getValue());
			}
		}

		return values;
	}

	@Override
	public String toString()
	{
		return "Cache(" + storage + ")";
	}

	// /////////////////////////////////////////////////////
	// internal helper methods
	// /////////////////////////////////////////////////////

	private synchronized void ensureThatAtLeastOneMoreEntryCanBePutted()
	{
		if (storage.size() < maxElements)
		 {
			return; // still enough space in the storage
		}

		// try the soft way
		final K remainingKey = removeExpiredEntries();

		if (storage.size() > maxElements - 1)
		{
			// do the hard way: remove one element
			if (remainingKey != null)
			{
				// remove
				storage.remove(remainingKey);
			}
			else
			{
				// could not remove: should never happens
				throw new IllegalStateException(
						"no remainingKey found, but storage is not empty: "
								+ storage);
			}
		}
	}

	/**
	 * @return any of the remaining keys after cleanup; null if no entry remains
	 *         in the storage
	 */
	private synchronized K removeExpiredEntries()
	{
		K remainingKey = null;

		// find expired entries
		final Collection<K> keysToRemove = new ArrayList<K>(storage.size());
		for (final Map.Entry<K, CacheEntry<K, V>> entry : storage.entrySet())
		{
			if (entry.getValue().isExpired())
			{
				keysToRemove.add(entry.getKey());
			}
			else
			{
				remainingKey = entry.getKey();
			}
		}

		// delete expired entries
		for (final K keyToRemove : keysToRemove)
		{
			storage.remove(keyToRemove);
		}

		return remainingKey;
	}
}
