package com.unklegeorge.flippy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FlippyRadioActivity extends FlippyBase {

	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String FILE = "File";
	private static final String TITLE = "Title";
	private static final String PLAYLIST = "playlist";
	private static final String PATH = "path";
	
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
		try { loadPlaylists(entries); } 
		catch(Exception e) { 
            TextView text = (TextView) findViewById(R.id.radioTextView1);
            text.setText(e.toString());
		}
	    final PlsAdapater adapter = new PlsAdapater(entries, this);
	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setAdapter(adapter);
	    list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startPlay(adapter.getItem(position));
            }});
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	    stopService(new Intent(this, FlippyPlayerService.class));
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
		
	    Intent intent = new Intent(this, FlippyPlayerService.class);
	    intent.putExtra(PlsEntry.PLSENTRY, entry);
	    intent.setAction(FlippyPlayerService.ACTION_PLAY);
	    startService(intent);
	}
	
	protected void loadPlaylists(ArrayList<PlsEntry> entries) throws XmlPullParserException, IOException {
		XmlResourceParser parser = getResources().getXml(R.xml.playlists);
		int eventType = -1;
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strName = parser.getName();
				if (strName.equals(PLAYLIST)) {
					readPlaylist(parser.getAttributeValue(null, PATH), entries);
				}
			}
			eventType = parser.next();
		}
	}
}