package com.codeworks.pai.processor;

import android.util.Log;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.util.Holiday;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.codeworks.pai.processor.DataReaderYahoo.UTC_FULL_RUN_TIME;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by glennverner on 12/12/17.
 */
@RunWith(AndroidJUnit4.class)
public class HistoryYahooTest {
    static String TAG = "HistoryTest";
    DataReaderYahoo reader;

    @Before
    public void setUp() throws Exception {
        reader = new DataReaderYahoo();
    }
    DateTimeFormatter dtf = DateTimeFormat.forPattern("yy-MM-dd HH:mm:ss EEE");

    @Test
    public void testDownloadYahooHistory() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        int years = 1;
        List<Price> history = reader.readHistoryYahooJson("XOP", years , errors, info);
    }

    @Test
    public void testCombine1and5year() {
        List<String> errors = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();

        List<Price> history5 = reader.readHistoryMSNJson("SPY", 5 , errors, info);
        Map<Date, Price> priceMap = new HashMap<>();
        for(Price price : history5) {
            priceMap.put(price.getDate(), price);
        }

        List<Price> history1 = reader.readHistoryMSNJson("SPY", 1 , errors, info);
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
        List<Price> history = reader.readHistoryMSNJson("SPY", 5, errors, info);
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
        List<Price> history5 = reader.readHistoryMSNJson("EFA", 5 , errors, info);
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
        List<Price> history1 = reader.readHistoryMSNJson("SPY", 1 , errors, info);
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
