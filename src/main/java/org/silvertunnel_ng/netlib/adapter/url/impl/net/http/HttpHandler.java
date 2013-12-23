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

package org.silvertunnel_ng.netlib.adapter.url.impl.net.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hapke
 */
public class HttpHandler extends Handler
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpHandler.class);

	/**
	 * TcpipNetLayer compatible layer; for class HttpsURLConnection: TLSNetLayer
	 * compatible layer.
	 */
	protected NetLayer netLayer;

	/**
	 * Create an instance.
	 * 
	 * @param netLayer
	 *            use this layer for network connections; should be compatible
	 *            with TcpipNetLayer; if null then prevent network connections
	 */
	public HttpHandler(final NetLayer netLayer)
	{
		this.netLayer = netLayer;
	}

	@Override
	public/* TODO: was protected */java.net.URLConnection openConnection(final URL u, final Proxy p) throws IOException
	{
		return new HttpURLConnection(netLayer, u, p, this);
	}

	public void setNetLayer(final NetLayer netLayer)
	{
		this.netLayer = netLayer;
	}

	/**
	 * Get the IP address of our host. An empty host field or a DNS failure will
	 * result in a null return.
	 * 
	 * @param u
	 *            a URL object
	 * @return an <code>InetAddress</code> representing the host IP address.
	 */
	@Override
	protected synchronized InetAddress getHostAddress(final URL u)
	{
		LOG.info("HttpHandler.getHostAddress(): do not determine correct address for security reasons - return null");

		// we must return here (and cannot throw an exception) to avoids problem
		// https://sourceforge.net/apps/trac/silvertunnel/ticket/127
		return null;

		/*
		 * if (u.hostAddress != null) return u.hostAddress;
		 * 
		 * String host = u.getHost(); if (host == null || host.equals("")) {
		 * return null; } else { try { u.hostAddress =
		 * InetAddress.getByName(host); } catch (UnknownHostException ex) {
		 * return null; } catch (SecurityException se) { return null; } } return
		 * u.hostAddress;
		 */
	}

	/**
	 * Compares the host components of two URLs.
	 * 
	 * @param url1
	 *            the URL of the first host to compare
	 * @param url2
	 *            the URL of the second host to compare
	 * @return <tt>true</tt> if and only if they are equal, <tt>false</tt>
	 *         otherwise.
	 */
	@Override
	protected boolean hostsEqual(final URL url1, final URL url2)
	{
		// if both have host names, compare them
		if (url1.getHost() != null && url2.getHost() != null)
		{
			return url1.getHost().equalsIgnoreCase(url2.getHost());
		}
		else
		{
			return url1.getHost() == null && url2.getHost() == null;
		}
	}
}
