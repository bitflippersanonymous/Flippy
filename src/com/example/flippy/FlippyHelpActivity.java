package com.example.flippy;

import java.util.LinkedList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.provider.ContactsContract.Data;

public class FlippyHelpActivity extends FlippyBase {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		/*
        Spinner s1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.planets, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        */
/*		
		class ListViewEntry {
			private final String destinationAddress;
			private final int typeResource;
			private final int entryLabelResource;
			public ListViewEntry(String number, int typeResource,
					int entryLabelResource) {
				this.destinationAddress = number;
				this.typeResource = typeResource;
				this.entryLabelResource = entryLabelResource;
			}
			public String getDestinationAddress() {
				return destinationAddress;
			}
			public int getTypeResource() {
				return typeResource;
			}
			public int getEntryLabelResource() {
				return entryLabelResource;
			}
		}

     
        Spinner contactSpinner = (Spinner)findViewById(R.id.contactsSpinner);
       
        Cursor managedCursor = getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Data.DISPLAY_NAME},
                null, null, null);
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
        		android.R.layout.simple_spinner_item,
        		managedCursor,
        		new String[] {Data.DISPLAY_NAME},
        		new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactSpinner.setAdapter(adapter);
        
        //final List<ListViewEntry> content = new LinkedList<ListViewEntry>();
        //contactListView.setAdapter(new ContactListViewAdapter(content, this));
        */
		

        Cursor listviewCurser = getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Data.DISPLAY_NAME},
                null, null, null);
        
        SimpleCursorAdapter listviewAdapter = new SimpleCursorAdapter(this,
        		R.layout.listview_entry,
        		listviewCurser,
        		new String[] {Data.DISPLAY_NAME, Data.DISPLAY_NAME},
        		new int[] {R.id.numberType, R.id.numberPhoneNumber});
        ListView contactListView = (ListView)findViewById(R.id.contactsListView);
        contactListView.setAdapter(listviewAdapter);

    }
}
