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
package org.silvertunnel_ng.netlib.tool;

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
			initBuffer(0);
		}
		else
		{
			final byte[] newBuffer = new byte[size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size];
			System.arraycopy(buffer, 0, newBuffer, 0, length);
			buffer = newBuffer;
			initBuffer(length);
		}
	}

	/**
	 * @param from
	 *            initializes the buffer from the given position on.
	 */
	private void initBuffer(final int from)
	{
		for (int i = from; i < buffer.length; i++)
		{
			buffer[i] = 0;
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
		if (length + 1 >= buffer.length)
		{
			increaseSize();
		}
		buffer[length] = (byte) (value ? 1 : 0);
		length++;
	}

	/**
	 * @param value
	 *            append a byte to the buffer.
	 */
	public final void append(final byte value)
	{
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
	public final void append(final byte[] array, final int offset,
			final int length)
	{
		if (offset + length > array.length)
		{
			// TODO : throw exception?
			return;
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
		for (int i = 0; i < length; i++)
		{
			buffer[this.length + i] = array[offset + i];
		}
		this.length += length;
	}

	/**
	 * @return returns the buffer as an byte array.
	 */
	public final byte[] toArray()
	{
		final byte[] tmp = new byte[length];
		System.arraycopy(buffer, 0, tmp, 0, length);
		return tmp;
	}

	/**
	 * initializes the buffer so it can be used again.
	 */
	public final void init()
	{
		initBuffer(0);
		length = 0;
	}

	/**
	 * @param value
	 *            append an int to the byte buffer.
	 */
	public final void append(final int value)
	{
		append(ByteUtils.intToBytes(value), false);
	}

	/**
	 * @param value
	 *            append a long to the byte buffer.
	 */
	public final void append(final long value)
	{
		append(ByteUtils.longToBytes(value), false);
	}

	/**
	 * @param value
	 *            append a Long to the byte buffer.
	 */
	public final void append(final Long value)
	{
		append(ByteUtils.longToBytes(value.longValue()), false);
	}

	/**
	 * @param value
	 *            appends the given {@link String} to the buffer.
	 */
	public final void append(final String value)
	{
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
	 * @return get the next data as int from the buffer.
	 */
	public final int getNextInt()
	{
		final int tmp = ByteUtils.bytesToInt(buffer, crrPosRead);
		crrPosRead += 4;
		return tmp;
	}

	/**
	 * @return gets the next data as long from the buffer.
	 */
	public final long getNextLong()
	{
		final long tmp = ByteUtils.bytesToLong(buffer, crrPosRead);
		crrPosRead += 8;
		return tmp;
	}

	/**
	 * @return gets the next data as byte from the buffer.
	 */
	public final byte getNextByte()
	{
		final byte tmp = buffer[crrPosRead];
		crrPosRead++;
		return tmp;
	}

	/**
	 * @return gets the next data as boolean from the buffer.
	 */
	public final boolean getNextBoolean()
	{
		final byte tmp = getNextByte();
		return tmp == (byte) 1;
	}

	/**
	 * @return gets the next byte array from the buffer. the length should be
	 *         infront of the byte array.
	 */
	public final byte[] getNextByteArray()
	{
		final int len = getNextInt();
		return getNextByteArray(len);
	}

	/**
	 * @return gets the next String from the buffer.
	 */
	public final String getNextString()
	{
		final byte[] tmp = getNextByteArray();
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
		final byte[] tmp = new byte[length];
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
		buffer = new byte[data.length];
		System.arraycopy(data, 0, buffer, 0, data.length);
		length = data.length;
		crrPosRead = 0;
	}
}
