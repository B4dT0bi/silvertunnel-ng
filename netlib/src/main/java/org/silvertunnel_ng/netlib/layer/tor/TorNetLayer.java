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


package org.silvertunnel_ng.netlib.layer.tor;

import org.silvertunnel_ng.netlib.api.*;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.control.ControlNetLayer;
import org.silvertunnel_ng.netlib.layer.control.ControlParameters;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.clientimpl.Tor;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.directory.FingerprintImpl;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.stream.TCPStream;
import org.silvertunnel_ng.netlib.nameservice.cache.CachingNetAddressNameService;
import org.silvertunnel_ng.netlib.nameservice.tor.TorNetAddressNameService;
import org.silvertunnel_ng.netlib.util.DatatypeConverter;
import org.silvertunnel_ng.netlib.util.StringStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Layer over Tor network: tunnels (TCP/IP) network traffic through the Tor
 * anonymity network.
 *
 * @author hapke
 * @author Tobias Boese
 */
public class TorNetLayer implements NetLayer {
    /** */
    private static final Logger LOG = LoggerFactory.getLogger(TorNetLayer.class);

    /**
     * the instance of tor used by this layer instance.
     */
    private final transient Tor tor;

    /**
     * the instance of NetAddressNameService; will be initialized during the
     * first call of getNetAddressNameService().
     */
    private transient NetAddressNameService netAddressNameService;

    private static final String EXIT = "exit";
    private static final Pattern EXIT_PATTERN = Pattern.compile("(.*)\\.([^\\.]+)\\." + EXIT);

    private final NetLayer thisTorNetLayerWithTimeoutControl;

    static {
        try {
            /** start init of TOR (see {@link tjava.proxy.Main} */
            // load custom policy file
            // TODO webstart:
            // Thread.currentThread().getContextClassLoader().getResource("data/TorJava.policy");
            // TODO webstart: Policy.getPolicy().refresh();

        } catch (final Exception e) {
            LOG.info("problem during static construction", e);
        }
    }

    public TorNetLayer(NetLayer lowerTlsConnectionNetLayer, NetLayer lowerDirConnectionNetLayer, StringStorage stringStorage) throws IOException {
        // create new Tor instance
        this(new Tor(lowerTlsConnectionNetLayer, lowerDirConnectionNetLayer, stringStorage));
    }

    public TorNetLayer(final Tor tor) throws IOException {
        this.tor = tor;

        // initialize thisTorNetLayerWithTimeoutControl,
        // use configuration parameters of Tor directory component
        final ControlParameters cp = ControlParameters.createTypicalFileTransferParameters();
        cp.setConnectTimeoutMillis(TorConfig.DIR_CONNECT_TIMEOUT_MILLIS);
        cp.setOverallTimeoutMillis(TorConfig.DIR_OVERALL_TIMEOUT_MILLIS);
        cp.setInputMaxBytes(TorConfig.DIR_MAX_FILETRANSFER_BYTES);
        cp.setThroughputTimeframeMinBytes(TorConfig.DIR_THROUGPUT_TIMEFRAME_MIN_BYTES);
        cp.setThroughputTimeframeMillis(TorConfig.DIR_THROUGPUT_TIMEFRAME_MILLIS);
        thisTorNetLayerWithTimeoutControl = new ControlNetLayer(this, cp);
    }

    // /////////////////////////////////////////////////////
    // layer methods
    // /////////////////////////////////////////////////////

    /**
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    @Override
    public NetSocket createNetSocket(Map<String, Object> localProperties, NetAddress localAddress, NetAddress remoteAddress) throws IOException {
        final TcpipNetAddress ra = (TcpipNetAddress) remoteAddress;

        // create TCP stream via Tor
        final TCPStreamProperties sp = convertTcpipNetAddress2TCPStreamProperties(ra);

        // check if we want a connection to a DirServer (so IP belongs to a
        // DirServer and dest port = dirport)
        if (tor.getDirectory().isDirServer(sp)) {
            sp.setExitPolicyRequired(false); // we dont need an exit router as
            // we do it inside the tor
            // network
            sp.setConnectToDirServer(true); // we want to connect to a dir
            // server so use BEGIN_DIR cell
            String[] octets = sp.getHostname().split("\\.");
            byte[] ip = new byte[4];
            ip[0] = (byte) Integer.parseInt(octets[0]);
            ip[1] = (byte) Integer.parseInt(octets[1]);
            ip[2] = (byte) Integer.parseInt(octets[2]);
            ip[3] = (byte) Integer.parseInt(octets[3]);
            sp.setMinRouteLength(1);
            sp.setMaxRouteLength(1);
            sp.setCustomExitpoint(tor.getDirectory().getValidRouterByIpAddressAndDirPort(new TcpipNetAddress(ip, sp.getPort()))
                    .getFingerprint());
            sp.setCustomRoute(new FingerprintImpl[]{(FingerprintImpl) sp.getCustomExitpoint()});
        }
        try {
            final TCPStream remote = tor.connect(sp, thisTorNetLayerWithTimeoutControl);
            return new TorNetSocket(remote, "TorNetLayer connection to " + ra);
        } catch (Throwable throwable) {
            throw new IOException(throwable);
        }
    }

    private TCPStreamProperties convertTcpipNetAddress2TCPStreamProperties(final TcpipNetAddress ra) {
        TCPStreamProperties sp = new TCPStreamProperties(ra);

        // check whether a specific exit node is requested
        /*
		 * SYNTAX: [hostname].[name-or-digest].exit [name-or-digest].exit
		 * Hostname is a valid hostname; [name-or-digest] is either the nickname
		 * of a Tor node or the hex-encoded digest of that node's public key.
		 */
        String hostname = ra.getHostname();
        if (hostname != null) {
            hostname = hostname.toLowerCase();
            final Matcher m = EXIT_PATTERN.matcher(hostname);
            if (m.find()) {
                // this looks like a .exit host name: extract the parts of this
                // special host name now
                if (LOG.isDebugEnabled()) {
                    LOG.debug("hostname with .exit pattern={}", hostname);
                }
                final String originalHostname = m.group(1);
                final String exitNodeNameOrDigest = m.group(2);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("originalHostname=" + originalHostname);
                    LOG.debug("exitNodeNameOrDigest=" + exitNodeNameOrDigest);
                }

                // reset the hostname
                final TcpipNetAddress raNew = new TcpipNetAddress(originalHostname, ra.getPort());
                sp = new TCPStreamProperties(raNew);

                // enforce exit node
                sp.setCustomExitpoint(new FingerprintImpl(DatatypeConverter.parseHexBinary(exitNodeNameOrDigest)));
            }
        }

        return sp;
    }

    /**
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     */
    @Override
    public NetServerSocket createNetServerSocket(Map<String, Object> properties, NetAddress localListenAddress) throws IOException {
        try {
            final TorHiddenServicePortPrivateNetAddress netAddress = (TorHiddenServicePortPrivateNetAddress) localListenAddress;
            final TorNetServerSocket torNetServerSocket = new TorNetServerSocket(netAddress.getPublicOnionHostname(), netAddress.getPort());

            final NetLayer torNetLayerToConnectToDirectoryService = this;
            final HiddenServiceProperties hiddenServiceProps = new HiddenServiceProperties(netAddress.getPort(), netAddress
                    .getTorHiddenServicePrivateNetAddress().getKeyPair());
            tor.provideHiddenService(torNetLayerToConnectToDirectoryService, hiddenServiceProps, torNetServerSocket);

            return torNetServerSocket;

        } catch (final Exception e) {
            final String msg = "could not create NetServerSocket for localListenAddress=" + localListenAddress;
            LOG.error("could not create NetServerSocket", e);
            throw new IOException(msg);
        }
    }

    /**
     * @see NetLayer#getStatus()
     */
    @Override
    public NetLayerStatus getStatus() {
        return tor.getStatus();
    }

    /**
     * Wait (block the current thread) until the Tor net layer is up and ready
     * or a configured timeout comes up.
     */
    @Override
    public void waitUntilReady() {
        tor.checkStartup();
    }

    /**
     * @see NetLayer#clear()
     */
    @Override
    public void clear() throws IOException {
        LOG.info("clear() started");
        tor.clear();
        LOG.info("clear() finished");
    }

    /**
     * @return a TorNetAddressNameService instance
     */
    @Override
    public NetAddressNameService getNetAddressNameService() {
        if (netAddressNameService == null) {
            // create a new instance
            netAddressNameService = new CachingNetAddressNameService(new TorNetAddressNameService(tor) {
                // use this anonymous class to access the protected
                // constructor
            });
        }

        return netAddressNameService;
    }

    // /////////////////////////////////////////////////////
    // layer specific methods
    // /////////////////////////////////////////////////////

    /**
     * @return read-only view of the currently valid Tor routers
     */
    public Collection<Router> getValidTorRouters() {
        waitUntilReady();
        return tor.getValidTorRouters();
    }

    /**
     * This will change the Tor identity by closing all open circuits.
     * <br>
     * This will force the TorNetLayer to build up new Circuits and therefore the "user" gets a new IP address.
     */
    public final void changeIdentity() {
        Iterator<Circuit> itCircuits = tor.getCurrentCircuits().iterator();
        while (itCircuits.hasNext()) {
            Circuit circuit = itCircuits.next();
            if (circuit != null && !circuit.isUnused() && !circuit.isClosed()) {
                circuit.close(false);
            }
        }
    }
}
