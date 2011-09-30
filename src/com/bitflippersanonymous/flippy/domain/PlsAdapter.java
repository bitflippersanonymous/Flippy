/*
package com.bitflippersanonymous.flippy.domain;

import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class PlsAdapter extends BaseAdapter implements ListAdapter {

	private final List<PlsEntry> mContent;

	public PlsAdapter(List<PlsEntry> content) {
		mContent = content;
	}
	
	@Override
	public int getCount() {
		return mContent.size();
	}

	@Override
	public PlsEntry getItem(int position) {
		return mContent.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View reuse, ViewGroup group) {
		EntryView view;
		if (reuse != null && reuse instanceof EntryView)
			view = (EntryView) reuse;
		else
			view = new EntryView(group.getContext());
		view.setEntry(getItem(position));
		return view;
	}
}
*/