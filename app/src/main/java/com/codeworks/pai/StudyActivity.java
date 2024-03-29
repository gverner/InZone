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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.processor.UpdateService;


/**
 * Starts up the task list that will interact with the AccessibilityService
 * sample.
 */
public class StudyActivity extends Activity implements OnItemSelectedListener, OnSharedPreferenceChangeListener {
    private static final String TAG = StudyActivity.class.getSimpleName();

	private int portfolioId = 1;
    Menu menu;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG,"On Create savedInstanceState "+(savedInstanceState == null ? "null" : "not null"));
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/*
		dailyIntent = new Intent(UpdateService.class.getName());
		dailyIntent.setPackage(UpdateService.class.getPackage().getName());
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL);

		startService(dailyIntent);
        */
        //oneTimeWorkRequest = WorkerUtil.startSingle(UpdateWorker.ACTION_MANUAL);

		setContentView(R.layout.study_activity_frame);


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        //actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // For each of the sections in the app, add a tab to the action bar.
 		Resources resources = getResources();
		for (int i = 1; i < 4; i++) {
			String portfolioName = PaiUtils.getPortfolioName(resources, sharedPreferences, i);
            String maType = PaiUtils.getStrategy(this, i);
            if (MaType.E.name().equals(maType)) {
                actionBar.addTab(actionBar.newTab()
                        .setText(portfolioName).setTag(i)
                        .setTabListener(this.new TabListener<StudyEListFragment>(this, i, StudyEListFragment.class)));
            } else if (MaType.D.name().equals(maType)) {
                actionBar.addTab(actionBar.newTab()
                        .setText(portfolioName).setTag(i)
                        .setTabListener(this.new TabListener<StudyDListFragment>(this, i, StudyDListFragment.class)));

            } else {
                actionBar.addTab(actionBar.newTab()
                        .setText(portfolioName).setTag(i)
                        .setTabListener(this.new TabListener<StudySListFragment>(this, i, StudySListFragment.class)));
            }

		}
        if (savedInstanceState != null) {
            actionBar.getTabCount();
            int index = savedInstanceState.getInt("index");
            if (actionBar.getTabCount() > index && index >=0 ) {
                Log.d(TAG,"Tab Selected Navigation Item "+index);
                getActionBar().setSelectedNavigationItem(index);
            }
        }

	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"On Destroy");
    }

    public void setPortfolioId(int portfolioId) {
        this.portfolioId = portfolioId;
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
            String maType = PaiUtils.getStrategy(this, portfolioId);
			if (MaType.E.name().equals(maType)) {
                newFragment = new StudyEDetailFragment();
                Bundle args = new Bundle();
                args.putLong(StudyEDetailFragment.ARG_STUDY_ID, studyId);
                newFragment.setArguments(args);
            } else if (MaType.D.name().equals(maType)) {
                newFragment = new StudyDDetailFragment();
                Bundle args = new Bundle();
                args.putLong(StudyDDetailFragment.ARG_STUDY_ID, studyId);
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
            // don't think this is ever called noted: 12/18/16
			Intent intent = new Intent(getApplicationContext(), StudyDetailActivity.class);
			intent.putExtra(StudyDetailActivity.STUDY_ID, studyId);
			intent.putExtra(StudyDetailActivity.PORTFOLIO_ID, portfolioId);
			startActivity(intent);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemStartSerivce:
                stopService(new Intent(this, UpdateService.class));
                startService(UpdateService.SERVICE_ACTION, UpdateService.ACTION_MANUAL_MENU);
                break;

            case R.id.itemStopService:
                stopService(new Intent(this, UpdateService.class));
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

            case R.id.mnu_reload_history:
                startService(UpdateService.SERVICE_ACTION, UpdateService.ACTION_RELOAD_HISTORY);
                break;

            case R.id.itemServiceLog:
                Intent serviceLogIntent = new Intent();
                serviceLogIntent.setClassName(getPackageName(), ServiceLogListActivity.class.getName());
                startActivity(serviceLogIntent);
                break;

            case R.id.itemRefreshPrice:
                startService(UpdateService.SERVICE_ACTION, UpdateService.ACTION_PRICE_UPDATE);
                break;
        }

        return true;
    }

    private Intent startService(String serviceAction, String actionType) {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(serviceAction, actionType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        return intent;
    }

    // handler for received Intents for the "ProgressBar status" even
    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer status = intent.getIntExtra(UpdateService.PROGRESS_BAR_STATUS, 0);
            Log.d(TAG, "Received Broadcase with status: " + status);
            setProgressBar(status);
        }
    };
    void setProgressBar(int value) {
        if (menu != null) {
            MenuItem item = menu.getItem(3);
            if (value == 0) {
                item.setActionView(R.layout.progress_bar);
                item.expandActionView();
            } else {
                item.collapseActionView();
                item.setActionView(null);
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged");
        // Calling recreateTab to recreate the view on orientation change.
        final ActionBar actionBar = getActionBar();
        ActionBar.Tab selectedTab = actionBar.getSelectedTab();
        //actionBar.selectTab(selectedTab);
    }

    @Override
	protected void onPause() {
		super.onPause();
        unregisterReceiver(mMessageReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
        // if progress was active probably done.
        setProgressBar(100);
        registerReceiver(mMessageReceiver, new IntentFilter(UpdateService.BROADCAST_UPDATE_PROGRESS_BAR));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int i = getActionBar().getSelectedNavigationIndex();
        outState.putInt("index", i);
        Log.d(TAG,"OnSaveInstanceState tab="+i);
    }
    /**
     * Created by glenn verner on 1/18/15.
     * Straight from API Guide
     *
     */
    public class TabListener<T extends Fragment> implements ActionBar.TabListener {
        final String TAG = "StudyActivity "+TabListener.class.getSimpleName();
        private Fragment mFragment;
        private final Activity mActivity;
        private final int mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
         * @param activity  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public TabListener(Activity activity, int tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

    /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            Log.d(TAG, "On Tab Selected " + mTag + " mFragment=" + (mFragment == null ? "null" : "not null"));
            // inform activity of selected portfolio
            ((StudyActivity) mActivity).setPortfolioId(mTag);
            // Check if the fragment is already initialized

            if (mFragment == null) {
                String tag = "portfolioId_" + mTag;
                // fragment may already exists if activity destroyed
                Fragment fragment = mActivity.getFragmentManager().findFragmentByTag(tag);
                if (fragment != null) {
                    Log.d(TAG, "ReAttach fragment to TabListener");
                    mFragment = fragment;
                    ft.attach(mFragment);
                } else {
                    // If not, instantiate and add it to the activity
                    mFragment = Fragment.instantiate(mActivity, mClass.getName());
                    ft.add(R.id.study_activity_frame, mFragment, tag);
                    // Create fragment and give it an argument specifying the article it
                    // should show
                    Bundle args = new Bundle();
                    args.putInt(StudyEListFragment.ARG_PORTFOLIO_ID, tab.getPosition() + 1);
                    mFragment.setArguments(args);
                }
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            Log.d(TAG, "On Tab Unselected "+mTag+ " mFragment="+ (mFragment == null ? "null" : "not null") );
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            Log.d(TAG,"on Tab Reselected");
            // Manually calling on orientation change, to recreate the view.
            if (mFragment != null) {
                // redisplay the fragment
                //ft.detach(mFragment);
                //ft.attach(mFragment);
            }
        }

    }
}
