package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.PlsDbAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FlippyBrowseActivity extends FlippyBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);
	
		update();
	}

	@Override
	protected void update() {
		super.update();

    	final ListView list = (ListView) findViewById(R.id.listViewBrowse);
    	long start = System.currentTimeMillis() ;
		Cursor queue =  getService().getDbAdapter().fetchAllEntries();
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