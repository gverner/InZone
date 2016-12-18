package com.codeworks.pai.processor;

import android.os.AsyncTask;
import android.test.AndroidTestCase;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Study;

import org.joda.time.DateTime;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Glenn Verner on 12/6/14.
 */
public class DownloadOptionTaskTest extends AndroidTestCase {

    public void runDownloadOptionsTask(final String symbol) throws Exception {
        DataReader reader = new DataReaderYahoo();
        Study study = new Study(symbol);

        List<String> errors = new ArrayList<String>();
        reader.readDelayedPrice(study, errors);
        Option call = new Option(symbol, OptionType.C, PaiUtils.round(study.getPrice(),0), DateTime.now());
        Option put = new Option(symbol, OptionType.P, PaiUtils.round(study.getPrice(),0), DateTime.now());

        DownloadOptionTask task = new DownloadOptionTask() {
            @Override
            protected void onPostExecute(List<Option> options) {
                for (Option option : options) {
                    assertNotNull(option);
                    assertEquals(symbol, option.getSymbol());
                    assertTrue(option.getAsk() > 0);
                    assertTrue(option.getBid() > 0);
                    System.out.println(option.getSymbol() + " " + option.getType() + " " + option.getStrike() + " " + option.getBid()+ " "+ option.getExpires());
                }
            }
        };
        task.execute(call, put);

        int count = 10;
        while (!task.getStatus().equals(AsyncTask.Status.FINISHED) && count > 0) {
            Thread.sleep(10000);
            System.out.println("Running "+count);
            count--;
        }
    }

    public void testDownloadOptionsTaskHyg() throws Exception {
        runDownloadOptionsTask("HYG");
    }

    public void testDownloadOptionsTaskSpy() throws Exception {
        runDownloadOptionsTask("SPY");
    }
}
