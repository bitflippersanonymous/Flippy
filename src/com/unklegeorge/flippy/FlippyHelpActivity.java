package com.unklegeorge.flippy;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.unklegeorge.flippy.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;

public class FlippyHelpActivity extends FlippyBase {
	

	class SpinnerEntry {
	        private final int contactId;
	        private final int contactPhotoId;
	        private final String contactName;

	        public SpinnerEntry(int contactID, 
	        				int contactPhotoId,
	                        String contactName) {
	                this.contactId = contactID;
	                this.contactPhotoId = contactPhotoId;
	                this.contactName = contactName;
	        }
	        public int getContactId() {
	                return contactId;
	        }
	        public int getContactPhotoId() {
	                return contactPhotoId;
	        }
	        public String getContactName() {
	                return contactName;
	        }
	}

	
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
		public ListViewEntry getItem(int position) {
			return mContent.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
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
	
	class ContactsSpinnerAdapater extends BaseAdapter implements SpinnerAdapter {
		private final List<SpinnerEntry> mContent;
		private final Activity mActivity;
	
		public ContactsSpinnerAdapater(List<SpinnerEntry> content, Activity activity) {
			mContent = content;
			mActivity = activity;
		}
		@Override
		public int getCount() {
			return mContent.size();
		}
		@Override
		public SpinnerEntry getItem(int position) {
			return mContent.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
            final LayoutInflater inflater = mActivity.getLayoutInflater();
            View spinnerEntry = inflater.inflate(android.R.layout.simple_spinner_item, null);
            TextView text = (TextView) spinnerEntry.findViewById(android.R.id.text1);
            SpinnerEntry entry = getItem(position);
            text.setText(entry.getContactName());
			return spinnerEntry;
		}
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View spinnerEntry;
			if ( convertView == null ) {
				final LayoutInflater inflater = mActivity.getLayoutInflater();
				spinnerEntry = inflater.inflate(R.layout.spinner_itemdrop, null);
			} else {
				spinnerEntry = convertView;
			}
			
			TextView text = (TextView) spinnerEntry.findViewById(R.id.contact_text1);
			ImageView image = (ImageView) spinnerEntry.findViewById(R.id.contact_image1);

			SpinnerEntry entry = getItem(position);
			text.setText(entry.getContactName());
			updatePhoto(entry.getContactId(), image);
			return spinnerEntry;
		}
		
	}
	
    private final List<SpinnerEntry> spinnerContent = new LinkedList<SpinnerEntry>();
    private final ContactsSpinnerAdapater adapter = new ContactsSpinnerAdapater(spinnerContent, this);
    private Spinner contactSpinner;
    private ListView contactListView;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
   
        contactSpinner = (Spinner)findViewById(R.id.contactsSpinner);
        contactListView = (ListView)findViewById(R.id.contactsListView);

        contactSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateList(position);
			}
			@Override
            public void onNothingSelected(AdapterView<?> parent) {
				updateList(contactSpinner.getSelectedItemPosition());
			}

        });
        
        queryAllRawContacts();
        contactSpinner.setAdapter(adapter);
        /*
        ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
        View layout = findViewById(R.id.helpLayout);
        if ( layout != null ) { 
        	layout.setOnTouchListener(activitySwipeDetector);
        }
        */

    }
    
	private void updateList(int position) {
    	if(position < adapter.getCount() && position >= 0) {
    		SpinnerEntry entry = adapter.getItem(position);
    		final List<ListViewEntry> content = new LinkedList<ListViewEntry>();
    		loadContent(entry.getContactId(), content);
    		contactListView.setAdapter(new ContactListViewAdapter(content, FlippyHelpActivity.this));
    	}
    }
	
	private void updatePhoto(int id, ImageView img) {
		Bitmap photo = queryContactBitmap(id);
		if ( photo == null ) {
			img.setVisibility(View.GONE);
			return;
		}
		img.setVisibility(View.VISIBLE);
		img.setImageBitmap(photo);
    }
    
	private void loadContent(long contactId, List<ListViewEntry> content) {
		
		 Cursor curs = getContentResolver().query(Data.CONTENT_URI,
		          new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
		          Data.CONTACT_ID + "=?" + " AND "
		                  + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
		          new String[] {String.valueOf(contactId)}, null);
         
		 if ( curs.moveToFirst() ) {
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
	
	private void queryAllRawContacts() {
		final Cursor curs = getContentResolver().query( // diff between managedQuery?
				RawContacts.CONTENT_URI, 
				new String[] {RawContacts.CONTACT_ID, RawContacts.DELETED},
				null, null, null);

        final int contactIdColumnIndex = curs.getColumnIndex(RawContacts.CONTACT_ID);
        final int deletedColumnIndex = curs.getColumnIndex(RawContacts.DELETED);
		
		spinnerContent.clear();
		if ( curs.moveToFirst() ) {
			while ( !curs.isAfterLast() ) {
				final int id = curs.getInt(contactIdColumnIndex);
				final int deleted = curs.getInt(deletedColumnIndex);
				if ( deleted != 1) {
					spinnerContent.add(querySpinnerEntry(id));
				}
                curs.moveToNext();
			}
		}
		curs.close();
	}
	
	private SpinnerEntry querySpinnerEntry(int id) {
        final Cursor curs = managedQuery(
                Contacts.CONTENT_URI,
                new String[]{Contacts.DISPLAY_NAME, Contacts.PHOTO_ID},
                Contacts._ID + "=?",
                new String[]{String.valueOf(id)},
                null);

        final int contactNameColumnIndex = curs.getColumnIndex(Contacts.DISPLAY_NAME);
        final int contactPhotoIdColumnIndex = curs.getColumnIndex(Contacts.PHOTO_ID);

		if ( curs.moveToFirst() ) {
			final String name = curs.getString(contactNameColumnIndex);
			final int photoId = curs.getInt(contactPhotoIdColumnIndex);
			curs.close();
			return new SpinnerEntry(id, photoId, name);
		}
		curs.close();
		return null;		
	}
	
    private Bitmap queryContactBitmap(int id) {
    	final Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
    	if ( uri == null ) {
    		return null;
    	}
    	
        InputStream input = Contacts.openContactPhotoInputStream(getContentResolver(), uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }


}
