package com.codeworks.pai.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PriceHistoryTable {
	  // Database table
	  public static final String TABLE_PRICE_HISTORY = "pricehistory";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_SYMBOL = "symbol";
	  public static final String COLUMN_DATE = "date";
	  public static final String COLUMN_OPEN = "open";
	  public static final String COLUMN_HIGH = "high";
	  public static final String COLUMN_LOW = "low";
	  public static final String COLUMN_CLOSE = "close";
	  public static final String COLUMN_ADJUSTED_CLOSE = "adjustedclose";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_PRICE_HISTORY
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_SYMBOL + " text not null, " 
	      + COLUMN_DATE + " text not null, " 
	      + COLUMN_OPEN + " real not null, " 
	      + COLUMN_HIGH + " real not null, " 
	      + COLUMN_LOW + " real not null, " 
	      + COLUMN_CLOSE + " real not null, " 
	      + COLUMN_ADJUSTED_CLOSE + " real not null" 
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(PriceHistoryTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_HISTORY);
	    onCreate(database);
	  }
}
