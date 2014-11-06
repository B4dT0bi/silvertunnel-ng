/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
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

package org.silvertunnel_ng.netlib.layer.tor.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.conn.util.InetAddressUtils;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compound data structure.
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author Tobias Boese
 */
public final class TCPStreamProperties
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TCPStreamProperties.class);

	/** The host which we want to connect to. */
	private String hostname;
	private InetAddress addr;
	/** set to true, if hostname is resolved into addr. */
	private boolean addrResolved;
	/** allow exit servers to be untrusted. */
	private boolean untrustedExitAllowed = true;
	/** allow entry node to be non Guard (dirv2). */
	private boolean nonGuardEntryAllowed = TorConfig.allowNonGuardEntry;
	/** 
	 * Is an exit policy required?
	 * 
	 * if not this Circuit is probably used for internal Communication.
	 */
	private boolean exitPolicyRequired = true;
	/** fast route preferred? (use only Router which are flagged as fast when set to true)*/
	private boolean fastRoute = true;
	/** use only stable flagged routers? */
	private boolean stableRoute = false;
	/** to which port should we connect? */
	private int port;
	/** minimum route length. */
	private int routeMinLength;
	/** maximum route length. */
	private int routeMaxLength;
	private int connectRetries;
	/** are we connecting to a dir server? */
	private boolean connectToDirServer = false;
	/**
	 * p = [0..1] 0 -&gt; select hosts completely randomly 1 -&gt; select hosts with
	 * good uptime/bandwidth with higher prob.
	 */
	private float rankingInfluenceIndex;
	/** custom/predefined route. */
	private Fingerprint[] route;

	/**
	 * preset the data structure with all necessary attributes.
	 * 
	 * @param host
	 *            give a hostname
	 * @param port
	 *            connect to this port
	 */
	public TCPStreamProperties(final String host, final int port)
	{
		this.hostname = host;
		this.port = port;
		addrResolved = false;

		init();
	}

	public TCPStreamProperties(final InetAddress addr, final int port)
	{
		this.hostname = addr.getHostAddress();
		this.addr = addr;
		this.port = port;
		addrResolved = true;

		init();
	}

	public TCPStreamProperties(final TcpipNetAddress address)
	{
		if (address.getIpaddress() != null)
		{
			// use IP address (preferred over host name)
			this.hostname = null;
			try
			{
				this.addr = InetAddress.getByAddress(address.getIpaddress());
			}
			catch (final UnknownHostException e)
			{
				LOG.warn("invalid address=" + address, e);
			}

			this.port = address.getPort();
			addrResolved = true;
		}
		else
		{
			// use host name
			this.hostname = address.getHostname();
			this.addr = null;
			this.port = address.getPort();
			addrResolved = false;
		}

		init();
	}

	public TCPStreamProperties()
	{
		this.hostname = null;
		this.addr = null;
		this.port = 0;
		addrResolved = false;

		init();
	}

	/** Default initialization of member variables. **/
	private void init()
	{
		routeMinLength = TorConfig.getRouteMinLength();
		routeMaxLength = TorConfig.getRouteMaxLength();
		rankingInfluenceIndex = 1;
		connectRetries = TorConfig.retriesStreamBuildup;
	}

	/**
	 * sets predefined route.
	 * 
	 * @param route
	 *            custom route (Fingerprints of the routers)
	 */
	public void setCustomRoute(final Fingerprint[] route)
	{
		this.route = route;
	}
	/** the custom exitpoint fingerprint. */
	private Fingerprint customExitpoint;
	/**
	 * Get the custom set exit point.
	 * 
	 * @return the {@link Fingerprint} of the custom exitpoint
	 */
	public Fingerprint getCustomExitpoint()
	{
		return customExitpoint;
	}
	/**
	 * sets this node as a predefined exit-point.
	 * 
	 * @param node
	 *            Fingerprint of the predefined exit-point router
	 */
	public void setCustomExitpoint(final Fingerprint node)
	{
		customExitpoint = node;
		if (route == null)
		{
			routeMinLength = routeMaxLength;
			route = new Fingerprint[routeMaxLength];
		}
		route[route.length - 1] = node;
	}

	/**
	 * @return predefined route
	 * 
	 */
	public Fingerprint[] getProposedRouteFingerprints()
	{
		return route;
	}

	/**
	 * @return hostname if set, in another case the IP.
	 */
	public String getDestination()
	{
		if (hostname.length() > 0)
		{
			return hostname;
		}
		return addr.getHostAddress();
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	/**
	 * @return rankingInfluenceIndex = [0..1] 0 -&gt; select hosts completely
	 *         randomly 1 -&gt; select hosts with good uptime/bandwidth with higher
	 *         prob.
	 */
	public float getRankingInfluenceIndex()
	{
		return rankingInfluenceIndex;
	}

	/**
	 * @param rankingInfluenceIndex
	 *            = [0..1] 0 -&gt; select hosts completely randomly 1 -&gt; select
	 *            hosts with good uptime/bandwidth with higher prob.
	 */
	public void setRankingInfluenceIndex(final float rankingInfluenceIndex)
	{
		this.rankingInfluenceIndex = rankingInfluenceIndex;
	}

	/**
	 * set minimum route length.
	 * 
	 * @param min
	 *            minimum route length
	 */
	public void setMinRouteLength(final int min)
	{
		if (min >= 0)
		{
			routeMinLength = min;
		}
	}

	/**
	 * set maximum route length.
	 * 
	 * @param max
	 *            maximum route length
	 */
	public void setMaxRouteLength(final int max)
	{
		if (max >= 0)
		{
			routeMaxLength = max;
		}
	}

	/**
	 * get minimum route length.
	 * 
	 * @return minimum route length
	 */
	public int getMinRouteLength()
	{
		return routeMinLength;
	}

	/**
	 * get maximum route length.
	 * 
	 * @return maximum route length
	 */
	public int getMaxRouteLength()
	{
		return routeMaxLength;
	}

	public String getHostname()
	{
		if (hostname == null && addr != null)
		{
			return addr.getHostAddress();
		}
		return hostname;
	}

	public void setAddr(final InetAddress addr)
	{
		this.addr = addr;
	}

	public InetAddress getAddr()
	{
		if (addr == null && hostname != null && !hostname.isEmpty() && InetAddressUtils.isIPv4Address(hostname))
		{
			try
			{
				String [] octets = hostname.split("\\.");
				byte [] ip = new byte [4];
				ip[0] = (byte) Integer.parseInt(octets[0]);
				ip[1] = (byte) Integer.parseInt(octets[1]);
				ip[2] = (byte) Integer.parseInt(octets[2]);
				ip[3] = (byte) Integer.parseInt(octets[3]);
				return InetAddress.getByAddress(ip);
			}
			catch (UnknownHostException e)
			{
				return addr;
			}
		}
		return addr;
	}

	public boolean isAddrResolved()
	{
		return addrResolved;
	}

	public void setAddrResolved(final boolean addrResolved)
	{
		this.addrResolved = addrResolved;
	}

	/**
	 * Can we use untrusted exit nodes?
	 * @return true if it is allowed to use untrusted existnodes
	 */
	public boolean isUntrustedExitAllowed()
	{
		return untrustedExitAllowed;
	}

	public void setUntrustedExitAllowed(final boolean untrustedExitAllowed)
	{
		this.untrustedExitAllowed = untrustedExitAllowed;
	}

	public boolean isNonGuardEntryAllowed()
	{
		return nonGuardEntryAllowed;
	}

	public void setNonGuardEntryAllowed(final boolean nonGuardEntryAllowed)
	{
		this.nonGuardEntryAllowed = nonGuardEntryAllowed;
	}

	public boolean isExitPolicyRequired()
	{
		return exitPolicyRequired;
	}

	public void setExitPolicyRequired(final boolean exitPolicyRequired)
	{
		this.exitPolicyRequired = exitPolicyRequired;
	}
	/**
	 * Get the port which we should connect to.
	 * 
	 * @return the port as integer
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Set the port which we should connect to.
	 * @param port the port as int
	 */
	public void setPort(final int port)
	{
		this.port = port;
	}

	public int getConnectRetries()
	{
		return connectRetries;
	}

	public void setConnectRetries(final int connectRetries)
	{
		this.connectRetries = connectRetries;
	}

	public Fingerprint[] getRouteFingerprints()
	{
		return route;
	}

	/**
	 * @return use only fast flagged routers for route creation?
	 */
	public boolean isFastRoute()
	{
		return fastRoute;
	}

	/**
	 * @param fastRoute true = use only Routers with flag fast for creating the route
	 */
	public void setFastRoute(final boolean fastRoute)
	{
		this.fastRoute = fastRoute;
	}

	/**
	 * @return use only stable flagged routers for route creation?
	 */
	public boolean isStableRoute()
	{
		if (stableRoute)
		{
			return true;
		}
		if (getPort() > 0)
		{
			if (TorConfig.getLongLivedPorts().contains(getPort()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param stableRoute true = use only Routers with flag stable for creating the route
	 */
	public void setStableRoute(final boolean stableRoute)
	{
		this.stableRoute = stableRoute;
	}

	/**
	 * @return are we connecting to a directory server?
	 */
	public boolean isConnectToDirServer()
	{
		return connectToDirServer;
	}

	/**
	 * @param connectToDirServer are we connecting to a directory server?
	 */
	public void setConnectToDirServer(final boolean connectToDirServer)
	{
		this.connectToDirServer = connectToDirServer;
	}
}
