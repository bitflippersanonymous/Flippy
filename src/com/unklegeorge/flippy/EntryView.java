package com.unklegeorge.flippy;


import java.util.WeakHashMap;

import com.unklegeorge.flippy.PlsEntry.Tags;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EntryView extends LinearLayout {

    private static final WeakHashMap<EntryView, ?> sInstances = new WeakHashMap<EntryView, Object>();
	private PlsEntry mEntry = null;
	
	public EntryView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.playlist_entry, this, true);
		sInstances.put(this, null);
	}
	
	@Override
	protected void onVisibilityChanged (View changedView, int visibility) {
		Log.i(getClass().getName(), "Visibility Changed");
		update();
	}
	
	public void setEntry(PlsEntry entry) {
		mEntry = entry;
		update();
	}
	
	public static void updateAll() {
		int updates = 0;
		try {
			for (EntryView view : sInstances.keySet()) {
				if ( view.getVisibility() == View.VISIBLE ) {
					view.update();
					updates++;
				}
			}
		} catch (Throwable x) {
			Log.w(EntryView.class.getName(), "Error when updating entry views.", x);
		}

		//Log.i(EntryView.class.getName(), String.valueOf(sInstances.size()) + " " + String.valueOf(updates));
	}
	
	public void update() {
        TextView title = (TextView) findViewById(R.id.entryTitle);
        title.setText(mEntry.get(Tags.title));

        FlippyPlayerService service = FlippyRadioActivity.getService();
        if ( (service.getState() == FlippyPlayerService.MediaState.PREPARE 
        		|| service.getState() == FlippyPlayerService.MediaState.PLAY)
        	&& service.getPlsAdapter().getItem(service.getPosition()) == mEntry ) {
			findViewById(R.id.EntryIcon).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.EntryIcon).setVisibility(View.GONE);
		}
	}

}
