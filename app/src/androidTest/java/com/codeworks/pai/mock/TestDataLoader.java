package com.codeworks.pai.mock;

import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import au.com.bytecode.opencsv.CSVReader;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.processor.DataReaderYahoo;
import com.codeworks.pai.study.GrouperTest;

public class TestDataLoader {
	public static String SPY = "SPY";
	public static String SPY2 = "SPY2";
	public static String SPY3 = "SPY3";
	public static String SPY_FRIDAY_CLOSE = "SPY_FRIDAY_CLOSE";
	public static String QQQ = "QQQ";
	public static String GLD = "GLD";
	public static String UNG = "UNG";
	public static String HYG = "HYG";
	public static String J_XOP = "XOP";
	public static String TAG = "TestDataLoader";

	public static List<Price> getTestHistory(String symbol) {
		
		String[] securities = new String[] {SPY, SPY2, SPY3, SPY_FRIDAY_CLOSE, QQQ, GLD, UNG, HYG};
		boolean symbolFound = false;
		for (String security : securities) {
			if (security.equals(symbol)) {
				symbolFound = true;
			}
		}
		if (!symbolFound) {
			throw new IllegalArgumentException("Unsupported symbol "+symbol);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		List<Price> history = new ArrayList<Price>();
//		String filename = "/com/codeworks/pai/mock/" + symbol + "_history.csv";
        String filename = "/assets/" + symbol + "_history.csv";
        filename = filename.toLowerCase(Locale.US);
		InputStream url = TestDataLoader.class.getResourceAsStream(filename);
		if (url == null) {
			throw new IllegalArgumentException(filename+" history data not found");
		}
		Reader streamReader = new java.io.InputStreamReader(url);
		CSVReader reader = new CSVReader(streamReader);
		try {
			try {
				List<String[]> lines = reader.readAll();
				for (String[] line : lines)
					try {
						if (!"Date".equals(line[0])) { // skip header
							Price price = new Price();
							price.setDate(sdf.parse(line[0]));
							price.setOpen(Double.parseDouble(line[1]));
							price.setHigh(Double.parseDouble(line[2]));
							price.setLow(Double.parseDouble(line[3]));
							price.setClose(Double.parseDouble(line[4]));
							price.setAdjustedClose(Double.parseDouble(line[6]));
							if (price.valid()) {
								history.add(price);
							}
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}

				return history;
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Price> getJsonHistory(String symbol, int years, List<String> errors, final Map<String, Object> info, String dayofweek) {

		String[] securities = new String[]{J_XOP};
		boolean symbolFound = false;
		for (String security : securities) {
			if (security.equals(symbol)) {
				symbolFound = true;
			}
		}
		if (!symbolFound) {
			throw new IllegalArgumentException("Unsupported symbol " + symbol);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		final List<Price> history = new ArrayList<Price>();
//		String filename = "/com/codeworks/pai/mock/" + symbol + "_history.csv";
		String filename = "/assets/" + symbol + "_history"+years+"y"+dayofweek+".json";
		filename = filename.toLowerCase(Locale.US);
		List<String[]> results;
		try {
			URLJsonReader reader = new URLJsonReader(errors);
			reader.process(filename, new DataReaderYahoo.JsonProcessor() {
				@Override
				public boolean process(JsonReader json, long startTime) {
					try {
						boolean isStitched = false;
						json.beginArray();
						json.beginObject();
						while (json.hasNext()) {
							String name = json.nextName();
							if ("AfterHoursSeries".equals(name)) {
								json.skipValue();
							} else if ("Series".equals(name)) {
								json.beginArray();
								while (json.hasNext()) {
									json.beginObject();
									Price price = new Price();
									history.add(price);
									while (json.hasNext()) {
										String name2 = json.nextName();
										if ("Op".equals(name2)) {
											price.setOpen(json.nextDouble());
										} else if ("Hp".equals(name2)) {
											price.setHigh(json.nextDouble());
										} else if ("Lp".equals(name2)) {
											price.setLow(json.nextDouble());
										} else if ("P".equals(name2)) {
											price.setClose(json.nextDouble());
										} else if ("T".equals(name2)) {
											// temp storage of date offset
											price.setAdjustedClose(json.nextInt());
										} else if ("IsStitched".equals(name2)) {
											isStitched = json.nextBoolean();
										} else if ("V".equals(name2)) {
											json.skipValue();
										}
									}
									json.endObject();
								}
								json.endArray();
							} else if ("utcFullRunTime".equals(name)) {
								String strDate = json.nextString();
								Long ms = DataReaderYahoo.extractMs(strDate);
								info.put("utcFullRunTime", ms);
								Log.d(TAG, strDate + " " + ms + " " + new Date(ms));
							} else {
								json.skipValue();
							}
						}
						json.endObject();
						json.endArray();
						info.put("IsStitched", isStitched);
					} catch (IOException e) {
						Log.e(TAG, "Error reading json stream ", e);
					}
					return true;
				}
			});


		} catch (Exception e) {
			Log.d(TAG, "readHistory " + e.getMessage(), e);
			errors.add("2-" + e.getMessage());
		}

		return history;
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

		public int process(String url, DataReaderYahoo.JsonProcessor jsonProcessor) {
			int response = 0;
			long start = System.currentTimeMillis();

			try {
				InputStream br = TestDataLoader.class.getResourceAsStream(url);

				try {
						JsonReader json = new JsonReader(new InputStreamReader(br));
						try {
							jsonProcessor.process(json, start);
							Log.d(TAG, "SCANNED  in ms " + (System.currentTimeMillis() - start));
						} finally {
							json.close();
						}
				} finally {
					br.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Exception in urlJsonReader " + url, e);
				errors.add("Net-5-" + e.getMessage());
			}

			return response;
		}
	}

	public static List<Price> generateHistory(double startPrice, double endPrice, int days) throws ParseException {
		double diff = endPrice - startPrice;
		double perday = PaiUtils.round(diff / days);
		List<Price> history = new ArrayList<Price>();
		DateTime dt = new DateTime(2013, 06, 10, 0, 0);

		System.out.println("Last Date " + dt.toString());
		double close = endPrice;
		do {
			dt = dt.minusDays(1);
			while (dt.getDayOfWeek() == DateTimeConstants.SUNDAY || dt.getDayOfWeek() == DateTimeConstants.SATURDAY) {
				dt = dt.minusDays(1);
			}
			history.add(buildPrice(dt.toDate(), close));
			close = PaiUtils.round(close - perday);
		} while (history.size() <= days);

		Collections.sort(history);
		return history;
	}
	
	public static Price buildPrice(Date date, double close) {
		Price price = new Price();
		price.setDate(date);
		price.setOpen((close));
		price.setHigh((close));
		price.setLow((close));
		price.setClose((close));
		price.setAdjustedClose((close));
		return price;
	}
}
