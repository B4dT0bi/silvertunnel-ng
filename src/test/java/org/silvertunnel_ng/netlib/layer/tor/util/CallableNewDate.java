package org.silvertunnel_ng.netlib.layer.tor.util;

import java.util.concurrent.Callable;
/**
 * This is just used for testing if the new UTC parser is thread safe.
 * 
 * @author Tobias Boese
 *
 */
public class CallableNewDate implements Callable<Object[]>
{
	private boolean newVersion;
	private String tmp;
	public CallableNewDate(String dateStr, boolean newVersion)
	{
		this.newVersion = newVersion;
		tmp = dateStr;
	}
	@Override
	public Object[] call() throws Exception
	{
		if (newVersion)
		{
			return new Object[]{tmp, Util.parseUtcTimestamp(tmp)};
		}
		else
		{
			return new Object[]{tmp, UtilOld.parseUtcTimestamp(tmp)};
		}
	}

}
