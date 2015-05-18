/*
 * @(#)AuthenticationHeader.java    1.9 05/12/01
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.silvertunnel_ng.netlib.adapter.url.impl.net.http;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is used to parse the information in WWW-Authenticate: and
 * Proxy-Authenticate: headers. It searches among multiple header lines and
 * within each header line for the best currently supported scheme. It can also
 * return a HeaderParser containing the challenge data for that particular
 * scheme.
 * 
 * Some examples:
 * 
 * WWW-Authenticate: Basic realm="foo" Digest realm="bar" NTLM Note the realm
 * parameter must be associated with the particular scheme.
 * 
 * or
 * 
 * WWW-Authenticate: Basic realm="foo" WWW-Authenticate: Digest
 * realm="foo",qop="auth",nonce="thisisanunlikelynonce" WWW-Authenticate: NTLM
 * 
 * or
 * 
 * WWW-Authenticate: Basic realm="foo" WWW-Authenticate: NTLM
 * ASKAJK9893289889QWQIOIONMNMN
 * 
 * The last example shows how NTLM breaks the rules of rfc2617 for the structure
 * of the authentication header. This is the reason why the raw header field is
 * used for ntlm.
 * 
 * At present, the class chooses schemes in following order : 1. Negotiate (if
 * supported) 2. Kerberos (if supported) 3. Digest 4. NTLM (if supported) 5.
 * Basic
 * 
 * This choice can be modified by setting a system property:
 * 
 * -Dhttp.auth.preference="scheme"
 * 
 * which in this case, specifies that "scheme" should be used as the auth scheme
 * when offered disregarding the default prioritisation. If scheme is not
 * offered then the default priority is used.
 * 
 * Attention: when http.auth.preference is set as SPNEGO or Kerberos, it's
 * actually "Negotiate with SPNEGO" or "Negotiate with Kerberos", which means
 * the user will prefer the Negotiate scheme with GSS/SPNEGO or GSS/Kerberos
 * mechanism.
 * 
 * This also means that the real "Kerberos" scheme can never be set as a
 * preference.
 */

public class AuthenticationHeader
{
	/** the response to be parsed. */
	MessageHeader rsp;
	HeaderParser preferred;
	/** raw strings. */
	String preferredRaw;
	/** the hostname for server, used in checking the availability of Negotiate. */
	String host = null;

	static String authPref = null;

	@Override
	public String toString()
	{
		return "AuthenticationHeader: prefer " + preferredRaw;
	}

	static
	{
		authPref = (String) java.security.AccessController
				.doPrivileged(new java.security.PrivilegedAction()
				{
					@Override
					public Object run()
					{
						return System.getProperty("http.auth.preference");
					}
				});

		// http.auth.preference can be set to SPNEGO or Kerberos.
		// In fact they means "Negotiate with SPNEGO" and "Negotiate with
		// Kerberos" separately, so here they are all translated into
		// Negotiate. Read NegotiateAuthentication.java to see how they
		// were used later.

		if (authPref != null)
		{
			authPref = authPref.toLowerCase();
			if (authPref.equals("spnego") || authPref.equals("kerberos"))
			{
				authPref = "negotiate";
			}
		}
	}
	/** name of the header to look for. */
	String hdrname;

	/**
	 * parse a set of authentication headers and choose the preferred scheme
	 * that we support.
	 */
	public AuthenticationHeader(final String hdrname, final MessageHeader response)
	{
		rsp = response;
		this.hdrname = hdrname;
		schemes = new HashMap<String, SchemeMapValue>();
		parse();
	}

	/**
	 * parse a set of authentication headers and choose the preferred scheme
	 * that we support for a given host.
	 */
	public AuthenticationHeader(final String hdrname, final MessageHeader response, final String host)
	{
		this.host = host;
		rsp = response;
		this.hdrname = hdrname;
		schemes = new HashMap<String, SchemeMapValue>();
		parse();
	}

	/* we build up a map of scheme names mapped to SchemeMapValue objects */
	static class SchemeMapValue
	{
		SchemeMapValue(final HeaderParser headerParser, final String rawValue)
		{
			raw = rawValue;
			parser = headerParser;
		}

		String raw;
		HeaderParser parser;
	}

	HashMap<String, SchemeMapValue> schemes;

	/**
	 * Iterate through each header line, and then within each line. If multiple
	 * entries exist for a particular scheme (unlikely) then the last one will
	 * be used. The preferred scheme that we support will be used.
	 */
	private void parse()
	{
		final Iterator<String> iter = rsp.multiValueIterator(hdrname);
		while (iter.hasNext())
		{
			final String raw = iter.next();
			final HeaderParser headerParser = new HeaderParser(raw);
			final Iterator<Object> keys = headerParser.keys();
			int i, lastSchemeIndex;
			for (i = 0, lastSchemeIndex = -1; keys.hasNext(); i++)
			{
				keys.next();
				if (headerParser.findValue(i) == null)
				{ /* found a scheme name */
					if (lastSchemeIndex != -1)
					{
						final HeaderParser hpn = headerParser.subsequence(
								lastSchemeIndex, i);
						final String scheme = hpn.findKey(0);
						schemes.put(scheme, new SchemeMapValue(hpn, raw));
					}
					lastSchemeIndex = i;
				}
			}
			if (i > lastSchemeIndex)
			{
				final HeaderParser hpn = headerParser.subsequence(lastSchemeIndex, i);
				final String scheme = hpn.findKey(0);
				schemes.put(scheme, new SchemeMapValue(hpn, raw));
			}
		}

		/*
		 * choose the best of them, the order is negotiate -> kerberos -> digest
		 * -> ntlm -> basic
		 */
		SchemeMapValue schemeMapValue = null;
		if (authPref == null || (schemeMapValue = schemes.get(authPref)) == null)
		{

			if (schemeMapValue == null)
			{
				SchemeMapValue tmp = schemes.get("negotiate");
				if (tmp != null)
				{
					if (host == null
							|| !NegotiateAuthentication.isSupported(host,
									"Negotiate"))
					{
						tmp = null;
					}
					schemeMapValue = tmp;
				}
			}

			if (schemeMapValue == null)
			{
				SchemeMapValue tmp = schemes.get("kerberos");
				if (tmp != null)
				{
					// the Kerberos scheme is only observed in MS ISA Server. In
					// fact i think it's a Kerberos-mechnism-only Negotiate.
					// Since the Kerberos scheme is always accompanied with the
					// Negotiate scheme, so it seems impossible to reach this
					// line. Even if the user explicitly set
					// http.auth.preference
					// as Kerberos, it means Negotiate with Kerberos, and the
					// code
					// will still tried to use Negotiate at first.
					//
					// The only chance this line get executed is that the server
					// only suggest the Kerberos scheme.
					if (host == null
							|| !NegotiateAuthentication.isSupported(host,
									"Kerberos"))
					{
						tmp = null;
					}
					schemeMapValue = tmp;
				}
			}

			if (schemeMapValue == null)
			{
				if ((schemeMapValue = schemes.get("digest")) == null)
				{
					if (((schemeMapValue = schemes.get("ntlm")) == null))
					{
						schemeMapValue = schemes.get("basic");
					}
				}
			}
		}
		if (schemeMapValue != null)
		{
			preferred = schemeMapValue.parser;
			preferredRaw = schemeMapValue.raw;
		}
	}

	/**
	 * return a header parser containing the preferred authentication scheme
	 * (only). The preferred scheme is the strongest of the schemes proposed by
	 * the server. The returned HeaderParser will contain the relevant
	 * parameters for that scheme
	 */
	public HeaderParser headerParser()
	{
		return preferred;
	}

	/**
	 * return the name of the preferred scheme.
	 */
	public String scheme()
	{
		if (preferred != null)
		{
			return preferred.findKey(0);
		}
		else
		{
			return null;
		}
	}

	/* return the raw header field for the preferred/chosen scheme */

	public String raw()
	{
		return preferredRaw;
	}

	/**
	 * returns true is the header exists and contains a recognized scheme.
	 */
	public boolean isPresent()
	{
		return preferred != null;
	}
}
