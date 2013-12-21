/**
 * 
 */
package org.silvertunnel_ng.netlib.util;

import java.util.logging.Level;

import org.slf4j.Logger;

/**
 * Helper class for Logging purposes.
 * 
 * @author Tobias Boese
 *
 */
public final class LogHelper
{
	private LogHelper()
	{
		
	}
	/**
	 * Is the desired LogLevel active?
	 * 
	 * @param logger the logger to be checked
	 * @param level the log-{@link Level} to be checked
	 * @return true if it is logable
	 */
	public static boolean isLoggable(final Logger logger, final Level level)
	{
		if (level == Level.INFO)
		{
			return logger.isInfoEnabled();
		}
		else if (level == Level.SEVERE)
		{
			return logger.isErrorEnabled();
		}
		else if (level == Level.WARNING)
		{
			return logger.isWarnEnabled();
		}
		else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST)
		{
			return logger.isDebugEnabled();
		}
		return false;
	}
	/**
	 * Directly log msg to desired {@link Logger}.
	 * 
	 * @param logger the {@link Logger} which should be used for logging
	 * @param level the log-{@link Level} to be used
	 * @param msg the message to log
	 * @param withStackTrace
	 *            true=log include current stack trace
	 * @param logMessagePrefix messagePreFix
	 */
	public static void logLine(final Logger logger, 
	                           final Level level, 
	                           final String msg,
	                           final boolean withStackTrace, 
	                           final String logMessagePrefix)
	{
		final String finalMsg = logMessagePrefix + msg;
		if (level == Level.INFO)
		{
			if (withStackTrace)
			{
				logger.info(finalMsg, new Throwable());
			}
			else
			{
				logger.info(finalMsg);
			}			
		}
		else if (level == Level.SEVERE)
		{
			if (withStackTrace)
			{
				logger.error(finalMsg, new Throwable());
			}
			else
			{
				logger.error(finalMsg);
			}			
		}
		else if (level == Level.WARNING)
		{
			if (withStackTrace)
			{
				logger.warn(finalMsg, new Throwable());
			}
			else
			{
				logger.warn(finalMsg);
			}			
		}
		else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST)
		{
			if (withStackTrace)
			{
				logger.debug(finalMsg, new Throwable());
			}
			else
			{
				logger.debug(finalMsg);
			}			
		}
	}
}
