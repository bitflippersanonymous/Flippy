package com.unklegeorge.flippy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

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

public class FlippyPlayerService extends Service implements MediaPlayer.OnPreparedListener {
	public static final String ACTION_PLAY = Util.PACKAGE + ".action.PLAY";
	private MediaPlayer mMediaPlayer = null;
	private final IBinder mBinder = new LocalBinder();
	private PlsAdapter mAdapter = null;
	private Bundle mExtras = null;
	private int mCurPlayingPos = 0;
	private boolean mLoadComplete = false;

	public boolean getloadComplete() {
		return mLoadComplete;
	}
	
	public PlsAdapter getPlsAdapter() {
		return mAdapter;
	}

	public int getPosition() {
		return mCurPlayingPos;
	}
	
	public boolean isPlaying() {
		return mMediaPlayer != null && mMediaPlayer.isPlaying();
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
		sendUpdate();
	}

	@Override
	public void onDestroy() {
		if ( mMediaPlayer != null ) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public boolean startPlay(int position) {
		if ( mMediaPlayer == null ) {
			mMediaPlayer = new MediaPlayer();
		}
		
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnPreparedListener(this);

		PlsEntry entry = mAdapter.getItem(mCurPlayingPos = position);

		try {
			mMediaPlayer.setDataSource(entry.getFile());
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

		Intent radioIntent = new Intent(getApplicationContext(), FlippyRadioActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
				radioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = entry.getTitle();
		notification.icon = R.drawable.icon;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(getApplicationContext(), "Flippy Player",
				"Playing: " + entry.getTitle(), pi);
		startForeground(R.string.radio_service_notif_id, notification);
		return true;
	}

	public void stopPlay() {
		stopForeground(true);
		onDestroy();
		sendUpdate();
	}

	public void sendUpdate() {
		mLoadComplete  = true;
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
			try { loadPlaylists(entries); } 
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
			sendUpdate();
		}

		@Override
		protected void onPreExecute() {
			mLoadComplete = false;
		}

		private void loadPlaylists(ArrayList<PlsEntry> entries) throws XmlPullParserException, IOException {
			XmlResourceParser parser = getResources().getXml(R.xml.playlists);
			int eventType = -1;
			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String strName = parser.getName();
					if (strName.equals(Util.PLAYLIST)) {
						String path = parser.getAttributeValue(null, Util.PATH);
						String name = parser.getAttributeValue(null, Util.NAME);
						readPlaylist(path, name, entries);
					}
				}
				eventType = parser.next();
			}
		}

		private String readPlaylist(String path, String name, ArrayList<PlsEntry> entries) {
			PlsEntry entry = null;
			String result = executeHttpGet(path);
			String lines[] = result.split(Util.NEWLINE);
			for ( int i=0; i<lines.length; i++ ) {
				String line = lines[i];
				if ( line.startsWith(Util.FILE) ) {
					entry = new PlsEntry(line.substring(Util.FILE.length()+2), name);
					entries.add(entry);
				} else if ( line.startsWith(Util.TITLE) ) {
					entry.setTitle(line.substring(Util.TITLE.length()+2));
				}
			}
			return result;
		}

		private String executeHttpGet(String path) {
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
					sb.append(line + Util.NEWLINE);
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
	}

}
