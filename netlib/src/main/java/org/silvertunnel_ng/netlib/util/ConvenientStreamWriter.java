/*
 * silvertunnel-ng.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2014 silvertunnel-ng.org
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides convenient helper Methods for writing streams.
 */
public class ConvenientStreamWriter {
    private OutputStream outputStream;

    public ConvenientStreamWriter(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeBoolean(final boolean value) throws IOException {
        outputStream.write(value ? (byte) 1 : (byte) 0);
    }

    public void writeByte(final byte value) throws IOException {
        outputStream.write(new byte[]{value});
    }

    public void writeInt(final int value) throws IOException {
        outputStream.write(ByteUtils.intToBytes(value));
    }

    public void writeLong(final long value) throws IOException {
        outputStream.write(ByteUtils.longToBytes(value));
    }

    public void writeFloat(final float value) throws IOException {
        writeByteArray(ByteUtils.intToBytes(Float.floatToIntBits(value)), false);
    }

    public void writeByteArray(final byte[] value, final boolean saveLen) throws IOException {
        if (saveLen) {
            writeInt(value.length);
        }
        outputStream.write(value);
    }

    public void writeString(final String value) throws IOException {
        if (value == null || value.length() == 0) {
            writeInt(0);
        } else {
            writeByteArray(value.getBytes(), true);
        }
    }

}
