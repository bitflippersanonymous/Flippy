package com.unklegeorge.flippy;

import com.unklegeorge.flippy.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


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
