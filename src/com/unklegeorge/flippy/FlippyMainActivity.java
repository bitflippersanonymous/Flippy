package com.unklegeorge.flippy;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FlippyMainActivity extends FlippyBaseActivity {
	private static final Class<?>[] sMenuItems = {
		FlippyInfoActivity.class,
		FlippyQueueActivity.class
	};
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		final ArrayAdapter<Class<?>> mAdapter = new ArrayAdapter<Class<?>>(this, 0, sMenuItems) { 
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = LayoutInflater.from(FlippyMainActivity.this).inflate(R.layout.main_entry, null);

				if ( position < sMenuItems.length ) {
					String text = sMenuItems[position].getSimpleName();
					TextView title = (TextView) view.findViewById(R.id.entryTitle);
					title.setText(text);
				}
				return view;
			}
			
		};
		
		ListView list = (ListView) findViewById(R.id.ListViewMain);
		list.setAdapter(mAdapter);
		
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	final Intent intent = new Intent(view.getContext(), sMenuItems[position]);
            	startActivity(intent);	
            }
	    });
	}
}
