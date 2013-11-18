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

package org.silvertunnel_ng.netlib.api;

import static org.testng.AssertJUnit.assertEquals;

import org.silvertunnel_ng.netlib.layer.buffered.BufferedNetLayer;
import org.silvertunnel_ng.netlib.layer.echo.EchoNetLayer;
import org.silvertunnel_ng.netlib.layer.modification.AddModificator;
import org.silvertunnel_ng.netlib.layer.modification.ModificatorNetLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author hapke
 * @author Tobias Boese
 *
 */
public class ApiClientLocalTest
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(ApiClientLocalTest.class);

	/**
	 * Setup the Testcase.
	 * @throws Exception
	 */
	@BeforeClass
	public void setUp() throws Exception
	{
		// create layer for modify_over_echo
		final NetLayer echoNetLayer = new EchoNetLayer();
		final NetLayer modificatorLayer = new ModificatorNetLayer(echoNetLayer,
				new AddModificator(1), new AddModificator(3));
		final NetLayer bufferedNetLayer = new BufferedNetLayer(modificatorLayer);
		final NetLayer modificatorLayer1a = new ModificatorNetLayer(
				bufferedNetLayer, new AddModificator(-3), new AddModificator(3 - 3));
		NetFactory.getInstance().registerNetLayer(NetLayerIDs.MODIFY_OVER_ECHO, modificatorLayer1a);
	}

	@Test
	public void testLayerModifyOverEcho() throws Exception
	{
		// create connection
		final NetSocket topSocket = NetFactory.getInstance()
				.getNetLayerById(NetLayerIDs.MODIFY_OVER_ECHO)
				.createNetSocket(null, (NetAddress) null, (NetAddress) null);

		// write data
		final String dataToSend = "hello1 world sent data";
		topSocket.getOutputStream().write(dataToSend.getBytes());
		topSocket.getOutputStream().flush();

		// read (result) data
		final byte[] resultBuffer = new byte[10000];
		final int len = topSocket.getInputStream().read(resultBuffer);
		final String result = new String(resultBuffer).substring(0, len);

		// close connection
		topSocket.close();

		// show and check result data
		LOG.info("result=\"" + result + "\"");
		assertEquals("got wrong result", "ifmmp2!xpsme!tfou!ebub", result);
	}
}
