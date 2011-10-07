package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.SimpleCursorLoader;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FlippySearchActivity extends FlippyBaseActivity 
	implements LoaderManager.LoaderCallbacks<Cursor> {


	private CursorAdapter mAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		getSupportLoaderManager().initLoader(0, null, this);

	    AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewSearch);
	    mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, 
	    	null, new String[]{Tags.keywords.name()}, new int[]{android.R.id.text1}, 0) {
	    	@Override
			public	String convertToString(Cursor cursor) {
				return cursor.getString(1);
	    	}
	    };
	    mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	        	Cursor cursor = mAdapter.getCursor(); 
	        	if ( cursor != null )
	        		cursor.close();
	            String partialKeyword = null;
	            if (constraint != null) 
	            	partialKeyword = constraint.toString();
				return getService().fetchAllKeywords(partialKeyword);
	        }
	    });
	    textView.setAdapter(mAdapter);
	    //textView.setHint("Search"); // Pull string from xml
	    textView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		Cursor cursor = (Cursor)mAdapter.getItem(position);
	    		if ( cursor == null ) return;
	    		updateResults(cursor.getInt(0));
	    	}
	    });
	}
	
	@Override
	protected void update() {
		super.update();
    	getSupportLoaderManager().restartLoader(0, null, this);
	}

	private void updateResults(int int1) {
		// Update a listview in this activity, or launch browse w/ filter
	}
	
	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.search));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.search_menu));
	}
	
	@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new SimpleCursorLoader(this) {
			@Override
			public Cursor loadInBackground() {
				return getService().fetchAllKeywords(null);
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