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

package org.silvertunnel_ng.netlib.layer.tor.api;

import org.silvertunnel_ng.netlib.util.ConvenientStreamWriter;

import java.io.IOException;

/**
 * Compound data structure for storing exit policies.
 * 
 * An object is read-only.
 * 
 * @author hapke
 */
public interface RouterExitPolicy
{
	/**
	 * Clone, but do not throw an exception.
	 */
	RouterExitPolicy cloneReliable() throws RuntimeException;

	@Override
	String toString();

	boolean isAccept();

	long getIp();

	long getNetmask();

	int getLoPort();

	int getHiPort();

    void save(ConvenientStreamWriter convenientStreamWriter) throws IOException;
}
