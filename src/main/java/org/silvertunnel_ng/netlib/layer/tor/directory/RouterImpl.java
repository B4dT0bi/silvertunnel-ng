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

package org.silvertunnel_ng.netlib.layer.tor.directory;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel_ng.netlib.layer.tor.api.Router;
import org.silvertunnel_ng.netlib.layer.tor.api.RouterExitPolicy;
import org.silvertunnel_ng.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel_ng.netlib.layer.tor.common.LookupServiceUtil;
import org.silvertunnel_ng.netlib.layer.tor.common.TorConfig;
import org.silvertunnel_ng.netlib.layer.tor.util.Encoding;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.Parsing;
import org.silvertunnel_ng.netlib.layer.tor.util.TorException;
import org.silvertunnel_ng.netlib.layer.tor.util.Util;
import org.silvertunnel_ng.netlib.tool.DynByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a compound data structure that keeps track of the static informations we have
 * about a single Tor server.
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author hapke
 * @author Tobias Boese
 */
public final class RouterImpl implements Router, Cloneable, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8605647987890472910L;

	/** */
	private static final Logger LOG = LoggerFactory.getLogger(RouterImpl.class);

	/** Information extracted from the Router descriptor. */
	private String nickname;
	/** ip or hostname. */
	private String hostname;
	/** the resolved hostname. */
	private InetAddress address;
	/** country code where it is located. */
	private String countryCode;

	/** Onion relay port. */
	private int orPort;
	/** Socks port. */
	private int socksPort;
	/** Directory port. */
	private int dirPort;

	private int bandwidthAvg;
	private int bandwidthBurst;
	private int bandwidthObserved;

	/** Platform of the relay. (tor version + os)*/
	private String platform;
	private long published;

	private Fingerprint fingerprint;
	private Fingerprint v3ident;

	private int uptime;

	private RSAPublicKey onionKey;
	private RSAPrivateKey onionKeyPrivate;

	private RSAPublicKey signingKey;
	private RSAPrivateKey signingKeyPrivate;

	private RouterExitPolicy[] exitpolicy;

	private byte[] routerSignature;
	private String contact;

	/** Fingerprints of the routers of the family. */
	private Set<Fingerprint> family = new HashSet<Fingerprint>();

	/** based on the time of loading this data. */
	private long validUntil;

	/** How many exit policy items do we parse? */
	private static final int MAX_EXITPOLICY_ITEMS = 300;

	// Additional information for V2-Directories
	private long lastUpdate;
	private boolean dirv2Authority = false;
	private boolean dirv2Exit = false;
	private boolean dirv2Fast = false;
	private boolean dirv2Guard = false;
	private boolean dirv2Named = false;
	private boolean dirv2Stable = false;
	private boolean dirv2Running = false;
	private boolean dirv2Valid = false;
	private boolean dirv2V2dir = false;
	private boolean dirv2HSDir = false;

	/** internal Server-Ranking data. */
	private float rankingIndex;
	/** see updateServerRanking(). */
	private static final int highBandwidth = 2097152;
	/** see updateServerRanking(). */
	private static final float alpha = 0.6f;
	/**
	 * coefficient to decrease server ranking if the server fails to respond in
	 * time.
	 */
	private static final float punishmentFactor = 0.75f;

	private static final int MAX_ROUTERDESCRIPTOR_LENGTH = 10000;

	/**
	 * takes a router descriptor as string.
	 * 
	 * @param routerDescriptor
	 *            a router descriptor to initialize the object from
	 */
	public RouterImpl(final String routerDescriptor) throws TorException
	{
		if (routerDescriptor.length() > MAX_ROUTERDESCRIPTOR_LENGTH)
		{
			throw new TorException("skipped router with routerDescriptor of length=" + routerDescriptor.length());
		}

		init();
		parseRouterDescriptor(routerDescriptor);
		updateServerRanking();

		this.countryCode = LookupServiceUtil.getCountryCodeOfIpAddress(this.address);
	}

	/**
	 * Special constructor for hidden service: Faked server in
	 * connectToHidden().
	 * 
	 * @param pk
	 * @throws TorException
	 */
	public RouterImpl(final RSAPublicKey pk) throws TorException
	{
		init();
		onionKey = pk;
		// this.countryCode =
		// LookupServiceUtil.getCountryCodeOfIpAddress(this.address);
		this.countryCode = "--";
	}

	/**
	 * takes input data and initializes the server object with it. A router
	 * descriptor and a signature will be automatically generated.
	 */
	RouterImpl(final String nickname, 
	           final InetAddress address, 
	           final int orPort, 
	           final int dirPort, 
	           final Fingerprint v3ident, 
	           final Fingerprint fingerprint)
				throws TorException
	{
		// Set member variables.
		this.nickname = nickname;
		this.address = address;
		this.hostname = address.getHostAddress();

		this.orPort = orPort;
		this.dirPort = dirPort;
		this.fingerprint = fingerprint.cloneReliable();
		this.v3ident = (v3ident == null) ? null : v3ident.cloneReliable();
	}

	/** Constructor-indepentent initialization. */
	private void init()
	{
		// unknown/new
		rankingIndex = -1;
	}

	/**
	 * Update this server's status.
	 * 
	 * @param flags
	 *            string containing flags
	 */
	void updateServerStatus(final RouterStatusDescription statusDescription)
	{
		dirv2Running = statusDescription.isRunning();
		dirv2Exit = statusDescription.isExit() ? !statusDescription.isBadExit() : false;
		dirv2Authority = statusDescription.isAuthority();
		dirv2Fast = statusDescription.isFast();
		dirv2Guard = statusDescription.isGuard();
		dirv2Stable = statusDescription.isStable();
		dirv2Named = statusDescription.isNamed();
		dirv2V2dir = statusDescription.isV2Dir() ? !statusDescription.isBadDirectory() : false;
		dirv2Valid = statusDescription.isValid();
		dirv2HSDir = statusDescription.isHSDir();
	}

	/**
	 * Clone, but do not throw an exception.
	 */
	public RouterImpl cloneReliable() throws RuntimeException
	{
		try
		{
			return (RouterImpl) clone();
		}
		catch (final CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}
	private static final Pattern EXIT_POLICY_PATTERN = Pattern.compile("^(accept|reject) (.*?):(.*?)$", Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE
	                  				+ Pattern.UNIX_LINES);

	/**
	 * This function parses the exit policy items from the router descriptor.
	 * 
	 * @param routerDescriptor
	 *            a router descriptor with exit policy items.
	 * @return the complete exit policy
	 */
	private RouterExitPolicy[] parseExitPolicy(final String routerDescriptor)
	{
		final ArrayList<RouterExitPolicy> epList = new ArrayList<RouterExitPolicy>(30);

		final Matcher matcher = EXIT_POLICY_PATTERN.matcher(routerDescriptor);

		// extract all exit policies from description
		int nr = 0;
		while (matcher.find() && (nr < MAX_EXITPOLICY_ITEMS))
		{
			boolean epAccept;
			long epIp;
			long epNetmask;
			int epLoPort;
			int epHiPort;
			epAccept = matcher.group(1).equals("accept");
			// parse network
			final String network = matcher.group(2);
			epIp = 0;
			epNetmask = 0;
			if (!network.equals("*"))
			{
				final int slash = network.indexOf("/");
				if (slash >= 0)
				{
					epIp = Encoding.dottedNotationToBinary(network.substring(0, slash));
					final String netmask = network.substring(slash + 1);
					if (netmask.indexOf(".") > -1)
					{
						epNetmask = Encoding.dottedNotationToBinary(netmask);
					}
					else
					{
						epNetmask = (((0xffffffffL << (32 - (Integer.parseInt(netmask))))) & 0xffffffffL);
					}
				}
				else
				{
					epIp = Encoding.dottedNotationToBinary(network);
					epNetmask = 0xffffffff;
				}
			}
			epIp = epIp & epNetmask;
			// parse port range
			if (matcher.group(3).equals("*"))
			{
				epLoPort = 0;
				epHiPort = 65535;
			}
			else
			{
				final int dash = matcher.group(3).indexOf("-");
				if (dash > 0)
				{
					epLoPort = Integer.parseInt(matcher.group(3).substring(0, dash));
					epHiPort = Integer.parseInt(matcher.group(3).substring(dash + 1));
				}
				else
				{
					epLoPort = Integer.parseInt(matcher.group(3));
					epHiPort = epLoPort;
				}
			}
			++nr;
			epList.add(new RouterExitPolicyImpl(epAccept, epIp, epNetmask, epLoPort, epHiPort));
		}

		return (epList.toArray(new RouterExitPolicy[epList.size()]));
	}

	// split into single server descriptors
	private static final Pattern ROUTER_DESCRIPTORS_PATTERN = Pattern.compile("^(router.*?END SIGNATURE-----)", Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE
			+ Pattern.UNIX_LINES);

	/**
	 * parse multiple router descriptors from one String.
	 * 
	 * @param routerDescriptors
	 * @return the result; if multiple entries with the same fingerprint are in
	 *         routerDescriptors, the last be be considered
	 */
	public static Map<Fingerprint, RouterImpl> parseRouterDescriptors(final String routerDescriptors)
	{
		final long timeStart = System.currentTimeMillis();
		final Map<Fingerprint, RouterImpl> result = new HashMap<Fingerprint, RouterImpl>();

		final Matcher m = ROUTER_DESCRIPTORS_PATTERN.matcher(routerDescriptors);

		final ExecutorService executor = Executors.newFixedThreadPool(5); // TODO : make threadpool configurable
		
		final Collection<RouterParserCallable> allTasks = new ArrayList<RouterParserCallable>();

		while (m.find())
		{
			allTasks.add(new RouterParserCallable(m.group(1)));
		}
		List<Future<RouterImpl>> results = null;
		try
		{
			results = executor.invokeAll(allTasks);
		}
		catch (InterruptedException exception)
		{
			LOG.warn("error while parsing the router descriptors in parallel", exception);
		}
		if (results != null && !results.isEmpty())
		{
			for (Future<RouterImpl> item : results)
			{
				RouterImpl router = null;
				try
				{
					router = item.get();
				}
				catch (InterruptedException exception)
				{
					LOG.warn("error while parsing the router descriptors in parallel", exception);
				}
				catch (ExecutionException exception)
				{
					LOG.warn("error while parsing the router descriptors in parallel", exception);
				}
				if (router != null)
				{
					result.put(router.getFingerprint(), router);
				}
			}
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("RouterImpl.parseRouterDescriptors took " + (System.currentTimeMillis() - timeStart) + " ms");
		}
		return result;
	}

	/**
	 * extracts all relevant information from the router descriptor and saves it
	 * in the member variables.
	 * 
	 * @param routerDescriptor
	 *            string encoded router descriptor
	 */
	private void parseRouterDescriptor(final String routerDescriptor) throws TorException
	{
		final long timeStart = System.currentTimeMillis();
		String[] tmpLine = routerDescriptor.split("\n");

		Map<RouterDescriptorFormatKeys, Integer> keysToFind = RouterDescriptorFormatKeys.getAllKeysAsMap();
		MessageDigest mdMessage = null;
		boolean runMd = false;
		// Router item: nickname, hostname, onion-router-port, socks-port,
		// dir-port
		StringBuffer exitPolicyString = new StringBuffer();
		for (int i = 0; i < tmpLine.length; i++)
		{
			if (mdMessage == null && tmpLine[i].startsWith("router "))
			{
				mdMessage = Encryption.getMessagesDigest();
				runMd = true;
			}
			if (runMd)
			{
				mdMessage.update((tmpLine[i] + "\n").getBytes());
				if ("router-signature".equals(tmpLine[i]))
				{
					runMd = false;
				}
			}
			if (tmpLine[i].startsWith("opt")) // remove the opt as we dont need it here
			{
				tmpLine[i] = tmpLine[i].substring(4);
			}
			final String[] tmpElements = tmpLine[i].split(" ");
			for (final Iterator<Map.Entry<RouterDescriptorFormatKeys, Integer>> it = keysToFind.entrySet().iterator(); it.hasNext();)
			{
				final Entry<RouterDescriptorFormatKeys, Integer> entry = it.next();
				final RouterDescriptorFormatKeys key = entry.getKey();
				if (tmpLine[i].startsWith(key.getValue()))
				{
					if (entry.getValue() == 1)
					{
						it.remove(); // remove the key to make the next searches
										// faster
					}
					else
					{
						entry.setValue(entry.getValue() - 1);
					}
					switch (key)
					{
						case ROUTER_INFO:
							this.nickname = tmpElements[1];
							this.hostname = tmpElements[2];
							this.orPort = Integer.parseInt(tmpElements[3]);
							this.socksPort = Integer.parseInt(tmpElements[4]);
							this.dirPort = Integer.parseInt(tmpElements[5]);
							break;
						case PLATFORM:
							this.platform = tmpLine[i].substring("platform".length() + 1);
							break;
						case FINGERPRINT:
							try
							{
								fingerprint = new FingerprintImpl(DatatypeConverter.parseHexBinary(tmpLine[i].substring("fingerprint".length())
										.replaceAll(" ", "")));
							}
							catch (final Exception e)
							{
								LOG.debug("got Exception while parsing fingerprint : {}", e, e);
								throw new TorException("Server " + nickname + " skipped as router", e);
							}
							break;
						case PUBLISHED:
							published = Util.parseUtcTimestamp(tmpElements[1] + " " + tmpElements[2]).getTime();
							validUntil = published + TorConfig.ROUTER_DESCRIPTION_VALID_PERIOD_MS;
							break;
						case UPTIME:
							uptime = Integer.parseInt(tmpElements[1]);
							break;
						case BANDWIDTH:
							bandwidthAvg = Integer.parseInt(tmpElements[1]);
							bandwidthBurst = Integer.parseInt(tmpElements[2]);
							bandwidthObserved = Integer.parseInt(tmpElements[3]);
							break;
						case CONTACT:
							contact = tmpLine[i].substring("contact".length() + 1);
							break;
						case FAMILY:
							for (int n = 1; n < tmpElements.length; n++)
							{
								if (tmpElements[n].startsWith("$"))
								{
									family.add(new FingerprintImpl(DatatypeConverter.parseHexBinary(tmpElements[n].substring(1, 41))));
								}
								else
								{
									LOG.debug("skipping family member {}", tmpElements[n]);
									//TODO : implement family members without fingerprint
								}
							}
						case HIBERNATING:
							// TODO : add flag that router is hibernating (do not use it for building circuits)
							break;
						case HIDDEN_SERVICE_DIR:
							dirv2HSDir = true;
							break;
						case PROTOCOLS:
							// TODO : implement
							break;
						case EXTRA_INFO_DIGEST:
							// TODO : implement
							break;
						case CACHES_EXTRA_INFO:
							// TODO : implement
							break;
						case NTOR_ONION_KEY:
							// TODO : implement
							break;
						case ONION_KEY:
							StringBuffer tmpOnionKey = new StringBuffer();
							i++;
							while (!tmpLine[i].contains("END RSA PUBLIC KEY"))
							{
								tmpOnionKey.append(tmpLine[i]).append('\n');
								i++;
							}
							tmpOnionKey.append(tmpLine[i]).append('\n');
							mdMessage.update(tmpOnionKey.toString().getBytes());
							onionKey = Encryption.extractPublicRSAKey(tmpOnionKey.toString());
							break;
						case SIGNING_KEY:
							StringBuffer tmpSigningKey = new StringBuffer();
							i++;
							while (!tmpLine[i].contains("END RSA PUBLIC KEY"))
							{
								tmpSigningKey.append(tmpLine[i]).append('\n');
								i++;
							}
							tmpSigningKey.append(tmpLine[i]).append('\n');
							mdMessage.update(tmpSigningKey.toString().getBytes());
							signingKey = Encryption.extractPublicRSAKey(tmpSigningKey.toString());
							break;
						case ROUTER_SIGNATURE:
							StringBuffer tmpSignature = new StringBuffer();
							i += 2;
							while (!tmpLine[i].contains("END SIGNATURE"))
							{
								tmpSignature.append(tmpLine[i]);
								i++;
							}
							while (tmpSignature.length() % 4 != 0)
							{
								tmpSignature.append('='); // add missing padding
							}
							routerSignature = DatatypeConverter.parseBase64Binary(tmpSignature.toString());
							break;
						case REJECT:
							exitPolicyString.append(tmpLine[i]).append('\n');
							break;
						case ACCEPT:
							exitPolicyString.append(tmpLine[i]).append('\n');
							break;
						case ALLOW_SINGLE_HOP_EXITS:
							// TODO : implement
							break;
						case IPV6_POLICY:
							// TODO : implement
							break;
						case OR_ADDRESS:
							// TODO : implement
							break;
						default:
							LOG.debug("it seems that we are not reading the following key : {}", key.getValue());
							break;
					}
				}
			}
		}

		// secondary information

		// verify signing-key against fingerprint
		try
		{
			final byte[] pkcs = Encryption.getPKCS1EncodingFromRSAPublicKey(signingKey);
			final byte[] keyHash = Encryption.getDigest(pkcs);
			if (!new FingerprintImpl(keyHash).equals(fingerprint))
			{
				throw new TorException("Server " + nickname + " doesn't verify signature vs fingerprint");
			}
		}
		catch (final TorException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new TorException("Server " + nickname + " doesn't verify signature vs fingerprint", e);
		}

		// check the validity of the signature
		final byte[] sha1Digest = mdMessage.digest();
		if (!Encryption.verifySignatureWithHash(routerSignature, signingKey, sha1Digest))
		{
			LOG.info("Server -> router-signature check failed for " + nickname);
			throw new TorException("Server " + nickname + ": description signature verification failed");
		}

		// exit policy
		exitpolicy = parseExitPolicy(exitPolicyString.toString());
		// usually in directory the hostname is already set to the IP
		// so, following resolve just converts it to the InetAddress
		try
		{
			address = InetAddress.getByName(hostname);
		}
		catch (final UnknownHostException e)
		{
			throw new TorException("Server.ParseRouterDescriptor: Unresolvable hostname " + hostname);
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("RouterImpl.parseRouterDescriptor took " + (System.currentTimeMillis() - timeStart) + " ms");
		}
	}

	/**
	 * updates the server ranking index
	 * 
	 * Is supposed to be between 0 (undesirable) and 1 (very desirable). Two
	 * variables are taken as input:
	 * <ul>
	 * <li>the uptime
	 * <li>the bandwidth
	 * <li>if available: the previous ranking
	 * </ul>
	 */
	private void updateServerRanking()
	{
		final float rankingFromDirectory = (Math.min(1, uptime / 86400) + Math.min(1, (bandwidthAvg * alpha + bandwidthObserved * (1 - alpha))
				/ highBandwidth)) / 2; // 86400 is uptime of 24h
		// build over-all ranking from old value (if available) and new
		if (rankingIndex < 0)
		{
			rankingIndex = rankingFromDirectory;
		}
		else
		{
			rankingIndex = rankingFromDirectory * (1 - TorConfig.rankingTransferPerServerUpdate) + rankingIndex
					* TorConfig.rankingTransferPerServerUpdate;
		}
	}

	/**
	 * returns ranking index taking into account user preference.
	 * 
	 * @param p
	 *            user preference (importance) of considering ranking index
	 *            <ul>
	 *            <li>0 select hosts completely randomly
	 *            <li>1 select hosts with good uptime/bandwidth with higher
	 *            prob.
	 *            </ul>
	 */
	float getRefinedRankingIndex(final float p)
	{
		// align all ranking values to 0.5, if the user wants to choose his
		// servers
		// from a uniform probability distribution
		return (rankingIndex * p + TorConfig.rankingIndexEffect * (1 - p));
	}

	/**
	 * decreases rankingIndex by the punishmentFactor.
	 */
	public void punishRanking()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Punishing " + toLongString());
		}
		rankingIndex *= punishmentFactor;
	}

	/**
	 * can be used to query the exit policies whether this server would allow
	 * outgoing connections to the host and port as given in the parameters.
	 * <b>IMPORTANT:</b> this routing must be able to work, even if <i>addr</i>
	 * is not given!
	 * 
	 * @param addr
	 *            the host that someone wants to connect to
	 * @param port
	 *            the port that is to be connected to
	 * @return a boolean value whether the connection would be allowed
	 */
	public boolean exitPolicyAccepts(final InetAddress addr, final int port)
	{
		long ip;
		if (addr != null)
		{ // set IP as given
			final byte[] temp1 = addr.getAddress();
			final long[] temp = new long[4];
			for (int i = 0; i < 4; ++i)
			{
				temp[i] = temp1[i];
				if (temp[i] < 0)
				{
					temp[i] = 256 + temp[i];
				}
			}
			ip = ((temp[0] << 24) | (temp[1] << 16) | (temp[2] << 8) | temp[3]);
		}
		else
		{
			// HACK: if no IP and port is given, always return true
			if (port == 0)
			{
				return true;
			}
			// HACK: if no IP is given, use only exits that allow ALL ip-ranges
			// this should possibly be replaced by some other way of checking it
			ip = 0xffffffffL;
		}

		for (int i = 0; i < exitpolicy.length; ++i)
		{
			if ((exitpolicy[i].getLoPort() <= port) && (exitpolicy[i].getHiPort() >= port)
					&& (exitpolicy[i].getIp() == (ip & exitpolicy[i].getNetmask())))
			{
				return exitpolicy[i].isAccept();
			}
		}
		return false;
	}

	/**
	 * @return can this server be used as a directory-server?
	 */
	protected boolean isDirServer()
	{
		return (dirPort > 0); //TODO : in newer Tor versions it is also possible to be a dir-server without port...
	}

	/**
	 * @return is this router an exit node? (used for faster route creation)
	 */
	@Override
	public boolean isExitNode()
	{
		for (RouterExitPolicy singleExitPolicy : exitpolicy)
		{
			if (singleExitPolicy.isAccept())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * used for debugging purposes.
	 */
	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("router=" + nickname);
		sb.append("," + hostname);
		sb.append("," + fingerprint);
		sb.append("," + platform);
		return sb.toString();
	}

	/**
	 * used for debugging purposes.
	 */
	public String toLongString()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("---- ").append(nickname).append(" (").append(contact).append(")\n");
		sb.append("hostname:").append(hostname).append('\n');
		sb.append("or port:").append(orPort).append('\n');
		sb.append("socks port:").append(socksPort).append('\n');
		sb.append("dirserver port:").append(dirPort).append('\n');
		sb.append("platform:").append(platform).append('\n');
		sb.append("published:").append(new Date(published)).append('\n');
		sb.append("uptime:").append(uptime).append('\n');
		sb.append("rankingIndex:").append(rankingIndex).append('\n');
		sb.append("bandwidth: ").append(bandwidthAvg).append(' ').append(bandwidthBurst).append(' ').append(bandwidthObserved).append('\n');
		sb.append("fingerprint:").append(fingerprint).append('\n');
		sb.append("validUntil:").append(new Date(validUntil)).append('\n');
		sb.append("onion key:").append(onionKey).append('\n');
		sb.append("signing key:").append(signingKey).append('\n');
		sb.append("signature:").append(DatatypeConverter.printHexBinary(routerSignature)).append('\n');
		sb.append("exit policies:").append('\n');
		for (int i = 0; i < exitpolicy.length; ++i)
		{
			sb.append("  ").append(exitpolicy[i]).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Check if the router description is still valid.
	 * 
	 * @return true if valid
	 */
	public boolean isValid()
	{
		return validUntil > System.currentTimeMillis();
	}

	/**
	 * @return address + directory port
	 */
	public TcpipNetAddress getDirAddress()
	{
		final byte[] ipaddress = address.getAddress();
		if (ipaddress != null)
		{
			return new TcpipNetAddress(ipaddress, dirPort);
		}
		else
		{
			return new TcpipNetAddress(address.getHostName(), dirPort);
		}
	}

	/**
	 * @return address + or port
	 */
	public TcpipNetAddress getOrAddress()
	{
		final byte[] ipaddress = address.getAddress();
		if (ipaddress != null)
		{
			return new TcpipNetAddress(ipaddress, orPort);
		}
		else
		{
			return new TcpipNetAddress(address.getHostName(), orPort);
		}
	}

	// /////////////////////////////////////////////////////
	// generated getters and setters
	// /////////////////////////////////////////////////////

	@Override
	public String getNickname()
	{
		return nickname;
	}

	@Override
	public String getHostname()
	{
		return hostname;
	}

	@Override
	public InetAddress getAddress()
	{
		return address;
	}

	@Override
	public String getCountryCode()
	{
		return countryCode;
	}

	@Override
	public int getOrPort()
	{
		return orPort;
	}

	@Override
	public int getSocksPort()
	{
		return socksPort;
	}

	@Override
	public int getDirPort()
	{
		return dirPort;
	}

	@Override
	public int getBandwidthAvg()
	{
		return bandwidthAvg;
	}

	@Override
	public int getBandwidthBurst()
	{
		return bandwidthBurst;
	}

	@Override
	public int getBandwidthObserved()
	{
		return bandwidthObserved;
	}

	@Override
	public String getPlatform()
	{
		return platform;
	}

	@Override
	public long getPublished()
	{
		return published;
	}

	@Override
	public Fingerprint getFingerprint()
	{
		return fingerprint;
	}

	public Fingerprint getV3Ident()
	{
		return v3ident;
	}

	@Override
	public int getUptime()
	{
		return uptime;
	}

	@Override
	public RSAPublicKey getOnionKey()
	{
		return onionKey;
	}

	@Override
	public RSAPublicKey getSigningKey()
	{
		return signingKey;
	}

	@Override
	public RouterExitPolicy[] getExitpolicy()
	{
		return exitpolicy;
	}

	@Override
	public String getContact()
	{
		return contact;
	}

	@Override
	public Set<Fingerprint> getFamily()
	{
		return family;
	}

	@Override
	public long getValidUntil()
	{
		return validUntil;
	}

	@Override
	public long getLastUpdate()
	{
		return lastUpdate;
	}

	@Override
	public boolean isDirv2Authority()
	{
		return dirv2Authority;
	}

	@Override
	public boolean isDirv2Exit()
	{
		return dirv2Exit;
	}

	@Override
	public boolean isDirv2Fast()
	{
		return dirv2Fast;
	}

	@Override
	public boolean isDirv2Guard()
	{
		return dirv2Guard;
	}

	@Override
	public boolean isDirv2Named()
	{
		return dirv2Named;
	}

	@Override
	public boolean isDirv2Stable()
	{
		return dirv2Stable;
	}

	@Override
	public boolean isDirv2Running()
	{
		return dirv2Running;
	}

	@Override
	public boolean isDirv2Valid()
	{
		return dirv2Valid;
	}

	@Override
	public boolean isDirv2V2dir()
	{
		return dirv2V2dir;
	}

	/**
	 * @return true=if the router is considered a v2 hidden service directory
	 */
	public boolean isDirv2HSDir()
	{
		return dirv2HSDir;
	}

	@Override
	public float getRankingIndex()
	{
		return rankingIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + bandwidthAvg;
		result = prime * result + bandwidthBurst;
		result = prime * result + bandwidthObserved;
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
		result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
		result = prime * result + dirPort;
		result = prime * result + (dirv2Authority ? 1231 : 1237);
		result = prime * result + (dirv2Exit ? 1231 : 1237);
		result = prime * result + (dirv2Fast ? 1231 : 1237);
		result = prime * result + (dirv2Guard ? 1231 : 1237);
		result = prime * result + (dirv2HSDir ? 1231 : 1237);
		result = prime * result + (dirv2Named ? 1231 : 1237);
		result = prime * result + (dirv2Running ? 1231 : 1237);
		result = prime * result + (dirv2Stable ? 1231 : 1237);
		result = prime * result + (dirv2V2dir ? 1231 : 1237);
		result = prime * result + (dirv2Valid ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(exitpolicy);
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((fingerprint == null) ? 0 : fingerprint.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + (int) lastUpdate;
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((onionKey == null) ? 0 : onionKey.hashCode());
		result = prime * result + ((onionKeyPrivate == null) ? 0 : onionKeyPrivate.hashCode());
		result = prime * result + orPort;
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		result = prime * result + (int) published;
		result = prime * result + Float.floatToIntBits(rankingIndex);
		result = prime * result + Arrays.hashCode(routerSignature);
		result = prime * result + ((signingKey == null) ? 0 : signingKey.hashCode());
		result = prime * result + ((signingKeyPrivate == null) ? 0 : signingKeyPrivate.hashCode());
		result = prime * result + socksPort;
		result = prime * result + uptime;
		result = prime * result + ((v3ident == null) ? 0 : v3ident.hashCode());
		result = (int) (prime * result + validUntil);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof RouterImpl))
		{
			return false;
		}
		RouterImpl other = (RouterImpl) obj;
		if (address == null)
		{
			if (other.address != null)
			{
				return false;
			}
		}
		else if (!address.equals(other.address))
		{
			return false;
		}
		if (bandwidthAvg != other.bandwidthAvg)
		{
			return false;
		}
		if (bandwidthBurst != other.bandwidthBurst)
		{
			return false;
		}
		if (bandwidthObserved != other.bandwidthObserved)
		{
			return false;
		}
		if (contact == null)
		{
			if (other.contact != null)
			{
				return false;
			}
		}
		else if (!contact.equals(other.contact))
		{
			return false;
		}
		if (countryCode == null)
		{
			if (other.countryCode != null)
			{
				return false;
			}
		}
		else if (!countryCode.equals(other.countryCode))
		{
			return false;
		}
		if (dirPort != other.dirPort)
		{
			return false;
		}
		if (dirv2Authority != other.dirv2Authority)
		{
			return false;
		}
		if (dirv2Exit != other.dirv2Exit)
		{
			return false;
		}
		if (dirv2Fast != other.dirv2Fast)
		{
			return false;
		}
		if (dirv2Guard != other.dirv2Guard)
		{
			return false;
		}
		if (dirv2HSDir != other.dirv2HSDir)
		{
			return false;
		}
		if (dirv2Named != other.dirv2Named)
		{
			return false;
		}
		if (dirv2Running != other.dirv2Running)
		{
			return false;
		}
		if (dirv2Stable != other.dirv2Stable)
		{
			return false;
		}
		if (dirv2V2dir != other.dirv2V2dir)
		{
			return false;
		}
		if (dirv2Valid != other.dirv2Valid)
		{
			return false;
		}
		if (!Arrays.equals(exitpolicy, other.exitpolicy))
		{
			return false;
		}
		if (family == null)
		{
			if (other.family != null)
			{
				return false;
			}
		}
		else if (!family.equals(other.family))
		{
			return false;
		}
		if (fingerprint == null)
		{
			if (other.fingerprint != null)
			{
				return false;
			}
		}
		else if (!fingerprint.equals(other.fingerprint))
		{
			return false;
		}
		if (hostname == null)
		{
			if (other.hostname != null)
			{
				return false;
			}
		}
		else if (!hostname.equals(other.hostname))
		{
			return false;
		}
		if (lastUpdate != other.lastUpdate)
		{
				return false;
		}
		if (nickname == null)
		{
			if (other.nickname != null)
			{
				return false;
			}
		}
		else if (!nickname.equals(other.nickname))
		{
			return false;
		}
		if (onionKey == null)
		{
			if (other.onionKey != null)
			{
				return false;
			}
		}
		else if (!onionKey.equals(other.onionKey))
		{
			return false;
		}
		if (onionKeyPrivate == null)
		{
			if (other.onionKeyPrivate != null)
			{
				return false;
			}
		}
		else if (!Arrays.equals(onionKeyPrivate.getEncoded(), other.onionKeyPrivate.getEncoded()))
		{
			return false;
		}
		if (orPort != other.orPort)
		{
			return false;
		}
		if (platform == null)
		{
			if (other.platform != null)
			{
				return false;
			}
		}
		else if (!platform.equals(other.platform))
		{
			return false;
		}
		if (published != other.published)
		{
				return false;
		}
		if (Float.floatToIntBits(rankingIndex) != Float.floatToIntBits(other.rankingIndex))
		{
			return false;
		}
		if (!Arrays.equals(routerSignature, other.routerSignature))
		{
			return false;
		}
		if (signingKey == null)
		{
			if (other.signingKey != null)
			{
				return false;
			}
		}
		else if (!Arrays.equals(signingKey.getEncoded(), other.signingKey.getEncoded()))
		{
			return false;
		}
		if (signingKeyPrivate == null)
		{
			if (other.signingKeyPrivate != null)
			{
				return false;
			}
		}
		else if (!Arrays.equals(signingKeyPrivate.getEncoded(), other.signingKeyPrivate.getEncoded()))
		{
			return false;
		}
		if (socksPort != other.socksPort)
		{
			return false;
		}
		if (uptime != other.uptime)
		{
			return false;
		}
		if (v3ident == null)
		{
			if (other.v3ident != null)
			{
				return false;
			}
		}
		else if (!v3ident.equals(other.v3ident))
		{
			return false;
		}
		if (validUntil != other.validUntil)
		{
			return false;
		}
		return true;
	}
}
