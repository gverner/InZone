package com.codeworks.pai.mock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.processor.DataReader;

import org.joda.time.DateTime;

public class MockDataReader implements DataReader {
	public static final String	PRICE_CLOSE_DATE	= "04/12/2013";
	public static final String	PRICE_CLOSE_DATE2	= "04/15/2013";

	static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

	public static final double SPY_PRICE = 158.80d;
	public static final double QQQ_PRICE = 69.94d; // April 12 close, March 67.86d;
	public static final double GLD_PRICE = 143.95;
	public static final double UNG_PRICE = 23.10d;
	public static final double HYG_PRICE = 92.36d;

	@Override
	public boolean readCurrentPrice(Study security, List<String> errors) {
		if (TestDataLoader.SPY.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "S&P 500", SPY_PRICE,PRICE_CLOSE_DATE);
		} else if (TestDataLoader.QQQ.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "QQQ POWER SHARES", QQQ_PRICE,PRICE_CLOSE_DATE);
		} else if (TestDataLoader.GLD.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "Gold ETF", GLD_PRICE,PRICE_CLOSE_DATE);
		} else if (TestDataLoader.UNG.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "NAT GAS ETF", UNG_PRICE,PRICE_CLOSE_DATE);
		} else if (TestDataLoader.HYG.equalsIgnoreCase(security.getSymbol())) {
			buildSecurity(security, "High Yield Bond ETF", HYG_PRICE, "06/03/2013");
		} else {
			return false;
		}
		return true;
	}

	public static void buildSecurity(Study security, String name, double price, String date) {
		try {
			security.setPrice(price);
			security.setName(name);
			security.setPriceDate(sdf.parse(date));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<Price> readHistory(String symbol, List<String> errors) {
		List<Price> history = TestDataLoader.getTestHistory(symbol);
		return history;
	}

	@Override
	public boolean readRTPrice(Study security, List<String> errors) {
		return readCurrentPrice(security, errors);
	}

	@Override
	public Date latestHistoryDate(String symbol, List<String> errors) {
		List<Price> history = TestDataLoader.getTestHistory(symbol);
		Date latestDate = null;
		for (Price price : history) {
			if (latestDate == null || latestDate.before(price.getDate())) {
				latestDate = price.getDate();
			}
		}
		return latestDate;
	}

    @Override
    public List<DateTime> readOptionExpirations(String symbol, List<String> errors) {
        return null;
    }

    @Override
	public List<Option> readOptionPrice(final String symbol, final long expirationDate, List<String> errors) { return null; }

}

