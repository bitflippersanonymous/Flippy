package com.bitflippersanonymous.flippy.domain;

import java.util.HashMap;

import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;
import com.bitflippersanonymous.flippy.util.Util;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

public class PlsDbAdapter extends CursorAdapter implements ListAdapter {

	public PlsDbAdapter(Context context, Cursor c) {
		super(context, c);
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