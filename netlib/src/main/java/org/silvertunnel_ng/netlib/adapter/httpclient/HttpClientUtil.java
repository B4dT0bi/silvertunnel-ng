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
package org.silvertunnel_ng.netlib.adapter.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter to Apache HttpClient 4.x.
 * 
 * This is still very EXPERIMENTAL - not for serious use!!!
 * 
 * This code is currently not used and not needed outside of package
 * org.silvertunnel_ng.netlib.adapter.httpclient
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class HttpClientUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

	private static SchemeRegistry supportedSchemes;
//	private static ClientConnectionManager connMgr;
//	private static HttpParams params = new BasicHttpParams();

	private static NetLayer lowerNetLayer;

	// init
	static void init(final NetLayer lowerNetLayer)
	{
		try
		{
			HttpClientUtil.lowerNetLayer = lowerNetLayer;
			final Scheme http = new Scheme("http", new NetlibSocketFactory(lowerNetLayer), 80);

			supportedSchemes = new SchemeRegistry();
			supportedSchemes.register(http);

			// prepare parameters
			final HttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, Util.UTF8);
			HttpProtocolParams.setUseExpectContinue(httpParams, true);

//			connMgr = new ThreadSafeClientConnManager(httpParams, supportedSchemes);

		}
		catch (final Exception e)
		{
			LOG.error("error during class init", e);
		}
	}

	public static InputStream simpleAction(final URL url) throws IOException
	{
		final int port = url.getPort() < 0 ? 80 : url.getPort();
		final TcpipNetAddress httpServerNetAddress = new TcpipNetAddress(url.getHost(), port);
		final Map<String, Object> localProperties = new HashMap<String, Object>();
		final NetSocket lowerLayerNetSocket = lowerNetLayer.createNetSocket(localProperties, /* localAddress */null, httpServerNetAddress);
		String pathOnHttpServer = url.getPath();
		if (pathOnHttpServer == null || pathOnHttpServer.length() < 1)
		{
			pathOnHttpServer = "/";
		}
		final long timeoutInMs = 10L * 1000L;

		return HttpUtil.getInstance().getReponseBodyInputStream(lowerLayerNetSocket, httpServerNetAddress, pathOnHttpServer, timeoutInMs);
	}

	public static byte[] simpleBytesAction(final URL url) throws IOException
	{
		final int port = url.getPort() < 0 ? 80 : url.getPort();
		final TcpipNetAddress httpServerNetAddress = new TcpipNetAddress(url.getHost(), port);
		final Map<String, Object> localProperties = new HashMap<String, Object>();
		final NetSocket lowerLayerNetSocket = lowerNetLayer.createNetSocket(localProperties, /* localAddress */null, httpServerNetAddress);
		String pathOnHttpServer = url.getPath();
		if (pathOnHttpServer == null || pathOnHttpServer.length() < 1)
		{
			pathOnHttpServer = "/";
		}
		final long timeoutInMs = 10L * 1000L;

		HttpUtil.getInstance();
		return HttpUtil.get(lowerLayerNetSocket, httpServerNetAddress, pathOnHttpServer, timeoutInMs);
	}
}
