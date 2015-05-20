/*
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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.Cell;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellDestroy;
import org.silvertunnel_ng.netlib.layer.tor.circuit.cells.CellRelay;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * reads data arriving at the TLS connection and dispatches it to the
 * appropriate circuit or stream that it belongs to.
 *
 * @author Lexi Pimenidis
 * @author hapke
 * @author Tobias Boese
 */
class TLSDispatcherThread extends Thread {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TLSDispatcherThread.class);

    private final DataInputStream sin;
    private final TLSConnection tls;
    private boolean stopped;

    TLSDispatcherThread(final TLSConnection tls, final DataInputStream sin) {
        this.tls = tls;
        this.sin = sin;
        this.setName("TLSDispatcher for " + tls.getRouter().getNickname());
        this.start();
    }

    public void close() {
        this.stopped = true;
        this.interrupt();
    }

    @Override
    public void run() {
        boolean dispatched = false;
        while (!stopped) {

            // read next data-packet
            Cell cell = null;
            try {
                cell = new Cell(sin);
            } catch (final IOException e) {
                if (e instanceof SocketTimeoutException) {
                    LOG.debug("TLSDispatcher.run: {} connection error: socket timeout", this.getName(), e);
                    continue; // SocketTimeout should not be a showstopper here
                } else {
                    LOG.info("TLSDispatcher.run: connection error: " + e.getMessage(), e);
                }
                stopped = true;
                break;
            }
            // padding cell?
            if (cell.isTypePadding()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("TLSDispatcher.run: padding cell from {}", tls.getRouter().getNickname());
                }
            } else {
                dispatched = false;
                final int cellCircId = cell.getCircuitId();
                // dispatch according to circID
                final Circuit circ = tls.getCircuit(cellCircId);
                if (circ != null) {
                    // check for destination in circuit
                    if (cell.isTypeRelay()) {
                        CellRelay relay = null;
                        try {
                            // found a relay-cell! Try to strip off
                            // symmetric encryption and check the content
                            relay = new CellRelay(circ, cell);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("relay.getRelayCommandAsString()="
                                        + relay.getRelayCommandAsString());
                            }

                            // dispatch to stream, if a stream-ID is given
                            final int streamId = relay.getStreamId();
                            if (streamId != 0) {
                                final Stream stream = circ.getStreams().get(streamId);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("dispatch to stream with streamId="
                                            + streamId + ", stream=" + stream);
                                }
                                if (stream != null) {
                                    dispatched = true;
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("TLSDispatcher.run: data from "
                                                + tls.getRouter().getNickname()
                                                + " dispatched to circuit "
                                                + circ.getId()
                                                + "/stream "
                                                + streamId);
                                    }
                                    stream.processCell(relay);
                                } else if (circ
                                        .isUsedByHiddenServiceToConnectToRendezvousPoint()
                                        && relay.isTypeBegin()) {
                                    // new stream requested on a circuit that
                                    // was already established to the rendezvous
                                    // point
                                    circ.handleHiddenServiceStreamBegin(relay,
                                            streamId);
                                } else {
                                    // do nothing
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("else: circ.isUsedByHiddenServiceToConnectToRendezvousPoint()="
                                                + circ.isUsedByHiddenServiceToConnectToRendezvousPoint()
                                                + ", relay.getRelayCommand()="
                                                + relay.getRelayCommand());
                                    }
                                }
                            } else {
                                // relay cell for stream id 0: dispatch to
                                // circuit
                                if (relay.isTypeIntroduce2()) {
                                    if (circ.isUsedByHiddenServiceToConnectToIntroductionPoint()) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug("TLSDispatcher.run: introduce2 from "
                                                    + tls.getRouter()
                                                    .getNickname()
                                                    + " dispatched to circuit "
                                                    + circ.getId()
                                                    + " (stream ID=0)");
                                        }
                                        try {
                                            dispatched = circ.handleIntroduce2(relay);
                                        } catch (final IOException e) {
                                            LOG.info("TLSDispatcher.run: error handling intro2-cell: "
                                                    + e.getMessage());
                                        }
                                    } else {
                                        // do nothing
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug("else isTypeIntroduce2: from "
                                                    + tls.getRouter()
                                                    .getNickname()
                                                    + " dispatched to circuit "
                                                    + circ.getId()
                                                    + " (stream ID=0)");
                                        }
                                    }
                                } else {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("TLSDispatcher.run: data from "
                                                + tls.getRouter().getNickname()
                                                + " dispatched to circuit "
                                                + circ.getId()
                                                + " (stream ID=0)");
                                    }
                                    dispatched = true;
                                    circ.processCell(relay);
                                }
                            }
                        } catch (final TorException e) {
                            LOG.warn("TLSDispatcher.run: TorException "
                                    + e.getMessage()
                                    + " during dispatching cell");
                        } catch (final Exception e) {
                            LOG.warn(
                                    "TLSDispatcher.run: Exception "
                                            + e.getMessage()
                                            + " during dispatching cell", e);
                        }
                    } else {
                        // no relay cell: cell is there to control circuit
                        if (cell.isTypeDestroy()) {
                            if (LOG.isDebugEnabled()) {
                                try {
                                    LOG.debug("TLSDispatcher.run: received DESTROY-cell from "
                                            + tls.getRouter().getNickname()
                                            + " for circuit "
                                            + circ.getId()
                                            + " reason : "
                                            + ((CellDestroy) cell).getReason());
                                } catch (ClassCastException exception) {
                                    LOG.debug("TLSDispatcher.run: received DESTROY-cell from "
                                            + tls.getRouter().getNickname()
                                            + " for circuit "
                                            + circ.getId()
                                            + " reason : " + CellDestroy.getReason(cell.getPayload()[0]));
                                }
                            }
                            if (cell.getPayload()[0] == CellDestroy.REASON_END_CIRC_TOR_PROTOCOL) {
                                LOG.warn("got a DestroyCell with Reason protocol violation from " + circ);
                            }
                            dispatched = true;
                            circ.close(true);
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("TLSDispatcher.run: data from "
                                        + tls.getRouter().getNickname()
                                        + " dispatched to circuit "
                                        + circ.getId());
                            }
                            dispatched = true;
                            try {
                                circ.processCell(cell);
                            } catch (TorException exception) {
                                LOG.warn("got Exception while processing cell", exception);
                            }
                        }
                    }
                } else {
                    LOG.info("TLSDispatcher.run: received cell for circuit "
                            + cellCircId + " from "
                            + tls.getRouter().getNickname()
                            + ". But no such circuit exists.");
                }
            }
            if (!dispatched) {
                // used to be WARNING, but is given too often to be of $REAL
                // value, like a warning should
                if (LOG.isDebugEnabled()) {
                    LOG.debug("TLSDispatcher.run: data from "
                            + tls.getRouter().getNickname()
                            + " could not get dispatched");
                    LOG.debug("TLSDispatcher.run: " + cell.toString());
                }
            }
        }
    }
}
