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
package org.silvertunnel_ng.netlib.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides convenient helper Methods for reading streams.
 */
public class ConvenientStreamReader {
    private static final Logger LOG = LoggerFactory.getLogger(ConvenientStreamReader.class);
    private InputStream inputStream;
    public ConvenientStreamReader(final InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
    }

    /**
     * Reads a byte from the underlaying InputStream.
     * @return a byte
     * @throws IOException
     */
    public byte readByte() throws IOException {
        byte [] result = new byte [1];
        inputStream.read(result);
        return result[0];
    }
    /**
     * Reads a byte array from the underlaying InputStream.
     * @param len the len of the byte array
     * @return a byte array
     * @throws IOException
     */
    public byte [] readByteArray(final int len) throws IOException {
        byte [] result = new byte [len];
        inputStream.read(result);
        return result;
    }
    /**
     * Reads a byte array from the underlaying InputStream.
     * It first reads the length of the byte array and then the byte array itself.
     * @return a byte array
     * @throws IOException
     */
    public byte [] readByteArray() throws IOException {
        int len = readInt();
        if (len == 0) return null;
        return readByteArray(len);
    }
    /**
     * Reads an int from the underlaying InputStream.
     * @return an int
     * @throws IOException
     */
    public int readInt() throws IOException {
        byte [] result = new byte [4];
        inputStream.read(result);
        return ByteUtils.bytesToInt(result, 0);
    }
    /**
     * Reads a boolean from the underlaying InputStream.
     * @return a boolean
     * @throws IOException
     */
    public boolean readBoolean() throws IOException {
        return readByte() == (byte) 1;
    }
    /**
     * Reads a float from the underlaying InputStream.
     * @return a float
     * @throws IOException
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    /**
     * Reads a long from the underlaying InputStream.
     * @return a long
     * @throws IOException
     */
    public long readLong() throws IOException {
        byte [] result = new byte [8];
        inputStream.read(result);
        return ByteUtils.bytesToLong(result);
    }
    /**
     * Reads a String from the underlaying InputStream.
     * @return a String
     * @throws IOException
     */
    public String readString() throws IOException {
        byte[] tmp = readByteArray();
        if (tmp == null || tmp.length == 0)
        {
            return null;
        }
        return new String(tmp);
    }
}
