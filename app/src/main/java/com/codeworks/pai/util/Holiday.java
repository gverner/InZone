package com.codeworks.pai.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by glennverner on 1/6/18.
 */

public class Holiday {
    static List<DateTime> holiday = new ArrayList<>();
    {
        // New Year's Day	Sunday, January 1, 2017 (Observed Monday, January 2)
        holiday.add(new DateTime(2017, 1, 2,0,0));
        // Martin Luther King Jr. Day	Monday, January 16, 2017
        holiday.add(new DateTime(2017, 1, 16,0,0));
        // President's Day	Monday, February 20, 2017
        holiday.add(new DateTime(2017, 2, 20,0,0));
        // Good Friday	Friday, April 14, 2017
        holiday.add(new DateTime(2017, 4, 14,0,0));
        // Memorial Day	Monday, May 29, 2017
        holiday.add(new DateTime(2017, 5, 29,0,0));
        // Independence Day	Tuesday, July 4, 2017
        holiday.add(new DateTime(2017, 7, 4, 0, 0));
        // Labor Day	Monday, September 4, 2017
        holiday.add(new DateTime(2017, 9, 4, 0, 0));
        // Thanksgiving Day	Thursday, November 23, 2017
        holiday.add(new DateTime(2017, 11, 23, 0, 0));
        // Christmas	Monday, December 25, 2017
        holiday.add(new DateTime(2017, 7, 25, 0, 0));
        // New Year's Day	Monday, January 1, 2018
        holiday.add(new DateTime(2018, 1, 1, 0, 0));
        // Martin Luther King Jr. Day	Monday, January 15, 2018
        holiday.add(new DateTime(2018, 1, 15, 0, 0));
        // President's Day	Monday, February 19, 2018
        holiday.add(new DateTime(2018, 2, 19, 0, 0));
        // Good Friday	Friday, March 30, 2018
        holiday.add(new DateTime(2018, 3, 30, 0, 0));
        // Memorial Day	Monday, May 28, 2018
        holiday.add(new DateTime(2018, 5, 28, 0, 0));
        // Independence Day	Wednesday, July 4, 2018 (1)
        holiday.add(new DateTime(2018, 7, 4, 0, 0));
        // Labor Day	Monday, September 3, 2018
        holiday.add(new DateTime(2018, 9, 3, 0, 0));
        // Thanksgiving Day	Thursday, November 22, 2018 (2)
        holiday.add(new DateTime(2018, 11, 22, 0, 0));
        // Christmas	Tuesday, December 25, 2018 (3)
        holiday.add(new DateTime(2018, 12, 25, 0, 0));
    }


    public static boolean isHoliday(DateTime theDate) {
        return holiday.contains(theDate.withTimeAtStartOfDay());
    }

    public static boolean isHoliday(Date theDate) {
        return holiday.contains(new DateTime(theDate.getTime()).withTimeAtStartOfDay());
    }

    public static boolean isHolidayOrWeekend(DateTime theDate) {
        DateTime jodaDate = theDate.withTimeAtStartOfDay();
        boolean result = holiday.contains(jodaDate);

        result = result || (jodaDate.dayOfWeek().get() == DateTimeConstants.SATURDAY || jodaDate.dayOfWeek().get() == DateTimeConstants.SUNDAY);
        return result;
    }

    public static boolean isHolidayOrWeekend(Date theDate) {
        DateTime jodaDate = new DateTime(theDate.getTime()).withTimeAtStartOfDay();
        return isHolidayOrWeekend(jodaDate);
    }

}
