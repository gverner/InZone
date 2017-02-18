package com.codeworks.pai;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.PriceHistoryTable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class SecurityListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = SecurityListActivity.class.getSimpleName();

	public static final String ARG_PORTFOLIO_ID	= "com.codeworks.pai.PortfolioId";
	
	// private Cursor cursor;
	private SimpleCursorAdapter adapter;
	int portfolioId = 1;
	String portfolioPreferenceName;

    @Override
    protected void onResume() {
        super.onResume();
        TrackerUtil.sendScreenView(this,R.string.trackPortfolio);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_list);
		
		TextView mPortfolioNameText = (TextView) findViewById(R.id.sla_portfolio_name);
		
		TextView mStrategy = (TextView) findViewById(R.id.sla_strategy);

		/*
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.spinner_moving_average, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mStrategy.setAdapter(adapter);
		*/
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			portfolioId = extras.getInt(ARG_PORTFOLIO_ID);
		}		

		String portfolioName = PaiUtils.getPortfolioName(this, portfolioId);
		mPortfolioNameText.setText(portfolioName);

		String strategy = PaiUtils.getStrategy(this, portfolioId);
		Resources resources = getResources();
		if ("E".equals(strategy)) {
			mStrategy.setText(resources.getString(R.string.sla_maTypeEma));
		} else {
			mStrategy.setText(resources.getString(R.string.sla_maTypeSma));
		}

		
		/*
		Resources resources = getResources();
		portfolioPreferenceName = PaiUtils.PREF_PORTFOLIO_KEY + portfolioId;
		SharedPreferences sharedPreferences = getSharedPreferences(PaiUtils.PREF_FILE, MODE_PRIVATE);
		String portfolioName = PaiUtils.getPortfolioName(resources, sharedPreferences, portfolioId);
		mPortfolioNameText.setText(portfolioName);
		if (sharedPreferences.getString(PaiUtils.PREF_PORTFOLIO_MA_TYPE+portfolioId, portfolioId == 1 ? PaiUtils.MA_TYPE_EMA : PaiUtils.MA_TYPE_SMA).equals(PaiUtils.MA_TYPE_EMA) ) {
			mStrategy.setSelection(0);
		} else {
			mStrategy.setSelection(1);
		}
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mStrategy.setOnItemSelectedListener(this);

		*/
        getListView().setLongClickable(true);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // get the row the clicked button is in
                RelativeLayout vwParentRow = (RelativeLayout) view.getParent();
                TextView securityTextView = (TextView) vwParentRow.getChildAt(0);
                TextView symbolTextView = (TextView) vwParentRow.getChildAt(1);
                if (securityTextView != null) {
                    String securityId = securityTextView.getText().toString();
                    Log.d(TAG, "SecurityId1=" + securityId);
                    Intent i = new Intent(SecurityListActivity.this, SecurityLevelsActivity.class);
                    Uri todoUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + securityId);
                    i.putExtra(PaiContentProvider.CONTENT_ITEM_TYPE, todoUri);
                    i.putExtra(SecurityDetailActivity.ARG_PORTFOLIO_ID, portfolioId);
                    startActivity(i);
                }
                return false;
            }
        });
		fillData();
	}

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.security_list, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.security_list_insert:
			createSecurity();
			return true;
		case R.id.menu_item_done:
            setResult(RESULT_OK);
            finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/*
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        String selected  = (String)parent.getItemAtPosition(pos);
		SharedPreferences sharedPreferences = getSharedPreferences(PaiUtils.PREF_FILE, MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(PaiUtils.PREF_PORTFOLIO_MA_TYPE+portfolioId, String.valueOf(selected.charAt(0)));
		editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
	void saveChange() {
		String portfolioName = mPortfolioNameText.getText().toString();
		SharedPreferences sharedPreferences = getSharedPreferences(PaiUtils.PREF_FILE, MODE_PRIVATE);
		PaiUtils.savePortfolioName(getResources(), sharedPreferences, portfolioId, portfolioName);
	}
	*/

	private void createSecurity() {
		Intent i = new Intent(this, SecurityDetailActivity.class);
		i.putExtra(SecurityDetailActivity.ARG_PORTFOLIO_ID, portfolioId);
		startActivity(i);
	}

	public void deleteClickHandler(View v) {
		// get the row the clicked button is in
		RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
		TextView securityTextView = (TextView) vwParentRow.getChildAt(0);
		TextView symbolTextView = (TextView) vwParentRow.getChildAt(1);
		if (securityTextView != null) {
			String securityId = securityTextView.getText().toString();
			Log.d(TAG, "SecurityId1=" + securityId);
			deleteSecurity(securityId);
			String symbol = symbolTextView.getText().toString();
			deleteHistory(symbol);
		}
	}

	private void deleteSecurity(String securityId) {
		if (securityId.length() == 0) {
			return;
		}
		Uri securityUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + securityId);
		int countDeleted = getContentResolver().delete(securityUri, null, null);
		Log.d(TAG, "Uri=" + securityUri.toString() + " Delete Count=" + countDeleted);
	}

	private void deleteHistory(String symbol) {
		if (symbol == null || symbol.length() == 0) {
			return;
		}
		String selection = PriceHistoryTable.COLUMN_SYMBOL + " = ? ";
		String[] selectionArgs = { symbol };
		int countDeleted = getContentResolver().delete(PaiContentProvider.PRICE_HISTORY_URI, selection, selectionArgs);
		Log.d(TAG, "History Delete Count=" + countDeleted);
	}
	
	// Opens the second activity if an entry is clicked

	protected void editClickHandler(View v) {
        // get the row the clicked button is in
        RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
        TextView securityTextView = (TextView) vwParentRow.getChildAt(0);
        TextView symbolTextView = (TextView) vwParentRow.getChildAt(1);
        if (securityTextView != null) {
            String securityId = securityTextView.getText().toString();
            Log.d(TAG, "SecurityId1=" + securityId);
            Intent i = new Intent(this, SecurityLevelsActivity.class);

            Uri todoUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + securityId);
            i.putExtra(PaiContentProvider.CONTENT_ITEM_TYPE, todoUri);
			i.putExtra(SecurityDetailActivity.ARG_PORTFOLIO_ID, portfolioId);
            startActivity(i);
        }
	}

	private void fillData() {
		// Must include the _id column for the adapter to work
		// PaiStudyTable.COLUMN_ID,

		String[] from = new String[] { StudyTable.COLUMN_ID, StudyTable.COLUMN_SYMBOL, StudyTable.COLUMN_NAME };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.securityId, R.id.securitySymbol, R.id.securityName };
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.security_row, null, from, to, 0);

		setListAdapter(adapter);
	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { StudyTable.COLUMN_ID, StudyTable.COLUMN_SYMBOL, StudyTable.COLUMN_NAME };
		String selection = StudyTable.COLUMN_PORTFOLIO_ID + " = ? ";
		String[] selectionArgs = { Long.toString(portfolioId) };
		CursorLoader cursorLoader = new CursorLoader(this, PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs, null);
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

}
