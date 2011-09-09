package com.unklegeorge.flippy;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.unklegeorge.flippy.FlippyHelpActivity.ContactsSpinnerAdapater;
import com.unklegeorge.flippy.FlippyHelpActivity.SpinnerEntry;

public class FlippyRadioActivity extends FlippyBase {

	static final String NEWLINE = System.getProperty("line.separator");
	static final String FILE = "File";
	static final String TITLE = "Title";

	class PlsEntry {
		private final String mFile;
		private String mTitle;
		public PlsEntry(String file, String title) {
			mFile = file; mTitle = title;
		}
		public void setTitle(String title) {
			mTitle = title;
		}
		public String getFile() { return mFile; }
		public String getTitle() { return mTitle; }
	}
	
	class PlsAdapater extends BaseAdapter implements ListAdapter {

		private final List<PlsEntry> mContent;
		private final Activity mActivity;
	
		public PlsAdapater(List<PlsEntry> content, Activity activity) {
			mContent = content;
			mActivity = activity;
		}
		
		@Override
		public int getCount() {
			return mContent.size();
		}

		@Override
		public PlsEntry getItem(int position) {
			return mContent.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View reuse, ViewGroup group) {
			final LayoutInflater inflater = mActivity.getLayoutInflater();
            View res = inflater.inflate(R.layout.listview_entry, null);
            TextView file = (TextView) res.findViewById(R.id.numberType);
            TextView title = (TextView) res.findViewById(R.id.numberPhoneNumber);
            PlsEntry entry = getItem(position);
            file.setText(entry.getFile());
            title.setText(entry.getTitle());
			return res;
		}

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio);
		
		final ArrayList<PlsEntry> entries = new ArrayList<PlsEntry>();
		// @@@ Read these from a file
		String result = readPlaylist("http://www.opb.org/programs/streams/opb-radio.pls", entries);
		result += readPlaylist("http://wxpnhi.streamguys.com/listen.pls", entries);
		
		TextView text = (TextView) findViewById(R.id.radioTextView1);
		text.setText(result);
	    final PlsAdapater adapter = new PlsAdapater(entries, this);
	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setAdapter(adapter);
	    list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startPlay(adapter.getItem(position));
            }});
	}

	public String readPlaylist(String path, ArrayList<PlsEntry> entries) {
		PlsEntry entry = null;
		String result = executeHttpGet(path);
		String lines[] = result.split(NEWLINE);
		for ( int i=0; i<lines.length; i++ ) {
			String line = lines[i];
			if ( line.startsWith(FILE) ) {
				entry = new PlsEntry(line.substring(FILE.length()+2), null);
				entries.add(entry);
			} else if ( line.startsWith(TITLE) ) {
				entry.setTitle(line.substring(TITLE.length()+2));
			}
		}
		return result;
	}
	
	public String executeHttpGet(String path) {
		BufferedReader in = null;
		String page = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(path));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			while ((line = in.readLine()) != null) {
				sb.append(line + NEWLINE);
			}
			in.close();
			page = sb.toString();
		} catch (Exception e) {
			page = e.getMessage();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					page = e.getMessage();
				}
			}
		}
		return page;
	}
	
	public void startPlay(PlsEntry entry) {
		TextView text = (TextView) findViewById(R.id.radioTextView1);
		text.setText(entry.getFile() + NEWLINE + entry.getTitle());
	}
}