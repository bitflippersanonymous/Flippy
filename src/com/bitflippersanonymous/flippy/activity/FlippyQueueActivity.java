package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.db.FlippyDatabaseAdapter;
import com.bitflippersanonymous.flippy.domain.EntryView;
import com.bitflippersanonymous.flippy.domain.PlsDbAdapter;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class FlippyQueueActivity extends FlippyBaseActivity 
	implements View.OnClickListener, DialogInterface.OnClickListener {
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queue);

	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	int clickedId = ((EntryView)view).getEntry().getId();
            	switch ( getService().getState() ) {
            	case PREPARE:
            	case PLAY:
            		if ( clickedId == getService().getCurrentEntry().getId() )
            			break;
            	case STOP:
            		getService().startPlay(clickedId, 0);
            	}
            	Intent intent = new Intent(view.getContext(), FlippyInfoActivity.class);
            	startActivity(intent);
            }});
    
	    //TODO: Put in static method in util
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Amaranth-Italic.otf");
        TextView tv = (TextView) findViewById(R.id.textViewHeader);
        tv.setTypeface(tf);

	}

	// Is a little heavy, and is called every time the service changes something
    @Override
	protected void update() {
    	final ListView list = (ListView) findViewById(R.id.radioListView1);
    	if ( !getService().getloadComplete() ) 
    		return;
    	
    	if ( list.getAdapter() == null ) {
            long start = System.currentTimeMillis() ;
            list.setAdapter(getService().getQueueAdapter());
            long end = System.currentTimeMillis();
    		Log.i(getClass().getName(),	"Cursor load time: " + (end - start));
    	} //else {
    	//	((PlsDbAdapter)list.getAdapter()).notifyDataSetChanged();
    	//}
    	
    	super.update();
    	
    	list.setVisibility(View.VISIBLE);
    	findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
    	//EntryView.updateAll();
    }
	
	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.queue));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.queue_menu));
	}
	
}
