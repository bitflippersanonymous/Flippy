package com.example.flippy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class FlippySettingsActivity extends FlippyBase {
	static final int DIALOG_AYS_ID = 0;
	private SharedPreferences mSettings;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggleButton1);
        toggleButton.setChecked(mSettings.getBoolean(PREFERENCES_TOGGLE, true));
        
        toggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        Editor editor = mSettings.edit();
		        editor.putBoolean(PREFERENCES_TOGGLE, ((ToggleButton)v).isChecked());
		        editor.commit();
			}
		});
        
        Button nukeButton = (Button)findViewById(R.id.button_nuke);
        nukeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nukeSettings();
			}
		});
        
        Button aysButton = (Button)findViewById(R.id.button_are_you_sure);
        aysButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_AYS_ID);
			}
		});
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_AYS_ID:
			return createAYSDialog();
		default:
			return null;
		}
	}
	
	private Dialog createAYSDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.are_you_sure))
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                nukeSettings();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		return builder.create();
	}

	private void nukeSettings() {
        Editor editor = mSettings.edit();
        editor.clear();
        editor.commit();
        FlippySettingsActivity.this.finish();
	}
}
