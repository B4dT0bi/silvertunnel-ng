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
package org.silvertunnel_ng.netlib.layer.tor.circuit.cells;

import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtil;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtilLocalTest;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Node;
import org.silvertunnel_ng.netlib.layer.tor.hiddenservice.HiddenServiceProperties;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.FileUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Testing the correct creation of the CellRelayEstablishIntro.
 *
 * @author Tobias Boese
 */
public class CellRelayEstablishIntroTest {
    private static final String OLD_HIDDEN_SERVICE_PRIVATE_KEY_PEM_PATH = TorNetLayerUtilLocalTest.EXAMPLE_PRIVATE_KEY_PEM_PATH;

    private static final byte[] TEST_KEY = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

    private static final byte[] EXPECTED = new byte[]{0, -116, 48, -127, -119, 2, -127, -127, 0, -71, -12, 7, -33, -89, 40, -48, -24, -78, -62, 22, 77, 61, 12, -72, -86, 10, 6, -128, 69, 23, -75, -63, 37, 49, -100, 55, -101, 59, 113, -25, 49, -111, -127, 109, -118, -107, -75, -14, 66, 78, -15, 123, 4, 60, 8, -13, 114, -68, -79, 103, -10, 48, 106, 58, -63, 103, 68, 84, -73, -93, 61, -37, 103, 111, 95, -47, -103, 112, -96, -126, -64, -15, 95, -128, -104, -101, 79, 44, -60, -67, -7, -21, 67, 2, -26, 26, -94, -88, -11, -87, 120, -77, 47, -105, 107, -50, -81, 125, 45, -116, -11, -127, -119, -126, -69, -88, 25, -79, -22, -96, -106, -48, -86, 94, -32, -95, -115, -82, -86, -119, -44, 68, 52, -45, 53, 88, 53, 2, 3, 1, 0, 1, 87, -57, -79, -108, 9, -18, 77, -15, 74, -78, 123, -85, -124, 127, 122, -4, 120, 122, 95, 71, 118, 1, 95, 10, 123, 20, -44, 2, 116, 1, -31, 14, 104, 84, 73, -95, 22, -21, -8, -59, -13, 87, -36, -75, 43, 104, 99, -15, -73, 64, -112, 30, -115, -123, 64, -63, -30, 39, 26, -123, 123, -22, -38, -68, 91, 78, -118, 117, -68, 12, 50, 106, -20, -69, 78, -86, 25, -55, -78, 61, -128, 7, 68, -38, -17, -34, 3, 74, 10, 122, 18, 38, 119, 35, -88, 51, -48, 16, 72, 19, -92, 29, 24, 118, -71, -64, 4, -32, -126, -85, -69, 74, 22, -56, -23, 12, 127, -106, 32, -111, -107, 123, 47, 105, 104, 33, 43, 90, -112, 20, -24, 97, 78, 39, -126, 27, -43, -93, -86, 109, -13, 74, 5, -127, -126, -74, -36, 76, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Test
    public void testCellCreation() throws IOException, TorException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Constructor<Node> nodeConstructor = Node.class.getDeclaredConstructor();
        nodeConstructor.setAccessible(true);
        Node node = nodeConstructor.newInstance();
        Field nodeKeyHandshakeField = node.getClass().getDeclaredField("keyHandshake");
        nodeKeyHandshakeField.setAccessible(true);
        nodeKeyHandshakeField.set(node, TEST_KEY);

        // check if setting the KeyHandshake with reflections worked
        Assert.assertEquals(TEST_KEY, node.getKeyHandshake());

        Constructor<Circuit> circuitConstructor = Circuit.class.getDeclaredConstructor();
        circuitConstructor.setAccessible(true);
        Circuit circuit = circuitConstructor.newInstance();
        circuit.addNode(node);

        Field circuitIdField = circuit.getClass().getDeclaredField("circuitId");
        circuitIdField.setAccessible(true);
        circuitIdField.setInt(circuit, 2203);

        // check if setting the Circuit values with reflections worked
        Assert.assertEquals(2203, circuit.getId());
        Assert.assertNotNull(circuit.getLastRouteNode());
        Assert.assertEquals(TEST_KEY, circuit.getLastRouteNode().getKeyHandshake());

        TorNetLayerUtil torNetLayerUtil = TorNetLayerUtil.getInstance();


        String privateKeyPEMStr = FileUtil.readFileFromClasspath(OLD_HIDDEN_SERVICE_PRIVATE_KEY_PEM_PATH);
        TorHiddenServicePrivateNetAddress netAddressWithoutPort = torNetLayerUtil
                .parseTorHiddenServicePrivateNetAddressFromStrings(privateKeyPEMStr, null, false);

        HiddenServiceProperties service = new HiddenServiceProperties(2203, netAddressWithoutPort.getKeyPair());

        CellRelayEstablishIntro cellRelayEstablishIntro = new CellRelayEstablishIntro(circuit, service);

        Assert.assertEquals(CellRelay.RELAY_ESTABLISH_INTRO, cellRelayEstablishIntro.getRelayCommand());
        Assert.assertEquals(2203, cellRelayEstablishIntro.getCircuitId());
        Assert.assertEquals(EXPECTED, cellRelayEstablishIntro.getData());
        System.out.println(Arrays.toString(cellRelayEstablishIntro.getData()));

        System.out.println(Arrays.toString(service.getPrivateKey().getEncoded()));

        byte [] toSign = new byte [] {0, -116, 48, -127, -119, 2, -127, -127, 0, -71, -12, 7, -33, -89, 40, -48, -24, -78, -62, 22, 77, 61, 12, -72, -86, 10, 6, -128, 69, 23, -75, -63, 37, 49, -100, 55, -101, 59, 113, -25, 49, -111, -127, 109, -118, -107, -75, -14, 66, 78, -15, 123, 4, 60, 8, -13, 114, -68, -79, 103, -10, 48, 106, 58, -63, 103, 68, 84, -73, -93, 61, -37, 103, 111, 95, -47, -103, 112, -96, -126, -64, -15, 95, -128, -104, -101, 79, 44, -60, -67, -7, -21, 67, 2, -26, 26, -94, -88, -11, -87, 120, -77, 47, -105, 107, -50, -81, 125, 45, -116, -11, -127, -119, -126, -69, -88, 25, -79, -22, -96, -106, -48, -86, 94, -32, -95, -115, -82, -86, -119, -44, 68, 52, -45, 53, 88, 53, 2, 3, 1, 0, 1, 87, -57, -79, -108, 9, -18, 77, -15, 74, -78, 123, -85, -124, 127, 122, -4, 120, 122, 95, 71};
        byte [] signature = Encryption.signData(toSign, service.getPrivateKey());

        System.out.println(Arrays.toString(signature));
    }
}
