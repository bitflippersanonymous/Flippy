package com.unklegeorge.flippy;

import com.unklegeorge.flippy.FlippyPlayerService.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public abstract class FlippyActivityBase extends Activity {

	private boolean mBound = false;
	private static FlippyPlayerService mService;
	public static FlippyPlayerService getService() { return mService; }

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			update();
		}
	};

	protected Handler getHandler() {
		return mHandler;
	}
	
	protected abstract void update();
	
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
	public void onPause() {
		super.onPause();
	    if ( mBound ) {
	        unbindService(mConnection);
	    }
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = new Intent(this, FlippyPlayerService.class);
		intent.putExtra(Util.EXTRA_MESSENGER, new Messenger(getHandler()));
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}


}