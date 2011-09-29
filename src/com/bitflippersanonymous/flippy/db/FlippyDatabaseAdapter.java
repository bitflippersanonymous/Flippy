package com.bitflippersanonymous.flippy.db;

import java.util.ArrayList;

import com.bitflippersanonymous.flippy.domain.PlsEntry;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FlippyDatabaseAdapter {
	
	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String TABLE_ENTRY = "entry";
	public static final String TABLE_KEYWORDS = "keywords";

	
	private FlippyDatabaseHelper mDbHelper = null;

	public FlippyDatabaseAdapter(Context context) {
		mDbHelper = new FlippyDatabaseHelper(context);
	}

	public FlippyDatabaseAdapter recreate() throws SQLException {
		mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(), 1, 1); // Purge database on open
		return this;
	}

	public void close() {
		mDbHelper.close();
	}
	
	public long insertEntry(PlsEntry entry) {
		SQLiteDatabase database = mDbHelper.getWritableDatabase();
		ArrayList<Long> keywordIds = new ArrayList<Long>();

		ContentValues values = new ContentValues();
		for ( String keyword : entry.get(Tags.keywords).split(", ") ) {
			long keywordId = lookupKeyword(keyword);
			if ( keywordId == -1 ) {
				values.clear();
				values.put(Tags.keywords.name(), keyword);
				keywordId = database.insert(TABLE_KEYWORDS, null, values);
			}
			
			if ( keywordId != -1 )
				keywordIds.add(keywordId);
		}
		
		values = createEntryContentValues(entry);
		long idEntry = database.insert(TABLE_ENTRY, null, values);
		
		for ( long keywordId : keywordIds ) {
			values.clear();
			values.put(TABLE_ENTRY, idEntry);
			values.put(TABLE_KEYWORDS, keywordId);
			database.insert(TABLE_ENTRY+TABLE_KEYWORDS, null, values);
		}
				
		return idEntry;
	}
	
	//Will escape keyword.  Also could use DatabaseUtils.sqlEscapeString()
	public long lookupKeyword(String keyword) throws SQLException {
		long id = -1;
		keyword = DatabaseUtils.sqlEscapeString(keyword);
		Cursor cursor = mDbHelper.getReadableDatabase().query(true, TABLE_KEYWORDS, 
				new String[] { KEY_ROWID },
				Tags.keywords.name() + "='" + keyword + "'", null, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
			id = cursor.getLong(0);
		}
		return id;
	}
	
	public Cursor fetchEntry(long rowId) throws SQLException {
		Cursor cursor = mDbHelper.getReadableDatabase().query(true, TABLE_ENTRY, new String[] {
				KEY_ROWID, Tags.title.name(), Tags.description.name() },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	private ContentValues createEntryContentValues(PlsEntry entry) {
		ContentValues values = new ContentValues();
		values.put(Tags.title.name(), entry.get(Tags.title));
		values.put(Tags.description.name(), entry.get(Tags.description));
		return values;
	}
}
