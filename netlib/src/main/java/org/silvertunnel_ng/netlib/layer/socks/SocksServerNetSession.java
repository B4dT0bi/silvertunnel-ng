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

package org.silvertunnel_ng.netlib.layer.socks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.silvertunnel_ng.netlib.api.NetAddress;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.DataNetSocket;
import org.silvertunnel_ng.netlib.api.impl.DataNetSocketPair;
import org.silvertunnel_ng.netlib.api.impl.DataNetSocketUtil;
import org.silvertunnel_ng.netlib.api.impl.DataNetSocketWrapper;
import org.silvertunnel_ng.netlib.api.impl.InterconnectUtil;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO: This implementation could have the general problem that some
 * DataInputStream.read() operations expect a minimum number of returned bytes.
 * This will usually work but it is not guaranteed by the API specification. TO
 * CHECK.
 * 
 * @author hapke
 */
public class SocksServerNetSession implements Runnable
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(SocksServerNetSession.class);

	private final NetLayer lowerNetLayer;

	private DataNetSocket higherLayerSocketExported;

	/** inverted version of higherLayerSocketExported. */
	private DataNetSocket higherLayerSocketInternallyUsed;

	private DataNetSocket lowerLayerSocket;

	private DataInputStream socksIn;
	private DataOutputStream socksOut;
	private DataInputStream lowerIn;
	private DataOutputStream lowerOut;

	/** maximum length, compatible with Tor cell payload length. */
	static final int BUFFER_SIZE = 498;

	private static long id;

	public SocksServerNetSession(final NetLayer lowerNetLayer,
								 final Map<String, Object> localProperties, 
								 final NetAddress localAddress,
								 final NetAddress remoteAddress)
	{
		this.lowerNetLayer = lowerNetLayer;
	}

	public NetSocket createHigherLayerNetSocket() throws IOException
	{
		if (higherLayerSocketExported != null)
		{
			throw new IllegalStateException("cannot create multiple sockets for one session");
		}

		// create new socket
		final DataNetSocketPair dataNetSocketPair = DataNetSocketUtil.createDataNetSocketPair();
		higherLayerSocketExported = dataNetSocketPair.getSocket();
		higherLayerSocketInternallyUsed = dataNetSocketPair.getInvertedSocked();

		// init short cuts for streams
		socksIn = higherLayerSocketInternallyUsed.getDataInputStream();
		socksOut = higherLayerSocketInternallyUsed.getDataOutputStream();

		// start background processing
		new Thread(this, createUniqueThreadName()).start();

		return higherLayerSocketExported;
	}

	/**
	 * Perform the protocol processing of this layer - in an extra thread.
	 */
	@Override
	public void run()
	{
		try
		{
			// read socks-version
			final byte[] version = new byte[1];
			socksIn.read(version, 0, 1);
			// parse command
			if (version[0] == 4)
			{
				processSocks4Connection();
			}
			else if (version[0] == 5)
			{
				processSocks5Connection();
			}
			else
			{
				// prepare answer
				final byte[] answer = new byte[2];
				answer[0] = 0;
				answer[1] = 91; // failed
				socksOut.write(answer);
				socksOut.flush();
				throw new Exception("only support for Socks-4(a)/5");
			}
		}
		catch (final Exception e)
		{
			LOG.warn("got Exception", e);
		}
		finally
		{
			LOG.debug("{} closing down", id);
			// TODO: try{ local.close(); }catch(Exception e){};
		}

	}

	public void close()
	{
		// TODO
	}

	private void processSocks5Connection()
	{
		LOG.debug("processSocks5Connection(): start");

		byte[] methods;
		byte[] command = new byte[8];
		byte[] answer = new byte[2];

		try
		{
			answer[0] = 5;
			// read methods
			socksIn.read(command, 0, 1);
			if (command[0] <= 0)
			{
				// error
				answer[1] = (byte) 0xff;
				socksOut.write(answer);
				socksOut.flush();
				throw new Exception("number of supported methods must be >0");
			}
			methods = new byte[command[0]];
			socksIn.readFully(methods);
			// check for anonymous/unauthenticated connection
			boolean foundAnonymous = false;
			for (int i = 0; i < methods.length; ++i)
			{
				foundAnonymous = foundAnonymous || (methods[i] == 0);
			}
			if (!foundAnonymous)
			{
				// error
				answer[1] = (byte) 0xff;
				socksOut.write(answer);
				socksOut.flush();
				throw new Exception("no accepted method listed by client");
			}
			// ok, we can tell the client to connect without username/password
			answer[1] = 0;
			socksOut.write(answer);
			socksOut.flush();
			// read and parse client request
			command = new byte[4];
			socksIn.readFully(command);
			if (command[0] != 5)
			{
				throw new Exception("why the f*** does the client change its version number?");
			}
			if (command[1] != 1)
			{
				throw new Exception("only CONNECT supported");
			}
			if (command[2] != 0)
			{
				throw new Exception("do not play around with reserved fields");
			}
			if ((command[3] != 1) && (command[3] != 3))
			{
				throw new Exception("only IPv4 and HOSTNAME supported");
			}
			// parse address
			String hostname = null;
			byte[] address;
			if (command[3] == 1)
			{
				// IPv4 address
				address = new byte[4];
				socksIn.readFully(address);
			}
			else
			{
				// hostname
				final byte[] lenInfo = new byte[1];
				socksIn.readFully(lenInfo);
				address = new byte[(256 + lenInfo[0]) & 0xff];
				socksIn.readFully(address);
				hostname = new String(address);
			}
			// read port
			final byte[] port = new byte[2];
			socksIn.readFully(port);
			final int intPort = (((port[0]) & 0xff) << 8) + ((port[1]) & 0xff);

			// combine address/hostname + port
			NetAddress remoteAddress;
			if (hostname != null)
			{
				remoteAddress = new TcpipNetAddress(hostname, intPort);
			}
			else
			{
				remoteAddress = new TcpipNetAddress(address, intPort);
			}

			// send reply to client
			final List<Byte> answerL = new ArrayList<Byte>();
			// answer = new byte[6+address.length];
			answerL.add((byte) 5); // version
			answerL.add((byte) 0); // success
			answerL.add((byte) 0); // reserved
			answerL.add(command[3]);
			if (hostname != null)
			{
				answerL.add((byte) address.length); // hostname length
			}
			for (int i = 0; i < address.length; i++)
			{
				answerL.add(address[i]);
			}
			answerL.add(port[0]); // port
			answerL.add(port[1]); // port
			// to array
			answer = new byte[answerL.size()];
			for (int i = 0; i < answer.length; i++)
			{
				answer[i] = answerL.get(i);
			}
			socksOut.write(answer);
			socksOut.flush();

			// create lower layer connection
			lowerLayerSocket = new DataNetSocketWrapper(lowerNetLayer.createNetSocket(null, null, remoteAddress));
			lowerIn = lowerLayerSocket.getDataInputStream();
			lowerOut = lowerLayerSocket.getDataOutputStream();

			// copy the rest of the streams
			InterconnectUtil.relay(socksIn, lowerOut, lowerIn, socksOut, BUFFER_SIZE);

			LOG.debug("processSocks5Connection(): end");

		}
		catch (final Exception e)
		{
			LOG.error("unexpected end", e);
		}
		finally
		{
			// TODO: close???
		}
	}

	private void processSocks4Connection()
	{
		throw new UnsupportedOperationException("socks4 is currently not supported");
		/*
		 * TODO Logger.logStream(Logger.VERBOSE,
		 * "SocksConnection.socks4(): start");
		 * 
		 * TCPStream remote=null; Socket remoteS=null; byte[] command = new
		 * byte[8]; byte[] answer = new byte[8];
		 * 
		 * try{ // read socks-command socksIn.read(command,1,1); if
		 * (command[1]!=1) { answer[1] = 91; // failed socksOut.write(answer);
		 * socksOut.flush(); throw new Exception("only support for CONNECT"); }
		 * // read port and IP for Socks4 socksIn.read(command,2,6); byte[]
		 * rawIp = new byte[4]; System.arraycopy(command,4,rawIp,0,4); int port
		 * = ((((int)command[2])&0xff)<<8) + (((int)command[3])&0xff); // read
		 * user name (and throw away) while(socksIn.readByte()!=0) {} // check
		 * for SOCKS4a if
		 * ((rawIp[0]==0)&&(rawIp[1]==0)&&(rawIp[2]==0)&&(rawIp[3]!=0)) {
		 * StringBuffer sb = new StringBuffer(256); byte b; do { b =
		 * socksIn.readByte(); if (b!=0) sb.append((char)b); } while(b!=0);
		 * String hostname = sb.toString(); // connect if (MainWindow.useTor())
		 * remote = TorKeeper.getTor().connect(new
		 * TCPStreamProperties(hostname,port)); else remoteS = new
		 * Socket(hostname,port); } else { // SOCKS 4 InetAddress inAddr =
		 * InetAddress.getByAddress(rawIp); if (MainWindow.useTor()) remote =
		 * TorKeeper.getTor().connect(new TCPStreamProperties(inAddr,port));
		 * else remoteS = new Socket(inAddr,port); } // send OK for socks4
		 * socksOut.write(answer); // MAIN DATA TRANSFERCOPY LOOP if
		 * (MainWindow.useTor()) relay(local,new
		 * DataInputStream(remote.getInputStream()),new
		 * DataOutputStream(remote.getOutputStream()), remote); else
		 * relay(local,new DataInputStream(remoteS.getInputStream()),new
		 * DataOutputStream(remoteS.getOutputStream()), null); } catch(Exception
		 * e) { LOG.error(e); } finally{ if(remote!=null) remote.close();
		 * if(remoteS!=null){ try{ remoteS.close(); } catch(Exception e){};} }
		 */
	}

	/**
	 * @return a new unique name for a thread
	 */
	protected static synchronized String createUniqueThreadName()
	{
		id++;
		return SocksServerNetSession.class.getName() + id + "-" + Thread.currentThread().getName();
	}
}
