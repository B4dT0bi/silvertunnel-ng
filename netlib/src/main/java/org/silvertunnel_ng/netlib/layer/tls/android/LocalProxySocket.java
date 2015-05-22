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
package org.silvertunnel_ng.netlib.layer.tls.android;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * the LocalProxySocket is used on Android to trick the SSLSocket implementation to work
 * together with NetSockets which are transformed to "normal" Sockets.
 *
 * @author Tobias Boese
 */
public class LocalProxySocket extends Socket {

    private LocalServerSocket localServerSocket;
    private LocalSocket localSocketSend;
    private LocalSocket localSocketRecv;
    private Socket originalSocket;

    private static final Logger LOG = LoggerFactory.getLogger(LocalProxySocket.class);

    public LocalProxySocket(Socket original) throws TorException {
        super();
        try {
            // Prepare LocalSocket which will be used to trick the SSLSocket (or any other one)
            localSocketSend = new LocalSocket();
            // Local socket name
            String socketName = "local" + UUID.randomUUID();
            localServerSocket = new LocalServerSocket(socketName);
            localSocketSend.connect(new LocalSocketAddress(socketName));
            localSocketRecv = localServerSocket.accept();
            this.originalSocket = original;
            // Create 2 Threads which are taking care of the communication between the LocalSocket and the original Socket
            LocalProxyWorker lpw1 = new LocalProxyWorker(localSocketRecv.getInputStream(), originalSocket.getOutputStream(), "to");
            LocalProxyWorker lpw2 = new LocalProxyWorker(originalSocket.getInputStream(), localSocketRecv.getOutputStream(), "from");
            Thread t1 = new Thread(lpw1);
            Thread t2 = new Thread(lpw2);
            t1.start();
            t2.start();
            // Prepare this Socket to contain the FileDescriptor of the LocalSocket
            FileDescriptor fd = localSocketSend.getFileDescriptor();
            SocketImpl socketImpl = (SocketImpl) Class.forName("java.net.PlainSocketImpl").getConstructor(FileDescriptor.class).newInstance(fd);
            Field implField = this.getClass().getSuperclass().getDeclaredField("impl");
            implField.setAccessible(true);
            implField.set(this, socketImpl);
        } catch (Exception e) {
            LOG.debug("Got Exception while trying to create LocalProxySocket", e);
            throw new TorException("could not create LocalProxySocket", e);
        }
    }

    private class LocalProxyWorker implements Runnable {
        private InputStream inputStream;
        private OutputStream outputStream;
        private String direction;

        public LocalProxyWorker(InputStream inputStream, OutputStream outputStream, String direction) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.direction = direction;
        }

        @Override
        public void run() {
            boolean error = false;
            while (!error) {
                try {
                    if (inputStream.available() > 0) {
                        copyStream(inputStream, outputStream);
                    }
                } catch (IOException e) {
                    LOG.debug("got Exception during copy direction : {}", direction, e);
                    error = true;
                    try {
                        inputStream.close();
                    } catch (IOException e1) {
                        if (!(e1 instanceof SocketException && e1.getMessage().contains("closed"))) {
                            LOG.debug("got exception during close of inputStream direction : {}", direction, e1);
                        }
                    }
                    try {
                        outputStream.close();
                    } catch (IOException e1) {
                        if (!(e1 instanceof SocketException && e1.getMessage().contains("closed"))) {
                            LOG.debug("got exception during close of outputStream direction : {}", direction, e1);
                        }
                    }
                }
            }
        }

        void copyStream(InputStream input, OutputStream output)
                throws IOException {
            byte[] buffer = new byte[1024]; // Adjust if you want
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Closes the originalSocket. It is not possible to reconnect or rebind to this
     * originalSocket thereafter which means a new originalSocket instance has to be created.
     *
     * @throws IOException if an error occurs while closing the originalSocket.
     */
    public synchronized void close() throws IOException {
        super.close();
        originalSocket.close();
        localSocketRecv.close();
        LOG.debug("close() called", new Throwable());
    }

    /**
     * Returns the IP address of the target host this originalSocket is connected to, or null if this
     * originalSocket is not yet connected.
     */
    public InetAddress getInetAddress() {
        return originalSocket.getInetAddress();
    }

    /**
     * Returns an input stream to read data from this originalSocket. If the originalSocket has an associated
     * {@link SocketChannel} and that channel is in non-blocking mode then reads from the
     * stream will throw a {@link java.nio.channels.IllegalBlockingModeException}.
     *
     * @return the byte-oriented input stream.
     * @throws IOException if an error occurs while creating the input stream or the
     *                     originalSocket is in an invalid state.
     */
    public InputStream getInputStream() throws IOException {
        return super.getInputStream();
    }

    /**
     * Returns this originalSocket's {@link SocketOptions#SO_KEEPALIVE} setting.
     */
    public boolean getKeepAlive() throws SocketException {
        return originalSocket.getKeepAlive();
    }

    /**
     * Returns the local IP address this originalSocket is bound to, or an address for which
     * {@link InetAddress#isAnyLocalAddress()} returns true if the originalSocket is closed or unbound.
     */
    public InetAddress getLocalAddress() {
        return originalSocket.getLocalAddress();
    }

    /**
     * Returns the local port this originalSocket is bound to, or -1 if the originalSocket is unbound. If the originalSocket
     * has been closed this method will still return the local port the originalSocket was bound to.
     */
    public int getLocalPort() {
        return originalSocket.getLocalPort();
    }

    /**
     * Returns an output stream to write data into this originalSocket. If the originalSocket has an associated
     * {@link SocketChannel} and that channel is in non-blocking mode then writes to the
     * stream will throw a {@link java.nio.channels.IllegalBlockingModeException}.
     *
     * @return the byte-oriented output stream.
     * @throws IOException if an error occurs while creating the output stream or the
     *                     originalSocket is in an invalid state.
     */
    public OutputStream getOutputStream() throws IOException {
        return super.getOutputStream();
    }

    /**
     * Returns the port number of the target host this originalSocket is connected to, or 0 if this originalSocket
     * is not yet connected.
     */
    public int getPort() {
        return originalSocket.getPort();
    }

    /**
     * Returns this originalSocket's {@link SocketOptions#SO_LINGER linger} timeout in seconds, or -1
     * for no linger (i.e. {@code close} will return immediately).
     */
    public int getSoLinger() throws SocketException {
        return originalSocket.getSoLinger();
    }

    /**
     * Returns this originalSocket's {@link SocketOptions#SO_RCVBUF receive buffer size}.
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        return originalSocket.getReceiveBufferSize();
    }

    /**
     * Returns this originalSocket's {@link SocketOptions#SO_SNDBUF send buffer size}.
     */
    public synchronized int getSendBufferSize() throws SocketException {
        return originalSocket.getSendBufferSize();
    }

    /**
     * Returns this originalSocket's {@link SocketOptions#SO_TIMEOUT receive timeout}.
     */
    public synchronized int getSoTimeout() throws SocketException {
        return originalSocket.getSoTimeout();
    }

    /**
     * Returns this originalSocket's {@code SocketOptions#TCP_NODELAY} setting.
     */
    public boolean getTcpNoDelay() throws SocketException {
        return originalSocket.getTcpNoDelay();
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#SO_KEEPALIVE} option.
     */
    public void setKeepAlive(boolean keepAlive) throws SocketException {
        originalSocket.setKeepAlive(keepAlive);
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#SO_SNDBUF send buffer size}.
     */
    public synchronized void setSendBufferSize(int size) throws SocketException {
        originalSocket.setSendBufferSize(size);
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#SO_RCVBUF receive buffer size}.
     */
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        originalSocket.setReceiveBufferSize(size);
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#SO_LINGER linger} timeout in seconds.
     * If {@code on} is false, {@code timeout} is irrelevant.
     */
    public void setSoLinger(boolean on, int timeout) throws SocketException {
        originalSocket.setSoLinger(on, timeout);
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#SO_TIMEOUT read timeout} in milliseconds.
     * Use 0 for no timeout.
     * To take effect, this option must be set before the blocking method was called.
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        originalSocket.setSoTimeout(timeout);
    }

    /**
     * Sets this originalSocket's {@link SocketOptions#TCP_NODELAY} option.
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
        originalSocket.setTcpNoDelay(on);
    }


    /**
     * Returns a {@code String} containing a concise, human-readable description of the
     * originalSocket.
     *
     * @return the textual representation of this originalSocket.
     */
    @Override
    public String toString() {
        return "LocalProxySocket : " + super.toString() + " - " + originalSocket.toString() + " - " + localSocketSend.toString();
    }

    /**
     * Closes the input stream of this originalSocket. Any further data sent to this
     * originalSocket will be discarded. Reading from this originalSocket after this method has
     * been called will return the value {@code EOF}.
     *
     * @throws IOException     if an error occurs while closing the originalSocket input stream.
     * @throws SocketException if the input stream is already closed.
     */
    public void shutdownInput() throws IOException {
        originalSocket.shutdownInput();
        localSocketRecv.shutdownInput();
    }

    /**
     * Closes the output stream of this originalSocket. All buffered data will be sent
     * followed by the termination sequence. Writing to the closed output stream
     * will cause an {@code IOException}.
     *
     * @throws IOException     if an error occurs while closing the originalSocket output stream.
     * @throws SocketException if the output stream is already closed.
     */
    public void shutdownOutput() throws IOException {
        originalSocket.shutdownOutput();
        localSocketRecv.shutdownOutput();
    }

    /**
     * Returns the local address and port of this originalSocket as a SocketAddress or null if the originalSocket
     * has never been bound. If the originalSocket is closed but has previously been bound then an address
     * for which {@link InetAddress#isAnyLocalAddress()} returns true will be returned with the
     * previously-bound port. This is useful on multihomed hosts.
     */
    public SocketAddress getLocalSocketAddress() {
        return originalSocket.getLocalSocketAddress();
    }

    /**
     * Returns the remote address and port of this originalSocket as a {@code
     * SocketAddress} or null if the originalSocket is not connected.
     *
     * @return the remote originalSocket address and port.
     */
    public SocketAddress getRemoteSocketAddress() {
        return originalSocket.getRemoteSocketAddress();
    }

    /**
     * Returns whether this originalSocket is bound to a local address and port.
     *
     * @return {@code true} if the originalSocket is bound to a local address, {@code
     * false} otherwise.
     */
    public boolean isBound() {
        return originalSocket.isBound();
    }

    /**
     * Returns whether this originalSocket is connected to a remote host.
     *
     * @return {@code true} if the originalSocket is connected, {@code false} otherwise.
     */
    public boolean isConnected() {
        return originalSocket.isConnected();
        //return true;
    }

    /**
     * Returns whether this originalSocket is closed.
     *
     * @return {@code true} if the originalSocket is closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return originalSocket.isClosed();
    }

    /**
     * Binds this originalSocket to the given local host address and port specified by
     * the SocketAddress {@code localAddr}. If {@code localAddr} is set to
     * {@code null}, this originalSocket will be bound to an available local address on
     * any free port.
     *
     * @param localAddr the specific address and port on the local machine to bind to.
     * @throws IllegalArgumentException if the given SocketAddress is invalid or not supported.
     * @throws IOException              if the originalSocket is already bound or an error occurs while
     *                                  binding.
     */
    public void bind(SocketAddress localAddr) throws IOException {
        originalSocket.bind(localAddr);
    }

    /**
     * Connects this originalSocket to the given remote host address and port specified
     * by the SocketAddress {@code remoteAddr}.
     *
     * @param remoteAddr the address and port of the remote host to connect to.
     * @throws IllegalArgumentException if the given SocketAddress is invalid or not supported.
     * @throws IOException              if the originalSocket is already connected or an error occurs while
     *                                  connecting.
     */
    public void connect(SocketAddress remoteAddr) throws IOException {
        originalSocket.connect(remoteAddr);
    }

    /**
     * Connects this originalSocket to the given remote host address and port specified
     * by the SocketAddress {@code remoteAddr} with the specified timeout. The
     * connecting method will block until the connection is established or an
     * error occurred.
     *
     * @param remoteAddr the address and port of the remote host to connect to.
     * @param timeout    the timeout value in milliseconds or {@code 0} for an infinite
     *                   timeout.
     * @throws IllegalArgumentException if the given SocketAddress is invalid or not supported or the
     *                                  timeout value is negative.
     * @throws IOException              if the originalSocket is already connected or an error occurs while
     *                                  connecting.
     */
    public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
        originalSocket.connect(remoteAddr, timeout);
    }

    /**
     * Returns whether the incoming channel of the originalSocket has already been
     * closed.
     *
     * @return {@code true} if reading from this originalSocket is not possible anymore,
     * {@code false} otherwise.
     */
    public boolean isInputShutdown() {
        return originalSocket.isInputShutdown() || localSocketRecv.isInputShutdown();
    }

    /**
     * Returns whether the outgoing channel of the originalSocket has already been
     * closed.
     *
     * @return {@code true} if writing to this originalSocket is not possible anymore,
     * {@code false} otherwise.
     */
    public boolean isOutputShutdown() {
        return originalSocket.isOutputShutdown() || localSocketRecv.isOutputShutdown();
    }
}
