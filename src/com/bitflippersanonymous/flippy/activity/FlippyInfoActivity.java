package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.PlsEntry;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;
import com.bitflippersanonymous.flippy.service.FlippyPlayerService.MediaState;
import com.bitflippersanonymous.flippy.util.Util;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class FlippyInfoActivity extends FlippyBaseActivity {

	private PlsEntry mEntry = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		int entryId = -1;
		Bundle extras = getIntent().getExtras();
		if ( extras != null )
			entryId = extras.getInt(Util.ID, -1);
		
		// Update info view with entry passed in intent
		if ( entryId > 0 ) {
			Cursor cursor = getService().getDbAdapter().fetchEntry(entryId, 0);
			mEntry = PlsEntry.cursorToEntry(cursor);
			cursor.close();
		} else { 
			mEntry = getService().getCurrentEntry();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.imageButtonPP:
			if ( getService().getState() != MediaState.STOP )
				getService().stopPlay();	
			else
				getService().startPlay(getService().getCurrentEntry(), 0);
			break;
		case R.id.LinearLayoutControl:
			mEntry = getService().getCurrentEntry();
			update();
			break;
		case R.id.ButtonQueue:
			getService().toggleInQueue(mEntry);
			break;
		case R.id.ButtonPlay:
			getService().startPlay(mEntry, 0);
			break;
		default:
			super.onClick(v);
		}
	}

	// Gets called when service binder is connected
	@Override
	protected void update() {
		super.update();

		if ( mEntry != null ) {
			TextView viewInfo = (TextView) findViewById(R.id.viewInfo);
			viewInfo.setText(mEntry.get(Tags.description));
		}

    	// Update current playing footer info 
    	final PlsEntry entry = getService().getCurrentEntry();
    	if ( entry != null ) {
	    	final TextView text = (TextView) findViewById(R.id.textViewFooter);
	    	if ( getService().getState() == MediaState.STOP ) { 
	    		setPPIcon(false);
	    		text.setText(null);
	    	} else {
	    		setPPIcon(true);
	    	}

	    	text.setText(entry.get(Tags.title));			
	    	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	    	seekBar.setProgress(seekBar.getMax());
    	}
	}
	
	protected void setPPIcon(boolean state) {
		ImageView buttonPlay = (ImageView) findViewById(R.id.imageButtonPP);
		if ( state )
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause));
		else
			buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.play));
	}
	
	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.info));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.info_menu));
	}


	//TODO: needs info about the title, current location etc.
/*    	
	<string name="welcome_messages">Hello, %1$s! You have %2$d new messages.</string>
	In this example, the format string has two arguments: %1$s is a string and %2$d is a decimal number. You can format the string with arguments from your application like this:
	Resources res = getResources();
	String text = String.format(res.getString(R.string.welcome_messages), username, mailCount);
	
	String desc = TextUtils.htmlEncode(entry.get(Tags.description));
	
	final String description = "<html><body bgcolor='#CCCCCC'>" + desc + "</body></html>";
	WebView webview = (WebView) findViewById(R.id.webViewInfo);
	webview.setBackgroundColor(getResources().getColor(R.color.infoBackground));
	webview.loadData(description, "text/html", "utf-8");
	*/
	
}
