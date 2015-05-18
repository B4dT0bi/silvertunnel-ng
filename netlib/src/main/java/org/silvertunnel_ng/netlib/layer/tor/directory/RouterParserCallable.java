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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import java.util.concurrent.Callable;

import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this Callable is used for running the Routerparsing in parallel.
 *
 * @author Tobias Boese
 */
public final class RouterParserCallable implements Callable<Router>
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(RouterParserCallable.class);
	/** the router descriptor. */
	private final transient String descriptor;
	/**
	 * A Callable which will parse a given router descriptor 
	 * and returning the RouterImpl.
	 * @param descriptor the router descriptor
	 */
	public RouterParserCallable(final String descriptor)
	{
		this.descriptor = descriptor;
	}

	@Override
	public Router call() throws TorException
	{
		try
		{
			// parse and store a single router
			return new RouterImpl(descriptor);
		}
		catch (final TorException e)
		{
			LOG.info("got TorException while parsing RouterDescriptor", e);
		}
		return null;
	}

}
