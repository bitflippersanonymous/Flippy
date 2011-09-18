package com.unklegeorge.flippy;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EntryView extends LinearLayout {

	private PlsEntry mEntry = null;
	
	public EntryView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.playlist_entry, this, true);
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
	
	public void update() {
        TextView title = (TextView) findViewById(R.id.entryTitle);
        title.setText(mEntry.getTitle());

        FlippyPlayerService playServ = FlippyRadioActivity.getService();
		if ( playServ.isPlaying() && playServ.getPlsAdapter().getItem(playServ.getPosition()) == mEntry ) {
			findViewById(R.id.EntryIcon).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.EntryIcon).setVisibility(View.GONE);
		}
	}

}
