package com.codeworks.pai.processor;

import android.os.AsyncTask;

import com.codeworks.pai.db.model.Option;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Glenn Verner on 12/6/14.
 */
public abstract class DownloadOptionTask extends AsyncTask<Option, Integer, List<Option>> {

    DataReader dataReader = new DataReaderYahoo();
    String TAG = DownloadOptionTask.class.getSimpleName();

    /**
     * Read list of available option dates returning the front month and second monthly option dates.
     *
     * @param today
     * @param symbol
     * @return
     */
    List<DateTime> lookupMonthlyOptionDates(DateTime today, String symbol) {
        List<DateTime> monthlyOptions = new ArrayList<DateTime>();
        // get list of third Saturdays of months
        DateTime[] thirdSaturday = InZoneDateUtils.calcFrontAndSecondMonth(new DateTime(DateTimeZone.getDefault()));
        List<DateTime> optionDates = dataReader.readOptionDates(symbol);
        // find the option date that is before or equal the the third saturday.
        for (DateTime optionDate : optionDates) {
            Duration frontDuration = new Duration(optionDate, thirdSaturday[0]);
            Duration secondDuration = new Duration(optionDate, thirdSaturday[1]);
            // see if date is equal or 4 days before third saturday
            if (frontDuration.getStandardDays() >= 0 && frontDuration.getStandardDays() <= 4) {
                monthlyOptions.add(optionDate);
            }
            if (secondDuration.getStandardDays() >= 0 && secondDuration.getStandardDays() <= 4) {
                monthlyOptions.add(optionDate);
            }
        }
        return monthlyOptions;
    }

    protected List<Option> doInBackground(Option... optionsTypeAndStrike) {
        int count = 4;
        List<Option> option = new ArrayList<Option>();
        try {
            List<DateTime> dts = lookupMonthlyOptionDates(new DateTime(DateTimeZone.UTC), optionsTypeAndStrike[0].getSymbol());
            int ndx = 0;
            // loop front and second month
            for (DateTime dt : dts) {
                // loop option type and strike
                for (int typeStrike = 0; typeStrike < optionsTypeAndStrike.length; typeStrike++) {
                    optionsTypeAndStrike[typeStrike].setExpires(dt);
                    Option optionClone = (Option)optionsTypeAndStrike[typeStrike].clone();
                    option.add(dataReader.readOption(optionClone));
                    ndx++;
                    publishProgress((int) ((ndx / (float) count) * 100));
                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
            }
        } catch (CloneNotSupportedException e) {
            Log.i(TAG,e.getMessage());
        }
        return option;
    }
    @Override
    protected abstract void onPostExecute(List<Option> result);

}

