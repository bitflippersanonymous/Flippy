package com.bitflippersanonymous.flippy.domain;

import com.bitflippersanonymous.flippy.util.Util;

import java.sql.Time;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.DateFormat;

public class PlsEntry /*implements Parcelable*/ {
	
	public static final String PLSENTRY = Util.PACKAGE + ".PLSENTRY";
	public enum Tags { title, verses, description, enclosure, author, pubDate, keywords }
    
	private int mId;
	private final HashMap<Tags, String> mData;
	private boolean mInQueue;
	public PlsEntry(HashMap<Tags, String> data) {
		mData = data;
		if ( mData.containsKey(Tags.title) ) {
			String bybar[] = get(Tags.title).split("[|]");
			String byphen[] = bybar[0].split(" - ", -1);
			mData.put(Tags.title, byphen[0]);
			mData.put(Tags.verses, byphen[1]);
		}
	}
	
	public PlsEntry(HashMap<Tags, String> data, int id, boolean queue) {
		mId = id;
		mData = data;
		mInQueue = queue;
	}

	public int getId() {
		return mId;
	}
	
	public boolean getInQueue() {
		return mInQueue;
	}
	
	public void setInQueue(boolean inQueue) {
		mInQueue = inQueue;
	}
	
	public String get(Tags tag) {
		return mData.get(tag);
	}
	
	// Into DB
	public ContentValues createEntryContentValues() {
		ContentValues values = new ContentValues();
		for ( Tags tag : Tags.values() ) {
			if ( tag == Tags.keywords ) continue;
			if ( tag == Tags.pubDate )
				values.put(tag.name(), Time.parse(get(tag)));
			else
				values.put(tag.name(), get(tag));
		}
		return values;
	}
	
	// Outof DB
	public static PlsEntry cursorToEntry(Cursor cursor) {
		if ( cursor.getCount() == 0 )
			return null;
		
		boolean queue = false;
		int id = cursor.getInt(0);
		HashMap<Tags, String> data = new HashMap<Tags, String>();
		String[] colNames = cursor.getColumnNames();
		for ( int i = 0; i< cursor.getColumnCount(); i++ ) {
			Tags tag = null;
			try { 
				if ( colNames[i].equals(Util.QUEUE) ) {
					queue = cursor.getInt(i)>0;
				} else {
					tag = Tags.valueOf(colNames[i]);
					data.put(tag, cursor.getString(i));
				}
			} catch(IllegalArgumentException ex) { }
		}
		return new PlsEntry(data, id, queue);
	}
	
	/*
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFile);
		dest.writeString(mTitle);
	}
	
	public PlsEntry(Parcel in) {
        mFile = in.readString();
        mTitle = in.readString();
	}
	
	public static final Parcelable.Creator<PlsEntry> CREATOR = new Parcelable.Creator<PlsEntry>() {
		public PlsEntry createFromParcel(Parcel in) {
			return new PlsEntry(in);
		}
		public PlsEntry[] newArray(int size) {
			return new PlsEntry[size];
		}
	};
	*/
}


