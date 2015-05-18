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

package org.silvertunnel_ng.netlib.adapter.url.impl.net.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import org.silvertunnel_ng.netlib.adapter.url.impl.net.http.HttpHandler;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hapke
 */
public class HttpsHandler extends HttpHandler
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpsHandler.class);

	/**
	 * Create an instance
	 * 
	 * @param netLayer
	 *            use this layer for network connections; should be compatible
	 *            with TLSNetLayer; if null then prevent network connections
	 */
	public HttpsHandler(NetLayer netLayer)
	{
		super(netLayer);
	}

	@Override
	public java.net.URLConnection openConnection(URL u, Proxy p)
			throws IOException
	{
		return new HttpsURLConnection(netLayer, u, this);
	}
}
