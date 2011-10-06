package com.bitflippersanonymous.flippy.domain;


import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.support.v4.widget.*;


public class PlsDbAdapter extends CursorAdapter implements ListAdapter {
	
	public PlsDbAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((EntryView) view).setEntry(PlsEntry.cursorToEntry(cursor));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		EntryView view = new EntryView(context);
		view.setEntry(PlsEntry.cursorToEntry(cursor));
		return view;
	}
}