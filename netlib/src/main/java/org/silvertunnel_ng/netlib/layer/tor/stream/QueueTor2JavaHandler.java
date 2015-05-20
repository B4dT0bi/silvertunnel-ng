/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * <br>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.silvertunnel_ng.netlib.layer.tor.stream;

import org.silvertunnel_ng.netlib.layer.tor.circuit.QueueHandler;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * used to be TCPStreamThreadTor2Java.
 */
class QueueTor2JavaHandler implements QueueHandler {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(QueueTor2JavaHandler.class);

    private final TCPStream stream;
    /** read from tor and output to this stream. */
    private PipedInputStream sin;
    /** private end of this pipe. */
    private PipedOutputStream fromtor;
    /** as stop() is depreciated we use this toggle variable. */
    private boolean stopped;

    QueueTor2JavaHandler(final TCPStream stream) {
        this.stream = stream;
        try {
            sin = new SafePipedInputStream();
            fromtor = new PipedOutputStream(sin);
        } catch (final IOException e) {
            LOG.error("QueueTor2JavaHandler: caught IOException " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        this.stopped = true;
        /* leave data around, until no more referenced by someone else */
        // try{ sin.close(); } catch(Exception e) {}
        try {
            fromtor.close();
        } catch (final Exception e) {
            LOG.debug("got Exception : {}", e.getMessage(), e);
        }
    }

    /** return TRUE, if cell was handled. */
    @Override
    public boolean handleCell(final Cell cell) throws TorException {
        if (stream.isClosed() || this.stopped) {
            return false;
        }
        if (cell == null) {
            return false;
        }
        if (!cell.isTypeRelay()) {
            return false;
        }

        final CellRelay relay = (CellRelay) cell;
        if (relay.isTypeData()) {
            LOG.debug("QueueTor2JavaHandler.handleCell(): stream {} received data", stream.getId());
            try {
                fromtor.write(relay.getData(), 0, relay.getLength());
            } catch (final IOException e) {
                LOG.error("QueueTor2JavaHandler.handleCell(): caught IOException " + e.getMessage(), e);
            }
            return true;
        } else if (relay.isTypeEnd()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("QueueTor2JavaHandler.handleCell(): stream "
                        + stream.getId() + " is closed: "
                        + relay.getReasonForClosing());
            }
            stream.setClosedForReason((relay.getPayload()[0]) & 0xff);
            stream.setClosed(true);
            stream.close(true);
            this.stopped = true;
            return true;
        }
        return false;
    }

    public InputStream getInputStream() {
        return sin;
    }
}
