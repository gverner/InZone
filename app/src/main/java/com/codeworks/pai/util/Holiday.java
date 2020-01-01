package com.codeworks.pai.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by glennverner on 1/6/18.
 */

public class Holiday {

    static List<DateTime> holiday = new ArrayList<>();

    static {


        // New Year's Day	Monday, January 1, 2022
        holiday.add(new DateTime(2019, 1, 1, 0, 0));
        // Martin Luther King Jr. Day	Monday, January 15, 2022
        holiday.add(new DateTime(2019, 1, 21, 0, 0));
        // President's Day	Monday, February 19, 2022
        holiday.add(new DateTime(2019, 2, 18, 0, 0));
        // Good Friday	Friday, March 30, 2022
        holiday.add(new DateTime(2019, 4, 19, 0, 0));
        // Memorial Day	Monday, May 28, 2022
        holiday.add(new DateTime(2019, 5, 27, 0, 0));
        // Independence Day	Wednesday, July 4, 2022 (1)
        holiday.add(new DateTime(2019, 7, 4, 0, 0));
        // Labor Day	Monday, September 3, 2022
        holiday.add(new DateTime(2019, 9, 2, 0, 0));
        // Thanksgiving Day	Thursday, November 22, 2022 (2)
        holiday.add(new DateTime(2019, 11, 28, 0, 0));
        // Christmas	Tuesday, December 25, 2022 (3)
        holiday.add(new DateTime(2019, 12, 25, 0, 0));

        // New Year's Day	Monday, January 1, 2022
        holiday.add(new DateTime(2020, 1, 1, 0, 0));
        // Martin Luther King Jr. Day	Monday, January 15, 2022
        holiday.add(new DateTime(2020, 1, 20, 0, 0));
        // President's Day	Monday, February 19, 2022
        holiday.add(new DateTime(2020, 2, 17, 0, 0));
        // Good Friday	Friday, March 30, 2022
        holiday.add(new DateTime(2020, 4, 10, 0, 0));
        // Memorial Day	Monday, May 28, 2022
        holiday.add(new DateTime(2020, 5, 25, 0, 0));
        // Independence Day	Wednesday, July 4, 2022 (1)
        holiday.add(new DateTime(2020, 7, 3, 0, 0));
        // Labor Day	Monday, September 3, 2022
        holiday.add(new DateTime(2020, 9, 7, 0, 0));
        // Thanksgiving Day	Thursday, November 22, 2022 (2)
        holiday.add(new DateTime(2020, 11, 26, 0, 0));
        // Christmas	Tuesday, December 25, 2022 (3)
        holiday.add(new DateTime(2020, 12, 25, 0, 0));


        // New Year's Day
        holiday.add(new DateTime(2021, 1, 1, 0, 0));
        // Martin Luther King Jr. Day
        holiday.add(new DateTime(2021, 1, 18, 0, 0));
        // President's Day
        holiday.add(new DateTime(2021, 2, 15, 0, 0));
        // Good Friday
        holiday.add(new DateTime(2021, 4, 2, 0, 0));
        // Memorial Day
        holiday.add(new DateTime(2021, 5, 31, 0, 0));
        // Independence Day
        holiday.add(new DateTime(2021, 7, 5, 0, 0));
        // Labor Day
        holiday.add(new DateTime(2021, 9, 6, 0, 0));
        // Thanksgiving Day
        holiday.add(new DateTime(2021, 11, 25, 0, 0));
        // Christmas
        holiday.add(new DateTime(2021, 12, 24, 0, 0));


        // New Year's Day
        holiday.add(new DateTime(2022, 1, 1, 0, 0));
        // Martin Luther King Jr. Day
        holiday.add(new DateTime(2022, 1, 17, 0, 0));
        // President's Day
        holiday.add(new DateTime(2022, 2, 21, 0, 0));
        // Good Friday
        holiday.add(new DateTime(2022, 4, 15, 0, 0));
        // Memorial Day
        holiday.add(new DateTime(2022, 5, 30, 0, 0));
        // Independence Day
        holiday.add(new DateTime(2022, 7, 4, 0, 0));
        // Labor Day
        holiday.add(new DateTime(2022, 9, 5, 0, 0));
        // Thanksgiving Day
        holiday.add(new DateTime(2022, 11, 24, 0, 0));
        // Christmas
        holiday.add(new DateTime(2022, 12, 26, 0, 0));

        Collections.sort(holiday);
    }

    public static class DateTimeComparator implements Comparator<DateTime> {
        public int compare(DateTime d1, DateTime d2) {
            return d1.compareTo(d2);
        }
    }

    public static boolean isHoliday(DateTime theDate) {
        return Collections.binarySearch(holiday, theDate.withTimeAtStartOfDay(), new DateTimeComparator()) > -1;
    }

    public static boolean isHoliday(Date theDate) {
        return isHoliday(new DateTime(theDate.getTime()));
    }

    public static boolean isHolidayOrWeekend(DateTime theDate) {
        DateTime jodaDate = theDate.withTimeAtStartOfDay();
        boolean result = Collections.binarySearch(holiday, jodaDate, new DateTimeComparator()) > -1;

        result = result || (jodaDate.getDayOfWeek() == DateTimeConstants.SATURDAY || jodaDate.getDayOfWeek() == DateTimeConstants.SUNDAY);
        return result;
    }

    public static boolean isHolidayOrWeekend(Date theDate) {
        DateTime jodaDate = new DateTime(theDate.getTime()).withTimeAtStartOfDay();
        return isHolidayOrWeekend(jodaDate);
    }

}
