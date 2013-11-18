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
package org.silvertunnel_ng.netlib.layer.tor.common;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register TorEventHandler and fire TorEvents.
 * 
 * @author hapke
 */
public class TorEventService
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorEventService.class);

	private final Collection<TorEventHandler> eventHandlers = new ArrayList<TorEventHandler>();

	public void registerEventHandler(final TorEventHandler eventHandler)
	{
		eventHandlers.add(eventHandler);
	}

	public boolean removeEventHandler(final TorEventHandler eventHandler)
	{
		return eventHandlers.remove(eventHandler);
	}

	/**
	 * Fire the event - in all registered handlers.
	 * 
	 * @param event
	 */
	public void fireEvent(final TorEvent event)
	{
		for (final TorEventHandler eventHandler : eventHandlers)
		{
			try
			{
				eventHandler.fireEvent(event);
			}
			catch (final Exception e)
			{
				LOG.warn("TorEventService.fireEvent()", e);
			}
		}
	}
}
