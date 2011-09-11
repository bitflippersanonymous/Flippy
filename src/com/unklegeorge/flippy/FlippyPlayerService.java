package com.unklegeorge.flippy;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;

public class FlippyPlayerService extends Service  implements MediaPlayer.OnPreparedListener {
    private static final String ACTION_PLAY = "com.unklegeorge.action.PLAY";
    MediaPlayer mMediaPlayer = null;
    
    public int onStartCommand(Intent intent, int flags, int startId) {
		
        if (intent.getAction().equals(ACTION_PLAY)) {
        	if ( mMediaPlayer == null ) {
        		mMediaPlayer = new MediaPlayer();
        		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        		mMediaPlayer.setOnPreparedListener(this);
        	} else
        		mMediaPlayer.reset(); // Assume this is never called?
    		
        	Bundle bundle = intent.getBundleExtra(name);
    		String file = null;
    		try {
    			mMediaPlayer.setDataSource(file);
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalStateException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
            mMediaPlayer.prepareAsync();
        }
    	return START_STICKY; 
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

    @Override
    public void onDestroy() {
    	if ( mMediaPlayer != null ) {
    		mMediaPlayer.release();
    		mMediaPlayer = null;
    	}
    }

}
