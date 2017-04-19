package com.codeworks.pai.processor;

import android.text.Html;
import android.text.Spanned;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Study;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import au.com.bytecode.opencsv.CSVReader;

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
     * @see com.codeworks.pai.processor.SecurityDataReader#readDelayedPrice(com.codeworks.pai.db.model.Security)
	 */
    @Override
    public boolean readDelayedPrice(Study security, List<String> errors) {
        List<String[]> results;// "MM/dd/yyyy hh:mmaa"
        boolean found = false;
        double quote = 0;
        if (security != null && security.getSymbol() != null)
            try {
                String url = "https://download.finance.yahoo.com/d/quotes.csv?s=" + security.getSymbol() + "&f=sl1d1nt1ghop&e=.csv";
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
                Log.d(TAG, "readDelayedPrice " + e.getMessage(), e);
                errors.add("Net-1-"+e.getMessage());
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
        SimpleDateFormat ydf = new SimpleDateFormat("MMM dd, hh:mm aa zzz yyyy", Locale.US);
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
            Log.d(TAG, url);
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
            errors.add("2-" + e.getMessage());
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
        String url = "https://ichart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + startMonth + "&b=" + startDay + "&c=" + startYear
                + "&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + "&g=d&ignore=.csv";
        return url;
    }

    String buildRealtimeUrl(String symbol) {
        String url = "https://finance.yahoo.com/quote/"+ symbol;
        //String url = "http://finance.yahoo.com/q?s=" + symbol + "&ql=1";
        return url;
    }
    boolean readRTPriceJson(final Study security, final List<String> errors) {

        security.setExtMarketPrice(0d);// extended price may not be available
//        String urlStr = "https://query1.finance.yahoo.com/v10/finance/quoteSummary/"+security.getSymbol()+"?formatted=true&crumb=sRaAb86KidE&lang=en-US&region=US&modules=price%2CsummaryDetail&corsDomain=finance.yahoo.com";
        String urlStr = "https://query1.finance.yahoo.com/v10/finance/quoteSummary/"+security.getSymbol()+"?formatted=true&crumb=sRaAb86KidE&lang=en-US&region=US&modules=price&corsDomain=finance.yahoo.com";
        URLJsonReader reader = new URLJsonReader(errors);
        //final Map<String, Object> quoteSummary = new HashMap<String, Object>();
        final Map<String, Object> price = new HashMap<String, Object>();
        reader.process(urlStr, new JsonProcessor() {
            @Override
            public boolean process(JsonReader json, long startTime) {
                try {
                    json.beginObject();
                    while (json.hasNext()) {
                        if ("quoteSummary".equals(json.nextName())) {
                            json.beginObject();
                            if ("result".equals(json.nextName())) {
                                json.beginArray();
                                while (json.hasNext()) {
                                    json.beginObject();
                                    /*
                                    if ("summaryDetail".equals(json.nextName())) {
                                        quoteSummary.putAll(jsonObjectToMap(json));
                                    }*/
                                    if ("price".equals(json.nextName())) {
                                        price.putAll(jsonObjectToMap(json));
                                    }
                                    json.endObject();
                                }
                                json.endArray();
                            }
                        }
                        if ("error".equals(json.nextName())) {
                            if (JsonToken.NULL.equals(json.peek())) {
                                json.nextNull();
                            } else {
                                json.nextString();
                            }
                        }
                    }
                    json.endObject();

                } catch (IOException e) {
                    Log.e(TAG, "Error reading json stream ", e);
                }
                return true;
            }
        });

        if (price.size() >= 30) {
            security.setHigh(getFormatRaw(price.get("regularMarketDayHigh")));
            security.setLow(getFormatRaw(price.get("regularMarketDayLow")));
            security.setOpen(getFormatRaw(price.get("regularMarketOpen")));
            security.setLastClose(getFormatRaw(price.get("regularMarketPreviousClose")));
            security.setName((String) price.get("shortName"));
            if ("REGULAR".equals(price.get("marketState"))) {
                security.setExtMarketPrice(0d);
                security.setExtMarketDate(DateTime.now().toDate());
            } else if (((String)price.get("marketState")).startsWith("PRE")) {
                if (price.get("preMarketTime") != null) {
                    DateTime preDateTime = convertSecondsToDateTime(((Double) price.get("preMarketTime")).longValue(), false);
                    if((new DateTime(preDateTime).toLocalDate()).equals(new LocalDate())) {
                        security.setExtMarketDate(preDateTime.toDate());
                        security.setExtMarketPrice(getFormatRaw(price.get("preMarketPrice")));
                    } else {
                        // pre date could have been from yesterday
                        security.setExtMarketPrice(0d);
                        security.setExtMarketDate(DateTime.now().toDate());
                    }
                } else {
                    security.setExtMarketPrice(0d);
                    security.setExtMarketDate(DateTime.now().toDate());
                }
            } else if (((String)price.get("marketState")).startsWith("POST") || ((String)price.get("marketState")).equals("CLOSED")) {
                if (price.get("postMarketTime") != null) {
                    security.setExtMarketDate(convertSecondsToDateTime(((Double) price.get("postMarketTime")).longValue(), false).toDate());
                }
                security.setExtMarketPrice(getFormatRaw(price.get("postMarketPrice")));
            } else {
                security.setExtMarketPrice(0d);
                security.setExtMarketDate(DateTime.now().toDate());
            }
            security.setPrice(getFormatRaw(price.get("regularMarketPrice")));
            security.setPriceDate(convertSecondsToDateTime(((Double) price.get("regularMarketTime")).longValue(), false).toDate());
            security.setSymbol((String) price.get("symbol"));
            return true;
        } else {
            Log.w(TAG, "Found " + price.size() + " elements found in price expected at least 30");
            return false;
        }
    }

    double getFormatRaw(Object obj) {
        if (obj == null || ((Format)obj).raw == null) {
            return 0;
        } else {
            return ((Format)obj).raw;
        }
    }

    private Map<String, Object> jsonObjectToMap(JsonReader json) throws IOException {
        json.beginObject();
        Map<String, Object> map = new HashMap<String, Object>();
        while (json.hasNext()) {
            String name = json.nextName();
            JsonToken token = json.peek();
            if (JsonToken.BEGIN_OBJECT.equals(token)) {
                map.put(name, parseFormat(json));
            } else if (JsonToken.NUMBER.equals(token)) {
                map.put(name, json.nextDouble());
            } else if (JsonToken.STRING.equals(token)) {
                map.put(name, json.nextString());
            } else if (JsonToken.BOOLEAN.equals(token)) {
                map.put(name, json.nextBoolean());
            } else if (JsonToken.NULL.equals(token)) {
                json.nextNull();
            }
        }
        json.endObject();
        return map;
    }

    public boolean readRTPrice(final Study security, final List<String> errors) {
        boolean result = false;
        try {
            result = readRTPriceJson(security, errors);
        } catch (Exception e) {
            errors.add("JS-"+e.getMessage());
            Log.d(TAG,e.getMessage(),e);
        }
        if (!result) try {
            errors.add("JS-Unexpected");
            result = readRTPriceHtml(security,errors);
        } catch (Exception e) {
            errors.add("HT-"+e.getMessage());
        }
        return result;
    }

    public boolean readRTPriceHtml(final Study security, final List<String> errors) {
        security.setExtMarketPrice(0d);// extended price may not be available
        String urlStr = buildRealtimeUrl(security.getSymbol());
        String securityText = security.getSymbol().toLowerCase(Locale.US) + "\">";
        final String searchPrice = "$price.0\">";
        // Fund Different yfs_l10_pttrx
        //String searchBid = "yfs_b00_" + securityText;
        //String searchAsk = "yfs_a00_" + securityText;
        final String searchName = "$companyName\">";
        final String searchTime2 = "Quote.0.0.2.2.0\">As of";
        final String searchDaysRange = "$DAYS_RANGE.1\">";
        final String searchOpen = "$OPEN.1\">";
        final String searchPrevClose = "$PREV_CLOSE.1\">";
        final String searchExtMarket = "$prePost.2\">";
        final String searchExtTime = "$prePost.5\">as of";
        security.setExtMarketPrice(0);
        final URLLineReader reader = new URLLineReader(errors);
        reader.process(urlStr, new LineProcessor() {
            int itemsFound = 0;
            @Override
            public boolean process(String line, int lineNo, long startTime) {
                int lastItemsFound = itemsFound;
//                if (lineNo > 100) {
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

                result = scanLine(searchTime2, startTime, line, lineNo);
                    if (result != null) {
                        itemsFound++;
                        int pos = result.indexOf(".");
                        if (pos > -1) {
                            result = result.substring(0, pos);
                        }
                        result = result.trim();
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
                        result = result.trim();
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
                    result = scanLine(searchDaysRange, startTime, line, lineNo);
                    if (result != null) {
                        String[] daysRange = result.split(" - ");
                        if (daysRange.length > 0 && daysRange[0] != null && !N_A.equalsIgnoreCase(daysRange[0])) {
                            itemsFound++;
                            security.setLow(parseDouble(daysRange[0], searchDaysRange));
                        }
                        if (daysRange.length > 1 && daysRange[1] != null && !N_A.equalsIgnoreCase(daysRange[1])) {
                            itemsFound++;
                            security.setHigh(parseDouble(daysRange[1], searchDaysRange));
                        }
                    }
 //               }
                if (lastItemsFound != itemsFound) {
                    Log.d(TAG,"Total Items Found " + itemsFound+ " found "+(itemsFound - lastItemsFound)+" on line "+lineNo);
                }
                // could be up to 9 items but multiple items are on a single line. (only 7 when not extended market)
                return itemsFound < 7;
            }
        });
        return reader.getFound();
    }

    class OptionJsonResult {
        final List<DateTime> expirations = new ArrayList<DateTime>();
        final List<Option> calls = new ArrayList<Option>();
        final List<Option> puts = new ArrayList<Option>();
    }

    class Format {
        Double raw;
        String fmt;
        String longFmt;
    }

    public List<Option> readOptionPrice(final String symbol, final long expirationDate, List<String> errors) {
        try {
            String urlStr = "https://query1.finance.yahoo.com/v7/finance/options/" + symbol + "?formatted=true&crumb=sRaAb86KidE&lang=en-US&region=US&date=" + expirationDate + "&corsDomain=finance.yahoo.com";
            OptionJsonResult result = readOptionData(urlStr, errors);
            result.calls.addAll(result.puts);
            return result.calls;
        } catch (Exception e) {
            errors.add("8-" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<DateTime> readOptionExpirations(final String symbol, List<String> errors) {
        try {
            String urlStr = "https://query2.finance.yahoo.com/v7/finance/options/" + symbol + "?formatted=true&crumb=sRaAb86KidE&lang=en-US&region=US&corsDomain=finance.yahoo.com";

            OptionJsonResult result = readOptionData(urlStr, errors);
            return result.expirations;
        } catch (Exception e) {
            errors.add("9-" + e.getMessage());
            return new ArrayList<>();
        }
    }

    OptionJsonResult readOptionData(final String urlStr, List<String> errors) {
        final OptionJsonResult result = new OptionJsonResult();
        URLJsonReader reader = new URLJsonReader(errors);
        reader.process(urlStr, new JsonProcessor() {
            @Override
            public boolean process(JsonReader json, long startTime) {
                try {
                    json.beginObject();
                    while (json.hasNext()) {
                        if ("optionChain".equals(json.nextName())) {
                            json.beginObject();
                            if ("result".equals(json.nextName())) {
                                json.beginArray();
                                String symbol = "";
                                while (json.hasNext()) {
                                    json.beginObject();
                                    if ("underlyingSymbol".equals(json.nextName())) {
                                        symbol = json.nextString();
                                    }
                                    if ("expirationDates".equals(json.nextName())) {
                                        List<Long> expDates = new ArrayList<Long>();
                                        json.beginArray();
                                        while (json.hasNext()) {
                                            Long seconds = json.nextLong();
                                            result.expirations.add(convertSecondsToDateTime(seconds, true));
                                        }
                                        json.endArray();

                                    }
                                    if ("strikes".equals(json.nextName())) {
                                        List<BigDecimal> strikes = new ArrayList<BigDecimal>();
                                        json.beginArray();
                                        while (json.hasNext()) {
                                            BigDecimal strike = new BigDecimal(json.nextDouble());
                                            strikes.add(strike);
                                        }
                                        json.endArray();
                                    }
                                    if ("hasMiniOptions".equals(json.nextName())) {
                                        Boolean hasMinOptions = json.nextBoolean();
                                    }
                                    if ("quote".equals(json.nextName())) {
                                        json.beginObject();
                                        while (json.hasNext()) {
                                            String name = json.nextName();
                                            String value = json.nextString();
                                        }
                                        json.endObject();

                                    }
                                    if ("options".equals(json.nextName())) {
                                        json.beginArray();
                                        while (json.hasNext()) {
                                            json.beginObject();
                                            while (json.hasNext()) {
                                                if ("expirationDate".equals(json.nextName())) {
                                                    Long expDate = json.nextLong();
                                                }
                                                if ("hasMiniOptions".equals(json.nextName())) {
                                                    Boolean hasMinOptions = json.nextBoolean();
                                                }
                                                if ("calls".equals(json.nextName())) {
                                                    json.beginArray();
                                                    while (json.hasNext()) {
                                                        result.calls.add(parseOption(json, symbol, OptionType.C));
                                                    }
                                                    json.endArray();
                                                }
                                                if ("puts".equals(json.nextName())) {
                                                    json.beginArray();
                                                    while (json.hasNext()) {
                                                        result.puts.add(parseOption(json, symbol, OptionType.P));
                                                    }
                                                    json.endArray();

                                                }
                                            }
                                            json.endObject();
                                        }
                                        json.endArray();
                                    }
                                    json.endObject();
                                }
                                json.endArray();
                            }
                        }
                        if ("error".equals(json.nextName())) {
                            if (JsonToken.NULL.equals(json.peek())) {
                                json.nextNull();
                            } else {
                                String value = json.nextString();
                            }
                        }
                    }
                    json.endObject();

                } catch (IOException e) {
                    Log.e(TAG, "Error reading json stream ", e);
                }
                return true;
            }
        });

        return result;
    }
    DateTime convertSecondsToDateTime(Long seconds, boolean addTimeZone) {
        if (seconds == null) {
            return null;
        }
        long msNoTimeZone = seconds * 1000;
        // optionDate seconds is in local timezone add timezoneOffset ot get UTCs
        // use option date because this date is in the future and may have different daylight savings offset then today.
        long msToConvert = msNoTimeZone;
        if (addTimeZone) {
            final long timezoneOffset = Math.abs(DateTimeZone.getDefault().getOffset(new DateTime(msNoTimeZone)));
            msToConvert = msNoTimeZone + timezoneOffset;
        }
        DateTime optionDateTime = new DateTime(msToConvert, DateTimeZone.getDefault());
        return optionDateTime;
    }

    private Option parseOption(JsonReader json, String symbol, OptionType putCall) throws IOException {
        Map<String, Object> optionMap = new HashMap<String, Object>();
        optionMap.putAll(jsonObjectToMap(json));


        Option option = new Option(symbol, putCall, getFormatRaw(optionMap.get("strike")), convertSecondsToDateTime(((Format)optionMap.get("expiration")).raw.longValue(), false));
        option.setBid(getFormatRaw(optionMap.get("bid")));
        option.setAsk(getFormatRaw(optionMap.get("ask")));

        return option;
    }

    Format parseFormat(JsonReader json) throws IOException {
        Format format = new Format();
        json.beginObject();
        while (json.hasNext()) {
            String name = json.nextName();
            if ("raw".equals(name)) {
                format.raw = json.nextDouble();
            } else if ("fmt".equals(name)) {
                format.fmt = json.nextString();
            } else if ("longFmt".equals(name)) {
                format.longFmt = json.nextString();
            }
        }
        json.endObject();
        return format;
    }

    public interface LineProcessor {
        // return true to continueLoop
        boolean process(String line, int lineNo, long startTime);
    }
    public interface JsonProcessor {
        // return true to continueLoop
        boolean process(JsonReader json, long startTime);
    }
    class URLJsonReader {
        boolean found = false;
        public boolean getFound() {
            return found;
        }
        public void setFound(boolean found) {
            this.found = found;
        }
        List<String> errors;
        URLJsonReader(List<String> errors) {
            this.errors = errors;
        }
        public int process(String url, JsonProcessor jsonProcessor) {
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
                        JsonReader json = new JsonReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        try {
                            jsonProcessor.process(json, start);
                            Log.d(TAG, "SCANNED  in ms " + (System.currentTimeMillis() - start));
                        } finally {
                            json.close();
                        }
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception in urlJsonReader "+url, e);
                errors.add("Net-5-"+e.getMessage());
            }

            return response;
        }
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
                errors.add("Net-4-"+e.getMessage());
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
