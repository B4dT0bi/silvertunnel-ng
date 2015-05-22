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

package org.silvertunnel_ng.netlib.adapter.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

/**
 * See class SocketUtil.
 *
 * @author hapke
 */
class InvalidSocketImpl extends SocketImpl {

    private void notImplemented() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void accept(final SocketImpl s) throws IOException {
        notImplemented();
    }

    @Override
    protected int available() throws IOException {
        notImplemented();
        return 0;
    }

    @Override
    protected void bind(final InetAddress host, final int port) throws IOException {
        notImplemented();
    }

    @Override
    protected void close() throws IOException {
        notImplemented();
    }

    @Override
    protected void connect(final String host, final int port) throws IOException {
        notImplemented();
    }

    @Override
    protected void connect(final InetAddress address, final int port) throws IOException {
        notImplemented();
    }

    @Override
    protected void connect(final SocketAddress address, final int timeout) throws IOException {
        notImplemented();
    }

    @Override
    protected void create(final boolean stream) throws IOException {
        notImplemented();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        notImplemented();
        return null;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        notImplemented();
        return null;
    }

    @Override
    protected void listen(final int backlog) throws IOException {
        notImplemented();
    }

    @Override
    protected void sendUrgentData(final int data) throws IOException {
        notImplemented();
    }

    @Override
    public Object getOption(final int arg0) throws SocketException {
        notImplemented();
        return null;
    }

    @Override
    public void setOption(final int arg0, final Object arg1) throws SocketException {
        notImplemented();
    }
}
