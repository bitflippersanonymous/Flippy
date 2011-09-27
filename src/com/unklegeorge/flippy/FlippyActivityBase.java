package com.unklegeorge.flippy;

import java.io.IOException;
import java.io.InputStream;

import com.unklegeorge.flippy.FlippyPlayerService.LocalBinder;
import com.unklegeorge.flippy.FlippyPlayerService.MediaState;
import com.unklegeorge.flippy.PlsEntry.Tags;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public abstract class FlippyActivityBase extends Activity 
	implements View.OnClickListener, DialogInterface.OnClickListener {

	private boolean mBound = false;
	
	private static FlippyPlayerService mService;
	public static FlippyPlayerService getService() { 
		//TODO: If service not bound, bind it and wait until it's bound, then return it
		return mService; 
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			update();
		}
	};
	protected Handler getHandler() {
		return mHandler;
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
				mService = null;
				mBound = false;
			}
	    };

	@Override
	public void onStop() {
		super.onStop();
	    if ( mBound ) {
	        unbindService(mConnection);
	    }
	}

	@Override
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, FlippyPlayerService.class);
		intent.putExtra(Util.EXTRA_MESSENGER, new Messenger(getHandler()));
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.imageButtonPP:
			if ( getService().getState() != MediaState.STOP )
				getService().stopPlay();	
			else
				getService().startPlay(getService().getPosition(), 0);
			break;
		case R.id.imageButtonHeader:
			Intent intent = new Intent(this, FlippyInfoActivity.class);
			startActivity(intent);
			//showDialog(ABOUT_DIALOG);
		default:
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu); // need this?
		getMenuInflater().inflate(R.menu.options, menu);
		menu.findItem(R.id.settings_menu_item).setIntent(
				new Intent(this, FlippySettingsActivity.class));
		menu.findItem(R.id.help_menu_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(Util.ABOUT_DIALOG);
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
    	case Util.ABOUT_DIALOG:
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
	    	.setIcon(R.drawable.icon)
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

	protected void setPPIcon(boolean state) {
		ImageView buttonPlay = (ImageView) findViewById(R.id.imageButtonPP);
		if ( state )
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
		else
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
	}
	
	protected void update() {
		final TextView text = (TextView) findViewById(R.id.radioTextView1);
		final int position = getService().getPosition();
		if ( getService().getState() == MediaState.STOP ) { 
			setPPIcon(false);
			text.setText(null);
		} else {
			setPPIcon(true);
			final PlsEntry entry = (PlsEntry) getService().getPlsAdapter().getItem(position);
			text.setText(entry.get(Tags.title));			
		}
	}

}