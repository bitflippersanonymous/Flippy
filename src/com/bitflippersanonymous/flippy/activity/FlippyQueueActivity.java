package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.db.FlippyDatabaseAdapter;
import com.bitflippersanonymous.flippy.domain.EntryView;
import com.bitflippersanonymous.flippy.domain.PlsDbAdapter;
import com.bitflippersanonymous.flippy.domain.PlsEntry;
import com.bitflippersanonymous.flippy.domain.SimpleCursorLoader;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
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
	implements View.OnClickListener, DialogInterface.OnClickListener, 
	LoaderManager.LoaderCallbacks<Cursor> {
    
	private CursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queue);

	    final ListView list = (ListView) findViewById(R.id.radioListView1);
		getSupportLoaderManager().initLoader(0, null, this);
    	mAdapter = new PlsDbAdapter(this, null, 0);
		list.setAdapter(mAdapter);

	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	PlsEntry entry = ((EntryView)view).getEntry();
            	
            	switch ( getService().getState() ) {
            	case PREPARE:
            	case PLAY:
            		if ( entry.getId() == getService().getCurrentEntry().getId() )
            			break;
            	case STOP:
            		getService().startPlay(entry, 0);
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
    	super.update();
    	getSupportLoaderManager().restartLoader(0, null, this);

    	// Need to move this
    	final ListView list = (ListView) findViewById(R.id.radioListView1);
    	list.setVisibility(View.VISIBLE);
    	findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
    }
	
	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.queue));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.queue_menu));
	}

	@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new SimpleCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {
				return getService().fetchQueue();
			}
		};
	}

	@Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
		
	}	
}
