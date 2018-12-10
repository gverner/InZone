package com.codeworks.pai;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.ServiceType;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ServiceLogListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = ServiceLogListActivity.class.getSimpleName();

	
	// private Cursor cursor;
	private ServiceLogCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.servicelog_list_header);
		fillData();
	}

    @Override
    protected void onResume() {
        super.onResume();
        TrackerUtil.sendScreenView(this,R.string.trackServiceLog);
    }

    // Create the menu based on the XML definition
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.servicelog_list, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_done:
            setResult(RESULT_OK);
            finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void fillData() {
		getLoaderManager().initLoader(0, null, this);
		adapter = new ServiceLogCursorAdapter(this);
		setListAdapter(adapter);

	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { ServiceLogTable.COLUMN_ID, ServiceLogTable.COLUMN_ITERATION, ServiceLogTable.COLUMN_MESSAGE,
				ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceLogTable.COLUMN_TIMESTAMP, ServiceLogTable.COLUMN_RUNTIME };
		CursorLoader cursorLoader = new CursorLoader(this, PaiContentProvider.SERVICE_LOG_URI, projection, null, null , ServiceLogTable.COLUMN_TIMESTAMP);
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
	
	class ServiceLogCursorAdapter extends CursorAdapter {
		private LayoutInflater	mInflator;

		public ServiceLogCursorAdapter(Context context) {
			super(context, null, 0);
			// Log.d("TAG", "CursorAdapter Constr..");
			mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Log.d("TAG", "CursorAdapter BindView");
			if (null != cursor) {

				DateTime serviceDateTime = ServiceLogTable.timestampFormat.parseDateTime(cursor.getString(cursor
						.getColumnIndexOrThrow(ServiceLogTable.COLUMN_TIMESTAMP)));
				serviceDateTime = serviceDateTime.withZone(DateTimeZone.forID("US/Eastern"));
				setText(view, serviceDateTime.toString("hh:mmaa"), R.id.servicelogList_datetime);
				ServiceType serviceType = ServiceType.fromIndex(cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_SERVICE_TYPE)));
				setText(view, serviceType.name(), R.id.servicelogList_type);

				TextView runtimeView = (TextView) view.findViewById(R.id.servicelogList_runtime);
				if (cursor.isNull(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_RUNTIME))) {
					runtimeView.setText("");
				} else {
					int ms = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_RUNTIME));
					runtimeView.setText(Double.toString(PaiUtils.round(ms / 1000d, 2)));
				}

				String message = cursor.getString(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_MESSAGE));
				if ((ServiceType.FULL.equals(serviceType) || ServiceType.PRICE.equals(serviceType)) && 
					!cursor.isNull(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_ITERATION))) {
					int value = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceLogTable.COLUMN_ITERATION));
					message = message + " (" + value + ")";
				}

				TextView textView = (TextView) view.findViewById(R.id.servicelogList_message);
				textView.setText(message);
			}
		}

		TextView setText(Cursor cursor, String columnName, View view, int viewId) {
			String value = cursor.getString(cursor.getColumnIndexOrThrow(columnName));
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(value);
			return textView;
		}
		
		TextView setInt(Cursor cursor, String columnName, View view, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			if (cursor.isNull(cursor.getColumnIndexOrThrow(columnName))) {
				textView.setText("");
			} else {
				int value = cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
				textView.setText(Integer.toString(value));
			}
			return textView;
		}

		TextView setText(View view, String value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(value);
			return textView;
		}

		TextView setDouble(View view, double value, int viewId) {
			TextView textView = (TextView) view.findViewById(viewId);
			textView.setText(Study.format(value));
			return textView;
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			final View view = mInflator.inflate(R.layout.servicelog_list_row, arg2, false);
			return view;
		}
	}

}
