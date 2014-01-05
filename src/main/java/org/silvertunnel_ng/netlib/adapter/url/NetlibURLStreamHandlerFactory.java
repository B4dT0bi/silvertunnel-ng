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

package org.silvertunnel_ng.netlib.adapter.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import org.silvertunnel_ng.netlib.adapter.url.impl.net.http.HttpHandler;
import org.silvertunnel_ng.netlib.adapter.url.impl.net.https.HttpsHandler;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.layer.tls.TLSNetLayer;

/**
 * 
 * See class UrlUtil.
 * 
 * @author hapke
 */
public class NetlibURLStreamHandlerFactory implements URLStreamHandlerFactory
{
	private static final String PROTOCOL_HTTP = "http";
	private static final String PROTOCOL_HTTPS = "https";
//	private static final String PROTOCOL_FTP = "ftp";

	/**
	 * key=protocol (lower case) value=handler of the protocol, inclusive
	 * reference to lower layer.
	 */
	private Map<String, URLStreamHandler> handlers;

	/**
	 * true=do not allow access to other protocols; false=allow usage of
	 * JDK-built-in handler for unspecified protocols.
	 */
	private final boolean prohibitAccessForOtherProtocols;

	/**
	 * Create an instance without any handler, do not prohibit access for other
	 * protocols.
	 * 
	 * Call setNetLayerForXXX() later to initialize handlers.
	 */
	public NetlibURLStreamHandlerFactory()
	{
		this(false);
	}

	/**
	 * Create an instance without any handler.
	 * 
	 * Call setNetLayerForXXX() later to initialize handlers.
	 * 
	 * @param prohibitAccessForOtherProtocols
	 */
	public NetlibURLStreamHandlerFactory(final boolean prohibitAccessForOtherProtocols)
	{
		this(new HashMap<String, URLStreamHandler>(), prohibitAccessForOtherProtocols);
	}

	/**
	 * Create an instance.
	 * 
	 * @param handlers
	 *            key=protocol (lower case), value=NetLayer of the protocol
	 * @param prohibitAccessForOtherProtocols
	 */
	public NetlibURLStreamHandlerFactory(final Map<String, URLStreamHandler> handlers, final boolean prohibitAccessForOtherProtocols)
	{
		this.handlers = handlers;
		this.prohibitAccessForOtherProtocols = prohibitAccessForOtherProtocols;

	}

	/**
	 * Create an instance with the common NetLayer which is used for protocols
	 * http, https and ftp.
	 * 
	 * Other protocols are not supported and not allowed by this factory.
	 * 
	 * @param tcpipNetLayer
	 *            TcpipNetLayer compatible layer used for http; if null then
	 *            prevent network connections
	 * @param tlsNetLayer
	 *            TLSNetLayer compatible layer used for https; if null then
	 *            prevent network connections
	 * @param prohibitAccessForOtherProtocols
	 */
	public NetlibURLStreamHandlerFactory(final NetLayer tcpipNetLayer, final NetLayer tlsNetLayer, final boolean prohibitAccessForOtherProtocols)
	{
		setNetLayerForHttpHttpsFtp(tcpipNetLayer, tlsNetLayer);
		this.prohibitAccessForOtherProtocols = prohibitAccessForOtherProtocols;
	}

	/**
	 * Create an instance with the common NetLayer which is used for protocols
	 * http, https and ftp.
	 * 
	 * Other protocols are not supported and not allowed by this factory.
	 * 
	 * @param tcpipNetLayer
	 *            TcpipNetLayer compatible layer used for http; on top of this
	 *            layer a TLSNetLayer will be created for https; if null then
	 *            prevent network connections
	 * @param prohibitAccessForOtherProtocols
	 */
	public NetlibURLStreamHandlerFactory(final NetLayer tcpipNetLayer, final boolean prohibitAccessForOtherProtocols)
	{
		setNetLayerForHttpHttpsFtp(tcpipNetLayer);
		this.prohibitAccessForOtherProtocols = prohibitAccessForOtherProtocols;
	}

	/**
	 * Change the common NetLayer which is used for protocols http, https (maybe
	 * in the future: and ftp).
	 * 
	 * @param tcpipNetLayer
	 *            TcpipNetLayer compatible layer used for http; if null then
	 *            prevent network connections
	 * @param tlsNetLayer
	 *            TLSNetLayer compatible layer used for https; if null then
	 *            prevent network connections
	 */
	public final synchronized void setNetLayerForHttpHttpsFtp(final NetLayer tcpipNetLayer, final NetLayer tlsNetLayer)
	{
		if (handlers == null)
		{
			// set handlers
			handlers = new HashMap<String, URLStreamHandler>();
		}

		// HTTP
		URLStreamHandler handler = handlers.get(PROTOCOL_HTTP);
		if (handler != null && handler instanceof HttpHandler)
		{
			// change lower net layers
			((HttpHandler) handler).setNetLayer(tcpipNetLayer);
		}
		else
		{
			// initialize new handler
			handlers.put(PROTOCOL_HTTP, new HttpHandler(tcpipNetLayer));
		}
		// HTTPS
		handler = handlers.get(PROTOCOL_HTTPS);
		if (handler != null && handler instanceof HttpHandler)
		{
			// change lower net layers
			((HttpsHandler) handler).setNetLayer(tlsNetLayer);
		}
		else
		{
			// initialize new handler
			handlers.put(PROTOCOL_HTTPS, new HttpsHandler(tlsNetLayer));
		}

		// TODO: add further protocols e.g. file, ftp, gopher, jar, mailto,
		// netdoc

	}

	/**
	 * Change the common NetLayer which is used for protocols http, https (maybe
	 * in the future: and ftp).
	 * 
	 * @param tcpipNetLayer
	 *            TcpipNetLayer compatible layer used for http; on top of this
	 *            layer a TLSNetLayer will be created for https; if null then
	 *            prevent network connections
	 */
	public void setNetLayerForHttpHttpsFtp(final NetLayer tcpipNetLayer)
	{
		setNetLayerForHttpHttpsFtp(tcpipNetLayer, new TLSNetLayer(tcpipNetLayer));
	}

	/**
	 * Get or create a proper handler.
	 * 
	 * @param protocol
	 * @return the handler for the protocol; null if no handler could be find
	 */
	@Override
	public URLStreamHandler createURLStreamHandler(final String protocol)
	{
		// determine the handler
		URLStreamHandler result;
		synchronized (this)
		{
			result = handlers.get(protocol);
		}
		if (result != null)
		{
			return result;
		}

		// could not find netLayer or handler
		if (prohibitAccessForOtherProtocols)
		{
			// prohibit connections
			return new InvalidURLStreamHandler();
		}
		else
		{
			// allow the caller to decide to take another handler, e.g. a JDK
			// built-in handler
			return null;
		}
	}
}
