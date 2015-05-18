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

import java.util.HashMap;
import java.util.Map;

/**
 * This Enum is used for parsing the Router descriptor.
 * 
 * @author Tobias Boese
 */
public enum RouterDescriptorFormatKeys
{
	/**
	 * "router" nickname address ORPort SOCKSPort DirPort NL
	 * <br><br>
	 * [At start, exactly once.]
	 * <br><br>
	 * Indicates the beginning of a router descriptor. "nickname" must be a
	 * valid router nickname as specified in 2.3. "address" must be an IPv4
	 * address in dotted-quad format. The last three numbers indicate the TCP
	 * ports at which this OR exposes functionality. ORPort is a port at which
	 * this OR accepts TLS connections for the main OR protocol; SOCKSPort is
	 * deprecated and should always be 0; and DirPort is the port at which this
	 * OR accepts directory-related HTTP connections. If any port is not
	 * supported, the value 0 is given instead of a port number. (At least one
	 * of DirPort and ORPort SHOULD be set; authorities MAY reject any
	 * descriptor with both DirPort and ORPort of 0.)
	 */
	ROUTER_INFO("router", 1, 1),
	/**
	 * "bandwidth" bandwidth-avg bandwidth-burst bandwidth-observed NL
	 * <br><br>
	 * [Exactly once]
	 * <br><br>
	 * Estimated bandwidth for this router, in bytes per second. The "average"
	 * bandwidth is the volume per second that the OR is willing to sustain over
	 * long periods; the "burst" bandwidth is the volume that the OR is willing
	 * to sustain in very short intervals. The "observed" value is an estimate
	 * of the capacity this relay can handle. The relay remembers the max
	 * bandwidth sustained output over any ten second period in the past day,
	 * and another sustained input. The "observed" value is the lesser of these
	 * two numbers.
	 */
	BANDWIDTH("bandwidth", 1, 1),
	/**
	 * "platform" string NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * A human-readable string describing the system on which this OR is
	 * running. This MAY include the operating system, and SHOULD include the
	 * name and version of the software implementing the Tor protocol.
	 */
	PLATFORM("platform", 0, 1),
	/**
	 * "published" YYYY-MM-DD HH:MM:SS NL
	 * <br><br>
	 * [Exactly once]
	 * <br><br>
	 * The time, in UTC, when this descriptor (and its corresponding extra-info
	 * document if any) was generated.
	 */
	PUBLISHED("published", 1, 1),
	/**
	 * "fingerprint" fingerprint NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * A fingerprint (a HASH_LEN-byte of asn1 encoded public key, encoded in
	 * hex, with a single space after every 4 characters) for this router's
	 * identity key. A descriptor is considered invalid (and MUST be rejected)
	 * if the fingerprint line does not match the public key.
	 */
	FINGERPRINT("fingerprint", 0, 1),
	/**
	 * "hibernating" bool NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * If the value is 1, then the Tor relay was hibernating when the descriptor
	 * was published, and shouldn't be used to build circuits.
	 */
	HIBERNATING("hibernating", 0, 1),
	/**
	 * "uptime" number NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * The number of seconds that this OR process has been running.
	 */
	UPTIME("uptime", 0, 1),
	/**
	 * "onion-key" NL a public key in PEM format
	 * <br><br>
	 * [Exactly once]
	 * <br><br>
	 * This key is used to encrypt CREATE cells for this OR. The key MUST be
	 * accepted for at least 1 week after any new key is published in a
	 * subsequent descriptor. It MUST be 1024 bits.
	 */
	ONION_KEY("onion-key", 1, 1),
	/**
	 * "ntor-onion-key" base-64-encoded-key
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * A public key used for the ntor circuit extended handshake. It's the
	 * standard encoding of the OR's curve25519 public key, encoded in base 64.
	 * The trailing = sign may be omitted from the base64 encoding. The key MUST
	 * be accepted for at least 1 week after any new key is published in a
	 * subsequent descriptor.
	 */
	NTOR_ONION_KEY("ntor-onion-key", 0, 1),
	/**
	 * "signing-key" NL a public key in PEM format
	 * <br><br>
	 * [Exactly once]
	 * <br><br>
	 * The OR's long-term identity key. It MUST be 1024 bits.
	 */
	SIGNING_KEY("signing-key", 1, 1),
	/**
	 * "accept" exitpattern NL
	 * <br><br>
	 * [Any number]
	 * <br><br>
	 * These lines describe an "exit policy": the rules that an OR follows when
	 * deciding whether to allow a new stream to a given address. The
	 * 'exitpattern' syntax is described below. There MUST be at least one such
	 * entry. The rules are considered in order; if no rule matches, the address
	 * will be accepted. For clarity, the last such entry SHOULD be accept *:*
	 * or reject *:*.
	 */
	ACCEPT("accept", 0, Integer.MAX_VALUE),
	/**
	 * "reject" exitpattern NL
	 * <br><br>
	 * [Any number]
	 * <br><br>
	 * These lines describe an "exit policy": the rules that an OR follows when
	 * deciding whether to allow a new stream to a given address. The
	 * 'exitpattern' syntax is described below. There MUST be at least one such
	 * entry. The rules are considered in order; if no rule matches, the address
	 * will be accepted. For clarity, the last such entry SHOULD be accept *:*
	 * or reject *:*.
	 */
	REJECT("reject", 0, Integer.MAX_VALUE),
	/**
	 * "ipv6-policy" SP ("accept" / "reject") SP PortList NL
	 * <br><br>
	 * [At most once.]
	 * <br><br>
	 * An exit-policy summary as specified in 3.3 and 3.5.2, summarizing the
	 * router's rules for connecting to IPv6 addresses. A missing "ipv6-policy"
	 * line is equivalent to "ipv6-policy reject 1-65535".
	 */
	IPV6_POLICY("ipv6-policy", 0, 1),
	/**
	 * "router-signature" NL Signature NL
	 * <br><br>
	 * [At end, exactly once]
	 * <br><br>
	 * The "SIGNATURE" object contains a signature of the PKCS1-padded hash of
	 * the entire router descriptor, taken from the beginning of the "router"
	 * line, through the newline after the "router-signature" line. The router
	 * descriptor is invalid unless the signature is performed with the router's
	 * identity key.
	 */
	ROUTER_SIGNATURE("router-signature", 1, 1),
	/**
	 * "contact" info NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * Describes a way to contact the relay's administrator, preferably
	 * including an email address and a PGP key fingerprint.
	 */
	CONTACT("contact", 0, 1),
	/**
	 * "family" names NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * 'Names' is a space-separated list of relay nicknames or hexdigests. If
	 * two ORs list one another in their "family" entries, then OPs should treat
	 * them as a single OR for the purpose of path selection.
	 * <br><br>
	 * For example, if node A's descriptor contains "family B", and node B's
	 * descriptor contains "family A", then node A and node B should never be
	 * used on the same circuit.
	 */
	FAMILY("family", 0, 1),
	/**
	 * "caches-extra-info" NL
	 * <br><br>
	 * [At most once.]
	 * <br><br>
	 * Present only if this router is a directory cache that provides extra-info
	 * documents.
	 */
	CACHES_EXTRA_INFO("caches-extra-info", 0, 1),
	/**
	 * "extra-info-digest" digest NL
	 * <br><br>
	 * [At most once]
	 * <br><br>
	 * "Digest" is a hex-encoded digest (using upper-case characters) of the
	 * router's extra-info document, as signed in the router's extra-info (that
	 * is, not including the signature). (If this field is absent, the router is
	 * not uploading a corresponding extra-info document.)
	 */
	EXTRA_INFO_DIGEST("extra-info-digest", 0, 1),
	/**
	 * "hidden-service-dir" *(SP VersionNum) NL
	 * <br><br>
	 * [At most once.]
	 * <br><br>
	 * Present only if this router stores and serves hidden service descriptors.
	 * If any VersionNum(s) are specified, this router supports those descriptor
	 * versions. If none are specified, it defaults to version 2 descriptors.
	 */
	HIDDEN_SERVICE_DIR("hidden-service-dir", 0, 1),
	/**
	 * "protocols" SP "Link" SP LINK-VERSION-LIST SP "Circuit" SP
	 * CIRCUIT-VERSION-LIST NL
	 * <br><br>
	 * [At most once.]
	 * <br><br>
	 * Both lists are space-separated sequences of numbers, to indicate which
	 * protocols the server supports. As of 30 Mar 2008, specified protocols are
	 * "Link 1 2 Circuit 1". See section 4.1 of tor-spec.txt for more
	 * information about link protocol versions.
	 */
	PROTOCOLS("protocols", 0, 1),
	/**
	 * "allow-single-hop-exits" NL
	 * <br><br>
	 * [At most once.]
	 * <br><br>
	 * Present only if the router allows single-hop circuits to make exit
	 * connections. Most Tor relays do not support this: this is included for
	 * specialized controllers designed to support perspective access and such.
	 */
	ALLOW_SINGLE_HOP_EXITS("allow-single-hop-exits", 0, 1),
	/**
	 * "or-address" SP ADDRESS ":" PORT NL
	 * <br><br>
	 * [Any number]
	 * <br><br>
	 * ADDRESS = IP6ADDR | IP4ADDR IPV6ADDR = an ipv6 address, surrounded by
	 * square brackets. IPV4ADDR = an ipv4 address, represented as a dotted
	 * quad. PORT = a number between 1 and 65535 inclusive.
	 * <br><br>
	 * An alternative for the address and ORPort of the "router" line, but with
	 * two added capabilities:
	 * <br><br>
	 * or-address can be either an IPv4 or IPv6 address or-address allows for
	 * multiple ORPorts and addresses
	 * <br><br>
	 * A descriptor SHOULD NOT include an or-address line that does nothing but
	 * duplicate the address:port pair from its "router" line.
	 * <br><br>
	 * The ordering of or-address lines and their PORT entries matter because
	 * Tor MAY accept a limited number of addresses or ports. As of Tor 0.2.3.x
	 * only the first address and the first port are used.
	 */
	OR_ADDRESS("or-address", 0, Integer.MAX_VALUE);

	/** The internal string representation. */
	private String internalValue;
	/** minimum occurrence of key in descriptor. */
	private Integer min;
	/** maximum occurrence of key in descriptor. */
	private Integer max;

	/**
	 * Private Constructor to be able to save a String.
	 * 
	 * @param value
	 *            the string value
	 * @param rangeMin
	 *            minimum occurrence
	 * @param rangeMax
	 *            maximum occurrence
	 */
	private RouterDescriptorFormatKeys(final String value, final Integer rangeMin, final Integer rangeMax)
	{
		internalValue = value;
		min = rangeMin;
		max = rangeMax;
	}

	/**
	 * @return get the string value from this enum.
	 */
	public String getValue()
	{
		return internalValue;
	}

	/**
	 * @return get the minimum occurrence of this key
	 */
	public int getMin()
	{
		return min;
	}

	/**
	 * @return get the maximum occurrence of this key
	 */
	public int getMax()
	{
		return max;
	}
	/**
	 * Map of all Keys with maximum values for convenience.
	 */
	private static Map<RouterDescriptorFormatKeys, Integer> keysToFind;
	static
	{
		keysToFind = new HashMap<RouterDescriptorFormatKeys, Integer>();
		keysToFind.put(RouterDescriptorFormatKeys.ROUTER_INFO, RouterDescriptorFormatKeys.ROUTER_INFO.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.PLATFORM, RouterDescriptorFormatKeys.PLATFORM.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.PROTOCOLS, RouterDescriptorFormatKeys.PROTOCOLS.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.PUBLISHED, RouterDescriptorFormatKeys.PUBLISHED.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.FINGERPRINT, RouterDescriptorFormatKeys.FINGERPRINT.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.UPTIME, RouterDescriptorFormatKeys.UPTIME.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.BANDWIDTH, RouterDescriptorFormatKeys.BANDWIDTH.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.EXTRA_INFO_DIGEST, RouterDescriptorFormatKeys.EXTRA_INFO_DIGEST.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.ONION_KEY, RouterDescriptorFormatKeys.ONION_KEY.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.NTOR_ONION_KEY, RouterDescriptorFormatKeys.NTOR_ONION_KEY.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.SIGNING_KEY, RouterDescriptorFormatKeys.SIGNING_KEY.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.HIDDEN_SERVICE_DIR, RouterDescriptorFormatKeys.HIDDEN_SERVICE_DIR.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.HIBERNATING, RouterDescriptorFormatKeys.HIBERNATING.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.REJECT, RouterDescriptorFormatKeys.REJECT.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.ACCEPT, RouterDescriptorFormatKeys.ACCEPT.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.CONTACT, RouterDescriptorFormatKeys.CONTACT.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.CACHES_EXTRA_INFO, RouterDescriptorFormatKeys.CACHES_EXTRA_INFO.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.ALLOW_SINGLE_HOP_EXITS, RouterDescriptorFormatKeys.ALLOW_SINGLE_HOP_EXITS.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.OR_ADDRESS, RouterDescriptorFormatKeys.OR_ADDRESS.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.FAMILY, RouterDescriptorFormatKeys.FAMILY.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.IPV6_POLICY, RouterDescriptorFormatKeys.IPV6_POLICY.getMax());
		keysToFind.put(RouterDescriptorFormatKeys.ROUTER_SIGNATURE, RouterDescriptorFormatKeys.ROUTER_SIGNATURE.getMax());
	}
	/**
	 * @return Map of all Keys with maximum values for convenience.
	 */
	public static Map<RouterDescriptorFormatKeys, Integer> getAllKeysAsMap()
	{
		return new HashMap<RouterDescriptorFormatKeys, Integer>(keysToFind);
	}
}
