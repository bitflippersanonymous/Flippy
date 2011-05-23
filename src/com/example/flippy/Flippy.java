package com.example.flippy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

public class Flippy extends FlippyBase implements View.OnClickListener {
	
	ArrayList<Score> mScores;
    SharedPreferences mSettings;
	int mCurrLoc;
	FlippyTask mFlippyTask;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mScores = new ArrayList<Score>();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        startTask(mFlippyTask);
        
        Button backButton = (Button)findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateText(-1, true);
			}
		});
        
        Button nextButton = (Button)findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        if ( mSettings.getBoolean(PREFERENCES_TOGGLE, true) ) {
					updateText(1, true);
		        }
			}
		});
        
        findViewById(R.id.button_count).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        TextSwitcher flippyTextSwitcher = (TextSwitcher)findViewById(R.id.textSwitcher_flippy);
        flippyTextSwitcher.setInAnimation(inAnimation);
        flippyTextSwitcher.setOutAnimation(outAnimation);
        flippyTextSwitcher.setFactory(new MyTextSwitcherFactory());
    }
    
    public void onClick(View v) {
    	switch(v.getId()) {
    	case R.id.button_count:
    		startTask(mFlippyTask);
    		break;
    	case R.id.button_cancel:
    		cancelTask(mFlippyTask);
    		break;
    	default:
    	}
    }
    
    protected void startTask(FlippyTask task) {
    	task = new FlippyTask();
    	task.execute(2);
    }
    
    protected void cancelTask(FlippyTask task) {
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
        	task.cancel(true);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Editor editor = mSettings.edit();
        editor.putInt(PREFERENCES_LOCATION, mCurrLoc);
        editor.commit();
        cancelTask(mFlippyTask);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
       	mCurrLoc = mSettings.getInt(PREFERENCES_LOCATION, 0);
        updateText(0, false);
    }    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu); // need this?
        getMenuInflater().inflate(R.menu.options, menu);
        menu.findItem(R.id.settings_menu_item).setIntent(new Intent(this, FlippySettingsActivity.class));
        menu.findItem(R.id.help_menu_item).setIntent(new Intent(this, FlippyHelpActivity.class));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        startActivity(item.getIntent());
        return true;
    }
	
	protected void updateText(int offset, boolean animate) {
		if ( mScores.size() == 0 ) {
			return;
		}
		
		mCurrLoc += offset;
		if ( mCurrLoc < 0 )
			mCurrLoc = mScores.size()-1;
		if ( mCurrLoc >= mScores.size() )
			mCurrLoc = 0;

		Score score = mScores.get(mCurrLoc);
		String scoreString = score.mUsername + " " + score.mScore + " " + score.mRank;
        TextSwitcher flippyTextSwitcher = (TextSwitcher)findViewById(R.id.textSwitcher_flippy);
        if ( animate )
        	flippyTextSwitcher.setText(scoreString);
        else
        	flippyTextSwitcher.setCurrentText(scoreString);
	}
	
	protected void loadScores() throws XmlPullParserException, IOException {
        XmlResourceParser scores = getResources().getXml(R.xml.allscores);
        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strName = scores.getName();
                if (strName.equals("score")) {
                    String xUsername = scores.getAttributeValue(null, "username");
                    String xScore = scores.getAttributeValue(null, "score");
                    String xRank = scores.getAttributeValue(null, "rank");
                    Score score = new Score(xUsername, Integer.valueOf(xScore), Integer.valueOf(xRank));
                    mScores.add(score);
                }
            }
            eventType = scores.next();
        }
	}

	private class Score {
		String mUsername;
		int mScore;
		int mRank;
		Score(String username, int score, int rank) {
			mUsername = username;
			mScore = score;
			mRank = rank;
		}
	}

	public class MyTextSwitcherFactory implements ViewFactory {
		@Override
		public View makeView() {
			TextView textView = new TextView(Flippy.this);
			textView.setGravity(Gravity.CENTER);
			Resources res = getResources();
			float dimension = res.getDimension(R.dimen.text_size);
			textView.setTextSize(dimension);
			return textView;
		}
	}
	
	private class FlippyTask extends AsyncTask<Object, Integer, Integer> {
		ProgressBar mProgress;
		
		@Override
        protected Integer doInBackground(Object... params) {
        	int max = (Integer)params[0];
			mProgress.setMax(max);
        	for ( int i = 0; i <= max; i++ ) {
        		if ( !isCancelled() ) {
        			publishProgress(i);
        		} else {
        			return -1;
        		}
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
			try {
				loadScores();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
        	return 0;
        }
        @Override
        protected void onCancelled() {
        	mProgress.setMax(0);
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
        	mProgress.setProgress(progress[0]);
        }
        @Override
        protected void onPostExecute(Integer result) {
        	mProgress.setMax(0);
        }
        @Override
        protected void onPreExecute() {
        	mProgress = (ProgressBar)findViewById(R.id.progress);
        }
	}
}

