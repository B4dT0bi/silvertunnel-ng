/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 20132 silvertunnel-ng.org
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

package org.silvertunnel_ng.netlib.layer.tor.hiddenservice;

import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptor;

/**
 * This class takes care of caching the Hiddenservice descriptors.
 * 
 * @author Tobias Boese
 *
 */
public final class HiddenServiceDescriptorCache
{

	/**
	 * 
	 */
	public HiddenServiceDescriptorCache()
	{
	}
	/** instance of {@link HiddenServiceDescriptorCache}.*/
	private static HiddenServiceDescriptorCache instance;
	/**
	 * @return get an instance of {@link HiddenServiceDescriptorCache}.
	 */
	public static HiddenServiceDescriptorCache getInstance()
	{
		synchronized (instance)
		{
			if (instance == null)
			{
				instance = new HiddenServiceDescriptorCache();
				instance.init();
			}			
		}
		return instance;
	}
	/** cached {@link RendezvousServiceDescriptor}. */
	private static Map<String, RendezvousServiceDescriptor> cachedRendezvousServiceDescriptors = new HashMap<String, RendezvousServiceDescriptor>();

	/**
	 * Init the cache.
	 * 
	 * - Cleans the cache.
	 * - if option in {@link TorConfig} is set it will load the saved Descriptors from disk into the cache if it is not older than 24h
	 */
	public synchronized void init()
	{
		cachedRendezvousServiceDescriptors.clear();
		if (TorConfig.isCacheHiddenServiceDescriptor())
		{
			// TODO : implement loading from disk
		}
	}
	/**
	 * Saves the cache to disk.
	 */
	public synchronized void saveCacheToDisk()
	{
		if (!TorConfig.isCacheHiddenServiceDescriptor())
		{
			return; // dont save cache to disk
		}
		// TODO : implement saving to disk using path from TorConfig
	}
	/**
	 * Add a {@link RendezvousServiceDescriptor} to the cache.
	 * @param z the onion address without .onion
	 * @param descriptor the {@link RendezvousServiceDescriptor}
	 */
	public void put(final String z, final RendezvousServiceDescriptor descriptor)
	{
		cachedRendezvousServiceDescriptors.put(z, descriptor);
	}
	/**
	 * Try to get a cached {@link RendezvousServiceDescriptor}.
	 * 
	 * @param z the onion address without .onion
	 * @return {@link RendezvousServiceDescriptor} or null if not found/not valid anymore
	 */
	public RendezvousServiceDescriptor get(final String z)
	{
		RendezvousServiceDescriptor result = cachedRendezvousServiceDescriptors.get(z);
		if (result == null)
		{
			return null; // nothing found
		}
		if (result.isPublicationTimeValid())
		{
			return result; // valid so return it
		}
		return null; // not valid anymore
	}
}
