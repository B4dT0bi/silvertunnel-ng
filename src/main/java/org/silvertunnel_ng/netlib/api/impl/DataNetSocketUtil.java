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

package org.silvertunnel_ng.netlib.api.impl;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class DataNetSocketUtil
{

	public static DataNetSocketPair createDataNetSocketPair()
			throws IOException
	{
		final DataNetSocketPair result = new DataNetSocketPair();

		// create stream from higher layer
		final PipedInputStream fromHigherLayerIS = new PipedInputStream();
		final PipedOutputStream fromHigherLayerOS = new PipedOutputStream(
				fromHigherLayerIS);

		// stream to higher layer
		final PipedInputStream toHigherLayerIS = new PipedInputStream();
		final PipedOutputStream toHigherLayerOS = new PipedOutputStream(
				toHigherLayerIS);

		// create socket provided to higher layer
		result.setSocket(new DataNetSocketImpl(toHigherLayerIS,
				fromHigherLayerOS));
		result.setInvertedSocked(new DataNetSocketImpl(fromHigherLayerIS,
				toHigherLayerOS));

		return result;
	}
}
