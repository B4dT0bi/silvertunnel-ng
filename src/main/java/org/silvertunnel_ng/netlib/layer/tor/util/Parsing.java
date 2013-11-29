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
package org.silvertunnel_ng.netlib.layer.tor.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class contains utility functions concerning parsing.
 * 
 * @author hapke
 */
public class Parsing
{
	/** */
	private static final Logger LOG = LoggerFactory.getLogger(Parsing.class);

	/**
	 * convert a decoded fingerprint back into a stringc.
	 * 
	 * @param fingerprint
	 *            a bytearray containing the fingerprint data
	 */
	public static String renderFingerprint(final byte[] fingerprint,
			final boolean withSpace)
	{
		final String hex = "0123456789ABCDEF";
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fingerprint.length; ++i)
		{
			int x = fingerprint[i];
			if (x < 0)
			{
				x = 256 + x; // why are there no unsigned bytes in java?
			}
			sb.append(hex.substring(x >> 4, (x >> 4) + 1));
			sb.append(hex.substring(x % 16, (x % 16) + 1));
			if (((i + 1) % 2 == 0) && withSpace)
			{
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	/**
	 * @param regex
	 * @return pattern of regex, with appropriate flages (Pattern.DOTALL +
	 *         Pattern.MULTILINE + Pattern.CASE_INSENSITIVE +
	 *         Pattern.UNIX_LINES)
	 */
	public static Pattern compileRegexPattern(final String regex)
	{
		return Pattern.compile(regex, Pattern.DOTALL + Pattern.MULTILINE
				+ Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
	}

	/**
	 * parses a line by a regular expression and returns the first hit. If the
	 * regular expression doesn't fit, it returns the default value
	 * 
	 * @param s
	 *            the line to be parsed
	 * @param regexPattern
	 *            the parsing regular expression
	 * @param defaultValue
	 *            the value to be returned, if teh regex doesn't apply
	 * @return either the parsed result or the defaultValue
	 */
	public static String parseStringByRE(final String s,
	                                     final Pattern regexPattern, 
	                                     final String defaultValue)
	{
		final Matcher m = regexPattern.matcher(s);
		if (m.find())
		{
			return m.group(1);
		}
		return defaultValue;
	}

	/**
	 * Used to parse with startKeyWords valid-after, fresh-until, valid-until.
	 * 
	 * @param startKeyWord
	 * @param documentToSearchIn
	 * @return null in the case of an error
	 */
	public static Date parseTimestampLine(final String startKeyWord, final String documentToSearchIn)
	{
		final Pattern pValid = compileRegexPattern("^" + startKeyWord
				+ " (\\S+) (\\S+)");
		final Matcher mValid = pValid.matcher(documentToSearchIn);
		if (mValid.find())
		{
			String value = null;
			try
			{
				value = mValid.group(1) + " " + mValid.group(2);
				return Util.parseUtcTimestamp(value);
			}
			catch (final Exception e)
			{
				LOG.warn("could not parse " + startKeyWord + " from value=");
			}
		}

		// in the case of an error
		return null;
	}
}
