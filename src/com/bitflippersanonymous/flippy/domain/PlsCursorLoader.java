package com.bitflippersanonymous.flippy.domain;

import com.bitflippersanonymous.flippy.activity.FlippyBaseActivity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

public class PlsCursorLoader extends AsyncTaskLoader<Cursor> {

	public PlsCursorLoader(Context context) {
		super(context);
	}

	@Override
	public Cursor loadInBackground() {
		return FlippyBaseActivity.getService().fetchAllEntries();
	}

	// extend Loader<Cursor> and impl these?
	//onStartLoading() onStopLoading(), onForceLoad(), and onReset().
	
}

