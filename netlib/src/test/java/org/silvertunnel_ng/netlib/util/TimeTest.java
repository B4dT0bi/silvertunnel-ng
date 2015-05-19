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

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.nameservice.inetaddressimpl.DefaultIpNetAddressNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.AssertJUnit.fail;

/**
 * Check the atomic time against system time.
 *
 * @author Tobias Boese
 */
public final class TimeTest {
    /**
     * class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TimeTest.class);

    /**
     * maximum allowed difference in ms.
     */
    private static final long DIFFERENCE_OK = 30L * 60L * 1000L; // 30 minutes

    @BeforeClass
    public void setUp() {
        NameServiceGlobalUtil.initNameService();
        NameServiceGlobalUtil.setIpNetAddressNameService(DefaultIpNetAddressNameService.getInstance());
    }

    /**
     * Gets the current difference between system time and atomic time.
     */
    @Test(timeOut = 10000)
    public void testTimeDifference() {
        try {
            final long difference = AtomicTime.getTimeDiffinMillis();
            if (difference > DIFFERENCE_OK) {
                fail("time difference is " + difference + " ms. (this difference could lead to problems with the hidden services)");
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            fail("could not get time from atomic time server.");
        }

    }

}
