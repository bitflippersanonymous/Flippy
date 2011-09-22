package com.unklegeorge.flippy;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

class PlsEntry /*implements Parcelable*/ {
    public static final String PLSENTRY = Util.PACKAGE + ".PLSENTRY";
	
	public enum Tags { item, title, description, enclosure, author, pubDate, keywords }
    
	private final HashMap<Tags, String> mData;
	public PlsEntry(HashMap<Tags, String> data) {
		mData = data;
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


