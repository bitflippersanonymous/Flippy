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
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FlippyRadioActivity extends FlippyBase implements View.OnClickListener {

	private static final String TAG = "FlippyRadio";
	private static final String NEWLINE = System.getProperty("line.separator");

	// PLS file
	private static final String FILE = "File";
	private static final String TITLE = "Title";
	
	// XML file
	private static final String PATH = "path";
	private static final String NAME = "name";
	private static final String PLAYLIST = "playlist";
	
	private int mCurPlayingPos = 0;
	
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
            View res = inflater.inflate(R.layout.playlist_entry, null);
            TextView title = (TextView) res.findViewById(R.id.entryTitle);
            PlsEntry entry = getItem(position);
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
				list.setSelection(position);
            }});
	    
	    setPPIcon(isServiceRunning(FlippyPlayerService.class.getName()));
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.imageButtonNext:
			seek(1);
			break;
		case R.id.imageButtonPP:
			if ( stopService(new Intent(this, FlippyPlayerService.class)) )
				setPPIcon(false);
			else
				seek(0);
			break;
		case R.id.imageButtonPrev:
			seek(-1);
			break;
		default:
		}
	}
	
	public void seek(int direction) {
		final ListView list = (ListView) findViewById(R.id.radioListView1);
		final PlsEntry entry = (PlsEntry) list.getItemAtPosition(mCurPlayingPos+direction);
		if ( entry != null ) {
			mCurPlayingPos += direction; 
			startPlay(entry);
		}
	}
		
	public void startPlay(PlsEntry entry) {
		TextView text = (TextView) findViewById(R.id.radioTextView1);
		text.setText(entry.getFile() + NEWLINE + entry.getTitle());
		
	    Intent intent = new Intent(this, FlippyPlayerService.class);
	    intent.putExtra(PlsEntry.PLSENTRY, entry);
	    intent.setAction(FlippyPlayerService.ACTION_PLAY);
	    startService(intent);
	    setPPIcon(true);
	}
	
	private boolean isServiceRunning(String name) {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for ( RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) ) {
	        if ( name.equals(service.service.getClassName()) ) {
	            return true;
	        }
	    }
	    return false;
	}

	private void setPPIcon(boolean state) {
		ImageButton buttonPlay = (ImageButton) findViewById(R.id.imageButtonPP);
		if ( state )
			buttonPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
		else
			buttonPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
	}
	
	public String readPlaylist(String path, String name, ArrayList<PlsEntry> entries) {
		PlsEntry entry = null;
		String result = executeHttpGet(path);
		String lines[] = result.split(NEWLINE);
		for ( int i=0; i<lines.length; i++ ) {
			String line = lines[i];
			if ( line.startsWith(FILE) ) {
				entry = new PlsEntry(line.substring(FILE.length()+2), name);
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
	
	protected void loadPlaylists(ArrayList<PlsEntry> entries) throws XmlPullParserException, IOException {
		XmlResourceParser parser = getResources().getXml(R.xml.playlists);
		int eventType = -1;
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strName = parser.getName();
				if (strName.equals(PLAYLIST)) {
					String path = parser.getAttributeValue(null, PATH);
					String name = parser.getAttributeValue(null, NAME);
					readPlaylist(path, name, entries);
				}
			}
			eventType = parser.next();
		}
	}
}