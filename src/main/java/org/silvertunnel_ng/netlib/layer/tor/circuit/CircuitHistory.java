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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;

/**
 * Capture all Circuit information used for predicting the next needed Circuits.
 * 
 * @author Tobias Boese
 */
public final class CircuitHistory 
{
	/** How many Circuits did we have overall which have been used for internal communication ? */
	private int countInternal = 0;
	/** How many Circuits did we have overall which have been used for external communication ? */
	private int countExternal = 0;
	/** How many internal communications did we have in the last timeframe ? */
	private final Map<Long, Integer> mapCountInternal = new HashMap<Long, Integer>();
	/** How many external communications did we have in the last timeframe ? */
	private final Map<Long, Integer> mapCountExternal = new HashMap<Long, Integer>();
	/** What is the maximum time frame used for current historic data? */
	private static final long MAX_TIMEFRAME_IN_MINUTES = 10; // 10 minutes
	/** map of Ports and Count which have been used mostly in the past. */
	private final Map<Integer, Integer> mapHistoricPorts = new HashMap<Integer, Integer>();
	/** map containing the port info for the last timeframe. */ 
	private final Map<Long, Map<Integer, Integer>> mapCurrentHistoricPorts = new HashMap<Long, Map<Integer, Integer>>();
	/**
	 * 
	 */
	public CircuitHistory() 
	{
		// TODO implement history loading and saving
	}
	/** 
	 * Add the information from a {@link Circuit} object to the history.
	 * 
	 * @param streamProperties the {@link TCPStreamProperties} object which has been used for a specific task.
	 */
	public void addCircuit(final TCPStreamProperties streamProperties)
	{
		checkTimeframe();
		Long crrTime = System.currentTimeMillis() / 60000;
		if (streamProperties != null)
		{
			if (streamProperties.isExitPolicyRequired())
			{ // external
				synchronized (mapCountExternal)
				{
					countExternal++;
					Integer count = mapCountExternal.get(crrTime);
					if (count == null)
					{
						count = 0;
					}
					count++;
					mapCountExternal.put(crrTime, count);
				}
				synchronized (mapCurrentHistoricPorts)
				{
					Integer port = streamProperties.getPort();
					Integer count = mapHistoricPorts.get(port);
					if (count == null)
					{
						count = 0;
					}
					count++;
					mapHistoricPorts.put(port, count);
					Map<Integer, Integer> ports = mapCurrentHistoricPorts.get(crrTime);
					if (ports == null)
					{
						ports = new HashMap<Integer, Integer>();
						mapCurrentHistoricPorts.put(crrTime, ports);
					}
					count = ports.get(port);
					if (count == null)
					{
						count = 0;
					}
					count++;
					ports.put(port, count);
				}
			}
			else
			{ // internal
				synchronized (mapCountInternal)
				{
					countInternal++;
					Integer count = mapCountInternal.get(crrTime);
					if (count == null)
					{
						count = 0;
					}
					count++;
					mapCountInternal.put(crrTime, count);
				}
			}
		}
	}
	/**
	 * @return the countInternal
	 */
	public int getCountInternal()
	{
		checkTimeframe();
		return countInternal;
	}
	/**
	 * @return the countExternal
	 */
	public int getCountExternal()
	{
		checkTimeframe();
		return countExternal;
	}
	/**
	 * @return the mapCountInternal
	 */
	public Map<Long, Integer> getMapCountInternal()
	{
		checkTimeframe();
		return mapCountInternal;
	}
	/**
	 * @return the mapCountExternal
	 */
	public Map<Long, Integer> getMapCountExternal()
	{
		checkTimeframe();
		return mapCountExternal;
	}
	/**
	 * @return the mapHistoricPorts
	 */
	public Map<Integer, Integer> getMapHistoricPorts()
	{
		checkTimeframe();
		return mapHistoricPorts;
	}
	/**
	 * @return the mapCurrentHistoricPorts
	 */
	public Map<Long, Map<Integer, Integer>> getMapCurrentHistoricPorts()
	{
		checkTimeframe();
		return mapCurrentHistoricPorts;
	}
	/** minimum timestamp of internal map. */
	private long minTSInternal = 0;
	/** minimum timestamp of external map. */
	private long minTSExternal = 0;
	/** minimum timestamp of ports map. */
	private long minTSPorts = 0;
	private void checkTimeframe()
	{
		long crrTime = System.currentTimeMillis() / 60000;
		if (minTSInternal + MAX_TIMEFRAME_IN_MINUTES < crrTime)
		{
			minTSInternal = crrTime;
			Iterator<Entry<Long, Integer>> itEntry = mapCountInternal.entrySet().iterator();
			while (itEntry.hasNext())
			{
				Entry<Long, Integer> entry = itEntry.next();
				if (entry.getKey() + MAX_TIMEFRAME_IN_MINUTES < crrTime)
				{
					itEntry.remove();
				}
				else
				{
					if (entry.getKey() < minTSInternal)
					{
						minTSInternal = entry.getKey();
					}
				}
			}
			if (mapCountInternal.isEmpty())
			{
				minTSInternal = Long.MAX_VALUE - MAX_TIMEFRAME_IN_MINUTES;
			}
		}
		if (minTSExternal + MAX_TIMEFRAME_IN_MINUTES < crrTime)
		{
			minTSExternal = crrTime;
			Iterator<Entry<Long, Integer>> itEntry = mapCountExternal.entrySet().iterator();
			while (itEntry.hasNext())
			{
				Entry<Long, Integer> entry = itEntry.next();
				if (entry.getKey() + MAX_TIMEFRAME_IN_MINUTES < crrTime)
				{
					itEntry.remove();
				}
				else
				{
					if (entry.getKey() < minTSExternal)
					{
						minTSExternal = entry.getKey();
					}
				}
			}
			if (mapCountExternal.isEmpty())
			{
				minTSExternal = Long.MAX_VALUE - MAX_TIMEFRAME_IN_MINUTES;
			}
		}
		if (minTSPorts + MAX_TIMEFRAME_IN_MINUTES < crrTime)
		{
			minTSPorts = crrTime;
			Iterator<Entry<Long, Map<Integer, Integer>>> itEntryPorts = mapCurrentHistoricPorts.entrySet().iterator();
			while (itEntryPorts.hasNext())
			{
				Entry<Long, Map<Integer, Integer>> entry = itEntryPorts.next();
				if (entry.getKey() + MAX_TIMEFRAME_IN_MINUTES < crrTime)
				{
					itEntryPorts.remove();
				}
				else
				{
					if (entry.getKey() < minTSPorts)
					{
						minTSPorts = entry.getKey();
					}
				}
			}
			if (mapCurrentHistoricPorts.isEmpty())
			{
				minTSPorts = Long.MAX_VALUE - MAX_TIMEFRAME_IN_MINUTES;
			}
		}
	}
}
