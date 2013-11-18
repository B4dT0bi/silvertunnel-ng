/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel_ng.netlib.layer.tor.serverimpl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.circuit.TLSConnection;
import org.silvertunnel_ng.netlib.layer.tor.clientimpl.Tor;
import org.silvertunnel_ng.netlib.layer.tor.common.TorX509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main class for Tor server functionality
 * 
 * @author Lexi Pimenidis
 */
class ServerMain
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

	private static final String[] enabledSuites = {
			"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
			"TLS_DHE_RSA_WITH_AES_128_CBC_SHA" };
	private static final String filenameKeyStore = "/tmp";

	private final Tor tor;
	private Thread orListener;
	private SSLServerSocket orServer;
	/** reference to tls-connections in FirstNodeHandler!! */
	private Map<Fingerprint, TLSConnection> tlsConnectionMap;

	/**
	 * creates the TLS server socket and installs a dispatcher for incoming
	 * data.
	 * 
	 * @param orPort
	 *            the port to open for or-connections
	 * @param dirPort
	 *            the port to open for directory services
	 * @exception IOException
	 * @exception SSLPeerUnverifiedException
	 */
	ServerMain(Tor tor, int orPort, int dirPort) throws IOException,
			SSLPeerUnverifiedException, SSLException
	{
		this.tor = tor;

		if (orPort < 1)
		{
			throw new IOException("invalid port given");
		}
		if (dirPort < 1)
		{
			throw new IOException("invalid port given");
		}
		if (orPort > 0xffff)
		{
			throw new IOException("invalid port given");
		}
		if (dirPort > 0xffff)
		{
			throw new IOException("invalid port given");
		}

		// tlsConnectionMap = tor.getTlsConnectionAdmin().getConnectionMap();
		final KeyManager [] kms = new KeyManager[1];
		kms[0] = tor.getPrivateKeyHandler();

		// use the keys and certs from above to connect to Tor-network
		try
		{
			final TrustManager[] tms = { new TorX509TrustManager() };
			final SSLContext context = SSLContext.getInstance("TLS", "SunJSSE");
			context.init(kms, tms, null);
			final SSLServerSocketFactory factory = context
					.getServerSocketFactory();

			orServer = (SSLServerSocket) factory.createServerSocket(orPort);

			// FIXME: check certificates received in TLS

			/*
			 * // for debugging purposes
			 * javax.net.ssl.HandshakeCompletedListener hscl = new
			 * javax.net.ssl.HandshakeCompletedListener() { public void
			 * handshakeCompleted(HandshakeCompletedEvent e) { try {
			 * LOG.info("Cipher: "+e.getCipherSuite());
			 * java.security.cert.Certificate[] chain =
			 * e.getLocalCertificates(); LOG.info("Send cert-chain of length
			 * "+chain.length); for(int i=0;i<chain.length;++i)
			 * LOG.info(" cert "+i+": "+chain[i].toString()); chain =
			 * e.getPeerCertificates(); LOG.info("Recieved cert-chain of length
			 * "+chain.length); for(int i=0;i<chain.length;++i)
			 * LOG.info(" cert "+i+": "+chain[i].toString()); } catch(Exception
			 * ex) {} } }; tls.addHandshakeCompletedListener(hscl);
			 */
			orServer.setEnabledCipherSuites(enabledSuites);

			// start listening for incoming data
			orListener = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						while (true)
						{
							try
							{
								final SSLSocket ssl = (SSLSocket) (orServer.accept());
								ssl.setEnabledCipherSuites(enabledSuites);
								ssl.startHandshake();
								final TLSConnection tls = null; // TODO: new
								// TLSConnection(ssl);
								// add connection to array
								final String descr = ssl.getInetAddress()
										.getHostAddress() + ":" + ssl.getPort();
								LOG.debug("Incoming TLS connection from {}", descr);
								throw new RuntimeException(
										"currently not implemented correctly");
								// tlsConnectionMap.put(descr, tls);
							}
							catch (final SecurityException e)
							{
								LOG.debug("got SecurityException : {}", e.getMessage(), e);
							}
						}
					}
					catch (final IOException e)
					{
						LOG.debug("got IOException : {}", e.getMessage(), e);
					}
				}
			};
			orListener.start();

		}
		catch (final NoSuchProviderException e)
		{
			final SSLException e2 = new SSLException(e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
		catch (final NoSuchAlgorithmException e)
		{
			final SSLException e2 = new SSLException(e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
		catch (final KeyManagementException e)
		{
			final SSLException e2 = new SSLException(e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	/**
	 * @param force
	 *            set to TRUE if close anyway as fast as possible
	 */
	void close(boolean force)
	{
		LOG.debug("ServerMain.close(): Closing TLS server");

		// tls-connections are handled by FirstNodeHandler, no need to close
		// form here and there
		// close connections
		try
		{
			orServer.close();
		}
		catch (final IOException e)
		{
			LOG.debug("got IOException : {}", e.getMessage(), e);
		}
		// join thread listening on server port
		try
		{
			orListener.join();
		}
		catch (final InterruptedException e)
		{
			LOG.debug("got IterruptedException : {}", e.getMessage(), e);
		}
	}
}
