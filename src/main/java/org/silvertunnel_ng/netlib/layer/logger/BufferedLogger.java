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

package org.silvertunnel_ng.netlib.layer.logger;

import java.util.logging.Level;

import org.silvertunnel_ng.netlib.util.LogHelper;
import org.slf4j.Logger;

/**
 * Log bytes - but delay the logging until the next flush.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class BufferedLogger
{
	public static final Level LOG_LEVEL_NULL = Level.OFF;
	public static final Level LOG_LEVEL_DEBUG = Level.FINE;
	public static final Level LOG_LEVEL_INFO = Level.INFO;
	private static final char SPECIAL_CHAR = '?';

	private final Logger summaryLog;
	private final Level summaryLogLevel;
	private final Logger detailLog;
	private final Level detailLogLevel;
	private final boolean logSingleBytes;
	private final String logMessagePrefix;

	private StringBuffer buffer = new StringBuffer();
	int byteCount = 0;

	/**
	 * Initialize a new BufferedLogger.
	 * 
	 * @param summaryLog
	 * @param summaryLogLevel
	 * @param logSingleBytes
	 * @param logMessagePrefix
	 */
	public BufferedLogger(final Logger summaryLog, final Level summaryLogLevel,
			final Logger detailLog, final Level detailLogLevel, final boolean logSingleBytes,
			final String logMessagePrefix)
	{
		this.summaryLog = summaryLog;
		this.summaryLogLevel = summaryLogLevel;
		this.detailLog = detailLog;
		this.detailLogLevel = detailLogLevel;
		this.logSingleBytes = logSingleBytes;
		this.logMessagePrefix = logMessagePrefix;
	}

	/**
	 * log b - but delay the logging until the next flush.
	 * 
	 * @param b
	 */
	public void log(final byte b)
	{
		if (logSingleBytes && LogHelper.isLoggable(detailLog, detailLogLevel))
		{
			final char c = (char) b;
			if (c >= ' ' && c <= 0x7f)
			{
				logAndCount(c);
			}
			else
			{
				logAndCount(SPECIAL_CHAR);
				// add hex value (always two digits)
				final int i = b < 0 ? 256 + b : b;
				final String hex = Integer.toHexString(i);
				if (hex.length() < 2)
				{
					logAndDoNotCount("0");
				}
				logAndDoNotCount(hex);
			}
		}
		else
		{
			byteCount++;
		}
	}

	/**
	 * log bytes - but delay the logging until the next flush.
	 * 
	 * @param bytes
	 * @param offset
	 *            start at this array index in bytes
	 * @param numOfBytes
	 *            log this number of bytes
	 */
	public void log(final byte[] bytes, final int offset, int numOfBytes)
	{
		if (logSingleBytes && LogHelper.isLoggable(detailLog, detailLogLevel))
		{
			final int len = bytes.length;
			for (int i = 0; i < numOfBytes; i++)
			{
				final int idx = offset + i;
				if (idx < len)
				{
					log(bytes[idx]);
				}
			}
		}
		else
		{
			byteCount += numOfBytes;
		}
	}

	private void logAndCount(final char c)
	{
		buffer.append(c);
		byteCount++;
	}

	private void logAndDoNotCount(final String s)
	{
		buffer.append(s);
	}
	/**
	 * Log out the current buffer.
	 */
	public void flush()
	{
		if (buffer.length() > 0)
		{
			if (LogHelper.isLoggable(detailLog, detailLogLevel))
			{
				final String msg = byteCount + " bytes \"" + buffer.toString()
						+ "\"";
				logDetailLine(msg);
			}
			byteCount = 0;
			buffer = new StringBuffer();
		}
		if (byteCount > 0)
		{
			logDetailLine(byteCount + " bytes");
			byteCount = 0;
			buffer = new StringBuffer();
		}
	}

	/**
	 * Directly log msg, without current stack trace.
	 * 
	 * @param msg the message to be logged
	 */
	public void logSummaryLine(final String msg)
	{
		LogHelper.logLine(summaryLog, summaryLogLevel, msg, false, logMessagePrefix);
	}

	/**
	 * Directly log msg, without current stack trace.
	 * 
	 * @param msg the message to be logged.
	 */
	public void logDetailLine(final String msg)
	{
		LogHelper.logLine(detailLog, detailLogLevel, msg, false, logMessagePrefix);
	}

	public boolean isLogSingleBytesEnabled()
	{
		return logSingleBytes;
	}
}
