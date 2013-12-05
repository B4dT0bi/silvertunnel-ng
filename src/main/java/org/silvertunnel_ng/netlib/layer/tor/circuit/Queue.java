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
package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.io.IOException;
import java.util.Vector;

import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.TorNoAnswerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a helper class for queueing data (FIFO).
 * 
 * @author Lexi Pimenidis
 */
public final class Queue
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Queue.class);

	private static final int WAIT = 100;

	private volatile boolean closed = false;
	private volatile boolean addClosed = false;
	/** timeout internally represented in ms. */
	private int timeoutMs = 1000;
	private final Vector<Cell> queue = new Vector<Cell>();
	private final Vector<QueueHandler> handler = new Vector<QueueHandler>();

	/**
	 * init class.
	 * 
	 * @param timeoutS
	 *            queue timeout in seconds
	 */
	public Queue(final int timeoutS)
	{
		this.timeoutMs = timeoutS * 1000;
	}

	public Queue()
	{
		this(1000);
	}

	public synchronized void addHandler(final QueueHandler qh)
	{
		handler.add(qh);
	}

	public synchronized boolean removeHandler(final QueueHandler qh)
	{
		return handler.remove(qh);
	}

	/** add a cell to the queue. */
	public synchronized void add(final Cell cell)
	{
		if (addClosed)
		{
			return;
		}
		/* first check if there are handlers installed */
		try
		{
			for (final QueueHandler qh : handler)
			{
				try
				{
					if (qh.handleCell(cell))
					{
						return;
					}
				}
				catch (final TorException te)
				{ /* die silently */
					LOG.debug("got TorException : {}", te.getMessage(), te);
				}
			}
		}
		catch (final ClassCastException e)
		{
			LOG.debug("got ClassCastException : {}", e.getMessage(), e);
		}

		// otherwise add to queue
		queue.add(cell);

		this.notify();
	}

	/**
	 * close the queue and remove all pending messages.
	 */
	public synchronized void close()
	{
		addClosed = true;
		closed = true;

		for (final QueueHandler qh : handler)
		{
			qh.close();
		}

		queue.clear();
		this.notify();
	}

	/**
	 * prohibit further writing to the queue.
	 */
	public synchronized void closeAdd()
	{
		addClosed = true;
		this.notify();
	}

	/** determines whether the queue is empty. */
	boolean isEmpty()
	{
		if (closed)
		{
			return true;
		}
		return queue.size() == 0;
	}

	public Cell get()
	{
		return get(timeoutMs);
	}

	/**
	 * get the first element from out of the class. Behaviour
	 * 
	 * @param timeout
	 *            determines what will happen, if no data is in queue.
	 * @return a Cell or null
	 */
	public synchronized Cell get(final int timeout)
	{

		boolean forever = false;
		if (timeout == -1)
		{
			forever = true;
		}

		int retries = timeout / WAIT;
		do
		{

			if (closed)
			{
				return null;
			}

			if (queue.size() > 0)
			{
				final Cell cell = queue.get(0);
				queue.remove(0);
				return cell;
			}
			else if (addClosed)
			{
				closed = true;
				return null;
			}

			try
			{
				// wait for data
				wait(WAIT);
			}
			catch (final InterruptedException e)
			{
				LOG.debug("got InterruptedException : {}", e.getMessage(), e);
			}
			--retries;
		}
		while (forever || (retries > 0) || (queue.size() > 0));

		return null;
	}

	/**
	 * interface to receive a cell that is not a relay-cell.
	 */
	public Cell receiveCell(final int type) throws IOException, TorException,
			TorNoAnswerException
	{
		final Cell cell = get();
		if (cell == null)
		{
			throw new TorNoAnswerException(
					"Queue.receiveCell: connection closed or no answer after "
							+ this.timeoutMs / 1000 + " s");
		}
		if (cell.getCommand() != type)
		{
			throw new TorException("Queue.receiveCell: expected cell of type "
					+ Cell.type(type) + " received type " + cell.type());
		}
		// if (cell.command == Cell.CELL_RELAY)
		// Tor.LOG.logCell(Logger.WARNING,"used from interface for receiving a
		// cell");
		return cell;
	}

	/**
	 * Receive a special {@link CellRelay}.
	 * @param type the type of the RELAY-cell to be received
	 * @return a {@link CellRelay} if received correctly
	 * @throws TorNoAnswerException
	 * @throws IOException
	 * @throws TorException
	 */
	public CellRelay receiveRelayCell(final int type) throws IOException,
			TorException, TorNoAnswerException
	{
		final CellRelay relay = (CellRelay) receiveCell(Cell.CELL_RELAY);
		if (relay.getRelayCommand() != type)
		{

			if ((relay.getRelayCommand() == CellRelay.RELAY_END)
					&& (relay.getData() != null))
			{
				throw new TorException(
						"Queue.receiveRelayCell: expected relay-cell of type "
								+ CellRelay.getRelayCommandAsString(type)
								+ ", received END-CELL for reason: "
								+ relay.getReasonForClosing());
			}
			else
			{
				if (relay.isTypeTruncated())
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Queue.receiveRelayCell: expected relay-cell of type "
							+ CellRelay.getRelayCommandAsString(type)
							+ " received type "
							+ relay.getRelayCommandAsString()
							+ " reason : "
							+ relay.getReasonForTruncated());
					}
					throw new TorException(
							"Queue.receiveRelayCell: expected relay-cell of type "
									+ CellRelay.getRelayCommandAsString(type)
									+ " received type "
									+ relay.getRelayCommandAsString()
									+ " reason : "
									+ relay.getReasonForTruncated());
				}
				else
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Queue.receiveRelayCell: expected relay-cell of type "
							+ CellRelay.getRelayCommandAsString(type)
							+ " received type "
							+ relay.getRelayCommandAsString());
					}
					throw new TorException(
							"Queue.receiveRelayCell: expected relay-cell of type "
									+ CellRelay.getRelayCommandAsString(type)
									+ " received type "
									+ relay.getRelayCommandAsString());
				}
			}
		}
		else
		{
			LOG.debug("got correct type.");
		}
		return relay;
	}

	// /////////////////////////////////////////////////////
	// getters and setters
	// /////////////////////////////////////////////////////

	public boolean isClosed()
	{
		return closed;
	}

	public int getTimeoutMs()
	{
		return timeoutMs;
	}

	public void setTimeoutMs(int timeoutMs)
	{
		this.timeoutMs = timeoutMs;
	}

}
