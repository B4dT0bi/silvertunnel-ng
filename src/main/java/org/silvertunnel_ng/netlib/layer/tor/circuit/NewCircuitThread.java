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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hapke
 */
public class NewCircuitThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(NewCircuitThread.class);

	private final TLSConnectionAdmin fnh;
	private final Directory dir;
	private final TCPStreamProperties spFinal;
	private final TorEventService torEventService;

	public NewCircuitThread(TLSConnectionAdmin fnh, Directory dir,
			TCPStreamProperties spFinal, TorEventService torEventService)
	{
		this.fnh = fnh;
		this.dir = dir;
		this.spFinal = spFinal;
		this.torEventService = torEventService;
	}

	@Override
	public void run()
	{
		try
		{
			new Circuit(fnh, dir, spFinal, torEventService);
		}
		catch (final Exception e)
		{
			LOG.warn("unexcpected", e);
		}
	}
}
