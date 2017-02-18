package com.codeworks.pai;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Study;

import java.math.BigDecimal;

/*
 * SecurityLevelsActivity allows to edit security demand zones
 */
public class SecurityLevelsActivity extends Activity {
	private static final String	TAG				= SecurityLevelsActivity.class.getSimpleName();
	public static String	ARG_PORTFOLIO_ID	= "com.codeworks.pai.SecurityLevlesActivity.portfolioId";
    public static String	ARG_STUDY_ID	    = "com.codeworks.pai.SecurityLevlesActivity.studyId";

	private Uri				securityUri;
	private int				portfolioId			= 1;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.security_levels);
		// Check from the saved Instance
		securityUri = null;
		if (bundle != null) {
			securityUri = (Uri) bundle.getParcelable(PaiContentProvider.CONTENT_ITEM_TYPE);
			portfolioId = bundle.getInt(ARG_PORTFOLIO_ID);
		}
		if (getIntent() != null) {
			Bundle extras = getIntent().getExtras();
			// Or passed from the other activity
			if (extras != null) {
				securityUri = extras.getParcelable(PaiContentProvider.CONTENT_ITEM_TYPE);
				portfolioId = extras.getInt(ARG_PORTFOLIO_ID);
			}
		}

		if (portfolioId == 0) {
			portfolioId = 1;
		}
		if (securityUri != null && !securityUri.getPath().endsWith("-1")) {
			fillData(securityUri);
		}

	}

	// Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.security_detail, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_done:
			if (TextUtils.isEmpty(((TextView)findViewById(R.id.securitySymbol)).getText().toString())) {
				setResult(RESULT_CANCELED);
				finish();
			} else {
				setResult(RESULT_OK);
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putParcelable(PaiContentProvider.CONTENT_ITEM_TYPE, securityUri);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	private void saveState() {
		String symbol = ((TextView)findViewById(R.id.securitySymbol)).getText().toString();
		// must have a symbol
		if (symbol.length() == 0) {
			return;
		}
		ContentValues values = new ContentValues();
        values.put(StudyTable.COLUMN_DEMAND_ZONE, readDouble(R.id.demandZone));
        values.put(StudyTable.COLUMN_PDL2, readDouble(R.id.pdl2));
        values.put(StudyTable.COLUMN_PDL1, readDouble(R.id.pdl1));

		if (securityUri != null) {
			// Update security
			getContentResolver().update(securityUri, values, null, null);
		}
	}

	private void fillData(Uri uri) {
		//Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
		//String[] projection = { StudyTable.COLUMN_SYMBOL, StudyTable.COLUMN_PORTFOLIO_ID, StudyTable.COLUMN_NAME, StudyTable.COLUMN_DEMAND_ZONE, StudyTable.COLUMN_PDL1, StudyTable.COLUMN_PDL2 };

		Cursor cursor = getContentResolver().query(uri, StudyTable.getFullProjection(), null, null, null);
		if (cursor != null)
			try {
				cursor.moveToFirst();
				Study security = StudyTable.loadStudy(cursor);

				setString(security.getSymbol(),R.id.securitySymbol);
				setString(security.getName(),R.id.securityName);
				setDouble(security.getDemandZone(),R.id.demandZone, 2);
				setDouble(security.getPdl1(),R.id.pdl1, 2);
				setDouble(security.getPdl2(),R.id.pdl2, 2);

				// mSymbolText.setText(symbol);
			} catch (Exception e) {
				Log.e(TAG, "Exception reading Study from db ", e);
			} finally {
				// Always close the cursor
				cursor.close();
			}
	}
    double readDouble(int viewId) {
        String value = ((TextView) findViewById(viewId)).getText().toString();
        if (value == null || value.trim().length() == 0) {
            return 0;
        } else {
            return PaiUtils.round(Double.parseDouble(value));
        }
    }

	TextView setDouble(double value, int viewId, int scale) {
		TextView textView = (TextView) findViewById(viewId);
		textView.setText(new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString());
		return textView;
	}
	TextView setString(String value, int viewId) {
		TextView textView = (TextView) findViewById(viewId);
        if (value != null) {
            value = value.trim();
        }
		textView.setText(value);
		return textView;
	}
}