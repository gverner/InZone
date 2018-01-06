package com.codeworks.pai.processor;

import android.test.AndroidTestCase;
import android.util.Log;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.util.Holiday;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by glennverner on 12/12/17.
 */

public class HistoryTest extends AndroidTestCase {
    static String TAG = "HistoryTest";
    DataReaderYahoo reader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reader = new DataReaderYahoo();
    }
    DateTimeFormatter dtf = DateTimeFormat.forPattern("yy-MM-dd HH:mm:ss EEE");

    public void testDownloadMSNHistory() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        int years = 1;
        List<Price> history = reader.readHistoryJson("XOP", years , errors, info);
        long first = Math.round(history.get(0).getAdjustedClose());
        DateTime begin3 = new DateTime().withTimeAtStartOfDay().minusYears(years);
        Log.d(TAG, "Today - " + years + " year StartofDay " + begin3);
        Log.d(TAG, "Today - " + years + " year " + new DateTime().minusYears(years).toString(dtf));
        Log.d(TAG, "Today - " + years + " year - first " + new DateTime().minusYears(years).minus(first).toString(dtf));
        Log.d(TAG, "Today - " + years + " year - first SOD " + new DateTime().minusYears(years).minus(first).withTimeAtStartOfDay().toString(dtf));
        long utcFullRunTime2 = (Long) info.get("utcFullRunTime");
        Log.d(TAG, "utcFullRunTime " + new DateTime(utcFullRunTime2).toString(dtf) + " first=" + first);
        Log.d(TAG, "utcFullRunTime - " + years + " years " + new DateTime(utcFullRunTime2).minusYears(years).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + years + " years - first " + new DateTime(utcFullRunTime2).minusYears(years).minus(first).toString(dtf));
        long last = Math.round(history.get(history.size() - 1).getAdjustedClose());
        Log.d(TAG, "utcFullRunTime - " + years + " years - first + Last " + new DateTime(utcFullRunTime2).minusYears(years).minus(first).plusMinutes((int) last).toString(dtf));
        DateTime beginstart = new DateTime().withTimeAtStartOfDay().minusYears(1).plus(last);
        DateTime begintime = new DateTime().minusYears(1).plus(last);
        Log.d(TAG, "IsStiched=" + info.get("IsStitched") + " begin startofday=" + beginstart + " begintime+" + begintime);
        long lastPeriodEnd = Math.round(history.get(history.size() -2).getAdjustedClose());
        long diff = last-lastPeriodEnd;
        Log.d(TAG, "utcFullRunTime " + new DateTime(utcFullRunTime2).toString(dtf) + " first=" + first + " Last="+last+" lastPeriodEnd="+lastPeriodEnd+" diff="+diff);
        Log.d(TAG, "utcFullRunTime - " + years + "year " + new DateTime(utcFullRunTime2).minusYears(years).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + years + "year - diff " + new DateTime(utcFullRunTime2).minusYears(years).minusMinutes((int)diff).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + years + "year - first " + new DateTime(utcFullRunTime2).minusYears(years).minusMinutes((int)diff).minus(first).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + first + " first " + new DateTime(utcFullRunTime2).minusSeconds((int) first).toString(dtf));
        long totalMinutes = reader.calcTotalSeconds(history);
        Log.d(TAG, "utcFullRunTime - totalMinues " + totalMinutes + " " + new DateTime(utcFullRunTime2).minusMinutes(new Long(totalMinutes).intValue()).plus(first).toString(dtf));
        reader.calcDatesBySubtractMinutes(info, history);
        for(Price price : history) {
            Log.d(TAG, price.toString());
        }
        if (1==2) {
            Log.d(TAG, "1==2");
        }
    }

    public void testCombine1and5year() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();

        List<Price> history5 = reader.readHistoryJson("AMZN", 5 , errors, info);
        Map<Date, Price> priceMap = new HashMap<>();
        for(Price price : history5) {
            priceMap.put(price.getDate(), price);
        }

        List<Price> history1 = reader.readHistoryJson("AMZN", 1 , errors, info);
        // this should overwrite matching dates in 5 year history
        for(Price price : history1) {
            priceMap.put(price.getDate(), price);
        }
        List<Price> history = Arrays.asList(priceMap.values().toArray(new Price[priceMap.size()]));
        Collections.sort(history);
        for(Price price : history) {
            Log.d(TAG, price.toString());
        }
    }

    public void testConvertDateOffsets1Y() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Monday 12/18/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 1, errors, info, "mon");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        DateTime lastFriday = new DateTime(2017,12,15,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2016,12,16,0,0);
        assertEquals(253, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    public void testConvertDateOffsets5Y() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Monday 12/18/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 5, errors, info, "mon");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        DateTime lastFriday = new DateTime(2017,12,15,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2012,12,21,0,0);
        assertEquals(265, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    public void testConvertDateOffsets1YFriday() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Friday 12/15/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 1, errors, info, "fri");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        DateTime lastFriday = new DateTime(2017,12,8,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2016,12,9,0,0);
        assertEquals(253, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    public void testConvertDateOffsets5YFriday() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Friday 12/15/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 5, errors, info, "fri");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        DateTime lastFriday = new DateTime(2017,12,8,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2012,12,14,0,0);
        assertEquals(265, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    public void testHistoryDatesAreNotHoliday() {
        List<String> errors = new ArrayList<>();
        List<Price> history = reader.readHistory("XOP", errors);
        assertNotNull(history);
        for (Price price : history) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertTrue(history.size() > 1);
    }

    public void testHistoryGroup() {
        List<String> errors = new ArrayList<>();
        List<Price> history = reader.readHistory("XOP", errors);
        assertNotNull(history);
        Grouper grouper = new Grouper();
        List<Price> weekly = grouper.periodList(history, Period.Week);

        for (Price price : weekly) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertTrue(history.size() > 1);
    }
}
