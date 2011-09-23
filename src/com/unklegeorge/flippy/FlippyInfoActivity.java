package com.unklegeorge.flippy;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class FlippyInfoActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		 String summary = "<html><body>You scored <b>192</b> points.</body></html>";
		 WebView webview = (WebView) findViewById(R.id.webViewInfo);
		 webview.loadData(summary, "text/html", "utf-8");
	}
}
