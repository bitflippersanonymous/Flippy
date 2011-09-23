package com.unklegeorge.flippy;

import java.io.IOException;
import java.io.InputStream;
import com.unklegeorge.flippy.FlippyPlayerService.LocalBinder;
import com.unklegeorge.flippy.FlippyPlayerService.MediaState;
import com.unklegeorge.flippy.PlsEntry.Tags;
import com.unklegeorge.flippy.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;
import android.os.Messenger;

public class FlippyRadioActivity extends Activity implements View.OnClickListener, OnClickListener {
	private static final int ABOUT_DIALOG = 0;
		
    private static FlippyPlayerService mService;
    private boolean mBound = false;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio);

	    final ListView list = (ListView) findViewById(R.id.radioListView1);
	    list.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	switch ( mService.getState() ) {
            	case PREPARE:
            	case PLAY:
            		if ( mService.getPosition() == position ) {
            			stopPlay();
            			break;
            		}
            	default:
            		startPlay(position, 0);
            	}
            		
            }});
	    setPPIcon(false);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		// Done loading callback
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				update();
			}
		};
		
		Intent intent = new Intent(this, FlippyPlayerService.class);
		intent.putExtra(Util.EXTRA_MESSENGER, new Messenger(handler));
		startService(intent);
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
            update();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            mBound = false;
		}
    };
    
    public static FlippyPlayerService getService() { return mService; }
 
	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.imageButtonPP:
			if ( mService.getState() != MediaState.STOP )
				stopPlay();
			else
				startPlay(mService.getPosition(), 0);
			break;
		case R.id.imageButtonHeader:
			showDialog(ABOUT_DIALOG);
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

	// Is a little heavy, and is called every time the service changes something
    private void update() {
    	final ListView list = (ListView) findViewById(R.id.radioListView1);
    	final PlsAdapter adapter = mService.getPlsAdapter();
    	list.setAdapter(adapter);
    	if ( !mService.getloadComplete() ) 
    		return;
    	
    	list.setVisibility(View.VISIBLE);
    	findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
    	EntryView.updateAll();
    	
		final TextView text = (TextView) findViewById(R.id.radioTextView1);
		final int position = mService.getPosition();
		if ( mService.getState() == MediaState.STOP ) { 
			setPPIcon(false);
			text.setText(null);
		} else {
			setPPIcon(true);
			final PlsEntry entry = (PlsEntry) list.getItemAtPosition(position);
			text.setText(entry.get(Tags.title));			
		}
        
    }
	
	public void stopPlay() {
		mService.stopPlay();	
	}
	
	public void startPlay(int position, int offset) {
		final ListView list = (ListView) findViewById(R.id.radioListView1);
		
		final int newPos = position + offset;
		if ( newPos < 0 || newPos >= list.getAdapter().getCount() ) {
			position = 0;
		} else {
			position = position + offset;
		}
		
		mService.startPlay(position);
	}


	private void setPPIcon(boolean state) {
		ImageView buttonPlay = (ImageView) findViewById(R.id.imageButtonPP);
		if ( state )
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
		else
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
	}
	
}
