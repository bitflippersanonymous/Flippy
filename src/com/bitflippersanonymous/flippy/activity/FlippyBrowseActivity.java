package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.EntryView;
import com.bitflippersanonymous.flippy.domain.PlsDbAdapter;
import com.bitflippersanonymous.flippy.domain.PlsEntry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;



public class FlippyBrowseActivity extends FlippyBaseActivity 
	implements LoaderManager.LoaderCallbacks<Cursor> {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);
	
        getSupportLoaderManager().initLoader(0, null, this);
        
	    final ListView list = (ListView) findViewById(R.id.listViewBrowse);
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	PlsEntry entry = ((EntryView)view).getEntry();
            	getService().toggleInQueue(entry);
            	PlsDbAdapter adapter = ((PlsDbAdapter)list.getAdapter());
            	adapter.getCursor().requery();
            	adapter.notifyDataSetChanged();
            	adapter.swapCursor(null);  //TODO: fixme
            }});
		
		update();
	}

	@Override
	protected void update() {
		super.update();

		//TODO: too slow.  Need to use fragments and cursor loader
    	final ListView list = (ListView) findViewById(R.id.listViewBrowse);
    	long start = System.currentTimeMillis() ;
		Cursor queue =  getService().fetchAllEntries();
		startManagingCursor(queue);
		list.setAdapter(new PlsDbAdapter(this, queue, 0));
    	long end = System.currentTimeMillis();
    	Log.i(getClass().getName(),	"Cursor load time: " + (end - start));
  
	}

	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.browse));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.browse_menu));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}