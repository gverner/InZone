package com.codeworks.pai.contentprovider;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.ServiceType;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.processor.ProcessorImpl;

public class PaiContentProviderTest extends ProviderTestCase2<PaiContentProvider> {

	public PaiContentProviderTest() {
		super(PaiContentProvider.class, PaiContentProvider.AUTHORITY);
	}

	private static final String TAG = PaiContentProviderTest.class.getSimpleName();


	private static final String SECURITY_PATH = "security";
	private static final String PRICE_HISTORY_PATH = "price_history";
	private static final String PAI_STUDY_PATH = "pai_study";

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/pai_table";

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/pai_table_item";
/*
	static final int PRICE_HISTORY = 10;
	static final int PRICE_HISTORY_ID = 20;
	static final int SETTINGS = 30;
	static final int SETTINGS_ID = 40;
	static final int PAI_STUDY = 50;
	static final int PAI_STUDY_ID = 60;
	
	public static final Uri SETTINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + SECURITY_PATH);
	public static final Uri PRICE_HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/" + PRICE_HISTORY_PATH);
	public static final Uri PAI_STUDY_URI = Uri.parse("content://" + AUTHORITY + "/" + PAI_STUDY_PATH);


	private static final String AUTHORITY = "com.codeworks.pai.contentprovider";
	public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH, PRICE_HISTORY);
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH + "/#", PRICE_HISTORY_ID);
		sURIMatcher.addURI(AUTHORITY, SECURITY_PATH, SETTINGS);
		sURIMatcher.addURI(AUTHORITY, SECURITY_PATH + "/#", SETTINGS_ID);
		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH, PAI_STUDY);
		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH + "/#", PAI_STUDY_ID);
	}
*/
	public void testUriMatcher() {
		assertEquals(PaiContentProvider.PRICE_HISTORY, PaiContentProvider.sURIMatcher.match(PaiContentProvider.PRICE_HISTORY_URI));
		assertEquals(PaiContentProvider.PAI_STUDY, PaiContentProvider.sURIMatcher.match(PaiContentProvider.PAI_STUDY_URI));
		assertEquals(PaiContentProvider.SERVICE_LOG, PaiContentProvider.sURIMatcher.match(PaiContentProvider.SERVICE_LOG_URI) );
	}

	public void loadHistory() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		for (Price price : history) {
			ContentValues values = new ContentValues();
			values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
			values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
			values.put(PriceHistoryTable.COLUMN_DATE, ProcessorImpl.dbStringDateFormat.format(price.getDate()));
			values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
			values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
			values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
			values.put(PriceHistoryTable.COLUMN_SYMBOL, "SPY");
			getMockContentResolver().insert(PaiContentProvider.PRICE_HISTORY_URI, values);
		}
	}

	public void testHistoryQuery() throws IOException {
		loadHistory();
		String[] projection = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_CLOSE, PriceHistoryTable.COLUMN_DATE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN,
				PriceHistoryTable.COLUMN_ADJUSTED_CLOSE };
		String selection = PriceHistoryTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { "SPY" };
		Cursor historyCursor = getMockContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, projection, selection, selectionArgs,
				PriceHistoryTable.COLUMN_DATE);
		try {
			boolean rowResult = historyCursor.moveToFirst();

			while (rowResult) {
				Log.d(TAG,
						historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_SYMBOL)) + " "
								+ historyCursor.getString(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_DATE)) + " "
								+ historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_OPEN)) + " "
								+ historyCursor.getDouble(historyCursor.getColumnIndexOrThrow(PriceHistoryTable.COLUMN_CLOSE)));
				rowResult = historyCursor.moveToNext();
			}
			System.out.println("history rowCount=" + historyCursor.getCount());
		} finally {
			historyCursor.close();
		}
	}


	public void loadServiceLog() throws IOException {
		for (int ndx=0; ndx < 4; ndx++) {
			ContentValues values = new ContentValues();
			values.put(ServiceLogTable.COLUMN_ITERATION, ndx);
			ServiceType en;
			values.put(ServiceLogTable.COLUMN_MESSAGE,"Test Message "+ ndx);
			values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.fromIndex(ndx % 2).getIndex());
			values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
			values.put(ServiceLogTable.COLUMN_RUNTIME, ndx);
			getMockContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
		}
	}

	public void testServiceLogQuery() throws IOException {
		loadServiceLog();
		String[] projection = { ServiceLogTable.COLUMN_ID, ServiceLogTable.COLUMN_ITERATION, ServiceLogTable.COLUMN_MESSAGE,
				ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceLogTable.COLUMN_TIMESTAMP, ServiceLogTable.COLUMN_RUNTIME };
		String selection = PriceHistoryTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { };
		Cursor cursor = getMockContentResolver().query(PaiContentProvider.SERVICE_LOG_URI, projection, null, null,
				ServiceLogTable.COLUMN_TIMESTAMP);
		try {
			boolean rowResult = cursor.moveToFirst();
			int ndx = 0;
			while (rowResult) {
				Log.d(TAG,
						cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_SERVICE_TYPE)) + " "
								+ cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_ITERATION)) + " "
								+ cursor.getString(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_MESSAGE)) + " "
								+ cursor.getString(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_TIMESTAMP)));
				assertEquals(ndx, cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_ITERATION)));
				assertEquals("Test Message "+ndx, cursor.getString(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_MESSAGE)));
				assertEquals(ndx % 2, cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_SERVICE_TYPE)));
				assertEquals(ndx, cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_RUNTIME)));
				rowResult = cursor.moveToNext();
				ndx++;
			}
			System.out.println("rowCount=" + cursor.getCount());
		} finally {
			cursor.close();
		}
	}


	public Uri loadSettings() throws IOException {
			ContentValues values = new ContentValues();
			values.put(StudyTable.COLUMN_SYMBOL, "SPY");
			values.put(StudyTable.COLUMN_MA_TYPE, "E");
			values.put(StudyTable.COLUMN_PRICE, 1.1d);
			values.put(StudyTable.COLUMN_NAME, "S&P");
			values.put(StudyTable.COLUMN_PORTFOLIO_ID, 1L);
			return getMockContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
	}

	public void testSettings() throws IOException {
		Uri settingsUri = loadSettings();
		String[] projection = { StudyTable.COLUMN_SYMBOL, StudyTable.COLUMN_MA_TYPE, StudyTable.COLUMN_PRICE,
				StudyTable.COLUMN_NAME };

		Cursor cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		try {
			assertEquals("one row", 1, cursor.getCount());
			boolean rowResult = cursor.moveToFirst();
			assertEquals("moveToFirst", true, rowResult);
			assertEquals("SPY", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_SYMBOL)));
			assertEquals("E", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_MA_TYPE)));
			assertEquals(1.1d, cursor.getDouble(cursor.getColumnIndex(StudyTable.COLUMN_PRICE)));
			assertEquals("S&P", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_NAME)));
		} finally {
			cursor.close();
		}
		ContentValues values = new ContentValues();
		values.put(StudyTable.COLUMN_SYMBOL, "QQQ");
		values.put(StudyTable.COLUMN_MA_TYPE, "S");
		values.put(StudyTable.COLUMN_PRICE, 2.2d);
		values.put(StudyTable.COLUMN_NAME, "THE Qs");
		assertEquals("Rows Updated", 1, getMockContentResolver().update(settingsUri, values, null, null));
		
		cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		try {
			assertEquals("one row", 1, cursor.getCount());
			boolean rowResult = cursor.moveToFirst();
			assertEquals("moveToFirst", true, rowResult);
			assertEquals("QQQ", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_SYMBOL)));
			assertEquals("S", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_MA_TYPE)));
			assertEquals(2.2d, cursor.getDouble(cursor.getColumnIndex(StudyTable.COLUMN_PRICE)));
			assertEquals("THE Qs", cursor.getString(cursor.getColumnIndex(StudyTable.COLUMN_NAME)));
		} finally {
			cursor.close();
		}
		getMockContentResolver().delete(settingsUri, null, null);
		cursor = getMockContentResolver().query(settingsUri, projection, null, null ,null);
		assertEquals("zero row", 0, cursor.getCount());
	}
	
}
