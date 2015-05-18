/**
 * 
 */
package org.silvertunnel_ng.netlib.layer.tor.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.Test;

/**
 * @author Tobias Boese
 *
 */
public final class UtilTest
{

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#parseUtcTimestamp(java.lang.String)}.
	 */
	@Test (enabled = false)
	public void testParseUtcTimestamp()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#parseUtcTimestampAsLong(java.lang.String)}.
	 */
	@Test(enabled = false)
	public void testParseUtcTimestampAsLong()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#parseUtcTimestampNew(java.lang.String)}.
	 * @throws ParseException 
	 */
	@Test
	public void testParseUtcTimestampNew() throws ParseException
	{
		String timeStamp = "1984-03-22 12:34:56";
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		calendar.setTime(dateFormat.parse(timeStamp));
		for (int i = 0; i < 100000; i++)
		{
			calendar.roll(Calendar.SECOND, true);
			if (calendar.get(Calendar.SECOND) == 0)
			{
				calendar.roll(Calendar.MINUTE, true);
			}
			if (calendar.get(Calendar.MINUTE) == 0)
			{
				calendar.roll(Calendar.HOUR_OF_DAY, true);
			}
			if (calendar.get(Calendar.HOUR_OF_DAY) == 0)
			{
				calendar.roll(Calendar.DAY_OF_YEAR, true);
			}
			String tmpTime = dateFormat.format(calendar.getTime());
			Date orgDate = UtilOld.parseUtcTimestamp(tmpTime);
			Date newDate = Util.parseUtcTimestamp(tmpTime);
			assertEquals(orgDate, newDate);
		}
		for (int i = 0; i < 100000; i++)
		{
			calendar.roll(Calendar.DAY_OF_YEAR, true);
			if (calendar.get(Calendar.DAY_OF_YEAR) == 1)
			{
				calendar.roll(Calendar.YEAR, true);
			}
			String tmpTime = dateFormat.format(calendar.getTime());
			Date orgDate = UtilOld.parseUtcTimestamp(tmpTime);
			Date newDate = Util.parseUtcTimestamp(tmpTime);
			assertEquals(orgDate, newDate);
		}
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#parseUtcTimestampNew(java.lang.String)}.
	 * @throws ParseException 
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testParseUtcTimestampNewThreaded() throws ParseException, InterruptedException, ExecutionException
	{
		String timeStamp = "1984-03-22 12:34:56";
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		calendar.setTime(dateFormat.parse(timeStamp));
		
		final ExecutorService executorOld = Executors.newFixedThreadPool(5);
		final Collection<Callable<Object[]>> allTasksOld = new ArrayList<Callable<Object[]>>();
		final ExecutorService executorNew = Executors.newFixedThreadPool(5);
		final Collection<Callable<Object[]>> allTasksNew = new ArrayList<Callable<Object[]>>();

		for (int i = 0; i < 100000; i++)
		{
			calendar.roll(Calendar.SECOND, true);
			if (calendar.get(Calendar.SECOND) == 0)
			{
				calendar.roll(Calendar.MINUTE, true);
			}
			if (calendar.get(Calendar.MINUTE) == 0)
			{
				calendar.roll(Calendar.HOUR_OF_DAY, true);
			}
			if (calendar.get(Calendar.HOUR_OF_DAY) == 0)
			{
				calendar.roll(Calendar.DAY_OF_YEAR, true);
			}
			String tmpTime = dateFormat.format(calendar.getTime());
			allTasksNew.add(new CallableNewDate(tmpTime, true));
			allTasksOld.add(new CallableNewDate(tmpTime, false));
		}
		for (int i = 0; i < 100000; i++)
		{
			calendar.roll(Calendar.DAY_OF_YEAR, true);
			if (calendar.get(Calendar.DAY_OF_YEAR) == 1)
			{
				calendar.roll(Calendar.YEAR, true);
			}
			String tmpTime = dateFormat.format(calendar.getTime());
			allTasksNew.add(new CallableNewDate(tmpTime, true));
			allTasksOld.add(new CallableNewDate(tmpTime, false));
		}
		long start = System.currentTimeMillis();
		List<Future<Object[]>> listOld = executorOld.invokeAll(allTasksOld);
		System.out.println("old : " + (System.currentTimeMillis() - start) + "ms");
		HashMap<String, Date> mapOld = new HashMap<String, Date>();
		for (Future<Object[]> item : listOld)
		{
			if (item != null)
			{
				if (item.get() != null)
				{
					if (item.get().length == 2)
					{
						mapOld.put((String) item.get()[0], (Date) item.get()[1]); 
					}
				}
			}
		}
		start = System.currentTimeMillis();
		List<Future<Object[]>> listNew = executorNew.invokeAll(allTasksNew);
		System.out.println("new : " + (System.currentTimeMillis() - start) + "ms");
		HashMap<String, Date> mapNew = new HashMap<String, Date>();
		for (Future<Object[]> item : listNew)
		{
			if (item != null)
			{
				if (item.get() != null)
				{
					if (item.get().length == 2)
					{
						mapNew.put((String) item.get()[0], (Date) item.get()[1]); 
					}
				}
			}
		}
		assertEquals(mapOld, mapNew);
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#formatUtcTimestamp(java.util.Date)}.
	 */
	@Test(enabled = false)
	public void testFormatUtcTimestampDate()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.silvertunnel_ng.netlib.layer.tor.util.Util#formatUtcTimestamp(java.lang.Long)}.
	 */
	@Test(enabled = false)
	public void testFormatUtcTimestampLong()
	{
		fail("Not yet implemented");
	}
	
}
