package com.codeworks.pai.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;

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
    public boolean readCurrentPrice(Study security) {
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
                        Log.d(TAG, line[0] + " last=" + line[1] + " name=" + line[3] + " time=" + line[4] + " low=" + line[5]);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "readCurrentPrice " + e.getMessage(), e);
            }
        return found;
    }

    public boolean readRTPrice(Study security) {
        boolean found = false;
        BufferedReader br = null;
        try {
            String urlStr = buildRealtimeUrl(security.getSymbol());
            String searchPrice = "yfs_l84_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            // Fund Different yfs_l10_pttrx
            //String searchBid = "yfs_b00_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            //String searchAsk = "yfs_a00_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            String searchName = "class=\"title\"><h2>";
            String searchTime2 = "yfs_t53_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            String searchTime1 = "yfs_t53_" + security.getSymbol().toLowerCase(Locale.US) + "\"><span id=\"yfs_t53_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            String searchLow = "yfs_g53_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            String searchHigh = "yfs_h53_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            String searchOpen = "Open:</th><td class=\"yfnc_tabledata1\">";
            String searchPrevClose = "Prev Close:</th><td class=\"yfnc_tabledata1\">";
            String searchPreMarket = "yfs_l86_" + security.getSymbol().toLowerCase(Locale.US) + "\">";
            long start = System.currentTimeMillis();
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(20000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("User-Agent", "Desktop");
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                count++;
                if (count > 200 && count < 300) {
                    String result = scanLine(searchPrice, start, line, count);
                    if (result != null) {
                        found = true;
                        security.setPrice(Double.parseDouble(result));
                    }
                    result = scanLine(searchPreMarket, start, line, count);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        security.setExtMarketPrice(Double.parseDouble(result));
                    }
                    result = scanLine(searchName, start, line, count);
                    if (result != null) {
                        //result = URLDecoder.decode(result, "UTF-8");
                        Spanned spanned = Html.fromHtml(result);
                        security.setName(spanned.toString());
                    }
                    result = scanLine(searchTime1, start, line, count);
                    if (result == null) {
                        result = scanLine(searchTime2, start, line, count);

                    }
                    if (result != null) {
                        security.setPriceDate(parseRTDate(result));
                    }
                    result = scanLine(searchPrevClose, start, line, count);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        security.setLastClose(Double.parseDouble(result));
                    }
                    result = scanLine(searchOpen, start, line, count);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        security.setOpen(Double.parseDouble(result));
                    }
                    result = scanLine(searchLow, start, line, count);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        security.setLow(Double.parseDouble(result));
                    }
                    result = scanLine(searchHigh, start, line, count);
                    if (result != null && !N_A.equalsIgnoreCase(result)) {
                        security.setHigh(Double.parseDouble(result));
                    }
                }

            }
            Log.d(TAG, "SCANNED " + count + " lines in ms " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            Log.e(TAG, "Exception in ReadRTPrice", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore on close
                }
            }
        }
        return found;
    }

    String scanLine(String searchStr, long start, String line, int count) {
        String result = null;
        int pos = line.indexOf(searchStr);
        if (pos > -1) {
            int endPos = line.indexOf("<", pos + searchStr.length());
            result = line.substring(pos + searchStr.length(), endPos);
            Log.d(TAG, "SCAN " + searchStr + " FOUND " + result + " on line " + count + " in ms " + (System.currentTimeMillis() - start));
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
    public List<Price> readHistory(String symbol) {
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
        }
        return history;
    }

    /**
     * Returns the latest history date for Symbol,
     *
     * @param symbol
     * @return will return null on failure
     */
    public Date latestHistoryDate(String symbol) {
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

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves
     * the content as a InputStream, which is CSV parsed and returned
     * as an ArrayList of String arrays.
     */
    List<String[]> downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
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

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
