package com.example.flippy;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class FlippyHelpActivity extends FlippyBase {
	
	class ListViewEntry {
		private final String destinationAddress;
		private final int typeResource;
		private final String entryLabelResource;
		public ListViewEntry(String number, int typeResource,
				String entryLabelResource) {
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
		public String getEntryLabelResource() {
			return entryLabelResource;
		}
	}
	
	class ContactListViewAdapter extends BaseAdapter implements ListAdapter {
        private final List<ListViewEntry> mContent;
        private final Activity mActivity;

		ContactListViewAdapter(List<ListViewEntry> content, Activity activity) {
            mContent = content;
            mActivity = activity;
		}
		
		@Override
		public int getCount() {
			return mContent.size();
		}

		@Override
		public ListViewEntry getItem(int arg0) {
			return mContent.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
    		final LayoutInflater factory = mActivity.getLayoutInflater();
    		final View listEntry = factory.inflate(R.layout.listview_entry, null);
    		final TextView number = (TextView) listEntry.findViewById(R.id.numberPhoneNumber);
    		final TextView type = (TextView) listEntry.findViewById(R.id.numberType);
    		ListViewEntry entry = getItem(position);
    		number.setText(entry.getDestinationAddress());
    		if ( entry.getEntryLabelResource() != null ) {
    			type.setText(entry.getEntryLabelResource());
    		} else {
    			type.setText(mActivity.getString(entry.getTypeResource()));
    		}
			return listEntry;
		}
	}
	
    private Spinner contactSpinner;
    private ListView contactListView;
	
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
     
        contactSpinner = (Spinner)findViewById(R.id.contactsSpinner);
        contactListView = (ListView)findViewById(R.id.contactsListView);
       
        Cursor managedCursor = getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Data.CONTACT_ID, Data.DISPLAY_NAME},
                null, null, null);
        
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
        		android.R.layout.simple_spinner_item,
        		managedCursor,
        		new String[] {Data.DISPLAY_NAME},
        		new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactSpinner.setAdapter(adapter);
        
        contactSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateList(position);
			}

			@Override
            public void onNothingSelected(AdapterView<?> parent) {
				updateList(contactSpinner.getSelectedItemPosition());
			}
			
            private void updateList(int position) {
            	if(position < adapter.getCount() && position >= 0) {
            		Cursor cursor = (Cursor) adapter.getItem(position);
            		long contactId = cursor.getLong(cursor.getColumnIndex(Data.CONTACT_ID));
            		final List<ListViewEntry> content = new LinkedList<ListViewEntry>();
            		loadContent(contactId, content);
            		contactListView.setAdapter(new ContactListViewAdapter(content, FlippyHelpActivity.this));
            	}
            }
        });
              
    }
    
	private void loadContent(long contactId, List<ListViewEntry> content) {
		
		 Cursor curs = getContentResolver().query(Data.CONTENT_URI,
		          new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
		          Data.CONTACT_ID + "=?" + " AND "
		                  + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
		          new String[] {String.valueOf(contactId)}, null);
         
		 if(curs.moveToFirst()) {
             final int contactNumberColumnIndex = curs.getColumnIndex(Phone.NUMBER);
             final int contactTypeColumnIndex = curs.getColumnIndex(Phone.TYPE);
             final int contactLabelColumnIndex = curs.getColumnIndex(Phone.LABEL);
             while(!curs.isAfterLast()) {
                     final String number = curs.getString(contactNumberColumnIndex);
                     final int type = curs.getInt(contactTypeColumnIndex);
                     final String label = curs.getString(contactLabelColumnIndex);
                     content.add(new ListViewEntry(number, Phone.getTypeLabelResource(type), label));
                     curs.moveToNext();
             }
             
		 }
		 curs.close();
	}

}
