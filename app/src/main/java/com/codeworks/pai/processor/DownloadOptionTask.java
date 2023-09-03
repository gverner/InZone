package com.codeworks.pai.processor;

import android.os.AsyncTask;
import android.util.Log;

import com.codeworks.pai.db.model.Option;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.ArrayList;
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
     * @param symbol
     * @return
     */
    List<DateTime> lookupMonthlyOptionDates(String symbol, List<String> errors) {
        List<DateTime> monthlyOptions = new ArrayList<DateTime>();
        // get list of third Saturdays of months
        DateTime[] thirdSaturday = InZoneDateUtils.calcFrontAndSecondMonth(new DateTime(DateTimeZone.getDefault()));
        List<DateTime> optionDates = dataReader.readOptionExpirations(symbol, errors);
        // byProtfolioId the option date that is before or equal the the third saturday.
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
        List<String> errors = new ArrayList<String>();
        try {
            List<DateTime> dts = lookupMonthlyOptionDates(optionsTypeAndStrike[0].getSymbol(), errors);
            int ndx = 0;
            // loop front and second month
            for (DateTime dt : dts) {
                // optionDate seconds is in local timezone add timezoneOffset ot get UTCs
                // use option date because this date is in the future and may have different daylight savings offset then today.
                final long timezoneOffset = Math.abs(DateTimeZone.getDefault().getOffset(dt));
                long seconds = (dt.getMillis() - timezoneOffset) / 1000;

                // loop option type and strike
                List<Option> optionPrices = dataReader.readOptionPrice(optionsTypeAndStrike[0].getSymbol(), seconds, errors);
                for (int typeStrike = 0; typeStrike < optionsTypeAndStrike.length; typeStrike++) {
                    for (Option optionPrice : optionPrices) {
                        if (optionsTypeAndStrike[typeStrike].getStrike() == optionPrice.getStrike()) {
                            if (optionsTypeAndStrike[typeStrike].getType().equals(optionPrice.getType())) {
                                option.add(optionPrice);
                            }
                        }
                    }
                    ndx++;
                    publishProgress((int) ((ndx / (float) count) * 100));
                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
            }
            // error if option list is empty and error exists
            if (option.size() == 0 && errors.size() > 0) {
                Option optionClone = (Option)optionsTypeAndStrike[0].clone();
                optionClone.setError(errors.get(0));
                option.add(optionClone);
            }
        } catch (CloneNotSupportedException e) {
            Log.i(TAG,e.getMessage());
        }
        return option;
    }
    @Override
    protected abstract void onPostExecute(List<Option> result);

}

