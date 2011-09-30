package com.bitflippersanonymous.flippy.db;

import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class FlippyDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "applicationdata.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String TABLE_ENTRY = FlippyDatabaseAdapter.TABLE_ENTRY;
	private static final String TABLE_KEYWORDS = FlippyDatabaseAdapter.TABLE_KEYWORDS;
	private static final String KEY_ROWID = FlippyDatabaseAdapter.KEY_ROWID;
	
	private static final String [] CREATE_TABLES = { 
		"create table " + TABLE_ENTRY + " ("
		+ KEY_ROWID + " integer primary key autoincrement ",
		
		"create table " + TABLE_KEYWORDS + " ("
		+ KEY_ROWID + " integer primary key autoincrement, "
		+ Tags.keywords.name() + " text not null);",

		"create table " + TABLE_ENTRY+TABLE_KEYWORDS
		+ " (" + TABLE_ENTRY + " integer not null, "
		+ TABLE_KEYWORDS + " integer not null, "
		+ "PRIMARY KEY (" + TABLE_ENTRY + ", " + TABLE_KEYWORDS + ") );" };
	
	private static final String[] TABLES = { TABLE_ENTRY, TABLE_KEYWORDS, TABLE_ENTRY+TABLE_KEYWORDS };

	public FlippyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		for ( Tags tag : Tags.values() ) {
			if ( tag == Tags.keywords ) continue;
			CREATE_TABLES[0] += ", " + tag.name() + " text "; 
		}
		CREATE_TABLES[0] += ");";
		
		for ( String table : CREATE_TABLES )
			db.execSQL(table);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(getClass().getName(),	"Upgrading database from version " + oldVersion + " to "
			+ newVersion + ", which will destroy all old data");
		for ( String table : TABLES )
			db.execSQL("DROP TABLE IF EXISTS " + table);
		onCreate(db);
	}

}
