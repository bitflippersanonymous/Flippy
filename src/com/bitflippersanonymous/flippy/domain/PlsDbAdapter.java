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
		((EntryView) view).setEntry(cursorToEntry(cursor));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		EntryView view = new EntryView(context);
		view.setEntry(cursorToEntry(cursor));
		return view;
	}

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
}