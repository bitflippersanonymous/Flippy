package com.bitflippersanonymous.flippy.domain;


import java.util.WeakHashMap;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.activity.FlippyBaseActivity;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;
import com.bitflippersanonymous.flippy.service.FlippyPlayerService;
import com.bitflippersanonymous.flippy.util.Util;

import android.content.Context;
import android.os.DropBoxManager.Entry;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.text.style.TextAppearanceSpan;
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
	
	//TODO: not sure this is ever called
	@Override
	protected void onVisibilityChanged (View changedView, int visibility) {
		Log.i(getClass().getName(), "Visibility Changed");
		update();
	}
	
	public PlsEntry getEntry() {
		return mEntry;
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

		if ( updates > 50 )
			Log.i(EntryView.class.getName(), String.valueOf(sInstances.size()) + " " + String.valueOf(updates));
	}
	
	public void update() {
        final TextView title = (TextView) findViewById(R.id.entryTitle);
        
        String text = mEntry.get(Tags.title) + Util.NEWLINE;
        int subPos = text.length();
        text += mEntry.get(Tags.verses) + Util.SPACE;
        String pstr = mEntry.get(Tags.pubDate);
        if ( pstr != null )
        	text += DateFormat.format("MMM dd, yyyy", Long.parseLong(pstr));
        title.setText(text, TextView.BufferType.SPANNABLE);
     	Spannable str = (Spannable)title.getText();
     	
     	//str.setSpan(new StyleSpan(Typeface.BOLD), 0, subPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     	str.setSpan(new TextAppearanceSpan(title.getContext(), 
     			android.R.style.TextAppearance_Small), subPos, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
     	//TODO: toggle an image here
     	if ( mEntry.getInQueue() )
     		findViewById(R.id.InQueueIcon).setVisibility(View.VISIBLE);
     	else
     		findViewById(R.id.InQueueIcon).setVisibility(View.GONE);

     	
        final FlippyPlayerService service = FlippyBaseActivity.getService();
        final PlsEntry curEntry = service.getCurrentEntry();
        
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
