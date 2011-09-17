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

import com.unklegeorge.flippy.FlippyPlayerService.LocalBinder;
import com.unklegeorge.flippy.R.drawable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.XmlResourceParser;
import android.database.DataSetObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

public class FlippyRadioActivity extends Activity implements View.OnClickListener, OnClickListener {
	private static final int ABOUT_DIALOG = 0;
	
	private int mCurPlayingPos = 0;
	
    private FlippyPlayerService mService;
    private boolean mBound = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio);

	    final ListView list = (ListView) findViewById(R.id.radioListView1);
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
        Intent intent = new Intent(this, FlippyPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onStop() {
		super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
    private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            connectList();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            mBound = false;
		}
    };
    
    private void connectList() {
    	final ListView list = (ListView) findViewById(R.id.radioListView1);
    	list.setAdapter(mService.getPlsAdapter());
    	// Gets called too early
    	list.setVisibility(View.VISIBLE);
    	findViewById(R.id.linearLayoutProgress).setVisibility(View.GONE);
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
		mService.startPlay(position);
		
		/*
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
	    */
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
	

	


	
	
}
