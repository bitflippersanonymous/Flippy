package com.example.flippy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import com.example.flippy.R.drawable;

public class Flippy extends FlippyBase implements View.OnClickListener,
		DialogInterface.OnClickListener {

	private static final int USELESS_DIALOG = 0;
	ArrayList<Score> mScores;
	SharedPreferences mSettings;
	int mCurrLoc;
	FlippyTask mFlippyTask;
	int mDialogHit = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mScores = new ArrayList<Score>();
		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		startTask();

		Button backButton = (Button) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateText(-1, true);
			}
		});

		Button nextButton = (Button) findViewById(R.id.button_next);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSettings.getBoolean(PREFERENCES_TOGGLE, true)) {
					updateText(1, true);
				}
			}
		});

		findViewById(R.id.button_count).setOnClickListener(this);
		findViewById(R.id.button_cancel).setOnClickListener(this);

		Animation inAnimation = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_in_left);
		Animation outAnimation = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
		TextSwitcher flippyTextSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher_flippy);
		flippyTextSwitcher.setInAnimation(inAnimation);
		flippyTextSwitcher.setOutAnimation(outAnimation);
		flippyTextSwitcher.setFactory(new MyTextSwitcherFactory());
		flippyTextSwitcher.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_count:
			startTask();
			break;
		case R.id.button_cancel:
			cancelTask();
			break;
		case R.id.textSwitcher_flippy:
			showDialog(USELESS_DIALOG);
			break;
		default:
			showDialog(v.getId());
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mDialogHit++;

		if (which >= 0) {
			dialog.cancel();
			String[] items = getResources().getStringArray(R.array.sarray);
			new AlertDialog.Builder(Flippy.this).setMessage(
					"You selected: " + which + " , " + items[which]).show();
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		dialog.setTitle(String.valueOf(mDialogHit) + " Times");
	}

	@Override
    protected Dialog onCreateDialog(int id, Bundle args) {
    	switch ( id ) {
    	case USELESS_DIALOG:
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
	    	.setTitle(String.valueOf(mDialogHit) + " Times")
	    	.setPositiveButton(R.string.got_it, this)
	    	.setCancelable(true);
	    	return builder.create();
    	case R.id.button1:
	    	return (new AlertDialog.Builder(this))
	    	.setMessage(((Button)findViewById(id)).getText())
	    	.setCancelable(true)
	    	.create();
    	case R.id.button2:
    		return (new AlertDialog.Builder(this))
    		.setItems(R.array.sarray, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				String[] items = getResources().getStringArray(R.array.sarray);
    				new AlertDialog.Builder(Flippy.this)
    				.setMessage("You selected: " + which + " , " + items[which])
    				.show();
    			}
    		})
	    	.setCancelable(true)
	    	.create();
    	case R.id.button3:
    		return (new AlertDialog.Builder(this))
    		.setSingleChoiceItems(R.array.sarray, 0, this)
    		.setCancelable(true)
    		.create();
    	case R.id.button4:
    		LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert, null);
    		return (new AlertDialog.Builder(this))
    		.setCancelable(true)
    		.setView(textEntryView)
	    	.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				EditText edit = (EditText) textEntryView.findViewById(R.id.editText1);
    				Button button = (Button) findViewById(R.id.button4);
    				button.setText(edit.getText());
    				
    			}})
    		.create();
		case R.id.button5:
			Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.custom_dialog);
			dialog.setTitle(R.string.how_custom);
			TextView text = (TextView) dialog.findViewById(R.id.cust_text);
			text.setText("Hello, this is a custom dialog!");
			ImageView image = (ImageView) dialog.findViewById(R.id.cust_image);
			image.setImageResource(R.drawable.icon);
			return dialog;
    	default:
    	}
    	return null;
    }

	protected void startTask() {
		findViewById(R.id.button_back).setEnabled(false);
		findViewById(R.id.button_next).setEnabled(false);
		mFlippyTask = new FlippyTask();
		mFlippyTask.execute(1);
	}

	protected void cancelTask() {
		if (mFlippyTask != null
				&& mFlippyTask.getStatus() != AsyncTask.Status.FINISHED) {
			mFlippyTask.cancel(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Editor editor = mSettings.edit();
		editor.putInt(PREFERENCES_LOCATION, mCurrLoc);
		editor.commit();
		cancelTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCurrLoc = mSettings.getInt(PREFERENCES_LOCATION, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu); // need this?
		getMenuInflater().inflate(R.menu.options, menu);
		menu.findItem(R.id.settings_menu_item).setIntent(
				new Intent(this, FlippySettingsActivity.class));
		menu.findItem(R.id.help_menu_item).setIntent(
				new Intent(this, FlippyHelpActivity.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}

	protected void updateText(int offset, boolean animate) {
		if (mScores.size() == 0) {
			return;
		}

		Boolean load = false;
		mCurrLoc += offset;
		if (mCurrLoc < 0) {
			mCurrLoc = mScores.size() - 1;
			load = true;
		}
		if (mCurrLoc >= mScores.size()) {
			mCurrLoc = 0;
			load = true;
		}

		if (load)
			startTask();
		else {
			Score score = mScores.get(mCurrLoc);
			String scoreString = score.mUsername + " " + score.mScore + " "
					+ score.mRank;
			TextSwitcher flippyTextSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher_flippy);
			if (animate)
				flippyTextSwitcher.setText(scoreString);
			else
				flippyTextSwitcher.setCurrentText(scoreString);
		}
	}

	protected void loadScores() throws XmlPullParserException, IOException {
		XmlResourceParser scores = getResources().getXml(R.xml.allscores);
		int eventType = -1;
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strName = scores.getName();
				if (strName.equals("score")) {
					String xUsername = scores.getAttributeValue(null,
							"username");
					String xScore = scores.getAttributeValue(null, "score");
					String xRank = scores.getAttributeValue(null, "rank");
					Score score = new Score(xUsername, Integer.valueOf(xScore),
							Integer.valueOf(xRank));
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
			int max = (Integer) params[0];
			mProgress.setMax(max);
			for (int i = 0; i <= max; i++) {
				if (!isCancelled()) {
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
			findViewById(R.id.button_back).setEnabled(true);
			findViewById(R.id.button_next).setEnabled(true);
			updateText(0, false);
		}

		@Override
		protected void onPreExecute() {
			mProgress = (ProgressBar) findViewById(R.id.progress);
		}
	}

}
