package com.codeworks.pai.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PaiDatabaseHelper;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.ServiceType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.study.ATR;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.study.SMA;
import com.codeworks.pai.study.StdDev;
import com.codeworks.pai.study.Stochastics;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProcessorImpl implements Processor {
    private static final String TAG = ProcessorImpl.class.getSimpleName();
    DataReader reader = new DataReaderYahoo();
    ContentResolver contentResolver;
    public static SimpleDateFormat dbStringDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    public static SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    Context context;
    PaiDatabaseHelper dbHelper;

    public ProcessorImpl(ContentResolver contentResolver2, DataReader reader, Context context) {
        this.contentResolver = contentResolver2;
        this.reader = reader;
        this.context = context;
        dbHelper = new PaiDatabaseHelper(context);
    }

    public void onClose() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Process all securities.
     * lookup current price and calculates study
     *
     * @param symbol when symbol is null all securities are processed.
     * @return
     * @throws InterruptedException
     */
    public List<Study> process(String symbol, boolean updateHistory) throws InterruptedException {
        List<String> errors = new ArrayList<String>();
        List<Study> studies = getSecurities(symbol);
        updateCurrentPrice(studies);
        String lastOnlineHistoryDbDate = getLastestOnlineHistoryDbDate(symbol == null ? "SPY" : symbol, errors);
        if (errors.size() > 0) {
            recordServiceLogErrorEvent(errors.get(0));
        }
        List<Price> history = new ArrayList<Price>();
        String lastSymbol = "";
        for (Study security : studies) {
            if (security.getPrice() != 0) {
                if (!lastSymbol.equals(security.getSymbol())) { // cache history
                    errors = new ArrayList<String>();
                    history = getPriceHistory(security, lastOnlineHistoryDbDate, errors, updateHistory);
                    if (errors.size() > 0) {
                        security.setNetworkError(true);
                        recordServiceLogErrorEvent(errors.get(0));
                    }
                    Collections.sort(history);
                    history = Collections.unmodifiableList(history);
                } else {
                    Log.d(TAG, "Using History Cache for " + security.getSymbol());
                }
                lastSymbol = security.getSymbol();
                if (history.size() >= 20) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    calculateStudy(security, history); // shallow copy of cashed history because it is modified
//					saveStudy(security);
                    security.setInsufficientHistory(false);
                    security.setNoPrice(false);
                } else {
                    security.setInsufficientHistory(true);
                }
            } else {
                security.setNoPrice(true);
            }
        }
        batchSaveStudy(studies);
        return studies;
    }

    /**
     * update Price all securities.
     * lookup current price only
     *
     * @return
     * @throws InterruptedException
     */
    public List<Study> updatePrice(String symbol) throws InterruptedException {
        List<Study> studies = getSecurities(symbol);
        updateCurrentPrice(studies);
        long start = System.currentTimeMillis();
        batchSaveStudyPrice(studies);
        Log.d(TAG, "Time to update db prices ms " + (System.currentTimeMillis() - start));
        return studies;
    }

    void calculateStudy(Study security, List<Price> daily) {
        // do not modify daily because it is cached.s
        List<Price> history = new ArrayList<Price>(daily);
        Log.d(TAG, "Daily price history start " + security.getSymbol() + " Price Date=" + security.getPriceDate() + " ListHistoryDate=" + daily.get(daily.size() - 1).getDate());

        Grouper grouper = new Grouper();
        {
            appendCurrentPrice(history, security, Period.Day);
            List<Price> weekly = grouper.periodList(history, Period.Week);
            if (weekly.size() >= 20) {
                security.setEmaLastWeek(EMA2.compute(weekly, 20));
                security.setSmaLastWeek(SMA.compute(weekly, 20));
                security.setPriceLastWeek(weekly.get(weekly.size() - 1).getClose());

                appendCurrentPrice(weekly, security, Period.Week);

                security.setEmaWeek(EMA2.compute(weekly, 20));
                security.setEmaStddevWeek(StdDev.calculate(weekly, 20));

                security.setSmaWeek(SMA.compute(weekly, 20));
                security.setSmaStddevWeek(StdDev.calculate(weekly, 20));

                if (InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(security.getPriceDate(), Period.Week)) {
                    security.setEmaLastWeek(security.getEmaWeek());
                    security.setSmaLastWeek(security.getSmaWeek());
                    security.setPriceLastWeek(security.getPrice());
                    Log.i(TAG, "IS AFTER OR EQUAL MARKET CLOSE FOR " + security.getSymbol());
                } else {
                    Log.i(TAG, "IS NOT AFTER OR EQUAL MARKET CLOSE FOR " + security.getSymbol());
                }

            } else {
                Log.w(TAG, "Insufficent Weekly History only " + weekly.size() + " periods.");
                security.setInsufficientHistory(true);
            }
        }
        {
            List<Price> monthly = grouper.periodList(history, Period.Month);
            if (monthly.size() >= 20) {
                security.setEmaLastMonth(EMA2.compute(monthly, 20));
                security.setSmaLastMonth(SMA.compute(monthly, 12));
                security.setPriceLastMonth(monthly.get(monthly.size() - 1).getClose());

                appendCurrentPrice(monthly, security, Period.Month);

                security.setEmaMonth(EMA2.compute(monthly, 20));
                security.setEmaStddevMonth(StdDev.calculate(monthly, 20));

                security.setSmaMonth(SMA.compute(monthly, 12));
                security.setSmaStddevMonth(StdDev.calculate(monthly, 12));

                if (InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(security.getPriceDate(), Period.Month)) {
                    security.setEmaLastMonth(security.getEmaMonth());
                    security.setSmaLastMonth(security.getSmaMonth());
                    security.setPriceLastMonth(security.getPrice());
                }

            } else {
                Log.w(TAG, "Insufficent Monthly History only " + monthly.size() + " periods.");
                security.setInsufficientHistory(true);
            }
        }
        {
            if (daily.size() > 40) {
                // updateLastClose(security, daily);
                // appendCurrentPrice(daily,security);
                security.setAverageTrueRange(ATR.compute(daily, 20));
                Stochastics stoch = new Stochastics();
                stoch.calculateSlow(daily, 9, 3, 3);
                security.setStochasticK(stoch.getK());
                security.setStochasticD(stoch.getD());
            }
        }
    }


    public void appendCurrentPrice(List<Price> weekly, Study security, Period period) {
        if (weekly != null && weekly.size() > 0) {
            Price lastHistory = weekly.get(weekly.size() - 1);
            /*
            if (InZoneDateUtils.isSameDay(security.getPriceDate(), lastHistory.getDate())) {
				if (security.getPrice() != lastHistory.getClose()) {
					lastHistory.setClose(security.getPrice());
					lastHistory.setOpen(security.getOpen());
					lastHistory.setLow(security.getLow());
					lastHistory.setHigh(security.getHigh());
					Log.d(TAG, "History and Price Close Differ Should not Happen=" + lastHistory.getDate() + " History Close" + lastHistory.getClose()+ " Current Price" + security.getPrice());
				}
			} else*/
            Date truncateDate = InZoneDateUtils.truncate(security.getPriceDate());
            if (truncateDate.after(lastHistory.getDate())) {
                Price lastPrice = new Price();
                lastPrice.setClose(security.getPrice()); // current price is close in history
                lastPrice.setDate(InZoneDateUtils.truncate(security.getPriceDate()));
                lastPrice.setOpen(security.getOpen());
                lastPrice.setLow(security.getLow());
                lastPrice.setHigh(security.getHigh());
                weekly.add(lastPrice);
                Log.d(TAG, "Last History " + period.name() + " Date=" + lastHistory.getDate() + " Add Current Price Date " + truncateDate);
            } else {
                Log.d(TAG, "Last History " + period.name() + " Date=" + lastHistory.getDate() + " Don't Add Current Price Date " + truncateDate);
            }
        }
    }

    private void updateCurrentPrice(List<Study> securities) throws InterruptedException {
        boolean extendedMarket = false;
        List<String> errors = new ArrayList<String>();
        Map<String, Study> cacheQuotes = new HashMap<String, Study>();
        for (Study quote : securities) {
            Log.d(TAG, quote.getSymbol());
            quote.setDelayedPrice(false);
            String oldName = quote.getName();

            Study cachedQuote = cacheQuotes.get(quote.getSymbol());
            if (cachedQuote == null) {
                readValidatePrice(cacheQuotes, quote, errors);
            } else { // from cache
                Log.d(TAG, "Using cached quote " + quote.getSymbol());
                quote.setPrice(cachedQuote.getPrice());
                quote.setPriceDate(cachedQuote.getPriceDate());
                quote.setOpen(cachedQuote.getOpen());
                quote.setLow(cachedQuote.getLow());
                quote.setHigh(cachedQuote.getHigh());
                quote.setName(cachedQuote.getName());
                quote.setLastClose(cachedQuote.getLastClose());
                quote.setDelayedPrice(cachedQuote.hasDelayedPrice());
                quote.setExtMarketPrice(cachedQuote.getExtMarketPrice());
                quote.setExtMarketDate(cachedQuote.getExtMarketDate());
            }
            // updating the name here, may need to move update to when security
            // is added by user or kick off Processor at that time.
            if (quote.getName() != null && !quote.getName().equals(oldName)) {
                updateSecurityName(quote);
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (quote.getExtMarketPrice() != 0) {
                extendedMarket = true;
            }
            boolean networkError = false;
            for (String error : errors) {
                if (error.startsWith("Net-")) {
                    networkError = true;
                }
            }
            quote.setNetworkError(networkError);
            Log.d(TAG, "Price=" + quote.getPrice() + " Low=" + quote.getLow() + " High=" + quote.getHigh() + " Open=" + quote.getOpen() + " Date=" + quote.getPriceDate() + " delayed=" + quote.hasDelayedPrice());
        }
        // record only the first error to the log
        if (errors.size() > 0) {
            recordServiceLogErrorEvent(errors.size() + " errors " + errors.get(0));
        }
        setPrefExtendedMarket(extendedMarket);
    }
    int MAX_ATTEMPTS = 4;

    private void readValidatePrice(Map<String, Study> cacheQuotes, Study quote, List<String> errors) {
        // save data for check below
        double lastClose = quote.getLastClose();
        Date priceDate = quote.getPriceDate();
        boolean validQuote = false;
        int attempts = 0;
        while (!validQuote && attempts < MAX_ATTEMPTS) {
            attempts++;

            if (reader.readRTPrice(quote, errors)) {
                quote.setDelayedPrice(false);
                if (quote.getPriceDate() != null) {
                    cacheQuotes.put(quote.getSymbol(), quote);
                }
                // check for stale data
                if (lastClose != 0 && (PaiUtils.round(lastClose) != PaiUtils.round(quote.getLastClose()))) {
                    String msg = "Validation (" + attempts + "," + quote.getSymbol() + ") History Last close=" + Study.format(lastClose) + " RTQuote=" + Study.format(quote.getLastClose());
                    Log.i(TAG, msg);
                    validQuote = false;
                } else if (priceDate != null && priceDate.after(quote.getPriceDate())) {
                    SimpleDateFormat priceDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
                    String msg = "Validation (" + attempts + "," + quote.getSymbol() + ") Date last=" + priceDateFormat.format(priceDate) + " this=" + priceDateFormat.format(quote.getPriceDate()) + " delayed=" + quote.hasDelayedPrice();
                    recordServiceLogErrorEvent(msg);
                    Log.i(TAG, msg);
                    validQuote = true;
                } else {
                    validQuote = true;
                }
            }
        }
    }

    void updateSecurityName(Study security) {
        ContentValues values = new ContentValues();
        values.put(StudyTable.COLUMN_NAME, security.getName());
        Uri securityUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + security.getSecurityId());
        getContentResolver().update(securityUri, values, null, null);
    }

    List<Price> getPriceHistory2(Study study, String lastOnlineHistoryDbDate, List<String> errors) {
        String TAG = "Get Price History";
        long readDbHistoryStartTime = System.currentTimeMillis();
        String nowDbDate = InZoneDateUtils.toDatabaseFormat(new Date());
        boolean reloadHistory = true;
        List<Price> history = new ArrayList<Price>();
        String[] projection = {PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE, PriceHistoryTable.COLUMN_HIGH,
                PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN, PriceHistoryTable.COLUMN_ADJUSTED_CLOSE};
        String selection = StudyTable.COLUMN_SYMBOL + " = ? ";
        String[] selectionArgs = {study.getSymbol()};
        Log.d(TAG, "Get History from database " + study.getSymbol());
		/*
		 * Note this query has two purposes 1) get lastHistoryDate (requires sort) and (when lastHistoryDate is up-to date) 2) get History
		 */
        Cursor historyCursor = getContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs,
                PriceHistoryTable.COLUMN_DATE + " desc");
        if (historyCursor != null) {
            try {
                if (historyCursor.moveToFirst()) {
                    String lastHistoryDate = historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE));
                    if (lastHistoryDate != null && (lastHistoryDate.compareTo(lastOnlineHistoryDbDate) >= 0 && lastHistoryDate.compareTo(nowDbDate) <= 0)) {
                        // how does lastHistoryDate get to be after now? added
                        // check
                        // 9/21/2013
                        Log.d(TAG, study.getSymbol() + " is upto date using data from database lastDate=" + lastHistoryDate + " now " + nowDbDate);

                        if (lastHistoryDate.compareTo(lastOnlineHistoryDbDate) >= 0 && lastHistoryDate.compareTo(nowDbDate) <= 0) {
                            reloadHistory = false;
                            do {
                                Price price = new Price();
                                price.setAdjustedClose(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE)));
                                price.setClose(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE)));
                                price.setOpen(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_OPEN)));
                                price.setLow(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_LOW)));
                                price.setHigh(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_HIGH)));
                                try {
                                    price.setDate(dbStringDateFormat.parse(historyCursor.getString(historyCursor
                                            .getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE))));
                                    // must have valid date
                                    history.add(price);
                                } catch (Exception e) {
                                    Log.d(TAG, "failed to parse price history date ");
                                }
                            } while (historyCursor.moveToNext());
                        }
                    } else {
                        Log.d(TAG, "Last History Date " + lastHistoryDate + " not equal Last on line History Date " + lastOnlineHistoryDbDate);
                    }
                }
            } finally {
                historyCursor.close();
            }
        }
        Log.d(TAG, "Time to read db history ms = " + (System.currentTimeMillis() - readDbHistoryStartTime) + " Obsolete " + reloadHistory);

        if (reloadHistory) {
            history = rebuildHistoryBatch(study, TAG, errors);
        }
        Log.d(TAG, "Returning " + history.size() + " Price History records for symbol " + study.getSymbol());
        return history;
    }

//    List<Price> getPriceHistory(Study study, String lastOnlineHistoryDbDate, List<String> errors, boolean forceReload) {
    List<Price> getPriceHistory(Study study, String lastOnlineHistoryDbDate, List<String> errors, boolean forceReload) {
        String TAG = "Get Price History";
        long readDbHistoryStartTime = System.currentTimeMillis();
        String nowDbDate = InZoneDateUtils.toDatabaseFormat(new Date());
        boolean reloadHistory = true;
        List<Price> history = new ArrayList<Price>();
        String lastDbHistoryDate = getMaxDbHistoryDate(study.getSymbol());
        String lastTradeDate = InZoneDateUtils.lastProbableTradeDate();
        // Reminder Friday's history is loaded on Saturday not Monday (not true with MSN history).
        //isHolidayOrWeekend(new DateTime());

        if (!forceReload && lastDbHistoryDate != null) {
            if (lastDbHistoryDate != null && (lastDbHistoryDate.compareTo(lastOnlineHistoryDbDate) >= 0 && lastDbHistoryDate.compareTo(nowDbDate) <= 0)) {
                //if (lastDbHistoryDate.compareTo(lastTradeDate) >= 0) {
                Log.d(TAG, study.getSymbol() + " is upto date using data from database lastDate=" + lastDbHistoryDate + " now " + nowDbDate);
                reloadHistory = !isHistoryReloadedFromDatabase(study, history);
                Log.d(TAG, "Time to read db history ms = " + (System.currentTimeMillis() - readDbHistoryStartTime) + " Obsolete " + reloadHistory);
                if (reloadHistory) {
                    Log.d(TAG, "Last History Date " + lastDbHistoryDate + " not equal Last on line History Date " + lastTradeDate);
                }
            }
        }

        if (reloadHistory || forceReload) {
            history = rebuildHistoryBatch(study, TAG, errors);
        }
        Log.d(TAG, "Returning " + history.size() + " Price History records for symbol " + study.getSymbol());
        return history;
    }

    private boolean isHistoryReloadedFromDatabase(Study study, List<Price> history) {
        boolean historyFound = false;
        String[] projection = {PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
                PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN, PriceHistoryTable.COLUMN_ADJUSTED_CLOSE};
        String selection = StudyTable.COLUMN_SYMBOL + " = ? ";
        String[] selectionArgs = {study.getSymbol()};
        Log.d(TAG, "Get History from database " + study.getSymbol());
        Cursor historyCursor = getContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs, null);
        try {
            if (historyCursor != null) {
                if (historyCursor.moveToFirst()) {
                    historyFound = true;
                    do {
                        Price price = new Price();
                        price.setAdjustedClose(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE)));
                        price.setClose(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE)));
                        price.setOpen(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_OPEN)));
                        price.setLow(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_LOW)));
                        price.setHigh(historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_HIGH)));
                        try {
                            price.setDate(dbStringDateFormat.parse(historyCursor.getString(historyCursor
                                    .getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE))));
                            // must have valid date
                            history.add(price);
                        } catch (Exception e) {
                            Log.d(TAG, "failed to parse price history date ");
                        }
                    } while (historyCursor.moveToNext());
                }
            }
        } finally {
            historyCursor.close();
        }
        return historyFound;
    }

    boolean needReloadHistory(String symbol, String lastOnlineHistoryDbDate) {
        String lastHistoryDate = getMaxDbHistoryDate(symbol);
        String nowDbDate = InZoneDateUtils.toDatabaseFormat(new Date());
        // Reminder Friday's history is loaded on Saturday not Monday.
        if (lastHistoryDate != null && (lastHistoryDate.compareTo(lastOnlineHistoryDbDate) >= 0 && lastHistoryDate.compareTo(nowDbDate) <= 0)) {
            return false;
        } else {
            return true;
        }
    }

    String getMaxDbHistoryDate(String symbol) {
        String returnDate = null;
        String[] selectionArgs = {symbol};
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor historyCursor = db.rawQuery("select max(date) from pricehistory where symbol = ? ", selectionArgs);
            if (historyCursor != null) {
                if (historyCursor.moveToFirst()) {
                    returnDate = historyCursor.getString(0);
                }
                historyCursor.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in getMaxDbHistoryDate ");
        }
        Log.d(TAG, "Get Max History Date from database for " + symbol + " returned " + returnDate);
        return returnDate;
    }

    List<Price> rebuildHistoryBatch(Study study, String TAG, List<String> errors) {
        String selection = StudyTable.COLUMN_SYMBOL + " = ? ";
        String[] selectionArgs = {study.getSymbol()};
        List<Price> history;
        long readHistoryStartTime = System.currentTimeMillis();
        Log.d(TAG, "Price History is out-of-date reloading from history provider");
        history = reader.readHistory(study.getSymbol(), errors);
        if (errors.size() > 0) {
            recordServiceLogErrorEvent(errors.get(0));
        }
        study.setHistoryReloaded(true);
        Log.d(TAG, "Time to read on line history ms = " + (System.currentTimeMillis() - readHistoryStartTime));
        long dbUpdateStartTime = System.currentTimeMillis();
        Price exceptionPrice= new Price();
        if (history != null && history.size() > 0)
            try {
                Log.d(TAG, "Replacing Price History in database");
                int rowsDeleted = getContentResolver().delete(PaiContentProvider.PRICE_HISTORY_URI, selection, selectionArgs);
                Log.d(TAG, "Deleted " + rowsDeleted + " history rows");
                ContentValues[] valueArray = new ContentValues[history.size()];
                int ndx = 0;

                for (Price price : history) {
                    ContentValues values = new ContentValues();
                    values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
                    values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
                    values.put(PriceHistoryTable.COLUMN_DATE, dbStringDateFormat.format(price.getDate()));
                    values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
                    values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
                    values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
                    values.put(PriceHistoryTable.COLUMN_SYMBOL, study.getSymbol());
                    valueArray[ndx] = values;
                    ndx++;
                }
                getContentResolver().bulkInsert(PaiContentProvider.PRICE_HISTORY_URI, valueArray);
                Log.d(TAG, "Time to delete/insert history ms = " + (System.currentTimeMillis() - dbUpdateStartTime));
                if (Math.abs(rowsDeleted - ndx) > 1) {
                    recordServiceLogInfoEvent("His-reload " + study.getSymbol() + " replaced " + rowsDeleted + " with " + ndx);
                } else {
                    if (ndx < 250) {
                        recordServiceLogInfoEvent("His-reload " + study.getSymbol() + " count " + ndx);
                    }
                }
            } catch (Exception e) {
                recordServiceLogErrorEvent("His-reload "+e.toString());
                Log.e(TAG, "Exception on Insert History price="+exceptionPrice.toString(), e);
            }
        return history;
    }

    void recordServiceLogErrorEvent(String message) {
        ContentValues values = new ContentValues();
        values.put(ServiceLogTable.COLUMN_MESSAGE, message);
        values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.ERROR.getIndex());
        values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
        getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
    }

    void recordServiceLogInfoEvent(String message) {
        ContentValues values = new ContentValues();
        values.put(ServiceLogTable.COLUMN_MESSAGE, message);
        values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.INFO.getIndex());
        values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
        getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
    }

    public String getLastestOnlineHistoryDbDate(String symbol, List<String> errors) {
        String lastOnlineHistoryDbDate = InZoneDateUtils.lastProbableTradeDate();
        Date latestHistoryDate = reader.latestHistoryDate(symbol, errors);
        Log.d(TAG, "probable TradeDate" + lastOnlineHistoryDbDate + " latestHistoryDate " + latestHistoryDate);
        if (latestHistoryDate != null) {
            lastOnlineHistoryDbDate = InZoneDateUtils.toDatabaseFormat(latestHistoryDate);
        }
        Log.d(TAG, "latestHistoryDate " + latestHistoryDate);
        return lastOnlineHistoryDbDate;
    }

    void saveStudyPrice(Study study) {
        ContentValues values = new ContentValues();
        values.put(StudyTable.COLUMN_PRICE, study.getPrice());
        values.put(StudyTable.COLUMN_PRICE_DATE, StudyTable.priceDateFormat.format(study.getPriceDate()));
        if (study.getStatusMap() != 0) {
            values.put(StudyTable.COLUMN_STATUSMAP, study.getStatusMap());
        }
        values.put(StudyTable.COLUMN_EXT_MARKET_DATE, study.getExtMarketPrice());
        try {
            values.put(StudyTable.COLUMN_EXT_MARKET_DATE, StudyTable.priceDateFormat.format(study.getExtMarketDate()));
        } catch (Exception e) {
            Log.e(TAG, "ExtMarketDateFormat Error ", e);
        }
        Log.d(TAG, "Updating Price Study " + study.toString());
        Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
        getContentResolver().update(studyUri, values, null, null);
    }

    void batchSaveStudyPrice(List<Study> studies) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Study study : studies) {
                ContentValues values = new ContentValues();
                values.put(StudyTable.COLUMN_PRICE, study.getPrice());
                if (study.getPriceDate() == null) {
                    study.setPriceDate(new Date());
                }
                values.put(StudyTable.COLUMN_PRICE_DATE, StudyTable.priceDateFormat.format(study.getPriceDate()));
                if (!study.hasDelayedPrice()) {
                    values.put(StudyTable.COLUMN_OPEN, study.getOpen());
                    values.put(StudyTable.COLUMN_LOW, study.getLow());
                    values.put(StudyTable.COLUMN_HIGH, study.getHigh());
                    values.put(StudyTable.COLUMN_LAST_CLOSE, study.getLastClose());
                }
                if (study.getStatusMap() != 0) {
                    values.put(StudyTable.COLUMN_STATUSMAP, study.getStatusMap());
                }
                values.put(StudyTable.COLUMN_EXT_MARKET_PRICE, study.getExtMarketPrice());
                try {
                    if (study.getExtMarketDate() != null) {
                        values.put(StudyTable.COLUMN_EXT_MARKET_DATE, StudyTable.priceDateFormat.format(study.getExtMarketDate()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "ExtMarketDateFormat Error ", e);
                }
                Log.d(TAG, "Updating Price Study " + study.toString());
                db.update(StudyTable.TABLE_STUDY, values, StudyTable.COLUMN_ID + "=?", new String[]{Long.toString(study.getSecurityId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        contentResolver.notifyChange(PaiContentProvider.PAI_STUDY_URI, null);
    }

    void saveStudy(Study study) {
        ContentValues values = populateStudyContentValues(study);
        Log.d(TAG, "Updating Study " + study.toString());
        Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
        getContentResolver().update(studyUri, values, null, null);
    }

    void batchSaveStudy(List<Study> studies) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Study study : studies) {
                ContentValues values = populateStudyContentValues(study);
                db.update(StudyTable.TABLE_STUDY, values, StudyTable.COLUMN_ID + "=?", new String[]{Long.toString(study.getSecurityId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        contentResolver.notifyChange(PaiContentProvider.PAI_STUDY_URI, null);
    }

    /**
     * populate ContentValues for Study no Notice
     *
     * @param study
     * @return
     */
    ContentValues populateStudyContentValues(Study study) {
        ContentValues values = new ContentValues();
        values.put(StudyTable.COLUMN_PORTFOLIO_ID, study.getPortfolioId());
        values.put(StudyTable.COLUMN_SYMBOL, study.getSymbol());
        values.put(StudyTable.COLUMN_PRICE, study.getPrice());
        values.put(StudyTable.COLUMN_OPEN, study.getOpen());
        values.put(StudyTable.COLUMN_HIGH, study.getHigh());
        values.put(StudyTable.COLUMN_LOW, study.getLow());
        values.put(StudyTable.COLUMN_LAST_CLOSE, study.getLastClose());
        try {
            values.put(StudyTable.COLUMN_PRICE_DATE, StudyTable.priceDateFormat.format(study.getPriceDate()));
        } catch (Exception e) {
            Log.e(TAG, "PriceDateFormat Error ", e);
        }
        values.put(StudyTable.COLUMN_PRICE_LAST_WEEK, study.getPriceLastWeek());
        values.put(StudyTable.COLUMN_PRICE_LAST_MONTH, study.getPriceLastMonth());
        values.put(StudyTable.COLUMN_AVG_TRUE_RANGE, study.getAverageTrueRange());
        values.put(StudyTable.COLUMN_STOCHASTIC_K, study.getStochasticK());
        values.put(StudyTable.COLUMN_STOCHASTIC_D, study.getStochasticD());
        values.put(StudyTable.COLUMN_EMA_WEEK, study.getEmaWeek());
        values.put(StudyTable.COLUMN_EMA_MONTH, study.getEmaMonth());
        values.put(StudyTable.COLUMN_EMA_LAST_WEEK, study.getEmaLastWeek());
        values.put(StudyTable.COLUMN_EMA_LAST_MONTH, study.getEmaLastMonth());
        values.put(StudyTable.COLUMN_EMA_STDDEV_WEEK, study.getEmaStddevWeek());
        values.put(StudyTable.COLUMN_EMA_STDDEV_MONTH, study.getEmaStddevMonth());
        values.put(StudyTable.COLUMN_SMA_WEEK, study.getSmaWeek());
        values.put(StudyTable.COLUMN_SMA_MONTH, study.getSmaMonth());
        values.put(StudyTable.COLUMN_SMA_LAST_WEEK, study.getSmaLastWeek());
        values.put(StudyTable.COLUMN_SMA_LAST_MONTH, study.getSmaLastMonth());
        values.put(StudyTable.COLUMN_SMA_STDDEV_WEEK, study.getSmaStddevWeek());
        values.put(StudyTable.COLUMN_SMA_STDDEV_MONTH, study.getSmaStddevMonth());
        values.put(StudyTable.COLUMN_EXT_MARKET_DATE, study.getExtMarketPrice());
        try {
            values.put(StudyTable.COLUMN_EXT_MARKET_DATE, StudyTable.priceDateFormat.format(study.getExtMarketDate()));
        } catch (Exception e) {
            Log.e(TAG, "ExtMarketDateFormat Error ", e);
        }
        values.put(StudyTable.COLUMN_STATUSMAP, study.getStatusMap());
        return values;
    }

    void removeObsoleteStudies(List<Study> securities) {
        String[] projection = new String[]{StudyTable.COLUMN_ID, StudyTable.COLUMN_SYMBOL};
        Cursor studyCursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, null, null, null);
        try {
            if (studyCursor.moveToFirst()) {
                do {
                    int studyId = studyCursor.getInt(studyCursor.getColumnIndexOrThrow(StudyTable.COLUMN_ID));
                    String symbol = studyCursor.getString(studyCursor.getColumnIndexOrThrow(StudyTable.COLUMN_SYMBOL));
                    boolean found = false;
                    for (Study security : securities) {
                        if (security.getSymbol().equals(symbol)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyId);
                        getContentResolver().delete(studyUri, null, null);
                    }
                } while (studyCursor.moveToNext());
            }
        } finally {
            studyCursor.close();
        }
    }


    List<Study> getSecurities(String inSymbol) {
        List<Study> securities = new ArrayList<Study>();
        String[] projection = StudyTable.getFullProjection();
        String selection = null;
        String[] selectionArgs = null;
        if (inSymbol != null && inSymbol.length() > 0) {
            selection = StudyTable.COLUMN_SYMBOL + " = ? ";
            selectionArgs = new String[]{inSymbol};
            Log.d(TAG, "Selecting Single Security from database");
        }
        Cursor cursor = getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, StudyTable.COLUMN_SYMBOL);
        try {
            if (cursor != null) {
                if (cursor.moveToFirst())
                    do {
                        Study security = StudyTable.loadStudy(cursor);
                        securities.add(security);

                    } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in Processor GetSecurities", e);
        } finally {
            // Always close the cursor
            cursor.close();
        }
        return securities;
    }

    ContentResolver getContentResolver() {
        return contentResolver;

    }

    public void setPrefExtendedMarket(boolean extendedMarket) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(UpdateService.KEY_PREF_EXTENDED_MARKET, extendedMarket);
        editor.apply();
    }

}