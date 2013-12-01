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
package org.silvertunnel_ng.netlib.layer.tor.circuit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Map;

import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.testng.annotations.Test;


/**
 * Testing the {@link CircuitHistory} class.
 * 
 * @author Tobias Boese
 */
public final class CircuitHistoryLocalTest
{

	/**
	 * Test method for {@link CircuitHistory#addCircuit(Circuit)}.
	 * @throws Exception should not happen
	 */
	@Test
	public void testAddCircuit() throws Exception
	{
		CircuitHistory history = new CircuitHistory();
		assertNotNull(history);
		assertEquals(0, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		streamProperties.setConnectToTorIntern(true);
				
		history.addCircuit(streamProperties);
		assertEquals(0, history.getCountExternal());
		assertEquals(1, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertFalse(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
	}

	/**
	 * Test method for {@link CircuitHistory#getCountInternal()}.
	 * @throws Exception should not happen
	 */
	@Test
	public void testGetCountInternal() throws Exception
	{
		CircuitHistory history = new CircuitHistory();
		assertNotNull(history);
		assertEquals(0, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		streamProperties.setConnectToTorIntern(true);

		history.addCircuit(streamProperties);
		assertEquals(0, history.getCountExternal());
		assertEquals(1, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertFalse(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitHistory#getCountExternal()}.
	 */
	@Test
	public void testGetCountExternal()
	{
		CircuitHistory history = new CircuitHistory();
		assertNotNull(history);
		assertEquals(0, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		streamProperties.setConnectToTorIntern(false);
		streamProperties.setPort(22);

		history.addCircuit(streamProperties);
		assertEquals(1, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertFalse(history.getMapHistoricPorts().isEmpty());
		assertFalse(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertFalse(history.getMapCurrentHistoricPorts().isEmpty());
	}

	/**
	 * Test method for {@link CircuitHistory#getMapCountInternal()}.
	 * @throws Exception should not happen
	 */
	@Test
	public void testGetMapCountInternal() throws Exception
	{
		CircuitHistory history = new CircuitHistory();
		assertNotNull(history);
		assertEquals(0, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		streamProperties.setConnectToTorIntern(true);
		
		history.addCircuit(streamProperties);
		assertEquals(0, history.getCountExternal());
		assertEquals(1, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertFalse(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		Map<Long, Integer> mapTemp = history.getMapCountInternal();
		assertNotNull(mapTemp);
		assertEquals(1, mapTemp.entrySet().iterator().next().getValue().intValue());
	}

	/**
	 * Test method for {@link CircuitHistory#getMapCountExternal()}.
	 * @throws Exception should not happen
	 */
	@Test
	public void testGetMapCountExternal() throws Exception
	{
		CircuitHistory history = new CircuitHistory();
		assertNotNull(history);
		assertEquals(0, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertTrue(history.getMapHistoricPorts().isEmpty());
		assertTrue(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertTrue(history.getMapCurrentHistoricPorts().isEmpty());
		
		TCPStreamProperties streamProperties = new TCPStreamProperties();
		streamProperties.setConnectToTorIntern(false);
		streamProperties.setPort(22);
		
		history.addCircuit(streamProperties);
		assertEquals(1, history.getCountExternal());
		assertEquals(0, history.getCountInternal());
		assertFalse(history.getMapHistoricPorts().isEmpty());
		assertFalse(history.getMapCountExternal().isEmpty());
		assertTrue(history.getMapCountInternal().isEmpty());
		assertFalse(history.getMapCurrentHistoricPorts().isEmpty());
		Map<Long, Integer> mapTemp = history.getMapCountExternal();
		assertNotNull(mapTemp);
		assertEquals(1, mapTemp.entrySet().iterator().next().getValue().intValue());
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitHistory#getMapHistoricPorts()}.
	 */
	@Test(enabled = false)
	public void testGetMapHistoricPorts()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitHistory#getMapCurrentHistoricPorts()}.
	 */
	@Test(enabled = false)
	public void testGetMapCurrentHistoricPorts()
	{
		fail("Not yet implemented");
	}

}
