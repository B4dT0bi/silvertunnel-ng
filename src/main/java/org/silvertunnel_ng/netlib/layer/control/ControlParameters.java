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
package org.silvertunnel_ng.netlib.layer.control;

/**
 * Parameters of ControlNetLayer.
 * 
 * @author hapke
 */
public class ControlParameters
{
	//
	// possible values
	//

	/** default connect timeout: 2 minutes. */
	public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 120L * 1000L;
	public static final long UNLIMITED_TIMOUT_MILLIS = 0;
	/** long connect timeout (e.g. on tunneled traffic) : 5 minutes */
	public static final long LONG_CONNECT_TIMEOUT_MILLIS = 300L * 1000L;
	/** default max. connection timeout: 60 minutes */
	public static final long DEFAULT_OVERALL_TIMEOUT_MILLIS = 60L * 60L * 1000L;
	/**
	 * default max. bytes to transfer (to avoid endless transfers and
	 * out-of-memory problems): 50 MByte
	 */
	public static final long DEFAULT_MAX_FILETRANSFER_BYTES = 50L * 1024L * 1024L;
	public static final long UNLIMITED_MAX_FILETRANSFER_BYTES = 0L;
	/** default minimum throughput: 60 KBytes / 60 seconds */
	public static final long DEFAUT_THROUGPUT_TIMEFRAME_MIN_BYTES = 60L * 1024L;
	public static final long UNLIMITED_THROUGPUT_TIMEFRAME_MIN_BYTES = 0L;
	/** default minimum throughput: 60 KBytes / 60 seconds */
	public static final long DEFAUT_THROUGPUT_TIMEFRAME_MILLIS = 60L * 1000L;

	//
	// current settings
	//

	// for connection setup
	private long connectTimeoutMillis;
	// for full connection:
	private long overallTimeoutMillis;
	private long inputMaxBytes;
	private long throughputTimeframeMinBytes;
	private long throughputTimeframeMillis;

	protected ControlParameters()
	{
	}

	// /////////////////////////////////////////////////////
	// typical parameter settings
	// /////////////////////////////////////////////////////

	/**
	 * @return new parameter object - for a file download with 50 MBytes maximum
	 */
	public static ControlParameters createTypicalFileTransferParameters()
	{
		final ControlParameters p = new ControlParameters();

		p.connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
		p.overallTimeoutMillis = DEFAULT_OVERALL_TIMEOUT_MILLIS;
		p.inputMaxBytes = DEFAULT_MAX_FILETRANSFER_BYTES;
		p.throughputTimeframeMinBytes = DEFAUT_THROUGPUT_TIMEFRAME_MIN_BYTES;
		p.throughputTimeframeMillis = DEFAUT_THROUGPUT_TIMEFRAME_MILLIS;

		return p;
	}

	/**
	 * @return new parameter object - for very long running streaming
	 *         connections, some continuous throughput is expected
	 */
	public static ControlParameters createTypicalStreamingParameters()
	{
		final ControlParameters p = new ControlParameters();

		p.connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
		p.overallTimeoutMillis = UNLIMITED_TIMOUT_MILLIS;
		p.inputMaxBytes = UNLIMITED_MAX_FILETRANSFER_BYTES;
		p.throughputTimeframeMinBytes = DEFAUT_THROUGPUT_TIMEFRAME_MIN_BYTES;
		p.throughputTimeframeMillis = DEFAUT_THROUGPUT_TIMEFRAME_MILLIS;

		return p;
	}

	/**
	 * @return new parameter object - without any limits
	 */
	public static ControlParameters createUnlimitedParameters()
	{
		final ControlParameters p = new ControlParameters();

		p.connectTimeoutMillis = UNLIMITED_TIMOUT_MILLIS;
		p.overallTimeoutMillis = UNLIMITED_TIMOUT_MILLIS;
		p.inputMaxBytes = UNLIMITED_MAX_FILETRANSFER_BYTES;
		p.throughputTimeframeMinBytes = UNLIMITED_THROUGPUT_TIMEFRAME_MIN_BYTES;
		p.throughputTimeframeMillis = DEFAUT_THROUGPUT_TIMEFRAME_MILLIS;

		return p;
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	public long getConnectTimeoutMillis()
	{
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(long connectTimeoutMillis)
	{
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public long getOverallTimeoutMillis()
	{
		return overallTimeoutMillis;
	}

	public void setOverallTimeoutMillis(long overallTimeoutMillis)
	{
		this.overallTimeoutMillis = overallTimeoutMillis;
	}

	public long getInputMaxBytes()
	{
		return inputMaxBytes;
	}

	public void setInputMaxBytes(long inputMaxBytes)
	{
		this.inputMaxBytes = inputMaxBytes;
	}

	public long getThroughputTimeframeMillis()
	{
		return throughputTimeframeMillis;
	}

	public void setThroughputTimeframeMillis(long throughputTimeframeMillis)
	{
		this.throughputTimeframeMillis = throughputTimeframeMillis;
	}

	public long getThroughputTimeframeMinBytes()
	{
		return throughputTimeframeMinBytes;
	}

	public void setThroughputTimeframeMinBytes(long throughputTimeframeMinBytes)
	{
		this.throughputTimeframeMinBytes = throughputTimeframeMinBytes;
	}
}
