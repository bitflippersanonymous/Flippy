package com.unklegeorge.flippy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class FlippyRadioActivity extends FlippyActivityBase 
	implements View.OnClickListener, DialogInterface.OnClickListener {
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio);

	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	switch ( getService().getState() ) {
            	case PREPARE:
            	case PLAY:
            		if ( getService().getPosition() == position ) {
            			getService().stopPlay();
            			break;
            		}
            	default:
    				getService().startPlay(getService().getPosition(), 0);
    				Intent intent = new Intent(view.getContext(), FlippyInfoActivity.class);
    				startActivity(intent);
            	}
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
    	final PlsAdapter adapter = getService().getPlsAdapter();
    	if ( list.getAdapter() == null )
    		list.setAdapter(adapter);
    	
    	if ( !getService().getloadComplete() ) 
    		return;

    	super.update();
    	
    	list.setVisibility(View.VISIBLE);
    	findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
    	EntryView.updateAll();
    }
	

	
}
