package com.codeworks.pai;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.CursorAdapter;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.processor.UpdateService;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by glennverner on 12/31/16.
 */

public abstract class StudyListFragmentBase extends ListFragment implements SharedPreferences.OnSharedPreferenceChangeListener,LoaderManager.LoaderCallbacks<Cursor>  {
    private static final String TAG = StudyListFragmentBase.class.getSimpleName();

    // private Cursor cursor
    protected CursorAdapter adapter;

    public static final String ARG_PORTFOLIO_ID = "com.codeworks.pai.portfolioId";
    public static final String DOWNTREND = "downtrend";
    public static final String UPTREND = "uptrend";

    SimpleDateFormat lastUpdatedFormat = new SimpleDateFormat("MM/dd/yyyy h:mmaa", Locale.US);
    SimpleDateFormat extendedFormat = new SimpleDateFormat("h:mmaa", Locale.US);

    protected OnItemSelectedListener listener;
    protected long portfolioId = 1;
    boolean extendedMarket = false;
    View footerView;
    View headerView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null)
            if (getArguments().getInt(ARG_PORTFOLIO_ID) != 0) {
                portfolioId = getArguments().getInt(ARG_PORTFOLIO_ID);
            }
        Log.i(TAG, "Activity Created portfolioId=" + portfolioId);
        lastUpdatedFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        ListView list = getListView();

        footerView = View.inflate(getActivity(), R.layout.studylist_footer, null);
        list.addFooterView(footerView);

        extendedMarket = isExtendedMarket();
        // ListView list = getListView();
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), SecurityLevelsActivity.class);
                Uri todoUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
                i.putExtra(PaiContentProvider.CONTENT_ITEM_TYPE, todoUri);
                i.putExtra(SecurityDetailActivity.ARG_PORTFOLIO_ID, portfolioId);
                startActivity(i);
                // Return true to consume the click event. In this case the
                // onListItemClick listener is not called anymore.
                return true;
            }
        });
        fillData();
    }

    abstract void fillData();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"Fragment OnCreateView child count "+(container.getChildCount()));
        View view = inflater.inflate(R.layout.studylist_main, container, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
        Log.d(TAG, "Fragment OnDestroyView ");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"On Save Instance State "+(outState == null? "null" : "not null"));
    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        getActivity().unregisterReceiver(mMessageReceiver);
        super.onPause();
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
        ProgressBar progressBar = (ProgressBar) footerView.findViewById(R.id.progressBar1);
        if (value == 0) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.OnItemSelectedListener");
        }
    }

    // May also be triggered from the Activity
    public void updateDetail(long id) {
        listener.onStudySelected(id);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        updateDetail(id);
    }

    public void setPortfolioId(long id) {
        portfolioId = id;
    }
    @Override
    public void onResume() {
        super.onResume();
        // if progress was active probably done.
        setProgressBar(100);
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(UpdateService.BROADCAST_UPDATE_PROGRESS_BAR));
        adapter.notifyDataSetChanged();
    }

    // Creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = StudyTable.COLUMN_PORTFOLIO_ID + " = ? ";
        String[] selectionArgs = {Long.toString(portfolioId)};
        Log.i(TAG, "Prepare Cursor Loader portfolio " + portfolioId);
        CursorLoader cursorLoader = new CursorLoader(getActivity(), PaiContentProvider.PAI_STUDY_URI, StudyTable.getFullProjection(), selection, selectionArgs, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }
    boolean isExtendedMarket() {
        boolean extendedMarket = false;
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            extendedMarket = (sharedPref.getBoolean(UpdateService.KEY_PREF_EXTENDED_MARKET, false));
        } catch (Exception e) {
            Log.e(TAG, "Exception reading update frequency preference", e);
        }
        return extendedMarket;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (UpdateService.KEY_PREF_EXTENDED_MARKET.equals(key)) {
            extendedMarket = sharedPreferences.getBoolean(UpdateService.KEY_PREF_EXTENDED_MARKET, false);
        }
    }

}
