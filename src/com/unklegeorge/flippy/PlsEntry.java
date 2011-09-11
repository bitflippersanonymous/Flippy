package com.unklegeorge.flippy;

import android.os.Parcel;
import android.os.Parcelable;

class PlsEntry implements Parcelable {
    public static final String PLSENTRY = FlippyBase.PACKAGE + ".PLSENTRY";

	private final String mFile;
	private String mTitle;
	public PlsEntry(String file, String title) {
		mFile = file; mTitle = title;
	}
	public void setTitle(String title) {
		mTitle = title;
	}
	public String getFile() { return mFile; }
	public String getTitle() { return mTitle; }
	
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
}


