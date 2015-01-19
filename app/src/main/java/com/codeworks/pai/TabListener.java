package com.codeworks.pai;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by glenn verner on 1/18/15.
 * Straight from API Guide
 *
 * Using this tab listener required that we handle orientation changes manually
 * see manifest    android:configChanges="keyboardHidden|orientation|screenSize"
 * see studyActivity onConfigurationChanged
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    final static String TAG = TabListener.class.getSimpleName();
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
        Log.d(TAG, "On Tab Selected "+mTag+ " mFragment="+ (mFragment == null ? "null" : "not null") );
        // inform activity of selected portfolio
        ((StudyActivity)mActivity).setPortfolioId(mTag);
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
//            ft.add(android.R.id.content, mFragment, "portfolioId_"+mTag);
            ft.add(R.id.study_activity_frame, mFragment, "portfolioId_"+mTag);
            // Create fragment and give it an argument specifying the article it
            // should show
            Bundle args = new Bundle();
            args.putInt(StudyEListFragment.ARG_PORTFOLIO_ID, tab.getPosition() + 1);
            mFragment.setArguments(args);
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
        // User selected the already selected tab. Usually do nothing.
        // Manually calling on orientation change, to recreate the view.
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
            ft.attach(mFragment);
        }
    }

}
