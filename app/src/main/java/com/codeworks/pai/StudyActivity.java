/*
 *
 * Copyright (C) 2011 The Andrfoid Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.codeworks.pai;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codeworks.pai.processor.UpdateService;

/**
 * Starts up the task list that will interact with the AccessibilityService
 * sample.
 */
public class StudyActivity extends Activity implements StudyEListFragment.OnItemSelectedListener, StudySListFragment.OnItemSelectedListener, ActionBar.TabListener, OnSharedPreferenceChangeListener {
	//private static final String TAG = StudyActivity.class.getSimpleName();

	private Intent dailyIntent;
	private int portfolioId = 1;
	boolean serviceStartedByCreate = false;
	// List<PaiStudy> quotes = new ArrayList<PaiStudy>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		//dailyIntent = new Intent(this, UpdateService.class);
		dailyIntent = new Intent(UpdateService.class.getName());
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL);
		startService(dailyIntent);
		
		serviceStartedByCreate = true;
		       
		setContentView(R.layout.study_activity_frame);	
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        //actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // For each of the sections in the app, add a tab to the action bar.
 		Resources resources = getResources();
		//SharedPreferences sharedPreferences = getSharedPreferences(PaiUtils.PREF_FILE, MODE_PRIVATE);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		for (int i = 1; i < 4; i++) {
			String portfolioName = PaiUtils.getPortfolioName(resources, sharedPreferences, i);
			actionBar.addTab(actionBar.newTab().setText(portfolioName).setTabListener(this));
		}
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		String strategy = PaiUtils.getStrategy(sharedPreferences, portfolioId);
		// the fragment_container FrameLayout
        if (findViewById(R.id.study_activity_frame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            Fragment firstFragment;
            if (PaiUtils.MA_TYPE_EMA.equals(strategy)) {
            	firstFragment = new StudyEListFragment();
            } else {
            	firstFragment = new StudySListFragment();
            }
            
            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
    	    Bundle args = new Bundle();
    	    args.putInt(StudyEListFragment.ARG_PORTFOLIO_ID, 1);
    	    firstFragment.setArguments(args);
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.study_activity_frame, firstFragment).commit();
        }
	}
	
	@Override
	public void onSStudySelected(Long studyId) {
		onStudySelected(studyId); 
	}		
	  @Override
	public void onStudySelected(Long studyId) {
		if (studyId < 1) {
			return;
		}
		StudyEDetailFragment fragment = (StudyEDetailFragment) getFragmentManager().findFragmentById(R.id.study_detail_frame);
		if (fragment != null && fragment.isInLayout()) {

			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			Fragment oldFragment = (StudyEListFragment) getFragmentManager().findFragmentById(R.id.study_detail_frame);
			if (oldFragment != null) {
				fragmentTransaction.remove(oldFragment);
			}
			// Create fragment and give it an argument specifying the article it
			// should show

			Fragment newFragment;
			if (PaiUtils.MA_TYPE_EMA.equals(PaiUtils.getStrategy(this, portfolioId))) {
				newFragment = new StudyEDetailFragment();
				Bundle args = new Bundle();
				args.putLong(StudyEDetailFragment.ARG_STUDY_ID, studyId);
				newFragment.setArguments(args);
			} else {
				newFragment = new StudySDetailFragment();
				Bundle args = new Bundle();
				args.putLong(StudySDetailFragment.ARG_STUDY_ID, studyId);
				newFragment.setArguments(args);
			}

			fragmentTransaction.add(R.id.study_detail_frame, newFragment);
			fragmentTransaction.commit();
		} else {
			Intent intent = new Intent(getApplicationContext(), StudyDetailActivity.class);
			intent.putExtra(StudyDetailActivity.STUDY_ID, studyId);
			intent.putExtra(StudyDetailActivity.PORTFOLIO_ID, portfolioId);
			startActivity(intent);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemStartSerivce:
			dailyIntent = new Intent(UpdateService.class.getName());
			dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL_MENU);
			startService(dailyIntent);
			break;
		
		case R.id.itemStopService:
			stopService(dailyIntent);
			break;
		
		case R.id.portfolio:
			Intent intent = new Intent();
			intent.setClassName(getPackageName(), SecurityListActivity.class.getName());
			intent.putExtra(SecurityListActivity.ARG_PORTFOLIO_ID, portfolioId);
			startActivity(intent);
			break;

		case R.id.action_settings:
			Intent settingsIntent = new Intent();
			settingsIntent.setClassName(getPackageName(), SettingsActivity.class.getName());
			startActivity(settingsIntent);
			break;
			
		case R.id.itemServiceLog:
			Intent serviceLogIntent = new Intent();
			serviceLogIntent.setClassName(getPackageName(), ServiceLogListActivity.class.getName());
			startActivity(serviceLogIntent);
			break;
		}
		
		return true;
	}

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// save portfolioId for calls to portfolio
		portfolioId = tab.getPosition() + 1;
		Fragment oldFragment = getFragmentManager().findFragmentById(R.id.study_activity_frame);
		if (oldFragment != null) {
			fragmentTransaction.remove(oldFragment);
			// Create fragment and give it an argument specifying the article it
			// should show
			Fragment newFragment = createFragment();
			Bundle args = new Bundle();
			args.putInt(StudyEListFragment.ARG_PORTFOLIO_ID, tab.getPosition() + 1);
			newFragment.setArguments(args);

			fragmentTransaction.add(R.id.study_activity_frame, newFragment);
		}
	}

	Fragment createFragment() {
		Fragment newFragment;
		if (PaiUtils.MA_TYPE_EMA.equals(PaiUtils.getStrategy(this, portfolioId))) {
			newFragment = new StudyEListFragment();
		} else {
			newFragment = new StudySListFragment();
		}
		return newFragment;
	}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (!serviceStartedByCreate) {
			dailyIntent = new Intent(this, UpdateService.class);
			dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_PRICE_UPDATE);
			startService(dailyIntent);
		}
		serviceStartedByCreate = false;
	}

	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(StudyActivity.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if ((PaiUtils.PREF_PORTFOLIO_KEY + 1).equals(key)) {
			getActionBar().getTabAt(0).setText(sharedPreferences.getString(key, ""));
		}
		if ((PaiUtils.PREF_PORTFOLIO_KEY + 2).equals(key)) {
			getActionBar().getTabAt(1).setText(sharedPreferences.getString(key, ""));
		}
		if ((PaiUtils.PREF_PORTFOLIO_KEY + 3).equals(key)) {
			getActionBar().getTabAt(2).setText(sharedPreferences.getString(key, ""));
		}

	}


}
