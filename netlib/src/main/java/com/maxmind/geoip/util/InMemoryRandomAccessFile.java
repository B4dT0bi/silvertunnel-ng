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

package com.maxmind.geoip.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class simulates most features of RandomAccessFile, but it is hold in
 * memory and it is read only.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public final class InMemoryRandomAccessFile
{
	/** this is the "file" content. */
	private final transient byte[] data;

	/** current position in the array. */
	private transient int position;

	// /////////////////////////////////////////////////////
	// open and close
	// /////////////////////////////////////////////////////

	/**
	 * Initialize an InMemoryRandomAccessFile with a given byte array.
	 * 
	 * @param dataSource
	 *            the caller should not change the content of this array to
	 *            avoid side effects
	 */
	public InMemoryRandomAccessFile(final byte[] dataSource)
	{
		data = dataSource;
		position = 0;
	}

	/**
	 * Initialize an InMemoryRandomAccessFile from an InputStream.
	 * 
	 * Inside this constructor the complete InputStream will be read (blocking)
	 * and copied to an internal byte array.
	 * 
	 * @param dataSource
	 * @param maxSize
	 *            limit the used memory to approximately maxSize bytes
	 * @exception IOException
	 *                if the dataSource could not be read
	 */
	public InMemoryRandomAccessFile(final InputStream dataSource, final int maxSize)
			throws IOException
	{
		this(getBytesOfInputStream(dataSource, maxSize));
	}

	/**
	 * Convert InputStream to byte array.
	 * 
	 * @param inputStream
	 *            data source
	 * @param maxSize
	 *            stop conversion if maxSize is reached
	 * @return inputStream converted into byte array
	 * @throws IOException
	 */
	public static byte[] getBytesOfInputStream(final InputStream inputStream,
			final int maxSize) throws IOException
	{
		if (inputStream == null)
		{
			throw new IOException("invalid inputStream=null");
		}

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				1024);
		final byte[] buffer = new byte[1024];

		// Read bytes from the input stream in bytes.length-sized chunks and
		// write
		// them into the output stream
		int totalReadBytes = 0;
		int readBytes;
		while ((readBytes = inputStream.read(buffer)) > 0)
		{
			outputStream.write(buffer, 0, readBytes);
			totalReadBytes += readBytes;
			if (totalReadBytes > maxSize)
			{
				// stop further reading
				break;
			}
		}

		// convert the contents of the output stream into a byte array
		final byte[] result = outputStream.toByteArray();
		inputStream.close();
		outputStream.close();

		return result;
	}

	public void close()
	{
	}

	// /////////////////////////////////////////////////////
	// 'Read' primitives
	// /////////////////////////////////////////////////////

	/**
	 * Reads a byte of data from this file. The byte is returned as an integer
	 * in the range 0 to 255 (<code>0x00-0x0ff</code>). This method blocks if no
	 * input is yet available.
	 * <p>
	 * Although <code>RandomAccessFile</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in exactly the same way as
	 * the {@link InputStream#read()} method of <code>InputStream</code>.
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the file
	 *         has been reached.
	 * @exception IOException
	 *                never thrown
	 */
	public int read() throws IOException
	{
		if (position >= data.length)
		{
			return -1;
		}
		else
		{
			return data[position++];
		}
	}

	/**
	 * Reads a sub array as a sequence of bytes.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset of the data.
	 * @param len
	 *            the number of bytes to read.
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	public int read(final byte[] b, int off, int len) throws IOException
	{
		if (b == null)
		{
			throw new NullPointerException("");
		}

		// copy
		int numberOfBytesCopied = 0;
		while (off < b.length && position < data.length && len > 0)
		{
			b[off] = data[position];
			off++;
			position++;
			numberOfBytesCopied++;
			len--;
		}

		return numberOfBytesCopied;
	}

	/**
	 * Returns the length of this file.
	 * 
	 * @return the length of this file, measured in bytes.
	 * @exception IOException
	 *                never
	 */
	public long length() throws IOException
	{
		return data.length;
	}

	/**
	 * Sets the file-pointer offset, measured from the beginning of this file,
	 * at which the next read or write occurs. The offset may be set beyond the
	 * end of the file. Setting the offset beyond the end of the file does not
	 * change the file length. The file length will change only by writing after
	 * the offset has been set beyond the end of the file.
	 * 
	 * @param pos
	 *            the offset position, measured in bytes from the beginning of
	 *            the file, at which to set the file pointer.
	 * @exception IOException
	 *                if <code>pos</code> is less than <code>0</code> or if an
	 *                I/O error occurs.
	 */
	public void seek(final long pos) throws IOException
	{
		if (pos < 0 || pos >= length() || pos > Integer.MAX_VALUE)
		{
			throw new IOException("seek() tried with invalid position=" + pos);
		}
		position = (int) pos;
	}

	/**
	 * Returns the current offset in this file.
	 * 
	 * @return the offset from the beginning of the file, in bytes, at which the
	 *         next read or write occurs.
	 * @exception IOException
	 *                never
	 */
	public long getFilePointer() throws IOException
	{
		return position;
	}

	/**
	 * Reads up to <code>b.length</code> bytes of data from this file into an
	 * array of bytes. This method blocks until at least one byte of input is
	 * available.
	 * <p>
	 * Although <code>RandomAccessFile</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in exactly the same way as
	 * the {@link InputStream#read(byte[])} method of <code>InputStream</code>.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of this
	 *         file has been reached.
	 * @exception IOException
	 *                If the first byte cannot be read for any reason other than
	 *                end of file, or if the random access file has been closed,
	 *                or if some other I/O error occurs.
	 * @exception NullPointerException
	 *                If <code>b</code> is <code>null</code>.
	 */
	public int read(final byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}

	// /////////////////////////////////////////////////////
	// Some "reading/writing Java data types" methods "stolen" from
	// DataInputStream and DataOutputStream.
	// /////////////////////////////////////////////////////

	/**
	 * Reads a signed eight-bit value from this file. This method reads a byte
	 * from the file, starting from the current file pointer. If the byte read
	 * is <code>b</code>, where
	 * <code>0&nbsp;&lt;=&nbsp;b&nbsp;&lt;=&nbsp;255</code>, then the result is:
	 * <blockquote>
	 * 
	 * <pre>
	 * (byte) (b)
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * This method blocks until the byte is read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next byte of this file as a signed eight-bit
	 *         <code>byte</code>.
	 * @exception EOFException
	 *                if this file has reached the end.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public byte readByte() throws IOException
	{
		final int ch = this.read();
		if (ch < 0)
		{
			throw new EOFException();
		}
		return (byte) (ch);
	}
}
