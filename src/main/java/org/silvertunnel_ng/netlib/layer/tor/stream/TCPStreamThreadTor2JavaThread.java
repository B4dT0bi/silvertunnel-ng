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
package org.silvertunnel_ng.netlib.layer.tor.stream;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.silvertunnel_ng.netlib.layer.tor.circuit.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CellRelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class contains a background thread that waits for incoming cells in a
 * TCPStream and makes them available to the Java-Application.
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @see TCPStreamThreadJava2TorThread
 */
class TCPStreamThreadTor2JavaThread extends Thread
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TCPStreamThreadTor2JavaThread.class);

	private final TCPStream stream;
	/** read from tor and output to this stream. */
	private PipedInputStream sin;
	/** private end of this pipe. */
	private PipedOutputStream fromtor;
	/** as stop() is depreacated we use this toggle variable. */
	private boolean stopped;

	TCPStreamThreadTor2JavaThread(final TCPStream stream)
	{
		this.stream = stream;
		try
		{
			sin = new SafePipedInputStream();
			fromtor = new PipedOutputStream(sin);
		}
		catch (final IOException e)
		{
			LOG.error("TCPStreamThreadTor2Java: caught IOException "
					+ e.getMessage());
		}
		this.start();
	}

	public void close()
	{
		this.stopped = true;
		this.interrupt();
	}

	@Override
	public void run()
	{
		while (!stream.isClosed() && !this.stopped)
		{
			final Cell cell = stream.queue.get();
			if (cell != null)
			{
				if (!cell.isTypeRelay())
				{
					LOG.error("TCPStreamThreadTor2Java.run(): stream "
							+ stream.getId() + " received NON-RELAY cell:\n"
							+ cell.toString());
				}
				else
				{
					final CellRelay relay = (CellRelay) cell;
					if (relay.isTypeData())
					{
						LOG.debug("TCPStreamThreadTor2Java.run(): stream {} received data", stream.getId());
						try
						{
							fromtor.write(relay.getData(), 0, relay.getLength());
						}
						catch (final IOException e)
						{
							LOG.error("TCPStreamThreadTor2Java.run(): caught IOException "
									+ e.getMessage());
						}
					}
					else if (relay.isTypeEnd())
					{
						if (LOG.isDebugEnabled())
						{
							LOG.debug("TCPStreamThreadTor2Java.run(): stream "
								+ stream.getId() + " is closed: "
								+ relay.getReasonForClosing());
						}
						stream.setClosedForReason((relay.getPayload()[0]) & 0xff);
						stream.setClosed(true);
						stream.close(true);
					}
					else
					{
						LOG.error("TCPStreamThreadTor2Java.run(): stream "
								+ stream.getId() + " received strange cell:\n"
								+ relay.toString());
					}
				}
			}
		}
	}
}
