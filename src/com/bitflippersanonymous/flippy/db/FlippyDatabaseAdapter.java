package com.bitflippersanonymous.flippy.db;

import java.util.ArrayList;
import java.util.HashMap;

import com.bitflippersanonymous.flippy.domain.PlsEntry;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;
import com.bitflippersanonymous.flippy.util.Util;

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
	private HashMap<String, Long> mKeywordIds = null;

	public FlippyDatabaseAdapter(Context context) {
		mDbHelper = new FlippyDatabaseHelper(context);
	}

	public FlippyDatabaseAdapter recreate() throws SQLException {
		mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(), 1, 1); // Purge database on open
		return this;
	}

	public void close() {
		if ( mDbHelper != null )
		mDbHelper.close();
		mDbHelper = null;
	}
	
	public long insertEntry(PlsEntry entry) {
		SQLiteDatabase database = mDbHelper.getWritableDatabase();
		ArrayList<Long> keywordIds = new ArrayList<Long>();

		for ( String keyword : entry.get(Tags.keywords).split(", ") ) {
			long keywordId = lookupKeyword(keyword);
			if ( keywordId != -1 )
				keywordIds.add(keywordId);
		}
		
		ContentValues values = createEntryContentValues(entry);
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
		if ( mKeywordIds == null )
			mKeywordIds = new HashMap<String, Long>();

		keyword = DatabaseUtils.sqlEscapeString(keyword.toLowerCase());
		Long iId = mKeywordIds.get(keyword);
		if ( iId != null )
			return iId.longValue();
		
		Cursor cursor = mDbHelper.getReadableDatabase().query(true, TABLE_KEYWORDS, 
				new String[] { KEY_ROWID },
				Tags.keywords.name() + "=?", new String[] {keyword}, null, null, null, null);
		
		if ( cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			id = cursor.getLong(0);
		} else {
			ContentValues values = new ContentValues();
			values.put(Tags.keywords.name(), keyword);
			id = mDbHelper.getWritableDatabase().insert(TABLE_KEYWORDS, null, values);
		}
		cursor.close();
		mKeywordIds.put(keyword, id);
		return id;
	}
	
	public Cursor fetchEntry(long rowId, int offset) throws SQLException {
		final String dir = offset > 0 ? ">" : offset < 0 ? "<" : "=";
		Cursor cursor = mDbHelper.getReadableDatabase().query(true, TABLE_ENTRY, 
				null,
				KEY_ROWID + dir + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	private ContentValues createEntryContentValues(PlsEntry entry) {
		ContentValues values = new ContentValues();
		for ( Tags tag : Tags.values() ) {
			if ( tag == Tags.keywords ) continue;
			values.put(tag.name(), entry.get(tag));
		}
		return values;
	}

	public Cursor fetchQueue() throws SQLException {
		Cursor cursor = mDbHelper.getReadableDatabase().query(true, TABLE_ENTRY,
				null, Util.QUEUE + "=1", null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

}
