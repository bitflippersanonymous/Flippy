package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.EntryView;
import com.bitflippersanonymous.flippy.domain.PlsDbAdapter;
import com.bitflippersanonymous.flippy.domain.PlsCursorLoader;
import com.bitflippersanonymous.flippy.domain.PlsEntry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

public class FlippyBrowseActivity extends FlippyBaseActivity 
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private CursorAdapter mAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);

        final ListView list = (ListView) findViewById(R.id.listViewBrowse);
		getSupportLoaderManager().initLoader(0, null, this);
    	mAdapter = new PlsDbAdapter(this, null, 0);
		list.setAdapter(mAdapter);
    	update();

		
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	PlsEntry entry = ((EntryView)view).getEntry();
            	getService().toggleInQueue(entry);
            	update();
            }});
	    
	}

	@Override
	protected void update() {
		super.update();
    	getSupportLoaderManager().restartLoader(0, null, FlippyBrowseActivity.this);
	}

	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.browse));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.browse_menu));
	}

	@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new PlsCursorLoader(this);
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