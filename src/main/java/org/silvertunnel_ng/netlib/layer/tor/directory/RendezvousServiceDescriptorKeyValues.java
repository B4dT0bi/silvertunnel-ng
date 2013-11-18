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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import org.silvertunnel_ng.netlib.util.ByteArrayUtil;

/**
 * Some important values of a RendezvousServiceDescriptor.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class RendezvousServiceDescriptorKeyValues
{
	private int timePeriod;
	private byte[] secretIdPart;
	private byte[] descriptorId;

	@Override
	public String toString()
	{
		return "timePeriod=" + timePeriod + ",secretIdPart="
				+ ByteArrayUtil.showAsString(secretIdPart) + ",descriptorId="
				+ ByteArrayUtil.showAsString(descriptorId);
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	public int getTimePeriod()
	{
		return timePeriod;
	}

	public void setTimePeriod(final int timePeriod)
	{
		this.timePeriod = timePeriod;
	}

	public byte[] getSecretIdPart()
	{
		return secretIdPart;
	}

	public void setSecretIdPart(final byte[] secretIdPart)
	{
		this.secretIdPart = secretIdPart;
	}

	public byte[] getDescriptorId()
	{
		return descriptorId;
	}

	public void setDescriptorId(byte[] descriptorId)
	{
		this.descriptorId = descriptorId;
	}
}
