package com.codeworks.pai.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.Notice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StudyTable {
    static String TAG = StudyTable.class.getSimpleName();
    public static SimpleDateFormat priceDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US);
    public static SimpleDateFormat noticeDateFormat = new SimpleDateFormat("yyyyMMdd kk:mm", Locale.US);

    static {
        priceDateFormat.setTimeZone(TimeZone.getDefault());
        noticeDateFormat.setTimeZone(TimeZone.getDefault());
    }

	// Database table
	public static final String		TABLE_STUDY				= "study";
	public static final String		COLUMN_ID				= "_id";
	public static final String		COLUMN_PORTFOLIO_ID		= "portfolio_id";
	public static final String		COLUMN_SYMBOL			= "symbol";
	public static final String		COLUMN_NAME				= "name";
	public static final String		COLUMN_PRICE			= "price";
	public static final String		COLUMN_OPEN				= "open";
	public static final String		COLUMN_HIGH				= "high";
	public static final String		COLUMN_LOW				= "low";
	public static final String		COLUMN_LAST_CLOSE		= "last_close";
	public static final String		COLUMN_PRICE_DATE		= "price_date";
	public static final String		COLUMN_PRICE_LAST_WEEK	= "price_last_week";
	public static final String		COLUMN_PRICE_LAST_MONTH	= "price_last_month";
	public static final String		COLUMN_AVG_TRUE_RANGE	= "avg_true_range";
	public static final String      COLUMN_STOCHASTIC_K     = "stochastic_k";
	public static final String      COLUMN_STOCHASTIC_D     = "stochastic_d";
	public static final String		COLUMN_MA_TYPE			= "ma_type";
	public static final String		COLUMN_EMA_WEEK			= "ema_week";
	public static final String		COLUMN_EMA_MONTH		= "ema_month";
	public static final String		COLUMN_EMA_LAST_WEEK	= "ema_last_week";
	public static final String		COLUMN_EMA_LAST_MONTH	= "ema_last_month";
	public static final String		COLUMN_EMA_STDDEV_WEEK	= "ema_stddev_week";
	public static final String		COLUMN_EMA_STDDEV_MONTH	= "ema_stddev_month";
	public static final String		COLUMN_SMA_WEEK			= "sma_week";
	public static final String		COLUMN_SMA_MONTH		= "sma_month";
	public static final String		COLUMN_SMA_LAST_WEEK	= "sma_last_week";
	public static final String		COLUMN_SMA_LAST_MONTH	= "sma_last_month";
	public static final String		COLUMN_SMA_STDDEV_WEEK	= "sma_stddev_week";
	public static final String		COLUMN_SMA_STDDEV_MONTH	= "sma_stddev_month";
	public static final String		COLUMN_NOTICE			= "notice";
	public static final String		COLUMN_NOTICE_DATE		= "notice_date";
	public static final String		COLUMN_CONTRACTS		= "contracts";
	public static final String 		COLUMN_STATUSMAP        = "status_map";
    public static final String      COLUMN_EXT_MARKET_PRICE = "ext_market_price";
    public static final String      COLUMN_EXT_MARKET_DATE  = "ext_market_date";
	public static final String		COLUMN_DEMAND_ZONE		= "demand_zone";
	public static final String      COLUMN_PDL1             = "PDL1";
	public static final String      COLUMN_PDL2				= "PDL2";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_STUDY
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_PORTFOLIO_ID + " integer not null, "
	      + COLUMN_SYMBOL + " text not null, " 
	      + COLUMN_NAME + " text null, "
	      + COLUMN_PRICE + " real null, " 
	      + COLUMN_OPEN + " real null, "
	      + COLUMN_HIGH + " real null, "
	      + COLUMN_LOW + " real null, "
		  + COLUMN_LAST_CLOSE + " real null, "
	      + COLUMN_PRICE_DATE + " string null, "
	      + COLUMN_PRICE_LAST_WEEK + " real null, " 
	      + COLUMN_PRICE_LAST_MONTH + " real null, " 
	      + COLUMN_AVG_TRUE_RANGE + " real null, "
	      + COLUMN_STOCHASTIC_K + " real null, "
	      + COLUMN_STOCHASTIC_D + " real null, "
	      + COLUMN_MA_TYPE + " text null, " 
	      + COLUMN_EMA_WEEK + " real null, " 
	      + COLUMN_EMA_MONTH + " real null, "
	      + COLUMN_EMA_LAST_WEEK + " real null, " 
	      + COLUMN_EMA_LAST_MONTH + " real null, "
	      + COLUMN_EMA_STDDEV_WEEK + " real null, " 
	      + COLUMN_EMA_STDDEV_MONTH + " real null, "
	      + COLUMN_SMA_WEEK + " real null, " 
	      + COLUMN_SMA_MONTH + " real null, " 
	      + COLUMN_SMA_LAST_WEEK + " real null, "
	      + COLUMN_SMA_LAST_MONTH + " real null, "
	      + COLUMN_SMA_STDDEV_WEEK + " real null, "
	      + COLUMN_SMA_STDDEV_MONTH + " real null, "
	      + COLUMN_NOTICE + " integer null, "
	      + COLUMN_NOTICE_DATE + " text null, "
		  + COLUMN_CONTRACTS + " integer null, "
          + COLUMN_EXT_MARKET_PRICE + " real null, "
          + COLUMN_EXT_MARKET_DATE + " text null, "
	      + COLUMN_STATUSMAP + " integer null, "
		  + COLUMN_DEMAND_ZONE + " real null, "
		  + COLUMN_PDL1 + " real null, "
		  + COLUMN_PDL2 + " real null"
	      + ");";


	public static String[] getFullProjection() {
		String[] projection = new String[] { COLUMN_ID, COLUMN_PORTFOLIO_ID, COLUMN_SYMBOL, COLUMN_NAME, COLUMN_PRICE, COLUMN_OPEN, COLUMN_HIGH, COLUMN_LOW,
				COLUMN_LAST_CLOSE, COLUMN_PRICE_DATE, COLUMN_PRICE_LAST_WEEK, COLUMN_PRICE_LAST_MONTH, COLUMN_AVG_TRUE_RANGE, COLUMN_STOCHASTIC_K,
				COLUMN_STOCHASTIC_D, COLUMN_MA_TYPE, COLUMN_EMA_WEEK, COLUMN_EMA_MONTH, COLUMN_EMA_LAST_WEEK, COLUMN_EMA_LAST_MONTH, COLUMN_EMA_STDDEV_WEEK,
				COLUMN_EMA_STDDEV_MONTH, COLUMN_SMA_WEEK, COLUMN_SMA_MONTH, COLUMN_SMA_LAST_WEEK, COLUMN_SMA_LAST_MONTH, COLUMN_SMA_STDDEV_WEEK,
				COLUMN_SMA_STDDEV_MONTH, COLUMN_NOTICE, COLUMN_NOTICE_DATE, COLUMN_CONTRACTS, COLUMN_EXT_MARKET_PRICE, COLUMN_EXT_MARKET_DATE, COLUMN_STATUSMAP,
				COLUMN_DEMAND_ZONE, COLUMN_PDL1, COLUMN_PDL2};
		return projection;
	}
	
	public static String toPriceDateFormat(Date priceDate) {
		if (priceDate != null) {
			return priceDateFormat.format(priceDate);
		} else {
			return null;
		}
	}

	public static Date fromPriceDateFormat(String priceDate) throws ParseException {
		if (priceDate != null && priceDate.length() > 8) {
			return priceDateFormat.parse(priceDate);
		} else {
			return null;
		}
	}
	public static Study loadStudy(Cursor cursor)  {
		String symbol = cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SYMBOL));
		Study study = new Study(symbol);

		study.setSecurityId(cursor.getLong(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_ID)));
		study.setPortfolioId(cursor.getInt(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PORTFOLIO_ID)));
		study.setName(cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_NAME)));
		study.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PRICE)));
		study.setOpen(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_OPEN)));
		study.setHigh(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_HIGH)));
		study.setLow(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_LOW)));
		study.setLastClose(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_LAST_CLOSE)));
		try {
			study.setPriceDate(cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PRICE_DATE)));
		} catch (Exception e) {
			Log.d(TAG, "Parse Exception Price Date", e);
		}
		study.setPriceLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PRICE_LAST_WEEK)));
		study.setPriceLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PRICE_LAST_MONTH)));
		study.setAverageTrueRange(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_AVG_TRUE_RANGE)));
		study.setStochasticK(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_STOCHASTIC_K)));
		study.setStochasticD(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_STOCHASTIC_D)));
		study.setMaType(MaType.parse(cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_MA_TYPE))));
		study.setEmaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_WEEK)));
		study.setEmaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_MONTH)));
		study.setEmaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_LAST_WEEK)));
		study.setEmaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_LAST_MONTH)));
		study.setEmaStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_STDDEV_WEEK)));
		study.setEmaStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EMA_STDDEV_MONTH)));
		study.setSmaWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_WEEK)));
		study.setSmaMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_MONTH)));
		study.setSmaLastWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_LAST_WEEK)));
		study.setSmaLastMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_LAST_MONTH)));
		study.setSmaStddevWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_STDDEV_WEEK)));
		study.setSmaStddevMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_SMA_STDDEV_MONTH)));
		study.setContracts(cursor.getInt(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_CONTRACTS)));
		study.setNotice(Notice.fromIndex(cursor.getInt(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_NOTICE))));
		try {
			String noticeDateStr = cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_NOTICE_DATE));
			if (noticeDateStr != null) {
				study.setNoticeDate(noticeDateFormat.parse(noticeDateStr));
			}
		} catch (Exception e) {
			Log.d(TAG, "Parse Exception Notice Date", e);
		}
        study.setExtMarketPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EXT_MARKET_PRICE)));
        try {
            String extMarketDateStr = cursor.getString(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_EXT_MARKET_DATE));
            if (extMarketDateStr != null && extMarketDateStr.length() > 10) {
                study.setExtMarketDate(priceDateFormat.parse(extMarketDateStr));
            }
        } catch (Exception e) {
            Log.d(TAG, "Parse Exception Ext Market Date", e);
        }
		study.setStatusMap(cursor.getInt(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_STATUSMAP)));
		study.setDemandZone(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_DEMAND_ZONE)));
		study.setPdl1(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PDL1)));
		study.setPdl2(cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_PDL2)));
		return study;
	}

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            Log.w(StudyTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDY);
            onCreate(database);
        } else {
            switch (oldVersion) {
                case 7:
                    Log.w(StudyTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", adding columns ExtMarketPrice and ExtMarketDate");
                    database.execSQL("ALTER TABLE " + TABLE_STUDY + " ADD COLUMN " + COLUMN_EXT_MARKET_PRICE + " REAL NULL");
                    database.execSQL("ALTER TABLE " + TABLE_STUDY + " ADD COLUMN " + COLUMN_EXT_MARKET_DATE + " TEXT NULL");
                    // we want both updates, so no break statement here...
				case 8:
					Log.w(StudyTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", adding columns demandZone, pdl1 and pdl2");
					database.execSQL("ALTER TABLE " + TABLE_STUDY + " ADD COLUMN " + COLUMN_DEMAND_ZONE + " REAL NULL");
					database.execSQL("ALTER TABLE " + TABLE_STUDY + " ADD COLUMN " + COLUMN_PDL1 + " REAL NULL");
					database.execSQL("ALTER TABLE " + TABLE_STUDY + " ADD COLUMN " + COLUMN_PDL2 + " REAL NULL");
					// we want both updates, so no break statement here...
            }
        }
	}
}
