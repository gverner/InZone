package com.codeworks.pai.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import static org.joda.time.DateTimeZone.UTC;

public class DataReaderYahoo implements DataReader {
    private static final String N_A = "N/A";
    private static final String TAG = DataReaderYahoo.class.getSimpleName();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /*
     * s=symbol l1=last price d1=last trade date t1=last trade time c1=change
     * o=open h=day high g=day low v=volume k1=last trade real times with time
     * b=bin b2=bin real time a=asks b1=ask real time n=name
     * p=previousClose
     */
    /* (non-Javadoc)
     * @see com.codeworks.pai.processor.SecurityDataReader#readCurrentPrice(com.codeworks.pai.db.model.Security)
	 */
    @Override
    public boolean readCurrentPrice(Study security, List<String> errors) {
        List<String[]> results;// "MM/dd/yyyy hh:mmaa"
        boolean found = false;
        double quote = 0;
        if (security != null && security.getSymbol() != null)
            try {
                String url = "http://download.finance.yahoo.com/d/quotes.csv?s=" + security.getSymbol() + "&f=sl1d1nt1ghop&e=.csv";
                results = downloadUrl(url);
                for (String[] line : results) {
                    if (line.length >= 7) {
                        quote = parseDouble(line[1], "Price");
                        security.setPrice(quote);
                        if (N_A.equals(line[2]) && quote == 0.0 && security.getSymbol().equals(line[3])) {
                            found = false;
                            security.setName("Not Found");
                        } else {
                            security.setName(line[3]);
                            found = true;
                        }
                        security.setPriceDate(parseDateTime(line[2] + " " + line[4], " Date Time"));
                        security.setLow(parseDouble(line[5], "Low"));
                        security.setHigh(parseDouble(line[6], "High"));
                        security.setOpen(parseDouble(line[7], "Open"));
                        security.setLastClose(parseDouble(line[8], "Last Close"));
                        security.setExtMarketPrice(0D);
                        Log.d(TAG, line[0] + " last=" + line[1] + " name=" + line[3] + " time=" + line[4] + " low=" + line[5]);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "readCurrentPrice " + e.getMessage(), e);
                errors.add("1-"+e.getMessage());
            }
        return found;
    }


    String scanLine(String searchStr, long start, String line, int count) {
        String result = null;
        int pos = line.indexOf(searchStr);
        if (pos > -1) {
            int endPos = line.indexOf("<", pos + searchStr.length());
            if (endPos > -1) {
                result = line.substring(pos + searchStr.length(), endPos);
                Log.d(TAG, "SCAN " + searchStr + " FOUND " + result + " on line " + count + " in ms " + (System.currentTimeMillis() - start));
            }
        }
        return result;
    }

    Date parseRTDate(String stringDate) {
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("US/Eastern"), Locale.US);
        Date returnDate = cal.getTime(); // return now on parse failure
        SimpleDateFormat ydf = new SimpleDateFormat("MMM dd, hh:mmaa zzz yyyy", Locale.US);
        ydf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        if (stringDate.length() >= 17) {
            stringDate = stringDate + " " + cal.get(Calendar.YEAR);
        } else if (stringDate.length() == 10 || stringDate.length() == 11) {
            stringDate = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + stringDate + " " + cal.get(Calendar.YEAR);
        }
        try {
            returnDate = ydf.parse(stringDate);
            Log.d(TAG, "Price Date " + ydf.format(returnDate));
        } catch (ParseException e) {
            Log.e(TAG, "Parse Date Exception", e);
        }
        return returnDate;
    }

    double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            Log.d(TAG, "unable to parse " + value + " as a double for field " + fieldName);
            return 0;
        }
    }

    Date parseDateTime(String value, String fieldName) {
        try {
            return dateTimeFormat.parse(value);
        } catch (Exception e) {
            Log.d(TAG, "Unable to parse " + value + " as date time for field " + fieldName);
            return null;
        }
    }

    Date parseDate(String value, String fieldName) {
        try {
            return dateFormat.parse(value);
        } catch (Exception e) {
            Log.d(TAG, "Unable to parse " + value + " as date for field " + fieldName);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.codeworks.pai.processor.SecurityDataReader#readHistory(java.lang.String)
     */
    @Override
    public List<Price> readHistory(String symbol, List<String> errors) {
        List<Price> history = new ArrayList<Price>();
        List<String[]> results;
        try {
            String url = buildHistoryUrl(symbol, 300);
            results = downloadUrl(url);
            int counter = 0;
            for (String[] line : results) {
                counter++;
                if (counter % 100 == 0) {
                    Log.d(TAG, counter + " records read for " + symbol);
                }
                if (!"Date".equals(line[0])) { // skip header

                    Price price = new Price();
                    Date priceDate = parseDate(line[0], "Date");
                    if (priceDate != null) { // must have valid date
                        history.add(price);
                        price.setDate(priceDate);
                        price.setOpen(parseDouble(line[1], "Open"));
                        price.setHigh(parseDouble(line[2], "High"));
                        price.setLow(parseDouble(line[3], "Low"));
                        price.setClose(parseDouble(line[4], "Close"));
                        price.setAdjustedClose(parseDouble(line[6], "AdjustedClose"));
                    }
                    //if (counter % 20 == 0) {
                    //	Log.d(TAG, symbol + " " + line[0] + " " + line[1]);
                    //}
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "readHistory " + e.getMessage(), e);
            errors.add("2-"+e.getMessage());
        }
        return history;
    }

    /**
     * Returns the latest history date for Symbol,
     *
     * @param symbol
     * @return will return null on failure
     */
    public Date latestHistoryDate(String symbol, List<String> errors) {
        long startTime = System.currentTimeMillis();
        Date latestDate = null;
        List<String[]> results;
        try {
            String url = buildHistoryUrl(symbol, 7);
            results = downloadUrl(url);
            for (String[] line : results) {
                if (!"Date".equals(line[0])) { // skip header
                    Date theDate = parseDate(line[0], "Date");
                    if (latestDate == null || latestDate.before(theDate)) {
                        latestDate = theDate;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "readLatestHistoryDate " + e.getMessage(), e);
            errors.add("3-"+e.getMessage());
        }
        Log.d(TAG, "Milliseconds to retrieve latest history date=" + (System.currentTimeMillis() - startTime));
        return latestDate;
    }

    String buildHistoryUrl(String symbol, int lengthInDays) {
        Calendar cal = GregorianCalendar.getInstance();
        String endDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String endMonth = Integer.toString(cal.get(Calendar.MONTH));
        String endYear = Integer.toString(cal.get(Calendar.YEAR));
        cal.add(Calendar.WEEK_OF_YEAR, -Math.abs(lengthInDays));
        String startDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String startMonth = Integer.toString(cal.get(Calendar.MONTH));
        String startYear = Integer.toString(cal.get(Calendar.YEAR));
        // chart.finance.yahoo.com/table.csv?s=SPY&amp;a=00&amp;b=1&amp;c=2012&amp;d=03&amp;e=12&amp;f=2013&amp;g=d&amp;ignore=.csv"
        String url = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + startMonth + "&b=" + startDay + "&c=" + startYear
                + "&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + "&g=d&ignore=.csv";
        return url;
    }

    String buildRealtimeUrl(String symbol) {
        String url = "http://finance.yahoo.com/q?s=" + symbol + "&ql=1";
        return url;
    }

    public boolean readRTPrice(final Study security, final List<String> errors) {

        security.setExtMarketPrice(0d);// extended price may not be available
        String urlStr = buildRealtimeUrl(security.getSymbol());
        String securityText = security.getSymbol().toLowerCase(Locale.US) + "\">";
        final String searchPrice = "yfs_l84_" + securityText;
        // Fund Different yfs_l10_pttrx
        //String searchBid = "yfs_b00_" + securityText;
        //String searchAsk = "yfs_a00_" + securityText;
        final String searchName = "class=\"title\"><h2>";
        final String searchTime2 = "yfs_t53_" + securityText;
        final String searchTime1 = "yfs_t53_" + securityText + "<span id=\"yfs_t53_" + securityText;
        final String searchLow = "yfs_g53_" + securityText;
        final String searchHigh = "yfs_h53_" + securityText;
        final String searchOpen = "Open:</th><td class=\"yfnc_tabledata1\">";
        final String searchPrevClose = "Prev Close:</th><td class=\"yfnc_tabledata1\">";
        final String searchExtMarket = "yfs_l86_" + securityText;
        final String searchExtTime = "yfs_t54_" + securityText;
        security.setExtMarketPrice(0);
        final URLLineReader reader = new URLLineReader(errors);
        reader.process(urlStr, new LineProcessor() {
            int itemsFound = 0;
            @Override
            public boolean process(String line, int lineNo, long startTime) {
                int lastItemsFound = itemsFound;
                if (lineNo > 100) {
                    String result = scanLine(searchPrice, startTime, line, lineNo);
                    if (result != null) {
                        itemsFound++;
                        reader.setFound(true);
                        security.setPrice(parseDouble(result, searchPrice));
                    }
                    result = scanLine(searchName, startTime, line, lineNo);
                    if (result != null) {
                        itemsFound++;
                        //result = URLDecoder.decode(result, "UTF-8");
                        Spanned spanned = Html.fromHtml(result);
                        security.setName(spanned.toString());
                    }
                    result = scanLine(searchTime1, startTime, line, lineNo);
                    if (result == null) {
                        result = scanLine(searchTime2, startTime, line, lineNo);
                    }
                    if (result != null) {
                        itemsFound++;
                        security.setPriceDate(parseRTDate(result));
                    }
                    result = scanLine(searchExtMarket, startTime, line, lineNo);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        itemsFound++;
                        security.setExtMarketPrice(parseDouble(result, searchExtMarket));
                    }
                    result = scanLine(searchExtTime, startTime, line, lineNo);
                    if (result != null) {
                        itemsFound++;
                        security.setExtMarketDate(parseRTDate(result));
                    }
                    result = scanLine(searchPrevClose, startTime, line, lineNo);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        itemsFound++;
                        security.setLastClose(parseDouble(result, searchPrevClose));
                    }
                    result = scanLine(searchOpen, startTime, line, lineNo);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        itemsFound++;
                        security.setOpen(parseDouble(result, searchOpen));
                    }
                    result = scanLine(searchLow, startTime, line, lineNo);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        itemsFound++;
                        security.setLow(parseDouble(result, searchLow));
                    }
                    result = scanLine(searchHigh, startTime, line, lineNo);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        itemsFound++;
                        security.setHigh(parseDouble(result, searchHigh));
                    }
                }
                if (lastItemsFound != itemsFound) {
                    Log.d(TAG,"Total Items Found " + itemsFound+ " found "+(itemsFound - lastItemsFound)+" on line "+lineNo);
                }
                // could be up to 9 items but multiple items are on a single line. (only 7 when not extended market)
                return itemsFound < 7;
            }
        });
        return reader.getFound();
    }

    /**
     * Reads Option Dates Dropdown converting the string date
     * @param symbol
     * @return
     */
    public List<DateTime> readOptionDatesStr(final String symbol, List<String> errors) {
       // http://finance.yahoo.com/q/op?s=SPY
       //  <option data-selectbox-link="/q/op?s=SPY&amp;date=1418342400" value="1418342400">December 12, 2014</option>
        final List<DateTime> optionDates = new ArrayList<DateTime>();
        // http://finance.yahoo.com/q/op?s=SPY+Options
        String urlStr = "http://finance.yahoo.com/q/op?s=" + symbol + "+Options";
        URLLineReader reader = new URLLineReader(errors);
        reader.process(urlStr, new LineProcessor() {
            @Override
            public boolean process(String line, int lineNo, long startTime) {
                if (lineNo > 1500) {
                    String searchOption = "<option data-selectbox-link=\"/q/op?s=" + symbol.toUpperCase() + "&date=";
                    int pos = line.indexOf(searchOption);
                    if (pos > -1) {
                        int pos2 = line.indexOf(">", pos + searchOption.length());
                        if (pos2 > -1) {
                            int endPos = line.indexOf("<", pos2);
                            if (endPos > -1) {
                                String optionDate = line.substring(pos2+1, endPos);
                                Log.d(TAG, "SCAN " + searchOption + " FOUND " + optionDate + " on line " + lineNo + " in ms " + (System.currentTimeMillis() - startTime));
                                //long optionMs = Long.parseLong(optionDate) * 1000;
                                //DateTime optionDateTime = new DateTime(optionMs, DateTimeZone.UTC);
                                DateTime optionDateTime  = DateTime.parse(optionDate, DateTimeFormat.forPattern("MMMM dd, yyyy"));
                                optionDates.add(optionDateTime);
                            }
                        }
                    }
                }
                // quit reading after line 1600
                return lineNo <= 1600;
            }
        });
        return optionDates;
    }

    public List<DateTime> readOptionDates(final String symbol, List<String> errors) {
        // http://finance.yahoo.com/q/op?s=SPY
        //  <option data-selectbox-link="/q/op?s=SPY&amp;date=1418342400" value="1418342400">December 12, 2014</option>
        final List<DateTime> optionDates = new ArrayList<DateTime>();
        // http://finance.yahoo.com/q/op?s=SPY+Options
        String urlStr = "http://finance.yahoo.com/q/op?s=" + symbol + "+Options";
        URLLineReader reader = new URLLineReader(errors);
        reader.process(urlStr, new LineProcessor() {
            @Override
            public boolean process(String line, int lineNo, long startTime) {
                if (lineNo > 1500) {
                    String searchOption = "<option data-selectbox-link=\"/q/op?s=" + symbol.toUpperCase() + "&date=";
                    int pos = line.indexOf(searchOption);
                    if (pos > -1) {
                        int pos2 = line.indexOf("\"", pos + searchOption.length());
                        if (pos2 > -1) {
                            String optionDate = line.substring(pos + searchOption.length(), pos2);
                            long optionMsNoTimeZone = Long.parseLong(optionDate) * 1000;
                            // optionDate seconds is in local timezone add timezoneOffset ot get UTCs
                            // use option date because this date is in the future and may have different daylight savings offset then today.
                            final long timezoneOffset = Math.abs(DateTimeZone.getDefault().getOffset(new DateTime(optionMsNoTimeZone)));
                            long optionMs = optionMsNoTimeZone + timezoneOffset;
                            DateTime optionDateTime = new DateTime(optionMs, DateTimeZone.getDefault());
                            Log.d(TAG, "SCAN " + searchOption + " FOUND " + optionDate + " on line " + lineNo + " in ms " + (System.currentTimeMillis() - startTime)+" date="+optionDateTime);
                            optionDates.add(optionDateTime);
                        }
                    }
                }
                // quit after 1600 lines
                return lineNo < 1600;
            }
        });
        return optionDates;
    }

    public Option readOption(final Option option) {
        SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd", Locale.US);
        String strike = String.format("%08d", new BigDecimal(option.getStrike()).movePointRight(3).intValue());
        final String optionId = option.getSymbol() + yyMMdd.format(option.getExpires().toDate()) + option.getType() + strike;
        String urlStr = "http://finance.yahoo.com/q?s=" + optionId;
        Log.d(TAG, "Option URL=" + urlStr);
        List<String> errors = new ArrayList<String>();
        URLLineReader reader = new URLLineReader(errors);
        reader.process(urlStr, new LineProcessor() {
            String searchBid = "yfs_b00_" + optionId.toLowerCase(Locale.US) + "\">";
            String searchAsk = "yfs_a00_" + optionId.toLowerCase(Locale.US) + "\">";
            String searchPrice = "yfs_l10_" + optionId.toLowerCase(Locale.US) + "\">";
            int valuesFound = 0;

            @Override
            public boolean process(String line, int lineNo, long startTime) {
                String result = scanLine(searchPrice, startTime, line, lineNo);
                if (result != null) {
                    valuesFound++;
                    option.setPrice(Double.parseDouble(result));
                }
                result = scanLine(searchBid, startTime, line, lineNo);
                if (result != null) {
                    valuesFound++;
                    if (!N_A.equalsIgnoreCase(result)) {
                        option.setBid(Double.parseDouble(result));
                    }
                }
                result = scanLine(searchAsk, startTime, line, lineNo);
                if (result != null) {
                    valuesFound++;
                    if (!N_A.equalsIgnoreCase(result)) {
                        option.setAsk(Double.parseDouble(result));
                    }
                }
                return true;
            }
        });
        if (errors.size() > 0) {
            option.setError(errors.get(0));
        }
        return option;
    }

    public interface LineProcessor {
        // return true to continueLoop
        boolean process(String line, int lineNo, long startTime);
    }

    class URLLineReader {
        boolean found = false;
        public boolean getFound() {
            return found;
        }
        public void setFound(boolean found) {
            this.found = found;
        }
        List<String> errors;
        URLLineReader(List<String> errors) {
            this.errors = errors;
        }
        public int process(String url, LineProcessor lineProcessor) {
            int response = 0;
            long start = System.currentTimeMillis();

            try {
                HttpURLConnection conn = getHttpURLConnection(url);
                // Starts the query
                conn.connect();
                try {
                    response = conn.getResponseCode();
                    Log.d(TAG, "The call to "+url+" response is: " + response);
                    if (response == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        try {
                            String line;
                            int lineNo = 0;
                            boolean continueLoop = true;
                            while ((line = br.readLine()) != null && continueLoop) {
                                lineNo++;
                                continueLoop = lineProcessor.process(line, lineNo, start);
                            }
                            Log.d(TAG, "SCANNED " + lineNo + " lines in ms " + (System.currentTimeMillis() - start));
                        } finally {
                            br.close();
                        }
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception in urlLineReader "+url, e);
                errors.add("4-"+e.getMessage());
            }

            return response;
        }
    }

    private HttpURLConnection getHttpURLConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000 /* milliseconds */);
        conn.setConnectTimeout(20000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setRequestProperty("User-Agent", "Desktop");
        return conn;
    }

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves
     * the content as a InputStream, which is CSV parsed and returned
     * as an ArrayList of String arrays.
     */
    List<String[]> downloadUrl(String urlStr) throws IOException {
        InputStream is = null;

        HttpURLConnection conn = getHttpURLConnection(urlStr) ;
        // Starts the query
        conn.connect();
        try {
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            CSVReader reader = new CSVReader(new InputStreamReader(is, "UTF-8"));
            try {
                List<String[]> lines = reader.readAll();
                return lines;
            } finally {
                reader.close();
            }
        } finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if (is != null) {
                is.close();
            }
            conn.disconnect();
        }
    }

}
