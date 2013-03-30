/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
	
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package com.duckduckgo.mobile.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.duckduckgo.mobile.android.DDGApplication;
import com.duckduckgo.mobile.android.R;
import com.duckduckgo.mobile.android.util.DDGControlVar;
import com.duckduckgo.mobile.android.util.DDGUtils;
import com.duckduckgo.mobile.android.util.PreferencesManager;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final int CONFIRM_CLEAR_HISTORY = 100;
	private boolean hasClearedHistory = false; 
	
  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
    	addPreferencesFromResource(R.xml.preferences);
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    	Preference clearHistoryPref = findPreference("clearHistoryPref");
    	clearHistoryPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
    		public boolean onPreferenceClick(Preference preference) {
    			showDialog(CONFIRM_CLEAR_HISTORY);		
    			return true;
    		}
    	});
    	
		Preference sourcesPref = findPreference("sourcesPref");
		sourcesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getBaseContext(), SourcePreferences.class);
		        startActivity(intent);
				
				return true;
			}
		});
		
		
		Preference sendFeedbackPref = findPreference("sendFeedbackPref");
		sendFeedbackPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				Context context = getPreferenceScreen().getContext();
				
				Intent intent = DDGUtils.newEmailIntent(context.getResources().getString(R.string.FeedbackTo), 
						context.getResources().getString(R.string.FeedbackSubject), DDGUtils.getBuildInfo(context), "");
		        startActivity(Intent.createChooser(intent, "Select application to send"));
				return true;
			}
		});
		
		Preference ratePref = findPreference("ratePref");
		ratePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.duckduckgo.mobile.android"));
				startActivity(intent);
				return true;
			}
		});
		
		Preference mainFontSizePref = findPreference("mainFontSizePref");
		mainFontSizePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				DDGControlVar.prevMainTextSize = DDGControlVar.mainTextSize;
				DDGControlVar.prevWebViewTextSize = DDGControlVar.webViewTextSize;
				DDGControlVar.prevLeftTitleTextSize = DDGControlVar.leftTitleTextSize;
				finish();
				return true;
			}
		});
		
    }
  }
  
  @Override
  public Dialog onCreateDialog(int id) {
	  Dialog d;
	  switch(id) {
		  case CONFIRM_CLEAR_HISTORY:
			  d = new AlertDialog.Builder(this)
			  .setTitle(getResources().getString(R.string.Confirm))
			  .setMessage(getResources().getString(R.string.ConfirmClearHistory))
			  .setIcon(android.R.drawable.ic_dialog_alert)
			  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	
				  public void onClick(DialogInterface dialog, int whichButton) {
					  
					  DDGApplication.getDB().deleteHistory();
		    		  hasClearedHistory = true;
					  
				  }})
				  .setNegativeButton(android.R.string.no, new OnClickListener() {
					
					@SuppressWarnings("deprecation")
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(Preferences.CONFIRM_CLEAR_HISTORY);
						
					}
				}).create();
	
			  break;
		  default:
			  d = null;
	  }
	  return d;
  }
  
  @Override
  public void finish() {
	  Intent res = new Intent();
	  res.putExtra("hasClearedHistory", hasClearedHistory);
	  setResult(RESULT_OK, res);
	  super.finish();
  }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PreferencesManager.onSharedPreferenceChanged(sharedPreferences, key);
    }
	
}
