package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.domain.PlsEntry;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FlippyInfoActivity extends FlippyBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
	}

	// Gets called when service binder is connected
	@Override
	protected void update() {
		super.update();
		
    	final PlsEntry entry = getService().getCurrentEntry();
    	if ( entry == null )
    		return;
    	
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
       	TextView viewInfo = (TextView) findViewById(R.id.viewInfo);
    	viewInfo.setText(entry.get(Tags.description));
	}
	
	// Invoked via reflection in MainActivity
	public static void popMenuView(View view) {
		ImageView icon = (ImageView) view.findViewById(R.id.EntryIcon);
		icon.setImageDrawable(view.getContext().getResources().getDrawable(R.drawable.info));
		TextView title = (TextView) view.findViewById(R.id.entryTitle);
		title.setText(view.getResources().getString(R.string.info_menu));
	}

}
