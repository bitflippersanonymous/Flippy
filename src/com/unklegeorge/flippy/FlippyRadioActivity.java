package com.unklegeorge.flippy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import com.unklegeorge.flippy.R.drawable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FlippyRadioActivity extends FlippyBase implements View.OnClickListener, OnClickListener {

	private static final String TAG = "FlippyRadio";
	private static final String NEWLINE = System.getProperty("line.separator");

	// PLS file
	private static final String FILE = "File";
	private static final String TITLE = "Title";
	
	// XML file
	private static final String PATH = "path";
	private static final String NAME = "name";
	private static final String PLAYLIST = "playlist";
	private static final int ABOUT_DIALOG = 0;
	
	private int mCurPlayingPos = 0;
	private LoadTask mLoadTask = null;
	
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

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio);

		TextView text = (TextView) findViewById(R.id.radioTextView1);
		final ArrayList<PlsEntry> entries = new ArrayList<PlsEntry>();
		
		mLoadTask = new LoadTask();
		mLoadTask.execute(entries);
		
	    final PlsAdapater adapter = new PlsAdapater(entries, this);
	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setAdapter(adapter);
	    list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	startPlay(position, 0);
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
			startPlay(mCurPlayingPos, 1);
			break;
		case R.id.imageButtonPP:
			if ( stopService(new Intent(this, FlippyPlayerService.class)) )
				stopPlay();
			else
				startPlay(mCurPlayingPos, 0);
			break;
		case R.id.imageButtonPrev:
			startPlay(mCurPlayingPos, -1);
			break;
		default:
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu); // need this?
		getMenuInflater().inflate(R.menu.options, menu);
		menu.findItem(R.id.settings_menu_item).setIntent(
				new Intent(this, FlippySettingsActivity.class));
		menu.findItem(R.id.help_menu_item).setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(ABOUT_DIALOG);
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}
	
	@Override
    protected Dialog onCreateDialog(int id, Bundle args) {
    	switch ( id ) {
    	case ABOUT_DIALOG:
    		String message = "";
    		try {
	    		InputStream ins = getResources().openRawResource(R.raw.about);
	    		int size = ins.available();
	    		byte[] buffer = new byte[size];
	    		ins.read(buffer);
	    		ins.close();
	    		message = new String(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(message)
	    	.setIcon(drawable.icon)
	    	.setTitle(R.string.app_name)
	    	.setPositiveButton(R.string.got_it, this)
	    	.setCancelable(true);
	    	return builder.create();
    	}
    	return super.onCreateDialog(id, args);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	}
	
	public void stopPlay() {
		final ListView list = (ListView) findViewById(R.id.radioListView1);
		final TextView text = (TextView) findViewById(R.id.radioTextView1);
		list.getChildAt(mCurPlayingPos).findViewById(R.id.EntryIcon).setVisibility(View.GONE);
		text.setText(null);
	    setPPIcon(false);
	}
	
	public void startPlay(int position, int offset) {
		stopPlay();
		final ListView list = (ListView) findViewById(R.id.radioListView1);

		final int newPos = position + offset;
		if ( newPos < 0 || newPos >= list.getAdapter().getCount() ) {
			mCurPlayingPos = 0;
		} else {
			mCurPlayingPos = position + offset;
		}
		
		final PlsEntry entry = (PlsEntry) list.getItemAtPosition(mCurPlayingPos);
		
		list.setSelection(mCurPlayingPos);
		list.getChildAt(mCurPlayingPos).findViewById(R.id.EntryIcon).setVisibility(View.VISIBLE);
		final TextView text = (TextView) findViewById(R.id.radioTextView1);
		text.setText(entry.getTitle());
		
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
		ImageView buttonPlay = (ImageView) findViewById(R.id.imageButtonPP);
		if ( state )
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
		else
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
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

	private class LoadTask extends AsyncTask<ArrayList<PlsEntry>, Integer, Integer> {
		@Override
		protected Integer doInBackground(ArrayList<PlsEntry>... params) {
			ArrayList<PlsEntry> entries = params[0];
			try { loadPlaylists(entries); } 
			catch(Exception e) { 
				TextView text = (TextView) findViewById(R.id.radioTextView1);
	            text.setText(e.toString());
	            return -1;
			}
			return 0;
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
		}

		@Override
		protected void onPostExecute(Integer result) {
		    final ListView list = (ListView) findViewById(R.id.radioListView1);
			((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
			findViewById(R.id.linearLayoutProgress).setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute() {
		}
	}

	
}
