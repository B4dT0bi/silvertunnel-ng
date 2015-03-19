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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.directory.RendezvousServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes care of caching the Hiddenservice descriptors.
 * 
 * @author Tobias Boese
 *
 */
public final class HiddenServiceDescriptorCache
{
	/** class logger. */
	private static final Logger LOG = LoggerFactory.getLogger(HiddenServiceDescriptorCache.class);
	/**
	 * 
	 */
	private HiddenServiceDescriptorCache()
	{
	}
	/** instance of {@link HiddenServiceDescriptorCache}.*/
	private static HiddenServiceDescriptorCache instance;
	/**
	 * @return get an instance of {@link HiddenServiceDescriptorCache}.
	 */
	public static synchronized HiddenServiceDescriptorCache getInstance()
	{
		if (instance == null)
		{
			instance = new HiddenServiceDescriptorCache();
			instance.init();
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
			try
			{
				FileInputStream fileInputStream = new FileInputStream(TorConfig.getTempDirectory() 
						   											+ File.separator
						   											+ TorConfig.FILENAME_PREFIX
						   											+ "hidden_service_descriptor.cache");
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				cachedRendezvousServiceDescriptors = (Map<String, RendezvousServiceDescriptor>) objectInputStream.readObject();
				objectInputStream.close();
						   							
			}
            catch (FileNotFoundException exception) {
                LOG.info("no cached hiddenservice descriptors found");
            }
			catch (Exception exception)
			{
				LOG.warn("could not load cached hiddenservice descriptors because of exception", exception);
			}
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
		LOG.debug("saving {} cached hiddenservice descriptors to disk", cachedRendezvousServiceDescriptors.size());
		try
		{
			FileOutputStream fileOutputStream = new FileOutputStream(TorConfig.getTempDirectory() 
																   + File.separator
																   + TorConfig.FILENAME_PREFIX
					                                               + "hidden_service_descriptor.cache");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(cachedRendezvousServiceDescriptors);
			objectOutputStream.close();
		}
		catch (Exception exception)
		{
			LOG.warn("cant save hiddenservice descriptor cache due to exception", exception);
		}
	}
	/**
	 * Add a {@link RendezvousServiceDescriptor} to the cache.
	 * @param z the onion address without .onion
	 * @param descriptor the {@link RendezvousServiceDescriptor}
	 */
	public void put(final String z, final RendezvousServiceDescriptor descriptor)
	{
		LOG.debug("adding {} to cache", z);
		cachedRendezvousServiceDescriptors.put(z, descriptor);
		saveCacheToDisk();
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
			LOG.debug("found cached descriptor for {}", z);
			return result; // valid so return it
		}
		//not valid anymore so remove it
		LOG.debug("removing {} because its too old", z);
		synchronized (cachedRendezvousServiceDescriptors) 
		{
			cachedRendezvousServiceDescriptors.remove(z);
		}
		return null;
	}
}
