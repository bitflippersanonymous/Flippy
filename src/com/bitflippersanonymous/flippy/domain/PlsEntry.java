package com.bitflippersanonymous.flippy.domain;

import com.bitflippersanonymous.flippy.util.Util;

import java.sql.Time;
import java.util.HashMap;

import android.content.ContentValues;
import android.text.format.DateFormat;

public class PlsEntry /*implements Parcelable*/ {
	
	public static final String PLSENTRY = Util.PACKAGE + ".PLSENTRY";
	public enum Tags { item, title, verses, description, enclosure, author, pubDate, keywords }
    
	private final HashMap<Tags, String> mData;
	public PlsEntry(HashMap<Tags, String> data) {
		mData = data;
		if ( mData.containsKey(Tags.title) ) {
			String bybar[] = get(Tags.title).split("[|]");
			String byphen[] = bybar[0].split(" - ", -1);
			mData.put(Tags.title, byphen[0]);
			mData.put(Tags.verses, byphen[1]);
		}
		
		if ( mData.containsKey(Tags.pubDate) ) {
			CharSequence prettyDate = DateFormat.format("MMM dd, yyyy", Time.parse(get(Tags.pubDate)));
			mData.put(Tags.pubDate, prettyDate.toString());
		}

	}
	
	public String get(Tags tag) {
		return mData.get(tag);
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


