package com.codeworks.pai.processor;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.util.Holiday;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by glennverner on 1/6/18.
 */
@RunWith(JUnit4.class)
@MediumTest
public class ProcessorHistoryTest {
    ProcessorImpl processor;
    Context mMockContext;
    DataReaderYahoo reader;

    String TAG = "ProcessorHistoryTest";

    @Before
    public void setUp() throws Exception {
        mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        processor = new ProcessorImpl(null, new MockDataReader(), mMockContext);
        reader = new DataReaderYahoo();
    }

    public double round(double value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    @Test
    public void testGrouperWithMSNData() throws ParseException {
        Study study = new Study("XOP");

        List<String> errors = new ArrayList<>();
        List<Price> history = reader.readHistory("XOP", errors);
        assertNotNull(history);
        int minHistorySize = 460;
        assertTrue(minHistorySize <= history.size());

        Grouper grouper = new Grouper();
        List<Price> weekly = grouper.periodList(history, Period.Week);

        int minWeeklySize = 250;
        assertTrue(minWeeklySize <= weekly.size());
        for (Price price : weekly) {
            assertFalse(Holiday.isHolidayOrWeekend(price.getDate()));
            Log.d(TAG, price.toString());
        }
        assertTrue(history.size() > 1);
        processor.calculateStudy(study, history);

        ProcessorFunctionalTest.logStudy(study);

    }

}
