package com.codeworks.pai;

import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class PaiStudyListActivityTest extends ActivityInstrumentationTestCase2<StudyActivity> {
	StudyActivity activity;
	static String SPY = "SPY";
	public PaiStudyListActivityTest() {
		super(StudyActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		activity = getActivity();
	}

	public void testStartSecondActivity() throws Exception {

		// Add monitor to check for the second activity
		ActivityMonitor monitorSecurityList = getInstrumentation().addMonitor(SecurityListActivity.class.getName(), null, false);
		// Add monitor to check for the second activity
		ActivityMonitor monitorSecurityDetail = getInstrumentation().addMonitor(SecurityDetailActivity.class.getName(), null, false);

		// Find button and click it

		View menuItemProtfolio = activity.findViewById(R.id.portfolio);
		TouchUtils.clickView(this, menuItemProtfolio);

		// To click on a click, e.g. in a listview
		// listView.getChildAt(0);

		// Wait 2 seconds for the start of the activity
		SecurityListActivity securityListActivity = (SecurityListActivity) monitorSecurityList.waitForActivityWithTimeout(2000);
		assertNotNull(securityListActivity);

		View menuItemAddSecurity = securityListActivity.findViewById(R.id.security_list_insert);
		TouchUtils.clickView(this, menuItemAddSecurity);

		// Wait 2 seconds for the start of the activity
		SecurityDetailActivity securityDetailActivity = (SecurityDetailActivity) monitorSecurityDetail.waitForActivityWithTimeout(2000);
		assertNotNull(securityDetailActivity);
		

		// Search for the textView
		final TextView securityEdit = (TextView) securityDetailActivity.findViewById(R.id.security_edit_symmbol);
		
		// Check that the TextView is on the screen
		ViewAsserts.assertOnScreen(securityDetailActivity.getWindow().getDecorView(), securityEdit);

	    // set text
		securityDetailActivity.runOnUiThread(new Runnable() {

	      @Override
	      public void run() {
	    	  securityEdit.setText(SPY);
	      }
	    });
	    
	    getInstrumentation().waitForIdleSync();
	    assertEquals("Text incorrect", SPY, securityEdit.getText().toString());
	    
		View menuItemDond = securityDetailActivity.findViewById(R.id.menu_item_done);
		TouchUtils.clickView(this, menuItemDond);
		
		// Wait 2 seconds for the start of the activity
	    securityListActivity = (SecurityListActivity) monitorSecurityList.waitForActivityWithTimeout(2000);
		assertNotNull(securityListActivity);		

		// Search for the textView
		TextView securitySymbol = (TextView) securityListActivity.findViewById(R.id.securitySymbol);

		
		// Validate the text on the TextView
		assertEquals("Text incorrect", "SPY", securitySymbol.getText().toString());

		// Press back and click again
		this.sendKeys(KeyEvent.KEYCODE_BACK);
		TouchUtils.clickView(this, menuItemProtfolio);

	}
}
