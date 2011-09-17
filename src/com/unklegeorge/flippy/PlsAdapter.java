package com.unklegeorge.flippy;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

class PlsAdapter extends BaseAdapter implements ListAdapter {

	private final List<PlsEntry> mContent;
	private LayoutInflater mInflater;

	public PlsAdapter(List<PlsEntry> content, Context context) {
		mContent = content;
		mInflater = (LayoutInflater)context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
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
        View res = mInflater.inflate(R.layout.playlist_entry, null);
        TextView title = (TextView) res.findViewById(R.id.entryTitle);
        PlsEntry entry = getItem(position);
        title.setText(entry.getTitle());
		return res;
	}
}