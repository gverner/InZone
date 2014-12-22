package com.codeworks.pai.db;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ServiceLogTable {
	
	public static final DateTimeFormatter timestampFormat = ISODateTimeFormat.dateTime();	
	public static final String	TABLE_SERVICE_LOG	= "service_log";
	public static final String	COLUMN_ID			= "_id";
	public static final String	COLUMN_TIMESTAMP	= "timestamp";
	public static final String	COLUMN_SERVICE_TYPE	= "service_type";
	public static final String	COLUMN_ITERATION	= "iteration";
	public static final String	COLUMN_MESSAGE		= "message";
	public static final String  COLUMN_RUNTIME      = "runtime";
	
	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_SERVICE_LOG
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_SERVICE_TYPE + " integer not null, " 
	      + COLUMN_TIMESTAMP + " text not null, "
	      + COLUMN_MESSAGE + " text not null, " 
	      + COLUMN_ITERATION + " integer null, "
	      + COLUMN_RUNTIME + " integer null "
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(ServiceLogTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_LOG);
	    onCreate(database);
	  }
	  

}
