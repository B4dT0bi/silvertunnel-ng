/*
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
package org.silvertunnel_ng.netlib.layer.tor.common;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.circuit.CircuitHistory;
import org.silvertunnel_ng.netlib.layer.tor.directory.FingerprintImpl;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.util.SystemPropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// TODO : implement bridge connect
// TODO : implement ExcludeSingleHopRelays (torrc)
/**
 * Global configuration of TorNetLayer.
 * 
 * @author Lexi
 * @author Michael Koellejan
 * @author Andriy Panchenko
 * @author hapke
 * @author Tobias Boese
 */
public final class TorConfig
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(TorConfig.class);

	/** instance to {@link TorConfig}. */
	private static TorConfig instance;

	/** Use Create_Fast Cells instead of normal Create Cells? */
	public static final boolean USE_CREATE_FAST_CELLS = true;
	/**
	 * Startup delay in seconds.
	 * 
	 * How long should we wait before trying the first connects?
	 */
	private int startupDelaySeconds = 20;
	/**
	 * Default list of "long-lived" ports listed in path-spec 2.2. a circuit needs to be
	 * "stable" for these ports. 
	 */
	private static final int[] DEFAULT_LONG_LIVED_PORTS = { 21, 22, 706, 1863, 5050, 5190, 5222, 5223, 6667, 6697, 8300 };
	/**
	 * List of "long-lived" ports listed in path-spec 2.2. a circuit needs to be
	 * "stable" for these ports. 
	 */
	private Set<Integer> longLivedPorts = new HashSet<Integer>();
	/**
	 * Add a port to the long lived ports list.
	 * @param port the port to be added
	 */
	public static void addLongLivedPort(final int port)
	{
		getInstance().longLivedPorts.add(port);
	}
	/**
	 * Set the list of long lived ports to the given list.
	 * @param list the list containing the long lived ports
	 */
	public static void setLongLivedPorts(final Set<Integer> list)
	{
		getInstance().longLivedPorts = list;
	}
	/**
	 * Get the list of long lived ports.
	 * @return a Set of long lived ports.
	 */
	public static Set<Integer> getLongLivedPorts()
	{
		return getInstance().longLivedPorts;
	}
	/** @return get an instance of {@link TorConfig}. */
	private static synchronized TorConfig getInstance()
	{
		if (instance == null)
		{
			instance = new TorConfig();
		}
		return instance;
	}

	/**
	 * How long should we wait before trying the first connects?
	 * 
	 * @return the startup delay in seconds.
	 */
	public static int getStartupDelay()
	{
		return getInstance().startupDelaySeconds;
	}

	/**
	 * How long should we wait before trying the first connects?
	 * 
	 * @param delay
	 *            the startup delay in seconds.
	 */
	public static void setStartupDelay(final int delay)
	{
		getInstance().startupDelaySeconds = delay;
	}

	// QoS-parameters
	/** How many times should we try to connect? */
	private int retriesConnect = 1;
	/** How many times should we try to reconnect a Circuit before failing? */
	private int reconnectCircuit = 1;
	public static int retriesStreamBuildup = 5;

	/** How many circuits should be allowed for idling as max? */
	private static final int MAXIMUM_IDLE_CIRCUITS = 20; // TODO : verify this value
	/**
	 * How many circuit should be idling ?
	 * 
	 * recommended value : 3 
	 * maximum value : 20 // TODO : verify or set a good max value
	 * 
	 * if this value is 0 then tor will only open circuits when a connection is
	 * needed, but in this case the connection will take longer as the circuit
	 * needs to be built first.
	 * 
	 * default value : 3
	 */
	private int minimumIdleCircuits = 3;

	/**
	 * @return get the amount of retries for establishing a connection.
	 */
	public static int getRetriesConnect()
	{
		return getInstance().retriesConnect;
	}

	/**
	 * Set the amount of connect retries.
	 * 
	 * Should be a value higher than 0
	 * 
	 * @param retries
	 *            max retry count for a connection
	 */
	public static void setRetriesConnect(final int retries)
	{
		if (retries <= 0)
		{
			LOG.warn("setRetriesConnect : wrong value for retriesConnect found!");
			return; // keep the old value
		}
		if (retries > 20)
		{
			LOG.warn("setRetriesConnect : number of retries could be to high.");
		}
		getInstance().retriesConnect = retries;
	}

	/**
	 * @return get the amount of retries for reconnecting a circuit.
	 */
	public static int getReconnectCircuit()
	{
		return getInstance().reconnectCircuit;
	}

	/**
	 * Set the amount of reconnects for a circuit.
	 * 
	 * Should be a value higher than 0
	 * 
	 * @param reconnects
	 *            max reconnects for a circuit
	 */
	public static void setReconnectCircuit(final int reconnects)
	{
		if (reconnects <= 0)
		{
			LOG.warn("setReconnectCircuit : wrong value for reconnectCircuit found!");
			return; // keep the old value
		}
		if (reconnects > 10)
		{
			LOG.warn("setReconnectCircuit : number of reconnects could be to high.");
		}
		getInstance().reconnectCircuit = reconnects;
	}

	/**
	 * @return get the number of minimum idle circuits.
	 */
	public static int getMinimumIdleCircuits()
	{
		return getInstance().minimumIdleCircuits;
	}

	/**
	 * Set the minimum number of idling circuits.
	 * <br><br>
	 * recommended value : 3<br> 
	 * maximum value : 20 // TODO : verify or set a good max value
	 * <br><br>
	 * if this value is 0 then tor will only open circuits when a connection is
	 * needed, but in this case the connection will take longer as the circuit
	 * needs to be built first.
	 * <br><br>
	 * default value : 3
	 * <br><br>
	 * if the value is not allowed (< 0 or > MAXIMUM_IDLE_CIRCUITS) the value
	 * will be either set to 0 or to MAXIMUM_IDLE_CIRCUITS and a warning will be
	 * logged.
	 * 
	 * @param nrOfCircuits
	 *            the minimum number of idling circuits
	 */
	public static void setMinimumIdleCircuits(final int nrOfCircuits)
	{
		if (nrOfCircuits < 0)
		{
			LOG.warn("setMinimumIdleCircuits : value should not be lower than 0. setting minimumIdleCircuits to 0!");
			setMinimumIdleCircuits(0);
		}
		if (nrOfCircuits > MAXIMUM_IDLE_CIRCUITS)
		{
			LOG.warn("setMinimumIdleCircuits : value should not be greater than "
					+ MAXIMUM_IDLE_CIRCUITS
					+ ". setting minimumIdleCircuits to "
					+ MAXIMUM_IDLE_CIRCUITS + "!");
			setMinimumIdleCircuits(MAXIMUM_IDLE_CIRCUITS);
		}
		getInstance().minimumIdleCircuits = nrOfCircuits;
	}

	/** prefix for System properties. */
	public static final String SYSTEMPROPERTY_TOR_PREFIX = "silvertunnel-ng.tor.";
	/** identifier for System properties. */
	public static final String SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS = SYSTEMPROPERTY_TOR_PREFIX
			+ "minimumIdleCircuits";
	/** identifier for min. Route length System property. */
	public static final String SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH = SYSTEMPROPERTY_TOR_PREFIX
			+ "minimumRouteLength";
	/** identifier for max. Route length System property. */
	public static final String SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH = SYSTEMPROPERTY_TOR_PREFIX
			+ "maximumRouteLength";
	/** identifier for System property @see caching Hidden service descriptor. */
	public static final String SYSTEMPROPERTY_TOR_CACHE_HS_DESCRIPTOR = SYSTEMPROPERTY_TOR_PREFIX
			+ "cacheHiddenServiceDescriptor";
	/** identifier for System properties. */
	public static final String SYSTEMPROPERTY_TOR_MAX_ALLOWED_SETUP_DURATION_MS = SYSTEMPROPERTY_TOR_PREFIX
			+ "maxAllowedSetupDurationMs";
	
	public static int queueTimeoutCircuit = 10; // was 20
	public static int queueTimeoutResolve = 5; // was 10
	/* TODO was: 11 */
	public static int queueTimeoutStreamBuildup = 5;

	/**
	 * How many stream failures are allowed till we close the Circuit?
	 * 
	 * default : 3
	 */
	private int circuitClosesOnFailures = 3;
	/**
	 * How many stream failures are allowed till we close the Circuit?

	 * @return the circuitClosesOnFailures
	 */
	public static int getCircuitClosesOnFailures()
	{
		return getInstance().circuitClosesOnFailures;
	}

	/**
	 * How many stream failures are allowed till we close the Circuit?
	 *
	 * default : 3
	 * 
	 * @param circuitClosesOnFailures the circuitClosesOnFailures to set
	 */
	public static void setCircuitClosesOnFailures(final int circuitClosesOnFailures)
	{
		getInstance().circuitClosesOnFailures = circuitClosesOnFailures;
	}

	public static int circuitsMaximumNumber = 30;
	public static long maxAllowedSetupDurationMs = 10000;

	/** 0..1 . */
	public static float rankingTransferPerServerUpdate = 0.95f;

	/** this is a truly asocial way of building streams!! */
	private boolean veryAggressiveStreamBuilding = false;

	/**
	 * @return the veryAggressiveStreamBuilding
	 */
	public static boolean isVeryAggressiveStreamBuilding()
	{
		return getInstance().veryAggressiveStreamBuilding;
	}

	/**
	 * @param veryAggressiveStreamBuilding the veryAggressiveStreamBuilding to set
	 */
	public static void setVeryAggressiveStreamBuilding(final boolean veryAggressiveStreamBuilding)
	{
		getInstance().veryAggressiveStreamBuilding = veryAggressiveStreamBuilding;
	}

	// directory parameters
	/** Interval of the Directory refresh in minutes. */
	private int intervalDirectoryRefresh = 5;

	/**
	 * Gets the amount of time to wait for a new Directory refresh check.
	 * @return the intervalDirectoryRefresh in minutes
	 */
	public static int getIntervalDirectoryRefresh()
	{
		return getInstance().intervalDirectoryRefresh;
	}

	/**
	 * @param intervalDirectoryRefresh the intervalDirectoryRefresh to set
	 */
	public static void setIntervalDirectoryRefresh(final int intervalDirectoryRefresh)
	{
		getInstance().intervalDirectoryRefresh = intervalDirectoryRefresh;
	}

	/** to access directory servers: connect timeout: 10 seconds. */
	public static final long DIR_CONNECT_TIMEOUT_MILLIS = 10L * 1000L;
	/** to access directory servers: max. connection timeout: 30 seconds */
	public static final long DIR_OVERALL_TIMEOUT_MILLIS = 30L * 1000L;
	/**
	 * to access directory servers: max. bytes to transfer (to avoid endless
	 * transfers and out-of-memory problems): 50 MByte
	 */
	public static final long DIR_MAX_FILETRANSFER_BYTES = 50L * 1024L * 1024L;
	/** to access directory servers: minimum throughput: 15 KBytes / 15 seconds. */
	public static final long DIR_THROUGPUT_TIMEFRAME_MIN_BYTES = 15L * 1024L;
	// public static long DIR_THROUGPUT_TIMEFRAME_MIN_BYTES = 6000L*1024L;//
	// TODO: "very fast" parameter
	/** to access directory servers: minimum throughput: 15 KBytes / 15 seconds. */
	public static final long DIR_THROUGPUT_TIMEFRAME_MILLIS = 15L * 1000L;
	// public static long DIR_THROUGPUT_TIMEFRAME_MILLIS = 1L*1000L; // TODO:
	// "very fast" parameter

	/** QoS-parameter, see updateRanking in Circuit.java. */
	public static final int CIRCUIT_ESTABLISHMENT_TIME_IMPACT = 5;

	// Security parameters
	/** How many streams are allowed in one Circuit? */
	private int streamsPerCircuit = 65535;
	/**
	 * How many streams are allowed in one Circuit?
	 * 
	 * @return the number of allowed streams
	 */
	public static int getStreamsPerCircuit()
	{
		return getInstance().streamsPerCircuit;
	}
	/**
	 * Set the maximum allowed streams per circuit.
	 * 
	 * @param streams the number of streams allowed in one circuit
	 */
	public static void setStreamsPerCircuit(final int streams)
	{
		if (streams <= 0)
		{
			LOG.error("it is not allowed to set the number of streams in a circuit lower than 1!");
		}
		else
		{
			LOG.debug("setting streamsPerCircuit from {} to {}", new Object[] {getInstance().streamsPerCircuit, streams});
			getInstance().streamsPerCircuit = streams;
		}
	}
	/** see Server.getRefinedRankingIndex. */
	public static float rankingIndexEffect = 0.9f;

	/** minimum allowed route length for creating a circuit. */
	private static final int MINIMUM_ROUTE_LENGTH = 2;
	/** maximum allowed route length for creating a circuit. */
	private static final int MAXIMUM_ROUTE_LENGTH = 8;
	/** default route length for creating a circuit. */
	private static final int DEFAULT_ROUTE_LENGTH = 3;

	/**
	 * minimum circuit path length. 
	 * 
	 * recommended value : 3 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) 
	 * using a value of 2 is only good for a simple IP obfuscation,
	 * for more security a value of at least 3 is recommended
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * 
	 * default value : 3
	 */
	private int routeMinLength = DEFAULT_ROUTE_LENGTH;

	/**
	 * @return get the minimum allowed route length for creating a circuit.
	 */
	public static int getRouteMinLength()
	{
		return getInstance().routeMinLength;
	}

	/**
	 * set the minimum circuit path length. 
	 * <br><br>
	 * recommended value : 3<br> 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP
	 * obfuscation, for more security a value of at least 3 is recommended<br>
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)<br>
	 * <br>
	 * default value : 3<br>
	 * 
	 * @param length
	 *            the desired minimum length Logs a message as WARNING in case
	 *            of wrong value
	 */
	public static void setRouteMinLength(final int length)
	{
		if (length < MINIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length has to be at least {}", MINIMUM_ROUTE_LENGTH);
			return;
		}
		if (length > MAXIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length should not exceed {}", MAXIMUM_ROUTE_LENGTH);
			return;
		}
		if (length > getInstance().routeMaxLength)
		{
			LOG.info("setRouteMinLength: length ({}) is smaller than current maxlen ({}). Setting maxlen to given value.", 
			         new Object[]{length, getInstance().routeMaxLength});
			getInstance().routeMaxLength = length;
		}
		getInstance().routeMinLength = length;
	}

	/**
	 * maximum circuit path length. 
	 * <br><br>
	 * recommended value : 3<br> 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP obfuscation,
	 * for more security a value of at least 3 is recommended<br> 
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * <br><br>
	 * default value : 3
	 */
	private int routeMaxLength = DEFAULT_ROUTE_LENGTH;

	/**
	 * @return get the minimum allowed route length for creating a circuit.
	 */
	public static int getRouteMaxLength()
	{
		return getInstance().routeMaxLength;
	}

	/**
	 * set the maximum circuit path length. 
	 * <br><br>
	 * recommended value : 3<br> 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP
	 * obfuscation, for more security a value of at least 3 is recommended<br>
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * <br><br>
	 * default value : 3
	 * 
	 * @param length
	 *            the desired maximum length LOGs a warning in case of a wrong
	 *            value and keep the old value
	 */
	public static void setRouteMaxLength(final int length)
	{
		if (length < MINIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length has to be at least " + MINIMUM_ROUTE_LENGTH + "!");
			return;
		}
		if (length > MAXIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length should not exceed " + MAXIMUM_ROUTE_LENGTH);
			return;
		}
		if (length < getInstance().routeMinLength)
		{
			LOG.info("setRouteMaxLength: length ("
					+ length
					+ ") is smaller than current minlen. Setting minlen to given value.");
			getInstance().routeMinLength = length;
		}
		getInstance().routeMaxLength = length;
	}

	/**
	 * Don't establish any circuits until a certain part of the descriptors of
	 * running routers is present.
	 */
	private double minDescriptorsPercentage = 0.1;

	/**
	 * @return get minDescriptorsPercentage
	 */
	public static double getMinDescriptorsPercentage()
	{
		return getInstance().minDescriptorsPercentage;
	}

	/**
	 * Set the minimum Descriptors percent value.
	 * 
	 * This value is used for determining if it is save to build a circuit with
	 * the known amount of routers.
	 * 
	 * @param percent
	 *            the percentage as double (range : 0.0 - 100.0)
	 */
	public static void setMinDescriptorsPercentage(final double percent)
	{
		if (percent < 0.0 || percent > 100.0) // check if it is in range
		{
			LOG.warn("setMinDescriptorsPercentage: value {} out of range (0.0 - 100.0)", percent);
		}
		if (percent == 0.0)
		{
			LOG.warn("setMinDescriptorsPercentage: setting this value to 0 is discouraged");
		}
		getInstance().minDescriptorsPercentage = percent;
	}

	/** Wait at most until this number of descriptors is known. */
	private Integer minDescriptors;

	/**
	 * Set the minimum amount of Descriptors to be available to safely start
	 * Circuitbuilding.
	 * 
	 * @param minDescriptors
	 *            if set to null the Standard will be used.
	 */
	public static void setMinDescriptors(final Integer minDescriptors)
	{
		getInstance().minDescriptors = minDescriptors;
	}

	/**
	 * @return Wait at most until this number of descriptors is known.
	 */
	public static int getMinDescriptors()
	{
		if (getInstance().minDescriptors == null)
		{
			return 10 * getInstance().routeMinLength;
		}
		return getInstance().minDescriptors;
	}

	/** True if there shouldn't be two class C addresses on the route. */
	private boolean routeUniqueClassC = true;

	/**
	 * @return true if there shouldn't be two class C addresses on the route.
	 */
	public static boolean isRouteUniqueClassC()
	{
		return getInstance().routeUniqueClassC;
	}

	/**
	 * Set to true if there should only be unique Class C address on the route.
	 * 
	 * @param value
	 *            true = unique, false = other Class C addresses are allowed
	 */
	public static void setRouteUniqueClassC(final boolean value)
	{
		getInstance().routeUniqueClassC = value;
	}

	/**
	 * True if there should be at most one router from one country (or block of
	 * countries) on the path.<br>
	 * Default : true
	 */
	private boolean routeUniqueCountry = true;

	/**
	 * @return the routeUniqueCountry
	 */
	public static boolean isRouteUniqueCountry()
	{
		return getInstance().routeUniqueCountry;
	}

	/**
	 * True if there should be at most one router from one country (or block of
	 * countries) on the path.
	 * 
	 * @param routeUniqueCountry
	 *            the routeUniqueCountry to set
	 */
	public static void setRouteUniqueCountry(final boolean routeUniqueCountry)
	{
		getInstance().routeUniqueCountry = routeUniqueCountry;
	}

	/** Allow a single node to be present in multiple circuits. */
	public static int allowModeMultipleCircuits = 3;

	/**
	 * List of countries of routers which should be avoided when creating
	 * circuits.
	 */
	private Set<String> avoidedCountries = new HashSet<String>();
	/**
	 * Check if the specified Country is allowed to be used for Circuit building.
	 * 
	 * @param countryCode the country code to be checked
	 * @return true if it is allowed to connect to, false if not
	 */
	public static boolean isCountryAllowed(final String countryCode)
	{
		// when avoidedCountries is empty all Countries are allowed
		if (getInstance().avoidedCountries.isEmpty())
		{
			return true;
		}
		if (getInstance().avoidedCountries.contains(countryCode))
		{
			return false;
		}
		return true;
	}
	/**
	 * Set the avoided countries.
	 * @param countryCodes a set of country codes to be avoided.
	 */
	public static synchronized void setCountryAllowed(final Set<String> countryCodes)
	{
		getInstance().avoidedCountries = countryCodes;
	}
	/**
	 * Allow a specified country or avoid it.
	 * @param countryCode the country code
	 * @param allowed if set to true we will allow connections to this country, if set to false we will avoid a connection
	 */
	public static synchronized void setCountryAllowed(final String countryCode, final boolean allowed)
	{
		if (allowed)
		{
			getInstance().avoidedCountries.remove(countryCode);
		}
		else
		{
			getInstance().avoidedCountries.add(countryCode);
		}
	}
	/** collection of fingerprints to be avoided. */
	private Set<Fingerprint> avoidedNodeFingerprints = new HashSet<Fingerprint>();

	/**
	 * @return get the {@link Set} of fingerprints which should be avoided in
	 *         route creation.
	 */
	public static Set<Fingerprint> getAvoidedNodeFingerprints()
	{
		return getInstance().avoidedNodeFingerprints;
	}

	/**
	 * Set the list of avoided fingerprints. These fingerprint will be used to
	 * check whether a connection to a specific router is allowed or not.
	 * 
	 * @param fingerprints
	 *            a list of fingerprints
	 */
	public static synchronized void setAvoidedNodeFingerprints(final Set<byte[]> fingerprints)
	{
		getInstance().avoidedNodeFingerprints.clear();
		for (byte [] fingerprint : fingerprints)
		{
			// TODO : check size of fingerprints
			getInstance().avoidedNodeFingerprints.add(new FingerprintImpl(fingerprint));
		}
	}

	/**
	 * Set the list of avoided fingerprints. These fingerprint will be used to
	 * check whether a connection to a specific router is allowed or not.
	 * 
	 * @param fingerprints
	 *            a list of fingerprints in hex notation
	 */
	public static synchronized void setAvoidedNodeFingerprintsHex(final Set<String> fingerprints)
	{
		getInstance().avoidedNodeFingerprints.clear();
		for (String fingerprint : fingerprints)
		{
			// TODO : check size of fingerprints
			getInstance().avoidedNodeFingerprints.add(new FingerprintImpl(DatatypeConverter.parseHexBinary(fingerprint)));
		}
	}

	/**
	 * Add a fingerprint to the set of avoided fingerprints.
	 * 
	 * @param fingerprint
	 *            the fingerprint which should be avoided
	 */
	public static synchronized void addAvoidedNodeFingerprint(final byte[] fingerprint)
	{
		// TODO : add check if size is correct
		getInstance().avoidedNodeFingerprints.add(new FingerprintImpl(fingerprint));
	}

	/**
	 * Add a fingerprint (in hex notation) to the set of avoided fingerprints.
	 * 
	 * @param hexFingerprint
	 *            the fingerprint (in hex notation) which should be avoided
	 */
	public static synchronized void addAvoidedNodeFingerprint(final String hexFingerprint)
	{
		// TODO : add check if size is correct
		getInstance().avoidedNodeFingerprints.add(new FingerprintImpl(DatatypeConverter.parseHexBinary(hexFingerprint)));
	}

	/** Path of the resource to the GeoIP DB. */
	public static final String TOR_GEOIPCITY_PATH = "/com/maxmind/geoip/GeoIP.dat";
	public static final int TOR_GEOIPCITY_MAX_FILE_SIZE = 2000000;

	/**
	 * Shall we cache the fetched Hidden service Descriptors?
	 * 
	 * Default : true
	 */
	private boolean cacheHiddenServiceDescriptor = true;
	/** directory and Co. config */
	public static final int MIN_NUMBER_OF_ROUTERS_IN_CONSENSUS = 50;
	/**
	 * the time span that a router description is valid (starting from its
	 * publishing time).
	 */
	public static final long ROUTER_DESCRIPTION_VALID_PERIOD_MS = 1L * 24L * 60L * 60L * 1000L;

	/**
	 * Try to load the TorConfig from System properties.
	 */
	public static void reloadConfigFromProperties()
	{
		try
		{
			// overwrite defaults if proper system properties are set
			setMinimumIdleCircuits(SystemPropertiesHelper.getSystemProperty(SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS,
			                                                                getInstance().minimumIdleCircuits));
			setRouteMinLength(SystemPropertiesHelper.getSystemProperty(SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH,
			                                                           getRouteMinLength()));
			setRouteMaxLength(SystemPropertiesHelper.getSystemProperty(SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH,
			                                                           getRouteMaxLength()));
			setCacheHiddenServiceDescriptor(SystemPropertiesHelper.getSystemProperty(SYSTEMPROPERTY_TOR_CACHE_HS_DESCRIPTOR,
			                                                                         isCacheHiddenServiceDescriptor()));
			maxAllowedSetupDurationMs = SystemPropertiesHelper.getSystemProperty(SYSTEMPROPERTY_TOR_MAX_ALLOWED_SETUP_DURATION_MS,
			                                                                     (int) maxAllowedSetupDurationMs);
		}
		catch (final Exception e)
		{
			LOG.error("config could not be loaded from properties",	e);
		}
	}
	// first load the config from System properties
	static
	{
		reset();
		reloadConfigFromProperties();		
	}
	private TorConfig()
	{
	}

	/**
	 * @return the cacheHiddenServiceDescriptor
	 */
	public static boolean isCacheHiddenServiceDescriptor()
	{
		return getInstance().cacheHiddenServiceDescriptor;
	}

	/**
	 * @param cacheHiddenServiceDescriptor
	 *            the cacheHiddenServiceDescriptor to set
	 */
	public static void setCacheHiddenServiceDescriptor(final boolean cacheHiddenServiceDescriptor)
	{
		getInstance().cacheHiddenServiceDescriptor = cacheHiddenServiceDescriptor;
	}
	/**
	 * Shall we save the Circuit history?
	 * @see CircuitHistory
	 * 
	 * @return true if we save the history for later use.
	 */
	public static boolean isSaveCircuitHistory() 
	{
		return getInstance().saveCircuitHistory;
	}
	/**
	 * Shall we save the Circuit history?
	 * @param saveCircuitHistory true if we save the history
	 */
	public void setSaveCircuitHistory(final boolean saveCircuitHistory) 
	{
		getInstance().saveCircuitHistory = saveCircuitHistory;
	}

	/**
	 * Shall we save the Circuit history?
	 * @see CircuitHistory
	 */
	private boolean saveCircuitHistory = true;
	
	/**
	 * How many parallel running tasks should we spawn for creating a circuit?
	 */
	private int parallelCircuitBuilds = 1;
	/**
	 * How many parallel running tasks should we spawn for creating a circuit?
	 * @return the number of allowed parallel tasks for creating a circuit
	 */
	public static int getParallelCircuitBuilds()
	{
		return getInstance().parallelCircuitBuilds;
	}
	/**
	 * How many parallel running tasks should we spawn for creating a circuit?
	 * @param number the number of allowed parallel tasks for creating a circuit
	 */
	public static void setParallelCircuitBuilds(final int number)
	{
		if (number < 1)
		{
			LOG.error("setParallelCircuitBuilds should not be less than 1");
		}
		else
		{
			getInstance().parallelCircuitBuilds = number;
		}
	}	
	/**
	 * Reset all configuration items to their default values.
	 */
	public static void reset()
	{
		TorConfig config = getInstance();
		config.avoidedCountries.clear();
		config.avoidedNodeFingerprints.clear();
		config.cacheHiddenServiceDescriptor = true;
		config.circuitClosesOnFailures = 3;
		config.minimumIdleCircuits = 3;
		config.parallelCircuitBuilds = 1;
		config.routeMaxLength = DEFAULT_ROUTE_LENGTH;
		config.routeMinLength = DEFAULT_ROUTE_LENGTH;
		config.routeUniqueClassC = true;
		config.routeUniqueCountry = true;
		config.saveCircuitHistory = true;
		config.veryAggressiveStreamBuilding = false;
		config.longLivedPorts.clear();
		for (int tmp : DEFAULT_LONG_LIVED_PORTS)
		{
			config.longLivedPorts.add(tmp);
		}
	}
}
