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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Parsing;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.util.SystemPropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// TODO : implement bridge connect
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

	/** @return get an instance of {@link TorConfig}. */
	public static synchronized TorConfig getInstance()
	{
		if (instance == null)
		{
			instance = new TorConfig();
			instance.init(null);
		}
		return instance;
	}

	/**
	 * Startup delay in seconds.
	 * 
	 * How long should we wait before trying the first connects?
	 */
	private int startupDelaySeconds = 20;

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

	public String nickname = Util.MYNAME;

	// QoS-parameters
	/** How many times should we try to connect? */
	private int retriesConnect = 5;
	public static int reconnectCircuit = 3;
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
	 * @return get the number of minimum idle circuits.
	 */
	public static int getMinimumIdleCircuits()
	{
		return getInstance().minimumIdleCircuits;
	}

	/**
	 * Set the minimum number of idling circuits.
	 * 
	 * recommended value : 3 
	 * maximum value : 20 // TODO : verify or set a good max value
	 * 
	 * if this value is 0 then tor will only open circuits when a connection is
	 * needed, but in this case the connection will take longer as the circuit
	 * needs to be built first.
	 * 
	 * default value : 3
	 * 
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
	
	public static int queueTimeoutCircuit = 20;
	public static int queueTimeoutResolve = 10;
	/* TODO was: 11 */
	public static int queueTimeoutStreamBuildup = 5;

	public static int circuitClosesOnFailures = 3;
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
	/** in minutes. */
	public static int intervalDirectoryRefresh = 2;

	/** to access directory servers: connect timeout: 1 minute. */
	public static final long DIR_CONNECT_TIMEOUT_MILLIS = 60L * 1000L;
	/** to access directory servers: max. connection timeout: 60 minutes */
	public static final long DIR_OVERALL_TIMEOUT_MILLIS = 60L * 60L * 1000L;
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
	public static int streamsPerCircuit = 50;
	/** see Server.getRefinedRankingIndex. */
	public static float rankingIndexEffect = 0.9f;

	/** minimum allowed route length for creating a circuit. */
	private static final int MINIMUM_ROUTE_LENGTH = 2;
	/** maximum allowed route length for creating a circuit. */
	private static final int MAXIMUM_ROUTE_LENGTH = 8;

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
	private int routeMinLength = 3;

	/**
	 * @return get the minimum allowed route length for creating a circuit.
	 */
	public static int getRouteMinLength()
	{
		return getInstance().routeMinLength;
	}

	/**
	 * set the minimum circuit path length. 
	 * recommended value : 3 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP
	 * obfuscation, for more security a value of at least 3 is recommended
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * 
	 * default value : 3
	 * 
	 * @param length
	 *            the desired minimum length Logs a message as WARNING in case
	 *            of wrong value
	 */
	public static void setRouteMinLength(final int length)
	{
		if (length < MINIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length has to be at least "
					+ MINIMUM_ROUTE_LENGTH + "!");
			return;
		}
		if (length > MAXIMUM_ROUTE_LENGTH)
		{
			LOG.warn("route length should not exceed "
					+ MAXIMUM_ROUTE_LENGTH);
			return;
		}
		if (length > getInstance().routeMaxLength)
		{
			LOG.info("setRouteMinLength: length ("
					+ length
					+ ") is smaller than current maxlen. Setting maxlen to given value.");
			getInstance().routeMaxLength = length;
		}
		getInstance().routeMinLength = length;
	}

	/**
	 * maximum circuit path length. 
	 * 
	 * recommended value : 5 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP obfuscation,
	 * for more security a value of at least 3 is recommended 
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * 
	 * default value : 5
	 */
	private int routeMaxLength = 5;

	/**
	 * @return get the minimum allowed route length for creating a circuit.
	 */
	public static int getRouteMaxLength()
	{
		return getInstance().routeMaxLength;
	}

	/**
	 * set the maximum circuit path length. 
	 * 
	 * recommended value : 4 
	 * minimum value : 2 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/115-two-hop-paths.txt) using a value of 2 is only good for a simple IP
	 * obfuscation, for more security a value of at least 3 is recommended
	 * maximum value : 8 (see https://gitweb.torproject.org/torspec.git/blob/HEAD:/proposals/110-avoid-infinite-circuits.txt for details)
	 * 
	 * default value : 4
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
	 * @throws TorException
	 *             is thrown when an invalid value is choosen
	 */
	public static void setMinDescriptorsPercentage(final double percent)
			throws TorException
	{
		if (percent < 0.0 || percent > 100.0) // check if it is in range
		{
			throw new TorException("invalid value for setMinDescriptorsPercentage");
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
	 * countries) on the path.
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
	private static Set<String> avoidedCountries; // TODO : currently not used. we should implement it.
	// TODO : create getter & setter for countries
	/** collection of fingerprints to be avoided. */
	private Set<byte[]> avoidedNodeFingerprints;

	/**
	 * @return get the {@link Set} of fingerprints which should be avoided in
	 *         route creation.
	 */
	public static Set<byte[]> getAvoidedNodeFingerprints()
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
	public static void setAvoidedNodeFingerprints(final Set<byte[]> fingerprints)
	{
		getInstance().avoidedNodeFingerprints = fingerprints;
	}

	/**
	 * Add a fingerprint to the set of avoided fingerprints.
	 * 
	 * @param fingerprint
	 *            the fingerprint which should be avoided
	 */
	public static void addAvoidednodeFingerprint(final byte[] fingerprint)
	{
		synchronized (getInstance().avoidedNodeFingerprints)
		{
			if (getInstance().avoidedNodeFingerprints == null)
			{
				getInstance().avoidedNodeFingerprints = new HashSet<byte[]>();
			}
		}
		getInstance().avoidedNodeFingerprints.add(fingerprint);
	}

	/** TOR config filename. */
	private static final String TOR_CONFIG_FILENAME = "torrc";

	/** Path of the resource to the GeoIP DB. */
	public static final String TOR_GEOIPCITY_PATH = "/com/maxmind/geoip/GeoIP.dat";
	public static final int TOR_GEOIPCITY_MAX_FILE_SIZE = 2000000;

	private static String filename;

	/**
	 * Shall we cache the fetched Hidden service Descriptors?
	 * 
	 * Default : true
	 */
	private static boolean cacheHiddenServiceDescriptor = true;
	/** directory and Co. config */
	public static final int MIN_NUMBER_OF_ROUTERS_IN_CONSENSUS = 50;
	/**
	 * the time span that a router description is valid (starting from its
	 * publishing time).
	 */
	public static final long ROUTER_DESCRIPTION_VALID_PERIOD_MS = 1L * 24L
			* 60L * 60L * 1000L;

	static
	{
		reloadConfigFromProperties();
	}

	/**
	 * Try to load the TorConfig from System properties.
	 */
	public static final void reloadConfigFromProperties()
	{
		try
		{
			// overwrite defaults if proper system properties are set
			setMinimumIdleCircuits(SystemPropertiesHelper.getSystemProperty(
					SYSTEMPROPERTY_TOR_MINIMUM_IDLE_CIRCUITS,
					getInstance().minimumIdleCircuits));
			setRouteMinLength(SystemPropertiesHelper.getSystemProperty(
					SYSTEMPROPERTY_TOR_MINIMUM_ROUTE_LENGTH,
					getRouteMinLength()));
			setRouteMaxLength(SystemPropertiesHelper.getSystemProperty(
					SYSTEMPROPERTY_TOR_MAXIMUM_ROUTE_LENGTH,
					getRouteMaxLength()));
			setCacheHiddenServiceDescriptor(SystemPropertiesHelper.getSystemProperty(
					SYSTEMPROPERTY_TOR_CACHE_HS_DESCRIPTOR,
					isCacheHiddenServiceDescriptor()));
			maxAllowedSetupDurationMs = SystemPropertiesHelper.getSystemProperty(
					SYSTEMPROPERTY_TOR_MAX_ALLOWED_SETUP_DURATION_MS,
					(int) maxAllowedSetupDurationMs);
		}
		catch (final Exception e)
		{
			LOG.error("config could not be loaded from properties",
					e);
		}
	}

	private TorConfig()
	{

	}

	/**
	 * @param readFileName
	 *            set to false to avoid any access to the local file system
	 */
	private TorConfig(final boolean readFileName)
	{
		instance = this;
		if (readFileName)
		{
			init(getConfigDir() + TOR_CONFIG_FILENAME);
		}
		else
		{
			init(null);
		}
	}

	private TorConfig(final String filename)
	{
		instance = this;
		init(filename);
	}

	public void reload()
	{
		if (filename == null)
		{
			return; // TODO : add reloading of System.properties
		}
		LOG.info("TorConfig.reload: reloading config-file " + filename);
		init(filename);
	}

	private void init(final String filename)
	{
		// init set of avoided nodes, countries
		avoidedCountries = new HashSet<String>();
		avoidedNodeFingerprints = new HashSet<byte[]>();
		// read everything else from config
		readFromConfig(filename);
		// set filename, such that file can be reloaded
		TorConfig.filename = filename;
	}

	public void close()
	{
		writeToFile("/tmp/torrc.test");
	}

	private String replaceSpaceWithSpaceRegExp(final String regexp)
	{
		return regexp.replaceAll(" ", "\\\\s+");
	}

	private int parseInt(final String config, final String name,
			final int myDefault)
	{
		final int x = Integer.parseInt(Parsing.parseStringByRE(
				config,
				Parsing.compileRegexPattern("^\\s*"
						+ replaceSpaceWithSpaceRegExp(name) + "\\s+(\\d+)"),
				Integer.toString(myDefault)));
		LOG.debug("TorConfig.parseInt: Parsed '{}' as '{}'", name, x);
		return x;
	}

	private String writeInt(final String name, final int value)
	{
		return name + " " + value + "\n";
	}

	/*
	 * private float parseFloat(String config,String name,float myDefault) {
	 * float x = Float.parseFloat(Parsing.parseStringByRE(config,"^\\s*"+
	 * replaceSpaceWithSpaceRegExp
	 * (name)+"\\s+([0-9.]+)",Float.toString(myDefault)));
	 * LOG.debug("TorConfig.parseFloat: Parsed '"+name+"' as '"+x+"'"); return
	 * x; }
	 */
	private String writeFloat(final String name, final float value)
	{
		return name + " " + value + "\n";
	}

	private String writeDouble(final String name, final double value)
	{
		return name + " " + value + "\n";
	}

	private float parseFloat(final String config, final String name,
			final float myDefault, final float lower, final float upper)
	{
		float x = Float.parseFloat(Parsing.parseStringByRE(
				config,
				Parsing.compileRegexPattern("^\\s*"
						+ replaceSpaceWithSpaceRegExp(name) + "\\s+([0-9.]+)"),
				Float.toString(myDefault)));
		if (x < lower)
		{
			x = lower;
		}
		if (x > upper)
		{
			x = upper;
		}
		LOG.debug("TorConfig.parseFloat: Parsed '{}' as '{}'", name, x);
		return x;
	}

	private double parseDouble(final String config, final String name,
			final double myDefault, final double lower, final double upper)
	{
		double x = Double.parseDouble(Parsing.parseStringByRE(
				config,
				Parsing.compileRegexPattern("^\\s*"
						+ replaceSpaceWithSpaceRegExp(name) + "\\s+([0-9.]+)"),
				Double.toString(myDefault)));
		if (x < lower)
		{
			x = lower;
		}
		if (x > upper)
		{
			x = upper;
		}
		LOG.debug("TorConfig.parseDouble: Parsed '{}' as '{}'", name, x);
		return x;
	}

	private String parseString(final String config, final String name,
			final String myDefault)
	{
		final String x = Parsing.parseStringByRE(
				config,
				Parsing.compileRegexPattern("^\\s*"
						+ replaceSpaceWithSpaceRegExp(name) + "\\s+(\\S.*?)$"),
				myDefault);
		LOG.debug("TorConfig.parseString: Parsed '{}' as '{}'", name, x);
		return x;
	}

	private String writeString(final String name, final String value)
	{
		return name + " " + value + "\n";
	}

	private boolean parseBoolean(final String config, final String name,
			final boolean myDefault)
	{
		String mydef = "false";
		if (myDefault)
		{
			mydef = "true";
		}
		final String x = Parsing.parseStringByRE(
				config,
				Parsing.compileRegexPattern("^\\s*"
						+ replaceSpaceWithSpaceRegExp(name) + "\\s+(\\S.*?)$"),
				mydef).trim();
		boolean ret = false;
		if (x.equals("1") || x.equalsIgnoreCase("true")
				|| x.equalsIgnoreCase("yes"))
		{
			ret = true;
		}
		LOG.debug("TorConfig.parseBoolean: Parsed '{}' as '{}'", name, ret);
		return ret;
	}

	private String writeBoolean(final String name, final boolean value)
	{
		if (value)
		{
			return name + " " + "true" + "\n";
		}
		else
		{
			return name + " " + "false" + "\n";
		}
	}

	private void readFromConfig(final String filename)
	{
		try
		{
			String config = "";
			if (filename != null)
			{
				final DataInputStream sin = new DataInputStream(
						new FileInputStream(new File(filename)));
				// DataInputStream sin = new
				// DataInputStream(ClassLoader.getSystemResourceAsStream(filename));
				config = readAllFromStream(sin);
				LOG.debug("TorConfig.readFromConfig(): {}", config);
			}
			// Read variable config information here

			// security parameters
			streamsPerCircuit = parseInt(config, "StreamsPerCircuit",
					streamsPerCircuit);
			rankingIndexEffect = parseFloat(config, "RankingIndexEffect",
					rankingIndexEffect, 0, 1);
			routeMinLength = parseInt(config, "RouteMinLength", routeMinLength);
			routeMaxLength = parseInt(config, "RouteMaxLength", routeMaxLength);
			try
			{
				setMinDescriptorsPercentage(parseDouble(config,
						"MinPercentage", minDescriptorsPercentage, 0, 1));
			}
			catch (TorException e)
			{
				LOG.warn("could not load MinPercentage from config file");
			}
			// minDescriptors = parseInt(config, "MinDescriptors",
			// minDescriptors);
			routeUniqueClassC = parseBoolean(config, "RouteUniqClassC",
					routeUniqueClassC);
			routeUniqueCountry = parseBoolean(config, "RouteUniqCountry",
					routeUniqueCountry);
			allowModeMultipleCircuits = parseInt(config,
					"AllowNodeMultipleCircuits", allowModeMultipleCircuits);
			// Avoid Countries
			Pattern p = Pattern.compile("^\\s*AvoidCountry\\s+(.*?)$",
					Pattern.MULTILINE + Pattern.CASE_INSENSITIVE
							+ Pattern.UNIX_LINES);
			Matcher m = p.matcher(config);
			while (m.find())
			{
				LOG.debug("TorConfig.readConfig: will avoid country: {}", m.group(1));
				avoidedCountries.add(m.group(1));
			}
			// Avoid Nodes
			p = Pattern.compile("^\\s*AvoidNode\\s+(.*?)$", Pattern.MULTILINE
					+ Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
			m = p.matcher(config);
			while (m.find())
			{
				LOG.debug("TorConfig.readConfig: will avoid node: {}", m.group(1));
				avoidedNodeFingerprints.add(DatatypeConverter.parseHexBinary(m
						.group(1)));
			}
			// functionality
			setStartupDelay(parseInt(config, "startupDelaySeconds", getStartupDelay()));

			nickname = parseString(config, "nickname", nickname);
			// QoS parameters
			retriesConnect = parseInt(config, "RetriesConnect", retriesConnect);
			retriesStreamBuildup = parseInt(config, "RetriesStreamBuildup",
					retriesStreamBuildup);
			reconnectCircuit = parseInt(config, "ReconnectCircuit",
					reconnectCircuit);

			queueTimeoutCircuit = parseInt(config, "QueueTimeoutCircuit",
					queueTimeoutCircuit);
			queueTimeoutResolve = parseInt(config, "QueueTimeoutResolve",
					queueTimeoutResolve);
			queueTimeoutStreamBuildup = parseInt(config,
					"QueueTimeoutStreamBuildup", queueTimeoutStreamBuildup);

			rankingTransferPerServerUpdate = parseFloat(config,
					"RankingTransferPerServerUpdate",
					rankingTransferPerServerUpdate, 0, 1);

			circuitClosesOnFailures = parseInt(config,
					"CircuitClosesOnFailures", circuitClosesOnFailures);
			circuitsMaximumNumber = parseInt(config, "circuitsMaximumNumber",
					circuitsMaximumNumber);

			veryAggressiveStreamBuilding = parseBoolean(config,
					"veryAggressiveStreamBuilding",
					veryAggressiveStreamBuilding);
			// directory parameters
			intervalDirectoryRefresh = parseInt(config, "DirectoryRefresh",
					intervalDirectoryRefresh);
		}
		catch (final IOException e)
		{
			LOG.warn("TorConfig.readFromConfig(): Warning: "
					+ e.getMessage());
		}
	}

	/**
	 * reads all data from an inputstream.
	 */
	private static String readAllFromStream(final InputStream in)
	{
		// DataInputStream.readLine() is depreciated
		final BufferedReader sin = new BufferedReader(new InputStreamReader(in));

		final StringBuffer buf = new StringBuffer();
		try
		{
			String str = sin.readLine();
			while (str != null)
			{
				buf.append(str);
				buf.append("\n");
				str = sin.readLine();
			}

		}
		catch (final IOException e)
		{
			/* eof, reset, ... */
			LOG.debug("got IOException : {}", e.getMessage(), e);
		}
		return buf.toString();
	}

	/** used to store some new values to a file. */
	private void writeToFile(final String filename)
	{
		if (filename == null)
		{
			return;
		}

		try
		{
			final StringBuffer config = new StringBuffer();

			LOG.debug("TorConfig.writeToFile(): {}", config);
			// Write variable config information here

			// security parameters
			config.append(writeInt("StreamsPerCircuit", streamsPerCircuit));
			config.append(writeFloat("RankingIndexEffect", rankingIndexEffect));
			config.append(writeInt("RouteMinLength", getRouteMinLength()));
			config.append(writeInt("RouteMaxLength", getRouteMaxLength()));
			config.append(writeDouble("MinPercentage",
					getMinDescriptorsPercentage()));
			config.append(writeInt("MinDescriptors", minDescriptors));
			config.append(writeBoolean("RouteUniqClassC", routeUniqueClassC));
			config.append(writeBoolean("RouteUniqCountry", routeUniqueCountry));
			config.append(writeInt("AllowNodeMultipleCircuits", allowModeMultipleCircuits));

			// Avoided countries
			final Iterator<String> it = avoidedCountries.iterator();
			while (it.hasNext())
			{
				final String countryName = it.next();
				config.append(writeString("AvoidCountry", countryName));
				LOG.debug("TorConfig.writeToFile: will avoid country {}", countryName);
			}
			// Avoided nodes
			for (final byte[] fingerprint : avoidedNodeFingerprints)
			{
				final String fingerprintStr = Encoding.toHexString(fingerprint);
				config.append(writeString("AvoidNode", fingerprintStr));
				LOG.debug("TorConfig.writeToFile: will avoid node {}", fingerprintStr);
			}
			// Functionality
			config.append(writeInt("startupDelaySeconds", getStartupDelay()));
			config.append(writeString("nickname", nickname));

			// QoS parameters
			config.append(writeInt("RetriesConnect", retriesConnect));
			config.append(writeInt("RetriesStreamBuildup", retriesStreamBuildup));
			config.append(writeInt("ReconnectCircuit", reconnectCircuit));

			config.append(writeInt("QueueTimeoutCircuit", queueTimeoutCircuit));
			config.append(writeInt("QueueTimeoutResolve", queueTimeoutResolve));
			config.append(writeInt("QueueTimeoutStreamBuildup",	queueTimeoutStreamBuildup));

			config.append(writeInt("CircuitClosesOnFailures", circuitClosesOnFailures));
			config.append(writeInt("circuitsMaximumNumber", circuitsMaximumNumber));

			config.append(writeBoolean("veryAggressiveStreamBuilding",
					veryAggressiveStreamBuilding));

			// FIXME: Check if this really works
			config.append(writeFloat("RankingTransferPerServerUpdate",
					rankingTransferPerServerUpdate));
			// directory parameters
			config.append(writeInt("DirectoryRefresh", intervalDirectoryRefresh));

			final FileWriter writer = new FileWriter(new File(filename));
			writer.write(config.toString());
			writer.close();

		}
		catch (final IOException e)
		{
			LOG.warn("TorConfig.writeToFile(): Warning: " + e.getMessage());
		}

	}

	private static String getConfigDir()
	{
		final String fileSeparator = System.getProperty("file.separator");
		if ("Linux".equals(operatingSystem()))
		{
			return System.getProperty("user.home") + fileSeparator + ".TorJava"
					+ fileSeparator;
		}
		return System.getProperty("user.home") + fileSeparator + "TorJava"
				+ fileSeparator;
	}

	/**
	 * @return get the operating system name.
	 */
	public static String operatingSystem()
	{
		return System.getProperty("os.name");
	}

	/**
	 * @return the cacheHiddenServiceDescriptor
	 */
	public static boolean isCacheHiddenServiceDescriptor()
	{
		return cacheHiddenServiceDescriptor;
	}

	/**
	 * @param cacheHiddenServiceDescriptor
	 *            the cacheHiddenServiceDescriptor to set
	 */
	public static void setCacheHiddenServiceDescriptor(
			final boolean cacheHiddenServiceDescriptor)
	{
		TorConfig.cacheHiddenServiceDescriptor = cacheHiddenServiceDescriptor;
	}
}
