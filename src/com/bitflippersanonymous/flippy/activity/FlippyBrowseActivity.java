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

public class FlippyBrowseActivity extends FlippyBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);
	
	    final ListView list = (ListView) findViewById(R.id.listViewBrowse);
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	PlsEntry entry = ((EntryView)view).getEntry();
            	getService().toggleInQueue(entry);
            	((EntryView)view).update();
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
		list.setAdapter(new PlsDbAdapter(this, queue));
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
}