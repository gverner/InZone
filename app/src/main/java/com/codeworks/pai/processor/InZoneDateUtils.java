package com.codeworks.pai.processor;

import com.codeworks.pai.study.Period;
import com.codeworks.pai.util.Holiday;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class InZoneDateUtils {

	public static final int			MARKET_CLOSE_HOUR	= 16;
	public static final int			MARKET_OPEN_HOUR	= 9;
	public static final int			MARKET_OPEN_MINUTE	= 30;

	public static SimpleDateFormat	dbStringDateFormat	= new SimpleDateFormat("yyyyMMdd", Locale.US);

	/**
	 * Is the DateTeme equal to or after Period (Week Or Month) close and less
	 * than market open. Date passed in should be date time of a price
	 * 
	 * <li>Week : true if date and time between Friday's close and Monday's
	 * open. <li>Month: true if date and time between last market day of month
	 * and first day of next month.
	 * 
	 * <li>NOTE: doesn't know holidays so fails to calc week period end on early 1pm close like Thanks Giving Friday.
	 * 
	 * @param priceDate
	 * @param period
	 * @return
	 */
	public static boolean isMarketClosedForThisDateTimeAndPeriod(Date priceDate, Period period) {
		boolean result = false;
		if (Period.Week.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

			cal.setTime(priceDate);

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
//			System.out.println(sdf.format(cal.getTime()));
//			System.out.println(sdf.format(priceDate));
			DateTime dateTime = new DateTime(cal.getTime());
			int lastTradeDayOfWeek = lastTradeDayOfWeek(dateTime);
			int firstTradeDayOfSeek = firstTradeDayOfWeek(dateTime);
			if ((cal.get(Calendar.DAY_OF_WEEK) == lastTradeDayOfWeek && cal.get(Calendar.HOUR_OF_DAY) >= MARKET_CLOSE_HOUR)
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					|| (cal.get(Calendar.DAY_OF_WEEK) == firstTradeDayOfSeek && cal.get(Calendar.HOUR_OF_DAY) == MARKET_OPEN_HOUR && cal.get(Calendar.MINUTE) < MARKET_OPEN_MINUTE)) {
				result = true;
			} else {
				result = false;
			}
		} else if (Period.Month.equals(period)) {
			Calendar monthEnd = getMonthClose(priceDate);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
//			System.out.println(sdf.format(monthEnd.getTime()));
//			System.out.println(sdf.format(priceDate.getTime()));

			result = (priceDate.compareTo(monthEnd.getTime()) >= 0);
		}
		return result;
	}

	/**
	 * Returns Last Trade Day They week, as Calendar DayOfWeek, not the same as Joda DayOfWeek
	 * @param dateTime
	 * @return usually Friday (6)
	 */
	public static int lastTradeDayOfWeek(DateTime dateTime) {
		DateTime endOfWeek = dateTime.withDayOfWeek(DateTimeConstants.FRIDAY);
		while (Holiday.isHolidayOrWeekend(endOfWeek)) {
			endOfWeek = endOfWeek.minusDays(1);
		}
		if (endOfWeek.getDayOfWeek() == 7) {
			return 1;
		} else {
			return endOfWeek.getDayOfWeek() + 1; // offset -1 for to match Calendar.DAY_OF_WEEK
		}
	}

	/**
	 * Returns Last Trade Day They week, as Calendar DayOfWeek, not the same as Joda DayOfWeek
	 * @param dateTime
	 * @return usually Monday (2)
	 */
	public static int firstTradeDayOfWeek(DateTime dateTime) {
		DateTime endOfWeek = dateTime.withDayOfWeek(DateTimeConstants.MONDAY);
		while (Holiday.isHolidayOrWeekend(endOfWeek)) {
			endOfWeek = endOfWeek.minusDays(1);
		}
		if (endOfWeek.getDayOfWeek() == 7) {
			return 1;
		} else {
			return endOfWeek.getDayOfWeek() + 1; // offset -1 for to match Calendar.DAY_OF_WEEK
		}
	}

	/*
	 * Note: Uses Holiday Class, need to keep upto date
	 */
	static Calendar getMonthClose(Date date) {
		Calendar monthEnd = GregorianCalendar.getInstance(Locale.US);
		monthEnd.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		monthEnd.setTime(date);

		monthEnd.add(Calendar.MONTH, 1);
		monthEnd.set(Calendar.DAY_OF_MONTH, 1);
		do {
			monthEnd.add(Calendar.DAY_OF_MONTH, -1);
//		} while (monthEnd.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || monthEnd.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
		} while (Holiday.isHolidayOrWeekend(monthEnd.getTime()));

		monthEnd.set(Calendar.HOUR_OF_DAY, MARKET_CLOSE_HOUR);
		monthEnd.set(Calendar.MINUTE, 0);
		monthEnd.set(Calendar.SECOND, 0);
		monthEnd.set(Calendar.MILLISECOND, 0);
		return monthEnd;
	}

	/*
	 * Note: Doesn't Know marked Holidays
	 */
	static Calendar getNextMonthOpen(Date date) {
		Calendar periodStart = GregorianCalendar.getInstance(Locale.US);
		periodStart.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		periodStart.setTime(date);
		periodStart.add(Calendar.MONTH, 1);
		periodStart.set(Calendar.DAY_OF_MONTH, 1);
		periodStart.set(Calendar.HOUR_OF_DAY, MARKET_OPEN_HOUR);
		periodStart.set(Calendar.MINUTE, MARKET_OPEN_MINUTE);
		periodStart.set(Calendar.SECOND, 0);
		periodStart.set(Calendar.MILLISECOND, 0);
		return periodStart;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if two date objects are on the same day ignoring time.
	 * </p>
	 * 
	 * <p>
	 * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true. 28 Mar 2002
	 * 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param date1
	 *            the first date, not altered, not null
	 * @param date2
	 *            the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either date is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			//throw new IllegalArgumentException("The date must not be null");
			return false;
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if two calendar objects are on the same day ignoring time.
	 * </p>
	 * 
	 * <p>
	 * 28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true. 28 Mar 2002
	 * 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param cal1
	 *            the first calendar, not altered, not null
	 * @param cal2
	 *            the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException
	 *             if either calendar is <code>null</code>
	 * @since 2.1
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2
				.get(Calendar.DAY_OF_YEAR));
	}

	public static Date truncate(Date date) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		return cal1.getTime();
	}

	/**
	 * last Probable Trade date because we don't have a holiday table.
	 * 
	 * @return
	 */
	public static String lastProbableTradeDate() {
		Calendar cal = GregorianCalendar.getInstance();

		while (Holiday.isHolidayOrWeekend(cal.getTime())) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		return dbStringDateFormat.format(cal.getTime());
	}

	public static String toDatabaseFormat(Date date) {
		if (date != null) {
			return dbStringDateFormat.format(date);
		} else {
			return "19700101";
		}
	}

	public static Date fromDatabaseFormat(String date) {
		try {
			if (date != null) {
				return dbStringDateFormat.parse(date);
			} else {
				return dbStringDateFormat.parse("19700101");
			}
		} catch (ParseException e) {
			try {
				return dbStringDateFormat.parse("19700101");
			} catch (ParseException e1) {
				// ignore
				return null;
			}
		}
	}

	public static DateTime getCurrentNYTime() {
		DateTime dt = new DateTime();
		// translate to New York local time
		DateTime nyDateTime = dt.withZone(DateTimeZone.forID("America/New_York"));
		return nyDateTime;

	}

    /**
     * Calculate the front month and the second month Monthly Options expiration dates based on the date passed.
     * Returns the Third Saturday of the month which is the latest possible expiration date not necessary the actual expiration date.
     * @param today (usually today)
     * @return front month array 0 second month array 1
     */
    public static DateTime[] calcFrontAndSecondMonth(DateTime today) {
        DateTime[] result = new DateTime[2];
        DateTime frontMonth = today.withTimeAtStartOfDay();
        DateTime currentMonthly = calcMonthlyExpiration(frontMonth);
        // is there a minimum 2 weeks left
        if (frontMonth.plus(org.joda.time.Period.days(14)).isAfter(currentMonthly)) {
            frontMonth = frontMonth.plus(org.joda.time.Period.months(1));
        }
        result[0] = calcMonthlyExpiration(frontMonth);
        result[1] = calcMonthlyExpiration(frontMonth.plus(org.joda.time.Period.months(1)));
        return result;
    }

    /**
     * Returns the third Saturday of the Month.
     * @param month month of year 1 througth 12
     * @return
     */
    public static DateTime calcMonthlyExpiration(DateTime month) {
        DateTime dt = month.withDayOfMonth(1).withTimeAtStartOfDay();
        if (dt.getDayOfWeek() == DateTimeConstants.SATURDAY) {
            dt = dt.plusWeeks(3);
        } else {
            while (dt.getDayOfWeek() != DateTimeConstants.SATURDAY) {
                dt = dt.plusDays(1);
            }
            dt = dt.plusWeeks(2);
        }
        return dt;
    }
}
