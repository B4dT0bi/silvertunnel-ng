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
package org.silvertunnel_ng.netlib.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A dynamically growing byte buffer.
 * 
 * @author Tobias Boese
 * 
 */
public class DynByteBuffer
{
	/** default start size of the buffer. */
	private static final int DEFAULT_START_SIZE = 10000;
	/** default increase size when buffer is too small. */
	private static final int DEFAULT_INC_SIZE = 10000;
	/** use the given byte buffer but when a write takes place copy the whole buffer. */
	private boolean copyOnWrite = false;
	/**
	 * Dynamically growing byte buffer.
	 */
	public DynByteBuffer()
	{
		setSize(DEFAULT_START_SIZE);
	}

	/**
	 * Dynamically growing byte buffer.
	 * 
	 * @param data
	 *            initial data.
	 */
	public DynByteBuffer(final byte[] data)
	{
		setBuffer(data);
	}

	/**
	 * Dynamically growing byte buffer.
	 * 
	 * @param inputStream
	 *            initial data read from an {@link InputStream}.
	 * @throws IOException when something went wrong during read or close
	 */
	public DynByteBuffer(final InputStream inputStream) throws IOException
	{
		setSize(DEFAULT_START_SIZE);
		init();
		append(inputStream);
		crrPosRead = 0;
	}

	/**
	 * Dynamically growing byte buffer.
	 * 
	 * @param data
	 *            initial data.
	 * @param copyOnWrite if set to true it will copy the buffer only when modified
	 */
	public DynByteBuffer(final byte[] data, final boolean copyOnWrite)
	{
		setBuffer(data, copyOnWrite);
	}

	/**
	 * Dynamically growing byte buffer.
	 * 
	 * @param data
	 *            initial data.
	 * @param copyOnWrite if set to true it will copy the buffer only when modified
	 * @param initialOffset the initial offset
	 */
	public DynByteBuffer(final byte[] data, final boolean copyOnWrite, final int initialOffset)
	{
		setBuffer(data, copyOnWrite);
		resetPosition(initialOffset);
	}

	/**
	 * Dynamically growing byte buffer.
	 * 
	 * @param initSize
	 *            initial size of the buffer.
	 */
	public DynByteBuffer(final long initSize)
	{
		if (initSize <= 0)
		{
			setSize(DEFAULT_START_SIZE);
		}
		else
		{
			setSize(initSize);
		}
	}

	/**
	 * @param size
	 *            set the new size of the buffer.
	 */
	private void setSize(final long size)
	{
		if (length == 0)
		{
			buffer = new byte[size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size];
		}
		else
		{
			final byte[] newBuffer = new byte[size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size];
			System.arraycopy(buffer, 0, newBuffer, 0, length);
			buffer = newBuffer;
		}
	}


	/**
	 * increase the size of the buffer by the DEFAULT size.
	 */
	private void increaseSize()
	{
		increaseSize(DEFAULT_INC_SIZE);
	}

	/**
	 * @param size
	 *            increase the size of the buffer by the given size.
	 */
	private void increaseSize(final int size)
	{
		setSize(buffer.length + size);
	}

	/**
	 * current length of data in this {@link DynByteBuffer}.
	 */
	private transient int length;
	/**
	 * the actual data.
	 */
	private transient byte[] buffer;

	/**
	 * @param value
	 *            append a boolean to the buffer.
	 */
	public final void append(final boolean value)
	{
		copyOnWrite();
		if (length + 1 >= buffer.length)
		{
			increaseSize();
		}
		buffer[length] = (byte) (value ? 1 : 0);
		length++;
	}
	/**
	 * This method checks wether we should make a copy of the buffer or if it is not needed.
	 */
	private void copyOnWrite()
	{
		if (copyOnWrite)
		{
			synchronized (buffer)
			{
				byte [] tmpBuffer = new byte[buffer.length];
				System.arraycopy(buffer, 0, tmpBuffer, 0, buffer.length);
				buffer = tmpBuffer;
				copyOnWrite = false;
			}
		}
	}
	/**
	 * @param value
	 *            append a byte to the buffer.
	 */
	public final void append(final byte value)
	{
		copyOnWrite();
		if (length + 1 >= buffer.length)
		{
			increaseSize();
		}
		buffer[length] = value;
		length++;
	}

	/**
	 * Append an byte array to the {@link DynByteBuffer}.
	 * 
	 * @param array
	 *            the byte array to be appended
	 * @param saveLength
	 *            should the length be saved?
	 */
	public final void append(final byte[] array, final boolean saveLength)
	{
		copyOnWrite();
		if (array == null)
		{
			if (saveLength)
			{
				append(0);
			}
		}
		else
		{
			if (saveLength)
			{
				append(array.length);
			}
			append(array, 0, array.length);
		}
	}

	/**
	 * Append an byte array to the {@link DynByteBuffer}.
	 * 
	 * @param array
	 *            the byte array to be appended
	 * @param offset
	 *            the offset from the array
	 */
	public final void append(final byte[] array, final int offset)
	{
		copyOnWrite();
		append(array, offset, array.length - offset);
	}

	/**
	 * Append an byte array to the {@link DynByteBuffer}.
	 * 
	 * @param array
	 *            the byte array to be appended
	 * @param offset
	 *            the offset from the array
	 * @param length
	 *            the length to be copied
	 */
	public final void append(final byte[] array, final int offset, final int length)
	{
		copyOnWrite();
		if (offset + length > array.length)
		{
			throw new ArrayIndexOutOfBoundsException(length + offset);
		}
		if (this.length + length >= buffer.length)
		{
			if (length > DEFAULT_INC_SIZE)
			{
				increaseSize(length + DEFAULT_INC_SIZE); // add a buffer to the
															// capacity
			}
			else
			{
				increaseSize();
			}
		}
		System.arraycopy(array, offset, buffer, this.length, length);
		this.length += length;
	}

	/**
	 * @return returns the buffer as an byte array.
	 */
	public final byte[] toArray()
	{
		return toArray(0);
	}
	/**
	 * Get the byte array (copy) from a specific position on.
	 * 
	 * @param offset the position where to start
	 * @return returns the buffer as an byte array.
	 */
	public final byte[] toArray(final int offset)
	{
		byte[] tmp = new byte[length - offset];
		System.arraycopy(buffer, offset, tmp, 0, length - offset);
		return tmp;
	}

	/**
	 * initializes the buffer so it can be used again.
	 */
	public final void init()
	{
		length = 0;
	}

	/**
	 * @param value
	 *            append an int to the byte buffer.
	 */
	public final void append(final int value)
	{
		copyOnWrite();
		append(ByteUtils.intToBytes(value), false);
	}

	/**
	 * @param value
	 *            append a float to the byte buffer.
	 */
	public final void append(final float value)
	{
		copyOnWrite();
		append(ByteUtils.intToBytes(Float.floatToIntBits(value)), false);
	}

	/**
	 * @param value
	 *            append a double to the byte buffer.
	 */
	public final void append(final double value)
	{
		copyOnWrite();
		append(ByteUtils.longToBytes(Double.doubleToLongBits(value)), false);
	}

	/**
	 * @param value
	 *            append a long to the byte buffer.
	 */
	public final void append(final long value)
	{
		copyOnWrite();
		append(ByteUtils.longToBytes(value), false);
	}

	/**
	 * @param value
	 *            append a Long to the byte buffer.
	 */
	public final void append(final Long value)
	{
		copyOnWrite();
		append(ByteUtils.longToBytes(value.longValue()), false);
	}

	/**
	 * @param value
	 *            appends the given {@link String} to the buffer.
	 */
	public final void append(final String value)
	{
		copyOnWrite();
		if (value == null || value.isEmpty())
		{
			append(0);
			return;
		}
		append(value.getBytes(), true);
	}

	/**
	 * @return get the size of the internal buffer.
	 */
	public final int getSize()
	{
		return buffer.length;
	}

	/**
	 * @return get the length of the data in the buffer.
	 */
	public final int getLength()
	{
		return length;
	}

	/**
	 * @return is the buffer empty?
	 */
	public final boolean isEmpty()
	{
		return getSize() == 0;
	}

	/** current read position in the buffer. */
	private transient int crrPosRead = 0;

	/**
	 * Resets the positions in the buffer to 0.
	 */
	public final void resetPosition()
	{
		resetPosition(0);
	}

	/**
	 * @param pos
	 *            Resets the positions in the buffer to the given value.
	 */
	public final void resetPosition(final int pos)
	{
		crrPosRead = pos;
	}
	/** 
	 * Clears the buffer (resets length and read position to 0).
	 * If you really want to delete the buffer content use setBuffer.
	 */
	public final void clear()
	{
		resetPosition();
		length = 0;
	}
	/**
	 * @return get the next data as int from the buffer.
	 */
	public final int getNextInt()
	{
		if (crrPosRead >= length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		int tmp = ByteUtils.bytesToInt(buffer, crrPosRead);
		crrPosRead += 4;
		return tmp;
	}

	/**
	 * @return get the next data as float from the buffer.
	 */
	public final float getNextFloat()
	{
		return Float.intBitsToFloat(getNextInt());
	}

	/**
	 * @return get the next data as double from the buffer.
	 */
	public final double getNextDouble()
	{
		return Double.longBitsToDouble(getNextLong());
	}

	/**
	 * @return gets the next data as long from the buffer.
	 */
	public final long getNextLong()
	{
		if (crrPosRead >= length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		long tmp = ByteUtils.bytesToLong(buffer, crrPosRead);
		crrPosRead += 8;
		return tmp;
	}

    /**
     * @return gets the next data as byte from the buffer.
     */
    public final byte getNextByte()
    {
        if (crrPosRead >= length)
        {
            throw new ArrayIndexOutOfBoundsException(crrPosRead);
        }
        byte tmp = buffer[crrPosRead];
        crrPosRead++;
        return tmp;
    }

    /**
     * @return gets the next data-byte as int from the buffer.
     */
    public final int getNextByteAsInt()
    {
        if (crrPosRead >= length)
        {
            throw new ArrayIndexOutOfBoundsException(crrPosRead);
        }
        byte tmp = buffer[crrPosRead];
        crrPosRead++;
        return ((int) tmp) & 0xff;
    }

    /**
	 * @return gets the next data as boolean from the buffer.
	 */
	public final boolean getNextBoolean()
	{
		if (crrPosRead >= length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		byte tmp = getNextByte();
		return tmp == (byte) 1;
	}

	/**
	 * @return gets the next byte array from the buffer. the length should be
	 *         infront of the byte array.
	 */
	public final byte[] getNextByteArray()
	{
		if (crrPosRead >= length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		int len = getNextInt();
		return getNextByteArray(len);
	}

	/**
	 * @return gets the next String from the buffer.
	 */
	public final String getNextString()
	{
		if (crrPosRead >= length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		byte[] tmp = getNextByteArray();
		if (tmp == null || tmp.length == 0)
		{
			return null;
		}
		return new String(tmp);
	}

	/**
	 * Gets the next byte array from the buffer with the given length.
	 * 
	 * @param length
	 *            the length
	 * @return a byte array
	 */
	public final byte[] getNextByteArray(final int length)
	{
		if (length == 0)
		{
			return null;
		}
		if (crrPosRead >= this.length)
		{
			throw new ArrayIndexOutOfBoundsException(crrPosRead);
		}
		byte[] tmp = new byte[length];
		System.arraycopy(buffer, crrPosRead, tmp, 0, length);
		crrPosRead += length;
		return tmp;
	}

	/**
	 * @param data
	 *            set the buffer to a new value.
	 */
	public final void setBuffer(final byte[] data)
	{
		setBuffer(data, false);
	}
	/**
	 * Appends the data from the {@link InputStream} to the current buffer.
	 * @param inputStream the {@link InputStream} to be read
	 * @throws IOException when something went wrong during read or close
	 */
	public final void append(final InputStream inputStream) throws IOException
	{
		byte [] buf = new byte[2048];
		int count;
		while ((count = inputStream.read(buf)) > 0)
		{
			append(buf, 0, count);
		}
		inputStream.close();		
	}
	/**
	 * Copy or use the given byte array as internal byte array.
	 * @param data
	 *            set the buffer to a new value.
	 * @param copyOnWrite if set to true we will use the original byte array till it gets changed
	 */
	public final synchronized void setBuffer(final byte[] data, final boolean copyOnWrite)
	{
		if (!copyOnWrite)
		{
			buffer = new byte [data.length];
			System.arraycopy(data, 0, buffer, 0, data.length);
		}
		else
		{
			buffer = data;
			this.copyOnWrite = copyOnWrite;
		}
		length = data.length;
		crrPosRead = 0;
	}
}
