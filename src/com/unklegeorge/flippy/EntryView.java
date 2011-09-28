package com.unklegeorge.flippy;


import java.util.WeakHashMap;


import com.unklegeorge.flippy.PlsEntry.Tags;
import com.unklegeorge.flippy.FlippyPlayerService.MediaState;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Typeface;

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
        final TextView title = (TextView) findViewById(R.id.entryTitle);
        
        String text = mEntry.get(Tags.title) + Util.NEWLINE;
        int subPos = text.length();
        text += mEntry.get(Tags.verses) + Util.SPACE + mEntry.get(Tags.pubDate);
        title.setText(text, TextView.BufferType.SPANNABLE);
     	Spannable str = (Spannable)title.getText();
     	
     	//str.setSpan(new StyleSpan(Typeface.BOLD), 0, subPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     	str.setSpan(new TextAppearanceSpan(title.getContext(), 
     			android.R.style.TextAppearance_Small), subPos, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             	
        final FlippyPlayerService service = FlippyBaseActivity.getService();
        final PlsEntry curEntry = service.getPlsAdapter().getItem(service.getPosition());
        
        switch ( service.getState() ) {
        case PREPARE:
        	if ( curEntry == mEntry ) {
        		findViewById(R.id.progressBarEntry).setVisibility(View.VISIBLE);
        		findViewById(R.id.EntryIcon).setVisibility(View.GONE);
        	} else {
        		findViewById(R.id.progressBarEntry).setVisibility(View.GONE);
    			findViewById(R.id.EntryIcon).setVisibility(View.GONE);
        	}
        	break;
        case PLAY:
        	if ( curEntry == mEntry ) {
    			findViewById(R.id.EntryIcon).setVisibility(View.VISIBLE);
        		findViewById(R.id.progressBarEntry).setVisibility(View.GONE);
        	} else {
        		findViewById(R.id.progressBarEntry).setVisibility(View.GONE);
    			findViewById(R.id.EntryIcon).setVisibility(View.GONE);
        	}
        	break;
        default:
    		findViewById(R.id.progressBarEntry).setVisibility(View.GONE);
			findViewById(R.id.EntryIcon).setVisibility(View.GONE);
        }
	}

}
