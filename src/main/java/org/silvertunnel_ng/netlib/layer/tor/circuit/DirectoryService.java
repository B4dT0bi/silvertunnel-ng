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

import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;

/**
 * Directory logic provided to circuits.
 * 
 * @author hapke
 */
public class DirectoryService
{
	/**
	 * checks whether the given circuit is compatible to the given restrictions.
	 * 
	 * @param circ
	 *            a circuit
	 * @param sp
	 *            the requirements to the route
	 * @param forHiddenService
	 * @return the boolean result
	 */
	public static boolean isCompatible(final Directory directory, 
	                                   final Circuit circ,
	                                   final TCPStreamProperties sp, 
	                                   final boolean forHiddenService)
	                                		   throws TorException
	{
		final Router[] routeCopy = new Router[circ.getRouteNodes().length];
		for (int i = 0; i < circ.getRouteNodes().length; ++i)
		{
			routeCopy[i] = circ.getRouteNodes()[i].getRouter();
		}
		if (forHiddenService)
		{
			// it is not allowed to use circuits that have already been used for
			// smth else
			if (circ.getStreamHistory() == null)
			{
				if (circ.getStreams() == null
						|| (circ.getStreams().size() == 1
								&& circ.getServiceDescriptor() != null 
								&& sp.getHostname().contains(circ.getServiceDescriptor().getURL())))
				{
					return directory.isCompatible(routeCopy, sp, forHiddenService);
				}
			}
		}
		else
		{
			// check for exit policies of last node
			if (circ.getServiceDescriptor() == null && !circ.isUsedByHiddenServiceToConnectToIntroductionPoint())
			{
				return directory.isCompatible(routeCopy, sp, forHiddenService);
			}
		}
		return false;
	}
}
