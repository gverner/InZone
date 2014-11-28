package com.codeworks.pai.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PaiDatabaseHelper extends SQLiteOpenHelper {

	private static final String	DATABASE_NAME		= "inzone.db";
	private static final int	DATABASE_VERSION	= 7;

	public PaiDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		PriceHistoryTable.onCreate(database);
		StudyTable.onCreate(database);
		ServiceLogTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		PriceHistoryTable.onUpgrade(database, oldVersion, newVersion);
		StudyTable.onUpgrade(database, oldVersion, newVersion);
		ServiceLogTable.onUpgrade(database, oldVersion, newVersion);
	}
}