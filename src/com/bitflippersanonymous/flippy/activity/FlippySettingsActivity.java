package com.bitflippersanonymous.flippy.activity;

import com.bitflippersanonymous.flippy.R;
import com.bitflippersanonymous.flippy.util.Util;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


public class FlippySettingsActivity extends Activity {
	static final int DIALOG_AYS_ID = 0;
	private SharedPreferences mSettings;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mSettings = getSharedPreferences(Util.APP_PREFERENCES, Context.MODE_PRIVATE);
       
    }

}
