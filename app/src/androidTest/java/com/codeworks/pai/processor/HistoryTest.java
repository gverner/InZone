package com.codeworks.pai.processor;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.util.Holiday;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codeworks.pai.processor.DataReaderYahoo.IS_STITCHED;
import static com.codeworks.pai.processor.DataReaderYahoo.UTC_FULL_RUN_TIME;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by glennverner on 12/12/17.
 */
@RunWith(AndroidJUnit4.class)
public class HistoryTest {
    static String TAG = "HistoryTest";
    DataReaderYahoo reader;

    @Before
    public void setUp() throws Exception {
        reader = new DataReaderYahoo();
    }
    DateTimeFormatter dtf = DateTimeFormat.forPattern("yy-MM-dd HH:mm:ss EEE");

    @Test
    public void testDownloadMSNHistory() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        int years = 1;
        List<Price> history = reader.readHistoryJson("XOP", years , errors, info);
        /*
         * each record contains the number of minutes from the previous record to itself.
         * First record contains a value that I have not been able to figure out.
         * Last record is same are others but may contain some additional time that can be removed by using last - (last % 1440)
         */
        int first = new Long(Math.round(history.get(0).getAdjustedClose())).intValue();
        int last = new Long(Math.round(history.get(history.size() - 1).getAdjustedClose())).intValue();
        long utcFullRunTime2 = (Long) info.get("utcFullRunTime");
        Log.d(TAG, "utcFullRunTime " + new DateTime(utcFullRunTime2).toString(dtf) + " first=" + first);

        /* subtracting a year from now didn't work
        DateTime begin3 = new DateTime().withTimeAtStartOfDay().minusYears(years);
        Log.d(TAG, "Today - " + years + " year StartofDay " + begin3);
        Log.d(TAG, "Today - " + years + " year " + new DateTime().minusYears(years).toString(dtf));
        Log.d(TAG, "Today - " + years + " year - first " + new DateTime().minusYears(years).minus(first).toString(dtf));
        Log.d(TAG, "Today - " + years + " year - first SOD " + new DateTime().minusYears(years).minus(first).withTimeAtStartOfDay().toString(dtf));
        */
        // using utcFullRunTime which seems to be the same data fetch time.
        /* subtracting years from utcFullRuntime didn't work
        Log.d(TAG, "utcFullRunTime - " + years + " years " + new DateTime(utcFullRunTime2).minusYears(years).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + years + " years - first " + new DateTime(utcFullRunTime2).minusYears(years).minus(first).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + years + " years - first + Last " + new DateTime(utcFullRunTime2).minusYears(years).minus(first).plusMinutes((int) last).toString(dtf));
        DateTime beginstart = new DateTime().withTimeAtStartOfDay().minusYears(1).plus(last);
        DateTime begintime = new DateTime().minusYears(1).plus(last);
        */

        Log.d(TAG, "IsStitched=" + info.get("IsStitched"));
        int priorToLast = new Long(Math.round(history.get(history.size() -2).getAdjustedClose())).intValue();
        int lastLessPrior = last- priorToLast;
        Log.d(TAG, "utcFullRunTime " + new DateTime(utcFullRunTime2).toString(dtf) + " first=" + first + " Last="+last+" priorToLast="+ priorToLast +" lastLessPrior="+lastLessPrior);
        Log.d(TAG, "utcFullRunTime - " + last + " last " + new DateTime(utcFullRunTime2).minusMinutes(last).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + last + " last - (last % 1440) " + new DateTime(utcFullRunTime2).minusMinutes(last - (last % 1440)).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + lastLessPrior + " last - lastLessPrior " + new DateTime(utcFullRunTime2).minusMinutes(lastLessPrior).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + lastLessPrior + " last - (last % 1440) - lastLessPrior " + new DateTime(utcFullRunTime2).minusMinutes(last - (last % 1440) - lastLessPrior).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + first + " first as minutes " + new DateTime(utcFullRunTime2).minusMinutes(first).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + first + " first as seconds " + new DateTime(utcFullRunTime2).minusSeconds(first).toString(dtf));
        Log.d(TAG, "utcFullRunTime - " + first + " first as ms " + new DateTime(utcFullRunTime2).minusMillis(first).toString(dtf));
        long totalMinutes = reader.sumTotalMinutes(history);
        Log.d(TAG, "utcFullRunTime - totalMinues " + totalMinutes + " " + new DateTime(utcFullRunTime2).minusMinutes(new Long(totalMinutes).intValue()).plus(first).toString(dtf));
        reader.calcDatesBySubtractMinutes(info, history);
        for(Price price : history) {
            Log.d(TAG, price.toString());
        }
        if (1==2) {
            Log.d(TAG, "1==2");
        }
    }

    @Test
    public void testCombine1and5year() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();

        List<Price> history5 = reader.readHistoryJson("SPY", 5 , errors, info);
        Map<Date, Price> priceMap = new HashMap<>();
        for(Price price : history5) {
            priceMap.put(price.getDate(), price);
        }

        List<Price> history1 = reader.readHistoryJson("SPY", 1 , errors, info);
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

    @Test
    public void testConvertDateOffsets1Y() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Monday 12/18/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 1, errors, info, "mon");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);

        reader.adjDatesDaily(history);

        DateTime lastFriday = new DateTime(2017,12,15,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2016,12,16,0,0);
        assertEquals(253, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    @Test
    public void testConvertDateOffsets5Y() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Monday 12/18/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 5, errors, info, "mon");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        reader.adjDatesDaily(history);
        DateTime lastFriday = new DateTime(2017,12,15,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2012,12,21,0,0);
        assertEquals(265, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    @Test
    public void testConvertDateOffsets1YFriday() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Friday 12/15/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 1, errors, info, "fri");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        reader.adjDatesDaily(history);
        DateTime lastFriday = new DateTime(2017,12,8,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2016,12,9,0,0);
        assertEquals(253, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    @Test
    public void testConvertDateOffsets5YFriday() {
        TestDataLoader loader = new TestDataLoader();
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        // Date imported Friday 12/15/2017
        List<Price> history = loader.getJsonHistory(TestDataLoader.J_XOP, 5, errors, info, "fri");
        long utcFullRunTime = (Long) info.get("utcFullRunTime");
        DateTime startDate = new DateTime(utcFullRunTime);
        reader.calcDatesBySubtractMinutes(info, history);
        reader.adjDatesDaily(history);
        DateTime lastFriday = new DateTime(2017,12,8,0,0).withTimeAtStartOfDay();
        DateTime firstFriday = new DateTime(2012,12,14,0,0);
        assertEquals(265, history.size());
        assertEquals(lastFriday, new DateTime(history.get(history.size() -2).getDate().getTime()));
        assertEquals(firstFriday, new DateTime(history.get(0).getDate().getTime()));
    }

    @Test
    public void testHistoryDatesAreNotHoliday() {
        List<String> errors = new ArrayList<>();
        List<Price> history = reader.readHistory("SPY", errors);
        assertNotNull(history);
        for (Price price : history) {
            if (Holiday.isHolidayOrWeekend(price.getDate())) {
                Log.d(TAG, price.toString());
            }
            assertFalse(price.toString(), Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertTrue(history.size() > 1);
    }
    @Test
    public void testHistoryDatesAreNotHoliday1Year() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        List<Price> history = reader.readHistoryJson("SPY", 5, errors, info);
        reader.calcDatesBySubtractMinutes(info, history);
        reader.adjDatesWeekly(history);
        assertNotNull(history);
        for (Price price : history) {
            if (Holiday.isHolidayOrWeekend(price.getDate())) {
                Log.d(TAG, price.toString());
            }
            assertFalse(price.toString(), Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertTrue(history.size() > 1);
    }
    @Test
    public void testHistoryGroup() {
        List<String> errors = new ArrayList<>();
        List<Price> history = reader.readHistory("EFA", errors);
        assertNotNull(history);
        Grouper grouper = new Grouper();
        List<Price> weekly = grouper.periodList(history, Period.Week);

        for (Price price : history) {
            assertFalse(price.toString(), Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        /*
        for (Price price : weekly) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }*/
        assertTrue(history.size() > 1);
    }

    @Test
    public void testDateWeeklyAdjust() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        List<Price> history5 = reader.readHistoryJson("EFA", 5 , errors, info);
        long utcFullRunTime = (Long) info.get(UTC_FULL_RUN_TIME);
        DateTime startDate = new DateTime(utcFullRunTime);
        // change start date by one.
        info.put(UTC_FULL_RUN_TIME, utcFullRunTime + (1000*60*60*24*1));
        // (NOTE THIS FAILED TEST) force skip of holiday check
        // info.put(IS_STITCHED, null);
        reader.calcDatesBySubtractMinutes(info, history5);

        Log.d(TAG, "Before");
        int[] days = reader.dayOfWeekCounts(history5);
        for (int x = 1; x < 8; x++) {
            Log.d(TAG, "Day "+x+" cnt="+days[x]);
        }
        reader.adjDatesWeekly(history5);
        Log.d(TAG, "After");
        days = reader.dayOfWeekCounts(history5);
        for (int x = 1; x < 8; x++) {
            Log.d(TAG, "Day "+x+" cnt="+days[x]);
        }
        for (Price price : history5) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertEquals(0, days[6]);
        assertEquals(0, days[7]);
    }

    @Test
    public void testDateDailyAdjust() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        List<Price> history1 = reader.readHistoryJson("SPY", 1 , errors, info);
        long utcFullRunTime = (Long) info.get(UTC_FULL_RUN_TIME);
        DateTime startDate = new DateTime(utcFullRunTime);
        // change start date by one.
        info.put(UTC_FULL_RUN_TIME, utcFullRunTime + (1000*60*60*24*1));

        // force skip of holiday test
        //info.put(IS_STITCHED, null); this doesn't always work broke
        reader.calcDatesBySubtractMinutes(info, history1);
        for (Price price : history1) {
            Log.d(TAG, price.toString());
        }

        Log.d(TAG, "Before");
        int[] days = reader.dayOfWeekCounts(history1);
        for (int x = 1; x < 8; x++) {
            Log.d(TAG, "Day "+x+" cnt="+days[x]);
        }

        reader.adjDatesDaily(history1);

        Log.d(TAG, "After");
        days = reader.dayOfWeekCounts(history1);
        for (int x = 1; x < 8; x++) {
            Log.d(TAG, "Day "+x+" cnt="+days[x]);
        }

        for (Price price : history1) {
            assertFalse("Date="+price.getDate() + " day"+new DateTime(price.getDate()).getDayOfWeek(), Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertEquals(0, days[6]);
        assertEquals(0, days[7]);

    }

    @Test
    public void testReadOldYahoo() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        List<Price> history1 = reader.readHistoryOldYahooAndGoogle("EFA", errors);
        for (Price price : history1) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
    }

}
