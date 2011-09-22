package com.unklegeorge.flippy;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.unklegeorge.flippy.PlsEntry.Tags;


public class FlippyPlayerService extends Service implements MediaPlayer.OnPreparedListener {
	public static final String ACTION_PLAY = Util.PACKAGE + ".action.PLAY";
	private MediaPlayer mMediaPlayer = null;
	private final IBinder mBinder = new LocalBinder();
	private PlsAdapter mAdapter = null;
	private Bundle mExtras = null;
	private int mCurPlayingPos = 0;
	private boolean mLoadComplete = false;

	enum MediaState {
		STOP, PREPARE, PLAY
	}
	
	private MediaState mState = MediaState.STOP;
	
	public boolean getloadComplete() {
		return mLoadComplete;
	}
	
	public PlsAdapter getPlsAdapter() {
		return mAdapter;
	}

	public int getPosition() {
		return mCurPlayingPos;
	}
	
	public MediaState getState() {
		return mState;
	}

	public class LocalBinder extends Binder {
		FlippyPlayerService getService() {
			return FlippyPlayerService.this;
		}
	}

	@SuppressWarnings("unchecked")
	public void onCreate() {
		final ArrayList<PlsEntry> entries = new ArrayList<PlsEntry>();
		mAdapter = new PlsAdapter(entries, this);
		final LoadTask loadTask = new LoadTask();
		loadTask.execute(entries);
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

	@Override
	public IBinder onBind(Intent intent) {
		mExtras = intent.getExtras();
		return mBinder;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		mState = MediaState.PLAY;
		sendUpdate();
	}

	@Override
	public void onDestroy() {
		if ( mMediaPlayer != null ) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mState = MediaState.STOP;
	}

	public boolean startPlay(int position) {
		if ( mMediaPlayer == null ) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setOnPreparedListener(this);
		}
		
		PlsEntry entry = mAdapter.getItem(mCurPlayingPos = position);
		mMediaPlayer.reset();
		try {
			mMediaPlayer.setDataSource(entry.get(Tags.enclosure));
		} catch (IllegalArgumentException e) {
			Log.w(getClass().getName(), "Exception setting data source", e);
			return false;
		} catch (IllegalStateException e) {
			Log.w(getClass().getName(), "Exception setting data source", e);
			return false;
		} catch (IOException e) {
			Log.w(getClass().getName(), "Exception setting data source", e);
			return false;
		}

		mMediaPlayer.prepareAsync();
		mState = MediaState.PREPARE;

		Intent radioIntent = new Intent(getApplicationContext(), FlippyRadioActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
				radioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = entry.get(Tags.title);
		notification.icon = R.drawable.icon;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(getApplicationContext(), "Flippy Player",
				"Playing: " + entry.get(Tags.title), pi);
		startForeground(R.string.radio_service_notif_id, notification);
		sendUpdate();
		return true;
	}

	public void stopPlay() {
		sendUpdate();
		stopForeground(true);
		onDestroy();
	}

	public void sendUpdate() {
		Messenger messenger = (Messenger)mExtras.get(Util.EXTRA_MESSENGER);
		Message msg = Message.obtain();
		try {
			messenger.send(msg);
		}
		catch (android.os.RemoteException e) {
			Log.w(getClass().getName(), "Exception sending message", e);
		}
	}

	class LoadTask extends AsyncTask<ArrayList<PlsEntry>, Integer, Integer> {
		@Override
		protected Integer doInBackground(ArrayList<PlsEntry>... params) {
			ArrayList<PlsEntry> entries = params[0];
			XmlResourceParser parser = getResources().getXml(R.xml.accf_recent_message);
			try { PodcastParser.parse(entries, parser); } 
			catch(Exception e) { 
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
			mLoadComplete  = true;
			sendUpdate();
		}

		@Override
		protected void onPreExecute() {
			mLoadComplete = false;
		}


	}
}
