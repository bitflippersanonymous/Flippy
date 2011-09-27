package com.unklegeorge.flippy;

import com.unklegeorge.flippy.PlsEntry.Tags;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class FlippyInfoActivity extends FlippyActivityBase {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
	}

	@Override
	protected void update() {
		if ( !getService().getloadComplete() )
			return;
		
		super.update();
		
    	final PlsAdapter adapter = getService().getPlsAdapter();
    	final PlsEntry entry = adapter.getItem(getService().getPosition());
/*    	
    	<string name="welcome_messages">Hello, %1$s! You have %2$d new messages.</string>
    	In this example, the format string has two arguments: %1$s is a string and %2$d is a decimal number. You can format the string with arguments from your application like this:
    	Resources res = getResources();
    	String text = String.format(res.getString(R.string.welcome_messages), username, mailCount);
  */  	
    	String desc = TextUtils.htmlEncode(entry.get(Tags.description));
    	/*
    	final String description = "<html><body bgcolor='#CCCCCC'>" + desc + "</body></html>";
		WebView webview = (WebView) findViewById(R.id.webViewInfo);
		webview.setBackgroundColor(getResources().getColor(R.color.infoBackground));
		webview.loadData(description, "text/html", "utf-8");
		*/
    	
    	TextView viewInfo = (TextView) findViewById(R.id.viewInfo);
    	viewInfo.setText(desc);
	}

}
