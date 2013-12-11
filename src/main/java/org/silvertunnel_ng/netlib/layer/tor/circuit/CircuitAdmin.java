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

package org.silvertunnel_ng.netlib.layer.tor.circuit;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.common.TCPStreamProperties;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.common.TorEventService;
import org.silvertunnel_ng.netlib.layer.tor.directory.Directory;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterImpl;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle circuits.
 * 
 * @author hapke
 * @author Tobias Boese
 */
public class CircuitAdmin
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(CircuitAdmin.class);

	// TODO test:
	/** key=host name, value=circuit to this host */
	static Map<String, Circuit[]> suitableCircuitsCache = Collections.synchronizedMap(new HashMap<String, Circuit[]>());

	/** keep track of built Circuits to predict the best new idle Circuits. */
	private static CircuitHistory circuitHistory = new CircuitHistory();
	/**
	 * fingerprint of currently used nodes in circuits as key, # of cirs -
	 * value.
	 */
	private static Map<Fingerprint, Integer> currentlyUsedNodes = Collections
			.synchronizedMap(new HashMap<Fingerprint, Integer>());

	private static SecureRandom rnd = new SecureRandom();

	/**
	 * Create new circuit.
	 * 
	 * @param tlsConnectionAdmin
	 * @param dir
	 * @param sp
	 * @param torEventService
	 * @return a new {@link Circuit} object if successful (null if not)
	 */
	static Circuit provideSuitableNewCircuit(final TLSConnectionAdmin tlsConnectionAdmin, 
	                                         final Directory dir,
	                                         final TCPStreamProperties sp, 
	                                         final TorEventService torEventService)
	{
		LOG.debug("provideSuitableNewCircuit called");
		final ExecutorService executor = Executors.newCachedThreadPool();
		final Collection<Callable<Circuit>> allTasks = new ArrayList<Callable<Circuit>>();
		for (int i = 0; i < TorConfig.getParallelCircuitBuilds(); i++)
		{
			final Callable<Circuit> callable = new Callable<Circuit>()
			{
				/** establish Circuit to one introduction point */
				@Override
				public Circuit call()
				{
					Circuit result = null;
					LOG.debug("Callable Started..");
					for (int retries = 0; retries < TorConfig.getRetriesConnect(); ++retries)
					{
						try
						{
							result = new Circuit(tlsConnectionAdmin, dir, sp, torEventService, circuitHistory);
						}
						catch (final InterruptedException e)
						{
							/* do nothing, continue trying */
							LOG.debug("got InterruptedException : {}", e.getMessage(), e);
						}
						catch (final TorException e)
						{
							/* do nothing, continue trying */
							LOG.debug("got TorException : {}", e.getMessage(), e);
						}
						catch (final IOException e)
						{
							/* do nothing, continue trying */
							LOG.debug("got IOException : {}", e.getMessage(), e);
						}
						LOG.debug("provideSuitableNewCircuit retry {} from {}", new Object[] {retries + 1, TorConfig.getRetriesConnect()});
					}
					LOG.debug("Callable Finished!");
					return result;
				}
			};
			allTasks.add(callable);
		}
		try
		{
			return executor.invokeAny(allTasks);
		}
		catch (InterruptedException exception)
		{
			LOG.debug("got Exception while executing tasks", exception);
		}
		catch (ExecutionException exception)
		{
			LOG.debug("got Exception while executing tasks", exception);
		}
		return null;
	}

	/**
	 * Provide a circuit that can exclusively be used by the caller.
	 * (mainly used for HiddenServices)
	 * 
	 * @param tlsConnectionAdmin
	 * @param dir
	 * @param sp
	 * @param torEventService
	 * @return an established {@link Circuit}
	 */
	public static Circuit provideSuitableExclusiveCircuit(final TLSConnectionAdmin tlsConnectionAdmin, 
	                                                      final Directory dir,
	                                                      final TCPStreamProperties sp, 
	                                                      final TorEventService torEventService)
	{
		try
		{
			for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
			{
				for (final Circuit circuit : tls.getCircuits())
				{
					if (circuit.isUnused())
					{
						if (sp.getCustomExitpoint() == null)
						{
							circuit.setUnused(false);
							LOG.debug("we successfully used an unused Circuit! Id : {}", circuit.getId());
							return circuit;
						}
						if (circuit.getRelayEarlyCellsRemaining() > 0) // is extendable?
						{
							circuit.extend(sp.getCustomExitpoint());
							circuit.setUnused(false);
							LOG.debug("we successfully extended and used an unused Circuit! Id : {}", circuit.getId());
							return circuit;
						}
					}
				}
			}
		}
		catch (Exception exception)
		{
			LOG.debug("we got an exception while finding a already established Circuit. using new one.", exception);
		}
		Circuit result = provideSuitableNewCircuit(tlsConnectionAdmin, dir, sp, torEventService);
		result.setUnused(false);
		return result;
	}

	/**
	 * used to return a number of circuits to a target. establishes a new
	 * circuit or uses an existing one
	 * 
	 * @param sp
	 *            gives some basic restrains
	 * @param forHiddenService
	 *            if set to true, use circuit that is unused and don't regard
	 *            exit-policies
	 */
	public static Circuit[] provideSuitableCircuits(final TLSConnectionAdmin tlsConnectionAdmin, 
													final Directory dir,
													final TCPStreamProperties sp, 
													final TorEventService torEventService,
													final boolean forHiddenService) throws IOException
	{
		LOG.debug("TLSConnectionAdmin.provideSuitableCircuits: called for {}", sp.getHostname());

		// TODO test: shortcut/cache
		final Circuit[] cachedResults = suitableCircuitsCache.get(sp.getHostname());
		if (cachedResults != null)
		{
			// TODO return cachedResults;
			LOG.debug("return chachedResults");
		}

		// list all suiting circuits in a vector
		int numberOfExistingCircuits = 0;
		final Vector<Circuit> allCircs = new Vector<Circuit>(10, 10);
		int rankingSum = 0;
		for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
		{
			for (final Circuit circuit : tls.getCircuits())
			{
				try
				{
					++numberOfExistingCircuits;
					if (circuit.isEstablished()
							&& (!circuit.isClosed())
							&& DirectoryService.isCompatible(dir, circuit, sp, forHiddenService))
					{
						allCircs.add(circuit);
						rankingSum += circuit.getRanking();
					}
				}
				catch (final TorException e)
				{ /* do nothing, just try next circuit */
					LOG.debug("got TorException : {}", e.getMessage(), e);
				}
			}
		}
		// sort circuits (straight selection... O(n^2)) by
		// - whether they contained a stream to the specific address
		// - ranking (stochastically!)
		// - implicit: whether they haven't had a stream at all
		for (int i = 0; i < allCircs.size() - 1; ++i)
		{
			final Circuit c1 = allCircs.get(i);
			int min = i;
			int minRanking = c1.getRanking();
			if (minRanking == 0)
			{
				minRanking = 1;
			}
			final boolean minPointsToAddr = c1.getStreamHistory().contains(sp.getHostname());
			for (int j = i + 1; j < allCircs.size(); ++j)
			{
				final Circuit thisCirc = allCircs.get(j);
				int thisRanking = thisCirc.getRanking();
				if (thisRanking == 0)
				{
					thisRanking = 1;
				}
				final boolean thisPointsToAddr = thisCirc.getStreamHistory().contains(sp.getHostname());
				final float rankingQuota = thisRanking / minRanking;
				if ((thisPointsToAddr && !minPointsToAddr)
						|| (rnd.nextFloat() > Math
								.exp(-rankingQuota)))
				{
					// sort stochastically
					min = j;
					minRanking = thisRanking;
				}
			}
			if (min > i)
			{
				final Circuit temp = allCircs.set(i, allCircs.get(min));
				allCircs.set(min, temp);
			}
		}
		// return number of circuits suiting to number of stream-connect
		// retries!
		int returnValues = sp.getConnectRetries();
		if (allCircs.size() < returnValues)
		{
			returnValues = allCircs.size();
		}
		if ((returnValues == 1) && (numberOfExistingCircuits < TorConfig.circuitsMaximumNumber))
		{
			// spawn new circuit IN BACKGROUND, unless maximum number of
			// circuits reached
			LOG.debug("TLSConnectionAdmin.provideSuitableCircuits: spawning circuit to {} in background", sp.getHostname());
			final Thread spawnInBackground = new NewCircuitThread(tlsConnectionAdmin, dir, sp, torEventService);
			spawnInBackground.setName("CuircuitAdmin.provideSuitableCircuits");
			spawnInBackground.start();
		}
		else if ((returnValues == 0)
				&& (numberOfExistingCircuits < TorConfig.circuitsMaximumNumber))
		{
			// spawn new circuit, unless maximum number of circuits reached
			LOG.debug("TLSConnectionAdmin.provideSuitableCircuits: spawning circuit to {}", sp.getHostname());
			final Circuit single = provideSuitableNewCircuit(tlsConnectionAdmin, dir, sp, torEventService);
			if (single != null)
			{
				returnValues = 1;
				allCircs.add(single);
			}
		}
		// copy values
		final Circuit[] results = new Circuit[returnValues];
		for (int i = 0; i < returnValues; ++i)
		{
			results[i] = allCircs.get(i);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("TLSConnectionAdmin.provideSuitableCircuits: Choose Circuit ranking "
						+ results[i].getRanking() + ":" + results[i].toString());
			}
		}

		// TODO gri test: shortcut/cache
		suitableCircuitsCache.put(sp.getHostname(), results);

		return results;
	}

	/**
	 * returns a route through the network as specified in
	 * @see TCPStreamProperties.
	 * 
	 * @param sp
	 *            tcp stream properties
	 * @param proposedRoute
	 *            array of fingerprints of routers that were proposed by tcp
	 *            stream properties
	 * @param excludedServerFingerprints
	 *            selfexplained
	 * @param route
	 *            current route array
	 * @param i
	 *            index in array route up to which the route has to be built
	 * @return a list of servers
	 */
	private static synchronized RouterImpl[] createNewRoute(
			final Directory directory, 
			final TCPStreamProperties sp,
			final Fingerprint[] proposedRoute,
			final HashSet<Fingerprint> excludedServerFingerprints,
			RouterImpl[] route, 
			final int i, 
			int maxIterations) throws TorException
	{
		// TODO : implement better logic for creating a route...
		final float rankingInfluenceIndex = sp.getRankingInfluenceIndex();
		final HashSet<Fingerprint> previousExcludedServerFingerprints = new HashSet<Fingerprint>();

		final Map<Fingerprint, RouterImpl> validRoutersByFingerprint = directory
				.getValidRoutersByFingerprint();
		for (final RouterImpl r : validRoutersByFingerprint.values()) // TODO : isnt it better to iterate through CircuitAdmin.currentlyUsedNodes ???
		{
			final Integer allowedCircuitsWithNode = CircuitAdmin.currentlyUsedNodes.get(r.getFingerprint());
			// check if server has been used already in other circuits
			if ((allowedCircuitsWithNode != null) && (allowedCircuitsWithNode.intValue() > TorConfig.allowModeMultipleCircuits))
			{
				excludedServerFingerprints.add(r.getFingerprint());
			}
		}

		if ((proposedRoute != null) && (i < proposedRoute.length) && (proposedRoute[i] != null))
		{
			// choose proposed server
			route[i] = validRoutersByFingerprint.get(proposedRoute[i]);
			if (route[i] == null)
			{
				throw new TorException("couldn't find server "
						+ proposedRoute[i] + " for position " + i);
			}
		}
		else
		{

			if (i == route.length - 1)
			{
				// the last router has to accept exit policy

				// determine suitable servers
				final Map<Fingerprint, RouterImpl> suitableServerFingerprints = new HashMap<Fingerprint, RouterImpl>();
				//for (final RouterImpl r : validRoutersByFingerprint.values())
				for (final RouterImpl r : directory.getRoutersWithExit().values())
				{
					// exit server must be trusted
					if (r.exitPolicyAccepts(sp.getAddr(), sp.getPort())
							&& (sp.isUntrustedExitAllowed() || r.isDirv2Exit()))
					{
						suitableServerFingerprints.put(r.getFingerprint(), r);
					}
				}

				final HashSet<Fingerprint> x = new HashSet<Fingerprint>(excludedServerFingerprints);
				// now select one of them

				route[i] = directory.selectRandomNode(suitableServerFingerprints, 
													  x, 
													  rankingInfluenceIndex,
													  sp.isFastRoute(),
													  sp.isStableRoute());

			}
			else if ((i == 0) && (!sp.isNonGuardEntryAllowed()))
			{
				// entry node must be guard

				// determine suitable servers
				final HashSet<Fingerprint> suitableServerFingerprints = new HashSet<Fingerprint>();
				for (final RouterImpl r : validRoutersByFingerprint.values())
				{
					// entry server must be guard
					if (r.isDirv2Guard())
					{
						suitableServerFingerprints.add(r.getFingerprint());
					}
				}

				final HashSet<Fingerprint> x = new HashSet<Fingerprint>(validRoutersByFingerprint.keySet());
				x.removeAll(suitableServerFingerprints);
				x.addAll(excludedServerFingerprints);
				// now select one of them
				route[i] = directory.selectRandomNode(
						validRoutersByFingerprint, x, rankingInfluenceIndex, sp.isFastRoute(), sp.isStableRoute());

			}
			else
			{
				route[i] = directory.selectRandomNode(
						validRoutersByFingerprint, excludedServerFingerprints,
						rankingInfluenceIndex, sp.isFastRoute(), sp.isStableRoute());
			}

			if (route[i] == null)
			{
				return null;
			}
			previousExcludedServerFingerprints.addAll(excludedServerFingerprints);
			excludedServerFingerprints.addAll(directory.excludeRelatedNodes(route[i]));

			int numberOfNodeOccurances;
			final Integer allowedCircuitsWithNode = CircuitAdmin.currentlyUsedNodes.get(route[i].getNickname());
			if (allowedCircuitsWithNode != null)
			{
				numberOfNodeOccurances = allowedCircuitsWithNode.intValue() + 1;
			}
			else
			{
				numberOfNodeOccurances = 0;
			}
			CircuitAdmin.currentlyUsedNodes.put(route[i].getFingerprint(), numberOfNodeOccurances);
		}

		if (i > 0)
		{
			final RouterImpl[] aRoute = createNewRoute(directory, sp,
					proposedRoute, excludedServerFingerprints, route, i - 1, -1);
			if (aRoute == null)
			{

				previousExcludedServerFingerprints.add(route[i - 1].getFingerprint());
				if (maxIterations > -1)
				{
					maxIterations = Math.min(maxIterations,	Directory.RETRIES_ON_RECURSIVE_ROUTE_BUILD) - 1;
				}
				else
				{
					maxIterations = Directory.RETRIES_ON_RECURSIVE_ROUTE_BUILD - 1;
				}
				if (maxIterations < 0)
				{
					return null;
				}
				route = createNewRoute(directory, sp, proposedRoute,
						previousExcludedServerFingerprints, route, i,
						maxIterations);

			}
			else
			{
				route = aRoute;
			}
		}

		return route;
	}

	/**
	 * returns a route through the network as specified in...
	 * 
	 * @see TCPStreamProperties
	 * 
	 * @param directory the {@link Directory}
	 * @param sp tcp stream properties
	 * @return a list of servers
	 * @throws TorException when directory is empty
	 */
	public static RouterImpl[] createNewRoute(final Directory directory, 
			                                  final TCPStreamProperties sp) throws TorException
	{
		// are servers available?
		if (directory.getValidRoutersByFingerprint().size() < 1) // TODO : directory.isDirectoryReady() would be better here?
		{
			throw new TorException("directory is empty");
		}

		// use length of route proposed by TCPStreamProperties
		int len;

		if (sp.getMinRouteLength() == sp.getMinRouteLength()) // random generator is not needed when min == max
		{
			len = sp.getMaxRouteLength();
		}
		else
		{
			// random value between min and max route length
			len = sp.getMinRouteLength() + rnd.nextInt(sp.getMaxRouteLength() - sp.getMinRouteLength() + 1);
		}

		// choose random servers to form route
		final RouterImpl[] route = new RouterImpl[len];

		final HashSet<Fingerprint> excludedServerFingerprints = new HashSet<Fingerprint>();
		// take care, that none of the specified proposed servers is selected
		// before in route
		final Fingerprint[] proposedRoute = sp.getProposedRouteFingerprints();
		if (proposedRoute != null)
		{
			for (int j = 0; j < proposedRoute.length; ++j)
			{
				if (proposedRoute[j] != null)
				{
					final RouterImpl s = directory.getValidRoutersByFingerprint().get(proposedRoute[j]);
					if (s != null)
					{
						excludedServerFingerprints.addAll(directory.excludeRelatedNodes(s));
					}
				}
			}
		}
		final RouterImpl[] result = createNewRoute(directory, 
		                                           sp,
		                                           proposedRoute, 
		                                           excludedServerFingerprints, 
		                                           route, 
		                                           len - 1, 
		                                           -1);

		// the end
		if (result == null)
		{
			LOG.warn("result new route is null");
		}
		else
		{
			if (LOG.isDebugEnabled())
			{
				final StringBuffer sb = new StringBuffer(50);
				for (final RouterImpl server : result)
				{
					sb.append("server(or=" + server.getHostname() + ":"
							+ server.getOrPort() + "(" + server.getNickname()
							+ "), fp=" + server.getFingerprint() + ") ");
				}
				LOG.debug("result new route: {}", sb.toString());
			}
		}
		return result;
	}

	/**
	 * restores circuit from the failed node route[failedNode].
	 * 
	 * @param sp
	 *            tcp stream properties
	 * @param route
	 *            existing route
	 * @param failedNode
	 *            index of node in route, that failed
	 * @return a route
	 */
	public static RouterImpl[] restoreCircuit(final Directory directory,
											  TCPStreamProperties sp, 
											  RouterImpl[] route, 
											  final int failedNode)
													  	throws TorException
	{

		// used to build the custom route up to the failed node
		final Fingerprint[] customRoute = new Fingerprint[route.length];

		// if TCPStreamProperties are NA, create a new one
		if (sp == null)
		{
			sp = new TCPStreamProperties();
		}

		// customize sp, so that createNewRoute could be used to do the job
		// make sure we build circuit of the same length
		sp.setMinRouteLength(route.length);
		// it used to be
		sp.setMaxRouteLength(route.length); //
		// make sure now to select with higher prob. reliable servers
		sp.setRankingInfluenceIndex(1.0f);

		// decreasing ranking of the failed one
		route[failedNode].punishRanking();

		// reuse hosts that are required due to TCPStreamProperties
		if (sp.getRouteFingerprints() != null)
		{
			for (int i = 0; (i < sp.getRouteFingerprints().length) && (i < customRoute.length); ++i)
			{
				customRoute[i] = sp.getRouteFingerprints()[i];
			}
		}
		// reuse hosts that were reported to be working
		for (int i = 0; i < failedNode; ++i)
		{
			customRoute[i] = route[i].getFingerprint();
		}

		sp.setCustomRoute(customRoute);

		try
		{
			route = createNewRoute(directory, sp);

		}
		catch (final TorException te)
		{
			LOG.warn("Directory.restoreCircuit: failed");
		}

		return route;
	}

	public static Integer getCurrentlyUsedNode(final Fingerprint fingerprint)
	{
		return currentlyUsedNodes.get(fingerprint);
	}

	public static void putCurrentlyUsedNodeNumber(final Fingerprint fingerprint, final Integer value)
	{
		currentlyUsedNodes.put(fingerprint, value);
	}

	/**
	 * Remove the current history. Close all circuits that were already be used.
	 */
	public static void clear(final TLSConnectionAdmin tlsConnectionAdmin)
	{
		suitableCircuitsCache.clear();

		// close all circuits that were already be used.
		for (final TLSConnection tls : tlsConnectionAdmin.getConnections())
		{
			for (final Circuit circuit : tls.getCircuits())
			{
				if (circuit.isEstablished()	|| circuit.getStreamHistory().size() > 0)
				{
					circuit.close(true);
				}
			}
		}
	}
}
