package com.codeworks.pai.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.codeworks.pai.db.PaiDatabaseHelper;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.processor.InZoneDateUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PaiContentProvider extends ContentProvider {
    static final String TAG = PaiContentProvider.class.getSimpleName();
    // database
    private PaiDatabaseHelper database;

    // Used for the UriMacher
    static final int PRICE_HISTORY = 10;
    static final int PRICE_HISTORY_ID = 20;
    static final int PRICE_HISTORY_MAX_DATE = 21;

	static final int PAI_STUDY = 50;
	static final int PAI_STUDY_ID = 60;

	static final int SERVICE_LOG = 70;
	static final int SERVICE_LOG_ID = 71;
	
	public static final String AUTHORITY = "com.codeworks.pai.contentprovider";

	private static final String SECURITY_PATH = "security";
	private static final String PRICE_HISTORY_PATH = "price_history";
	private static final String PAI_STUDY_PATH = "pai_study";
	private static final String SERVICE_LOG_PATH = "service_log";

	public static final Uri SECURITY_URI = Uri.parse("content://" + AUTHORITY + "/" + SECURITY_PATH);
	public static final Uri PRICE_HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/" + PRICE_HISTORY_PATH);
	public static final Uri PAI_STUDY_URI = Uri.parse("content://" + AUTHORITY + "/" + PAI_STUDY_PATH);
	public static final Uri SERVICE_LOG_URI = Uri.parse("content://" + AUTHORITY + "/" + SERVICE_LOG_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/pai_table";

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/pai_table_item";

	public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH, PRICE_HISTORY);
		sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH + "/#", PRICE_HISTORY_ID);
		// sURIMatcher.addURI(AUTHORITY, PRICE_HISTORY_PATH + "/",
		// PRICE_HISTORY_MAX_DATE);

		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH, PAI_STUDY);
		sURIMatcher.addURI(AUTHORITY, PAI_STUDY_PATH + "/#", PAI_STUDY_ID);

		sURIMatcher.addURI(AUTHORITY, SERVICE_LOG_PATH, SERVICE_LOG);
		sURIMatcher.addURI(AUTHORITY, SERVICE_LOG_PATH + "/#", SERVICE_LOG_ID);

	}

	@Override
	public boolean onCreate() {
		database = new PaiDatabaseHelper(getContext());
		return false;
	}

	@Override
	public void shutdown() {
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		sqlDB.close();
		database.close();
		super.shutdown();
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PRICE_HISTORY:
			// Set the table
			queryBuilder.setTables(PriceHistoryTable.TABLE_PRICE_HISTORY);
			break;
		case PRICE_HISTORY_ID:
			queryBuilder.setTables(PriceHistoryTable.TABLE_PRICE_HISTORY);
			// Adding the ID to the original query
			queryBuilder.appendWhere(PriceHistoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		case PRICE_HISTORY_MAX_DATE:
			break;
		case PAI_STUDY:
			queryBuilder.setTables(StudyTable.TABLE_STUDY);
			break;
		case PAI_STUDY_ID:
			queryBuilder.setTables(StudyTable.TABLE_STUDY);
			// Adding the ID to the original query
			queryBuilder.appendWhere(StudyTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		case SERVICE_LOG:
			queryBuilder.setTables(ServiceLogTable.TABLE_SERVICE_LOG);
			break;
		case SERVICE_LOG_ID:
			queryBuilder.setTables(ServiceLogTable.TABLE_SERVICE_LOG);
			// Adding the ID to the original query
			queryBuilder.appendWhere(ServiceLogTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case PRICE_HISTORY:
			id = sqlDB.insert(PriceHistoryTable.TABLE_PRICE_HISTORY, null, values);
			break;
		case PAI_STUDY:
			id = sqlDB.insert(StudyTable.TABLE_STUDY, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case SERVICE_LOG:
			id = sqlDB.insert(ServiceLogTable.TABLE_SERVICE_LOG, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		return ContentUris.withAppendedId(uri, id);
	}
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values){
	    int numInserted = 0;
	    String table;

	    int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case PRICE_HISTORY:
			table = PriceHistoryTable.TABLE_PRICE_HISTORY;
			break;
		case PAI_STUDY:
			table = StudyTable.TABLE_STUDY;
			break;
		case SERVICE_LOG:
			table = ServiceLogTable.TABLE_SERVICE_LOG;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    sqlDB.beginTransaction();
	    try {
	        for (ContentValues cv : values) {
	            long newID = sqlDB.insertOrThrow(table, null, cv);
	            if (newID <= 0) {
	                throw new SQLException("Failed to insert row into " + uri);
	            }
	        }
	        sqlDB.setTransactionSuccessful();
	        getContext().getContentResolver().notifyChange(uri, null);
	        numInserted = values.length;
	    } finally {         
	        sqlDB.endTransaction();
	    }
	    return numInserted;
	}
	
	public void batchInsertHistory(List<Price> historyList, String symbol) {
		PaiDatabaseHelper database = new PaiDatabaseHelper(getContext());
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		sqlDB.beginTransaction();
		try {
			for (Price price : historyList) {
				ContentValues values = new ContentValues();
				values.put(PriceHistoryTable.COLUMN_ADJUSTED_CLOSE, price.getAdjustedClose());
				values.put(PriceHistoryTable.COLUMN_CLOSE, price.getClose());
				if (price.getDate() != null) {
					values.put(PriceHistoryTable.COLUMN_DATE, InZoneDateUtils.toDatabaseFormat(price.getDate()));
				}
				values.put(PriceHistoryTable.COLUMN_HIGH, price.getHigh());
				values.put(PriceHistoryTable.COLUMN_LOW, price.getLow());
				values.put(PriceHistoryTable.COLUMN_OPEN, price.getOpen());
				values.put(PriceHistoryTable.COLUMN_SYMBOL, symbol);
				sqlDB.insert(PriceHistoryTable.TABLE_PRICE_HISTORY, null, values);
				sqlDB.setTransactionSuccessful();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception in Batch Insert History ", e);
		} finally {
			sqlDB.endTransaction();
		}
		sqlDB.close();
		database.close();
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case PRICE_HISTORY:
			rowsDeleted = sqlDB.delete(PriceHistoryTable.TABLE_PRICE_HISTORY, selection, selectionArgs);
			break;
		case PRICE_HISTORY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(PriceHistoryTable.TABLE_PRICE_HISTORY, PriceHistoryTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(PriceHistoryTable.TABLE_PRICE_HISTORY, PriceHistoryTable.COLUMN_ID + "=" + id + " and "
						+ selection, selectionArgs);
			}
			break;
		case PAI_STUDY:
			rowsDeleted = sqlDB.delete(StudyTable.TABLE_STUDY, selection, selectionArgs);
			break;
		case PAI_STUDY_ID:
			String quoteId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(StudyTable.TABLE_STUDY, StudyTable.COLUMN_ID + "=" + quoteId, null);
			} else {
				rowsDeleted = sqlDB.delete(StudyTable.TABLE_STUDY, StudyTable.COLUMN_ID + "=" + quoteId + " and " + selection,
						selectionArgs);
			}
			break;
		case SERVICE_LOG:
			rowsDeleted = sqlDB.delete(ServiceLogTable.TABLE_SERVICE_LOG, selection, selectionArgs);
			break;
		case SERVICE_LOG_ID:
			String serviceLogId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(ServiceLogTable.TABLE_SERVICE_LOG, ServiceLogTable.COLUMN_ID + "=" + serviceLogId, null);
			} else {
				rowsDeleted = sqlDB.delete(ServiceLogTable.TABLE_SERVICE_LOG, ServiceLogTable.COLUMN_ID + "=" + serviceLogId + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case PRICE_HISTORY:
			rowsUpdated = sqlDB.update(PriceHistoryTable.TABLE_PRICE_HISTORY, values, selection, selectionArgs);
			break;
		case PRICE_HISTORY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(PriceHistoryTable.TABLE_PRICE_HISTORY, values, PriceHistoryTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(PriceHistoryTable.TABLE_PRICE_HISTORY, values, PriceHistoryTable.COLUMN_ID + "=" + id + " and "
						+ selection, selectionArgs);
			}
			break;
		case PAI_STUDY:
			rowsUpdated = sqlDB.update(StudyTable.TABLE_STUDY, values, selection, selectionArgs);
			break;
		case PAI_STUDY_ID:
			String quoteId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(StudyTable.TABLE_STUDY, values, StudyTable.COLUMN_ID + "=" + quoteId, null);
			} else {
				rowsUpdated = sqlDB.update(StudyTable.TABLE_STUDY, values,
						StudyTable.COLUMN_ID + "=" + quoteId + " and " + selection, selectionArgs);
			}
			break;
		case SERVICE_LOG:
			rowsUpdated = sqlDB.update(ServiceLogTable.TABLE_SERVICE_LOG, values, selection, selectionArgs);
			break;
		case SERVICE_LOG_ID:
			String serviceLogId = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ServiceLogTable.TABLE_SERVICE_LOG, values, ServiceLogTable.COLUMN_ID + "=" + serviceLogId, null);
			} else {
				rowsUpdated = sqlDB.update(ServiceLogTable.TABLE_SERVICE_LOG, values,
						ServiceLogTable.COLUMN_ID + "=" + serviceLogId + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { PriceHistoryTable.COLUMN_SYMBOL, PriceHistoryTable.COLUMN_DATE, PriceHistoryTable.COLUMN_ADJUSTED_CLOSE,
				PriceHistoryTable.COLUMN_HIGH, PriceHistoryTable.COLUMN_LOW, PriceHistoryTable.COLUMN_OPEN, PriceHistoryTable.COLUMN_CLOSE,
				PriceHistoryTable.COLUMN_ID, 
				/*
				PaiStudyTable.COLUMN_SYMBOL, PaiStudyTable.COLUMN_NAME, PaiStudyTable.COLUMN_MA_TYPE,
				PaiStudyTable.COLUMN_MA_WEEK, PaiStudyTable.COLUMN_MA_MONTH, PaiStudyTable.COLUMN_MA_LAST_WEEK, PaiStudyTable.COLUMN_MA_LAST_MONTH,
				PaiStudyTable.COLUMN_PRICE, PaiStudyTable.COLUMN_OPEN, PaiStudyTable.COLUMN_HIGH, PaiStudyTable.COLUMN_LOW, PaiStudyTable.COLUMN_PRICE_DATE,
				PaiStudyTable.COLUMN_PRICE_LAST_WEEK, PaiStudyTable.COLUMN_PRICE_LAST_MONTH, PaiStudyTable.COLUMN_STDDEV_WEEK,
				PaiStudyTable.COLUMN_STDDEV_MONTH, PaiStudyTable.COLUMN_AVG_TRUE_RANGE, PaiStudyTable.COLUMN_NOTICE, PaiStudyTable.COLUMN_NOTICE_DATE,
				PaiStudyTable.COLUMN_SMA_MONTH, PaiStudyTable.COLUMN_SMA_LAST_MONTH, PaiStudyTable.COLUMN_SMA_STDDEV_MONTH,
				PaiStudyTable.COLUMN_PORTFOLIO_ID,
				PaiStudyTable.COLUMN_SMA_WEEK,PaiStudyTable.COLUMN_SMA_LAST_WEEK,PaiStudyTable.COLUMN_SMA_STDDEV_WEEK,PaiStudyTable.COLUMN_LAST_CLOSE,PaiStudyTable.COLUMN_CONTRACTS, PaiStudyTable.COLUMN_STATUSMAP,
				*/
				ServiceLogTable.COLUMN_ID, ServiceLogTable.COLUMN_ITERATION, ServiceLogTable.COLUMN_MESSAGE, ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceLogTable.COLUMN_TIMESTAMP, ServiceLogTable.COLUMN_RUNTIME
};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			availableColumns.addAll(Arrays.asList(StudyTable.getFullProjection()));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

}
