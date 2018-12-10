package com.codeworks.pai;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	private static final String	TAG	= SettingsFragment.class.getSimpleName();
	public static final String	KEY_PREF_SYNC_CONN		= "pref_syncConnectionType";
	public static final String  PREF_UPDATE_FREQUENCY   = "pref_updateFrequency";
	public static final String	PREF_PORTFOLIO_NAME1	= "pref_portfolio_name1";
	public static final String	PREF_PORTFOLIO_NAME2	= "pref_portfolio_name2";
	public static final String	PREF_PORTFOLIO_NAME3	= "pref_portfolio_name3";
	public static final String	PREF_PORTFOLIO_TYPE1	= "pref_portfolio_type1";
	public static final String	PREF_PORTFOLIO_TYPE2	= "pref_portfolio_type2";
	public static final String	PREF_PORTFOLIO_TYPE3	= "pref_portfolio_type3";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		updateSummary();
	}


	void updateSummary() {
		for (int x = 1; x < 4; x++) {
			updateSummaryName("pref_portfolio_name"+x);
			updateSummaryType("pref_portfolio_type"+x);
		}
		updateRingtoneSummary();
		updateVibrateSummary();
		updateFrequenceSummary();
	}

	void updateSummaryName(String key) {
		EditTextPreference pref = (EditTextPreference) findPreference(key);
		if (pref != null) {
			Log.d(TAG, "Setting Preference " + key + " = " + pref.getText());
			pref.setSummary(pref.getText());
		} else {
			Log.d(TAG, "PREF IS NULL");
		}
	}
	
	void updateRingtoneSummary() {
		RingtonePreference pref = (RingtonePreference) findPreference(PaiUtils.PREF_RINGTONE);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String ringtoneName = sharedPreferences.getString(PaiUtils.PREF_RINGTONE, "none");
	    updateRingtoneSummary((RingtonePreference) pref, ringtoneName);
	}
	
	void updateRingtoneSummary(RingtonePreference pref, String ringtoneName) {
		Log.d(TAG,"Update ringtone summary "+ringtoneName);
		if (ringtoneName != null && !ringtoneName.equals("")) {
			Uri ringtoneUri = Uri.parse(ringtoneName);
			Log.d(TAG, "Set Summary ringtone 2 " + ringtoneUri);
			Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
			if (ringtone != null)
				pref.setSummary(ringtone.getTitle(getActivity()));
			else {
				pref.setSummary(getActivity().getResources().getString(R.string.pref_ringtone_silent));
			}
		} else {
			pref.setSummary(getActivity().getResources().getString(R.string.pref_ringtone_silent));
		}
	}

	void updateVibrateSummary() {
		Log.d(TAG,"update vibrate summary");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean vibrate = sharedPreferences.getBoolean(PaiUtils.PREF_VIBRATE_ON, false);
		CheckBoxPreference pref = (CheckBoxPreference) findPreference(PaiUtils.PREF_VIBRATE_ON);
		Log.d(TAG, "Set vibrate  "+vibrate);
		pref.setSummary(vibrate ? getResources().getString(R.string.pref_vibrate_on) : getResources().getString(R.string.pref_vibrate_off));		
	}

	String updateSummaryType(String key) {
		String value = "E";
		ListPreference pref = (ListPreference) findPreference(key);
		if (pref != null) {
			Log.d(TAG, "Setting Preference " + key + " = " + pref.getValue());
			value = pref.getValue();
			if ("E".equals(value)) {
				pref.setSummary("EMA");
			} else if ("S".equals(value)) {
				pref.setSummary("SMA");
			} else if ("D".equals(value)) {
				pref.setSummary("DEMAND ZONE");
			}
		} else {
			Log.d(TAG, "PREF IS NULL");
		}
		return value;
	}

	void updateFrequenceSummary() {
		ListPreference pref = (ListPreference) findPreference(PREF_UPDATE_FREQUENCY);
		if (pref != null) {
			Log.d(TAG, "Setting Preference " + PREF_UPDATE_FREQUENCY + " = " + pref.getValue());
			String value = pref.getValue();
			Resources res = getResources();
			String[] freqStrings = res.getStringArray(R.array.pref_updateFrequencyTypes_entries);
			String[] freqValues = res.getStringArray(R.array.pref_updateFrequencyTypes_values);
			for (int i=0; i < freqValues.length; i++) {
				if (freqValues[i].equals(value)) {
					pref.setSummary(freqStrings[i]);
				}
			}
		} else {
			Log.d(TAG, PREF_UPDATE_FREQUENCY + "PREF IS NULL");
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
	    updateRingtoneSummary((RingtonePreference) preference, (String) newValue);
	    return true;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG,"onSharedPreferenceChanged for key "+key);
		if (key.equals(KEY_PREF_SYNC_CONN)) {
			// SharedPreferences sharedPref =
			// PreferenceManager.getDefaultSharedPreferences(this);
			// Set summary to be the user-description for the selected value
			// sharedPref. Summary(sharedPreferences.getString(key, ""))

		} else if (PREF_PORTFOLIO_NAME1.equals(key) || PREF_PORTFOLIO_NAME2.equals(key) || PREF_PORTFOLIO_NAME3.equals(key)) {
			updateSummaryName(key);
		} else if (PREF_PORTFOLIO_TYPE1.equals(key) || PREF_PORTFOLIO_TYPE2.equals(key) || PREF_PORTFOLIO_TYPE3.equals(key)) {
			String portfolioId = "1";
		    if (PREF_PORTFOLIO_TYPE1.equals(key)) {
		    	portfolioId = "1";
		    } else if (PREF_PORTFOLIO_TYPE2.equals(key)) {
		    	portfolioId = "2";
		    } else if (PREF_PORTFOLIO_TYPE3.equals(key)) {
		    	portfolioId = "3";
		    }
			String value = updateSummaryType(key);
			String selection = StudyTable.COLUMN_PORTFOLIO_ID + " = ?";
			String[] selectionArgs = { portfolioId };
			ContentValues values = new ContentValues();
			values.put(StudyTable.COLUMN_MA_TYPE, String.valueOf(value.charAt(0)));
			getActivity().getContentResolver().update(PaiContentProvider.PAI_STUDY_URI, values, selection, selectionArgs);
		} else if (PaiUtils.PREF_RINGTONE.equals(key)) {
			updateRingtoneSummary();
		} else if (PaiUtils.PREF_VIBRATE_ON.equals(key)) {
			updateVibrateSummary();
		} else if (PREF_UPDATE_FREQUENCY.equals(key)) {
			updateFrequenceSummary();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		   // A patch to overcome OnSharedPreferenceChange not being called by RingtonePreference bug 
	    RingtonePreference pref = (RingtonePreference) findPreference(PaiUtils.PREF_RINGTONE);
	    pref.setOnPreferenceChangeListener(this);
        TrackerUtil.sendScreenView(getActivity(), R.string.trackSettings);
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);
	}

}
