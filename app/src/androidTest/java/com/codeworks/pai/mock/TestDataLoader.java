package com.codeworks.pai.mock;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import au.com.bytecode.opencsv.CSVReader;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
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
