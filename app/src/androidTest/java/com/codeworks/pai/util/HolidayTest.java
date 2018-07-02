package com.codeworks.pai.util;

import android.support.test.runner.AndroidJUnit4;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by glennverner on 3/31/18.
 */
@RunWith(AndroidJUnit4.class)
public class HolidayTest {

    @Test
    public void testGoodFridayHoliday() {
        assertFalse(Holiday.isHolidayOrWeekend(DateTime.parse("20180329", ISODateTimeFormat.basicDate())));
        assertTrue(Holiday.isHolidayOrWeekend(DateTime.parse("20180330", ISODateTimeFormat.basicDate())));
        assertTrue(Holiday.isHolidayOrWeekend(DateTime.parse("20180331", ISODateTimeFormat.basicDate())));
    }

    @Test
    public void testMemorialHoliday() {
        assertTrue(Holiday.isHolidayOrWeekend(DateTime.parse("20180527", ISODateTimeFormat.basicDate())));
        assertTrue(Holiday.isHolidayOrWeekend(DateTime.parse("20180528", ISODateTimeFormat.basicDate())));
        assertTrue(Holiday.isHolidayOrWeekend(DateTime.parse("20180528", ISODateTimeFormat.basicDate())));
    }

}

