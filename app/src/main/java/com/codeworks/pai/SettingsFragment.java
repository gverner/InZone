package com.codeworks.pai;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.processor.NotifierImpl;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
    public static final String PREF_UPDATE_FREQUENCY = "pref_updateFrequency";
    public static final String PREF_PORTFOLIO_NAME1 = "pref_portfolio_name1";
    public static final String PREF_PORTFOLIO_NAME2 = "pref_portfolio_name2";
    public static final String PREF_PORTFOLIO_NAME3 = "pref_portfolio_name3";
    public static final String PREF_PORTFOLIO_TYPE1 = "pref_portfolio_type1";
    public static final String PREF_PORTFOLIO_TYPE2 = "pref_portfolio_type2";
    public static final String PREF_PORTFOLIO_TYPE3 = "pref_portfolio_type3";
    public static final int REQUEST_CODE_ALERT_RINGTONE = 999;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        updateSummary();
    }

    void updateSummary() {
        for (int x = 1; x < 4; x++) {
            updateSummaryName("pref_portfolio_name" + x);
            updateSummaryType("pref_portfolio_type" + x);
        }
        updateFrequenceSummary();
    }

    void updateSummaryName(String key) {
        EditTextPreference pref = findPreference(key);
        if (pref != null) {
            Log.d(TAG, "Setting Preference " + key + " = " + pref.getText());
            pref.setSummary(pref.getText());
        } else {
            Log.d(TAG, "PREF IS NULL");
        }
    }

    void callSystemChannelStyle() {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, "com.codeworks.pai");
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotifierImpl.CHANNEL_ID);
        startActivity(intent);
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
            for (int i = 0; i < freqValues.length; i++) {
                if (freqValues[i].equals(value)) {
                    pref.setSummary(freqStrings[i]);
                }
            }
        } else {
            Log.d(TAG, PREF_UPDATE_FREQUENCY + "PREF IS NULL");
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged for key " + key);
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
            String[] selectionArgs = {portfolioId};
            ContentValues values = new ContentValues();
            values.put(StudyTable.COLUMN_MA_TYPE, String.valueOf(value.charAt(0)));
            getActivity().getContentResolver().update(PaiContentProvider.PAI_STUDY_URI, values, selection, selectionArgs);
        } else if (PaiUtils.PREF_RINGTONE.equals(key)) {
            //updateRingtoneSummary();
        } else if (PaiUtils.PREF_VIBRATE_ON.equals(key)) {
            //updateVibrateSummary();
        } else if (PREF_UPDATE_FREQUENCY.equals(key)) {
            updateFrequenceSummary();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(PaiUtils.PREF_VIBRATE_ON)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callSystemChannelStyle();
            }
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

}
