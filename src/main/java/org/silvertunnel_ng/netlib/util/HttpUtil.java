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

package org.silvertunnel_ng.netlib.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides simple methods to access HTTP - they can be used for
 * testing.
 * 
 * The class should not be used in productive environments (because these
 * methods do not handle all edge cases). Consider to use class HttpClient in
 * productive environments.
 * 
 * This method does NOT internally use java.net.URL.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class HttpUtil
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

	public static final String HTTPTEST_SERVER_NAME = "httptest.silvertunnel-ng.org";
	public static final int HTTPTEST_SERVER_PORT = 80;
	public static final TcpipNetAddress HTTPTEST_SERVER_NETADDRESS = new TcpipNetAddress(HTTPTEST_SERVER_NAME, HTTPTEST_SERVER_PORT);

	private static HttpUtil instance = new HttpUtil();

	public static HttpUtil getInstance()
	{
		return instance;
	}

	/**
	 * Try to execute the /httptest/smalltest.php over the provided net socket
	 * with a random id.
	 * 
	 * Closes the socket after the test.
	 * 
	 * @param lowerLayerNetSocket
	 *            this net socket will be closed inside the method
	 * @param idPrefix
	 *            only digits and ASCII letters, becomes part of the id sent to
	 *            the server
	 * @param timeoutInMs
	 * @return true=test OK; false=test failed
	 * @throws IOException
	 */
	public boolean executeSmallTest(NetSocket lowerLayerNetSocket,
			String idPrefix, long timeoutInMs) throws IOException
	{
		// generate the id
		final int randomNo = (int) (1000000000 * Math.random());
		final String id = idPrefix + randomNo;

		// communicate with the remote side
		final byte[] httpResponse = get(lowerLayerNetSocket,
				HttpUtil.HTTPTEST_SERVER_NETADDRESS,
				"/httptest/smalltest.php?id=" + id, timeoutInMs);

		// check response
		LOG.info("http response body: " + ByteArrayUtil.showAsString(httpResponse));
		final byte[] expectedResponse = ("<response><id>" + id + "</id></response>\n").getBytes(Util.UTF8);
		final boolean testOK = Arrays.equals(expectedResponse, httpResponse);
		if (testOK)
		{
			LOG.info("http response body = expected response body");
		}
		else
		{
			LOG.info("expected http response body is different: "
					+ ByteArrayUtil.showAsString(expectedResponse));
		}

		lowerLayerNetSocket.close();

		return testOK;
	}

	/**
	 * Execute a simple HTTP 1.1 request and read the response.
	 * 
	 * @param lowerNetLayer
	 * @param httpServerNetAddress
	 * @param pathOnHttpServer
	 * @param timeoutInMs
	 *            do not wait longer the the specified milliseconds
	 * @return
	 * @throws IOException
	 */
	public byte[] get(final NetLayer lowerNetLayer,
	                  final TcpipNetAddress httpServerNetAddress, 
	                  final String pathOnHttpServer,
	                  final long timeoutInMs) throws IOException
	{
		// open network connection
		final NetSocket s = lowerNetLayer.createNetSocket(null, null, httpServerNetAddress);

		return get(s, httpServerNetAddress, pathOnHttpServer, timeoutInMs);
	}

	/**
	 * Execute a simple HTTP 1.1 request and provide the response body as
	 * InputStream. The response header is not part of the returned InputStream.
	 * 
	 * @param lowerLayer
	 * @param httpServerNetAddress
	 * @param pathOnHttpServer
	 * @param timeoutInMs
	 *            do not wait longer the the specified milliseconds
	 * @return the response body
	 * @throws IOException
	 */
	public InputStream getReponseBodyInputStream(final NetSocket lowerLayerNetSocket,
	                                             final TcpipNetAddress httpServerNetAddress, 
	                                             final String pathOnHttpServer,
	                                             final long timeoutInMs) throws IOException
	{
		final byte[] responseBody = get(lowerLayerNetSocket, httpServerNetAddress, pathOnHttpServer, timeoutInMs);

		return new ByteArrayInputStream(responseBody);
	}

	/**
	 * Execute a simple HTTP 1.1 request and read the response.
	 * 
	 * @param lowerLayer
	 * @param httpServerNetAddress
	 * @param pathOnHttpServer
	 * @param timeoutInMs
	 *            do not wait longer than the specified milliseconds
	 * @return the response body
	 * @throws IOException
	 */
	public static byte[] get(final NetSocket lowerLayerNetSocket,
	                         final TcpipNetAddress httpServerNetAddress, 
	                         final String pathOnHttpServer,
	                         final long timeoutInMs) throws IOException
	{
		try
		{
			final String request = "GET " + pathOnHttpServer + " HTTP/1.1\n"
					+ "Host: " + getCleanHostname(httpServerNetAddress.getHostnameOrIpaddress())
					+ "\n" 
					// disable keep-alive
					+ "Connection: close\n" + "\n";
			final byte[] requestBytes = request.getBytes(Util.UTF8);

			// do the work
			return request(lowerLayerNetSocket, httpServerNetAddress, pathOnHttpServer, requestBytes, timeoutInMs);
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("this exception may never occur", e);
			throw new IOException(e.toString());
		}
	}
	/**
	 * Get a clean hostname.
	 * 
	 * removes the exit-node fingerprint and the .exit from hostname
	 * 
	 * @param hostname the hostname which needs to be cleaned
	 * @return the cleaned hostname
	 */
	private static String getCleanHostname(final String hostname)
	{
		if (hostname.endsWith(".exit"))
		{
			String tmp = hostname.substring(0, hostname.length() - 5);
			tmp = tmp.substring(0, tmp.lastIndexOf('.'));
			return tmp;
		}
		return hostname;
	}
	/**
	 * Execute a simple HTTP 1.1 post and read the response.
	 * 
	 * @param lowerLayer
	 * @param httpServerNetAddress
	 * @param pathOnHttpServer
	 * @param timeoutInMs
	 *            do not wait longer the the specified milliseconds
	 * @return the response body
	 * @throws IOException
	 */
	public byte[] post(final NetSocket lowerLayerNetSocket,
	                   final TcpipNetAddress httpServerNetAddress, 
	                   final String pathOnHttpServer,
	                   final byte[] dataToPost, 
	                   final long timeoutInMs) throws IOException
	{
		try
		{
			final String request = "POST " + pathOnHttpServer + " HTTP/1.1\r\n"
					+ "Host: " + httpServerNetAddress.getHostnameOrIpaddress()
					+ "\r\n" + "Content-Type: text/plain; charset=utf-8\r\n"
					+ "Content-Length: " + dataToPost.length + "\r\n" 
					// disable keep-alive
					+ "Connection: close\r\n" + "\r\n";
			final byte[] requestBytes1 = request.getBytes(Util.UTF8);
			final byte[] requestBytes = ByteArrayUtil.concatByteArrays(
					requestBytes1, dataToPost);

			// TODO - remove?:
			LOG.info("httpServerNetAddress=" + httpServerNetAddress + " with request=" + new String(requestBytes, Util.UTF8));

			// do the work
			final byte[] response = request(lowerLayerNetSocket,
					httpServerNetAddress, pathOnHttpServer, requestBytes,
					timeoutInMs);

			// result
			if (LOG.isDebugEnabled())
			{
				try
				{
					LOG.debug("response=" + new String(response, Util.UTF8));
				}
				catch (final Exception e)
				{
					LOG.debug("response=" + response);
				}
			}

			return response;
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("this exception may never occur", e);
			throw new IOException(e.toString());
		}
	}

	/**
	 * Execute a simple HTTP 1.1 get or post request and read the response.
	 * 
	 * @param lowerLayer
	 * @param httpServerNetAddress
	 * @param pathOnHttpServer
	 * @param timeoutInMs
	 *            do not wait longer the the specified milliseconds
	 * @return the response body
	 * @throws IOException
	 */
	private static byte[] request(final NetSocket lowerLayerNetSocket,
	                              final TcpipNetAddress httpServerNetAddress, 
	                              final String pathOnHttpServer,
	                              final byte[] requestBytes, 
	                              final long timeoutInMs) throws IOException
	{
		final long startTime = System.currentTimeMillis();

		// open network connection
		final NetSocket s = lowerLayerNetSocket;

		// receive HTTP response
		// (start the extra thread before sending the HTTP request
		// to avoid dead locks in certain circumstances)
		final HttpUtilResponseReceiverThread receiverThread = new HttpUtilResponseReceiverThread(s.getInputStream());

		// send HTTP request
		final OutputStream os = s.getOutputStream();
		try
		{
			LOG.info("send HTTP request now: " + ByteArrayUtil.showAsString(requestBytes));
			os.write(requestBytes);
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("this exception may never occur", e);
		}
		os.flush();

		receiverThread.start();
		// wait for receiving data
		final long millis = Math.max(100, timeoutInMs - (System.currentTimeMillis() - startTime));
		try
		{
			receiverThread.join(millis);
		}
		catch (final InterruptedException e)
		{
			// to ignore
			LOG.debug("got IterruptedException", e.getMessage());
		}

		// read the HTTP response from the other thread
		final byte[] response = receiverThread.readCurrentResultAndStopThread();
		s.close();
		if (LOG.isDebugEnabled())
		{
			try
			{
				LOG.debug("response=" + new String(response, Util.UTF8));
			}
			catch (final Exception e)
			{
				LOG.debug("response=" + response);
			}
		}

		// split response header and body
		int endOfHeaders = response.length;
		int startOfBody = response.length + 1;
		for (int i = 0; i < response.length; i++)
		{
			if (i + 1 < response.length && response[i] == '\n'
					&& response[i + 1] == '\n')
			{
				endOfHeaders = i;
				startOfBody = i + 2;
				break;
			}
			else if (i + 3 < response.length)
			{
				if (response[i] == '\n' && response[i + 1] == '\r'
						&& response[i + 2] == '\n' && response[i + 3] == '\r')
				{
					endOfHeaders = i;
					startOfBody = i + 4;
					break;
				}
				if (response[i] == '\r' && response[i + 1] == '\n'
						&& response[i + 2] == '\r' && response[i + 3] == '\n')
				{
					endOfHeaders = i;
					startOfBody = i + 4;
					break;
				}
			}
		}
		final byte[] responseHeaders = new byte[endOfHeaders];
		if (endOfHeaders > 0)
		{
			System.arraycopy(response, 0, responseHeaders, 0, endOfHeaders);
		}
		final int bodyLen = Math.max(0, response.length - startOfBody);
		byte[] responseBody = new byte[bodyLen];
		if (bodyLen > 0)
		{
			System.arraycopy(response, startOfBody, responseBody, 0, bodyLen);
		}

		// need to handle chunked HTTP response?
		final String responseHeadersAsString = ByteArrayUtil.showAsString(responseHeaders);
		final String CHUNKED_CONTENT_HEADER = "Transfer-Encoding: chunked";
		if (responseHeadersAsString.contains(CHUNKED_CONTENT_HEADER))
		{
			// yes: handle chunked response
			responseBody = decodeChunkedHttpResponse(responseBody);
		}

		// short log of results
		LOG.info("received HTTP response header: " + responseHeadersAsString);
		LOG.info("received HTTP response body of " + responseBody.length + " bytes");

		// result
		return responseBody;
	}

	/**
	 * Decode a chunked HTTP response.
	 * 
	 * @param chunkedResponse
	 * @return the decoded response
	 */
	protected static byte[] decodeChunkedHttpResponse(final byte[] chunkedResponse)
	{
		final List<Byte> result = new ArrayList<Byte>(chunkedResponse.length);
		StringBuffer chunkLenStr = new StringBuffer();
		for (int i = 0; i < chunkedResponse.length;)
		{
			// end of chunk header?
			final int offset = isNewLine(chunkedResponse, i);
			if (offset > 0)
			{
				// yes: end of chunk header
				// convert hex length value to int
				i += offset;
				final int HEX_RADIX = 16;
				int chunkLength = Integer.parseInt(chunkLenStr.toString(),
						HEX_RADIX);
				if (chunkLength == 0)
				{
					// found the end
					break;
				}
				else
				{
					for (; i < chunkedResponse.length && chunkLength > 0; i++, chunkLength--)
					{
						result.add(chunkedResponse[i]);
					}
					// prepare collecting the byte of the next chunk header
					chunkLenStr = new StringBuffer();
					i += isNewLine(chunkedResponse, i);
				}
			}
			else
			{
				// no: this is part of the chunk header
				chunkLenStr.append((char) chunkedResponse[i++]);
			}
		}

		// end reached: convert result
		final byte[] decodedChunkedHttpResponse = new byte[result.size()];
		for (int i = 0; i < decodedChunkedHttpResponse.length; i++)
		{
			decodedChunkedHttpResponse[i] = result.get(i);
		}
		return decodedChunkedHttpResponse;
	}

	/**
	 * Check whether the index points to a 1 or 2 byte long new line.
	 * 
	 * @param data
	 * @param index
	 * @return 1=1 byte new line; 2=2 byte new lin; 0=no new line in data at
	 *         position index
	 */
	private static int isNewLine(final byte[] data, final int index)
	{
		if (index + 1 < data.length
				&& ((data[index] == '\n' && data[index + 1] == '\r') || data[index] == '\r'
						&& data[index + 1] == '\n'))
		{
			// found 2 byte new line
			return 2;
		}
		else if (index < data.length && data[index] == '\n')
		{
			// line
			return 1;
		}
		else
		{
			return 0;
		}

	}

}
