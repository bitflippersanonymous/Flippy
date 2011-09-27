package com.unklegeorge.flippy;

import android.os.Bundle;
import android.webkit.WebView;

public class FlippyInfoActivity extends FlippyActivityBase {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
	}

	@Override
	protected void update() {
		 String summary = "<html><body>You scored <b>192</b> points.</body></html>";
		 WebView webview = (WebView) findViewById(R.id.webViewInfo);
		 webview.loadData(summary, "text/html", "utf-8");		
	}

}
