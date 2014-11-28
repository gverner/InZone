package com.codeworks.pai;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class StudySDetailFragment extends Fragment {
	private static final String	TAG				= StudySDetailFragment.class.getSimpleName();
	public static final String	ARG_STUDY_ID	= "arg_study_id";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.study_s_detail_fragment, container, false);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		long studyId = 0;
		if (getArguments() != null) {
			studyId = getArguments().getLong(ARG_STUDY_ID);
		}

		fillData(studyId);
	}

	public void setText(String item) {
		if (item != null) {
			fillData(Long.parseLong(item));
		}
		/*
		 * TextView view = (TextView)
		 * getView().findViewById(R.id.tempStudyDetailsText);
		 * view.setText(item);
		 */
	}

	private void fillData(Long id) {

		Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
		Cursor cursor = getActivity().getContentResolver().query(uri, StudyTable.getFullProjection(), null, null, null);
		if (cursor != null)
			try {
				cursor.moveToFirst();
				Study security = StudyTable.loadStudy(cursor); 
				
				((TextView) getView().findViewById(R.id.sdfSymbol)).setText(security.getSymbol());
				((TextView) getView().findViewById(R.id.sdfName)).setText(security.getName());
				populateView(security);

				// mSymbolText.setText(symbol);
			} catch (Exception e) {
				Log.e(TAG, "Exception reading Study from db ", e);
			} finally {
				// Always close the cursor
				cursor.close();
			}
	}

	void populateView(Study study) {
		Rules rules;
		if (MaType.E.equals(study.getMaType())) {
			rules = new EmaRules(study);
			Log.e(TAG,"INVALID SMA MA TYPE="+study.getMaType().name());
		} else {
			rules = new SmaRules(study);
			Log.d(TAG,"Populate SMA Detail Page");
		}
		setDouble(getView(), study.getPrice(), R.id.sdfPrice);
		setDouble(getView(), study.getLow(), R.id.sdfLow);
		setDouble(getView(), study.getHigh(), R.id.sdfHigh);		
		setDouble(getView(), study.getAverageTrueRange() / 4, R.id.sdfAtr25);
		setDouble(getView(), study.getEmaWeek() + (study.getAverageTrueRange() / 4), R.id.sdfPricePlusAtr25);
		
		if (study.isValidWeek()) {
			setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand);
			setDouble(getView(), study.getSmaWeek(), R.id.sdfMaWeekly);
			setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand);
			if (rules.isUpTrend(Period.Week)) {
				setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), study.getSmaWeek(), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand);
				setString(getView(),  getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfZoneUpperType).setBackgroundColor(Color.LTGRAY);
				setString(getView(),  getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfZoneMidType).setBackgroundColor(Color.LTGRAY);
				setString(getView(),  "" , R.id.sdfZoneLowerType);
			} else {
				setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand);
				setDouble(getView(), study.getSmaWeek(), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand).setBackgroundColor(Color.LTGRAY);
				setString(getView(),  "" , R.id.sdfZoneUpperType);
				setString(getView(),  getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfZoneMidType).setBackgroundColor(Color.LTGRAY);
				setString(getView(),  getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfZoneLowerType).setBackgroundColor(Color.LTGRAY);
			}
		}
		
		if (study.isValidMonth()) {
			setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperBand);
			setDouble(getView(), rules.calcUpperBuyZoneBottom(Period.Month), R.id.sdfMaMonthly);
			setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBand);
			//setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyPDL2);
		}
		setDouble(getView(), study.getStochasticK(), R.id.sdfStochasticK);
		setDouble(getView(), study.getStochasticD(), R.id.sdfStochasticD);
		
		rules.updateNotice();
		StringBuilder alertMsg = new StringBuilder();
		alertMsg.append(rules.getTrendText(getResources()));
		boolean alert = false;
		if (!Notice.NONE.equals(study.getNotice())) {
			alert = true;
			alertMsg.append("\n");
			alertMsg.append(String.format(getResources().getString(study.getNotice().getMessage()), study.getSymbol()));
		}
		StringBuilder addAlert = rules.getAdditionalAlerts(getResources());
		if (addAlert != null && addAlert.length() > 0) {
			alert = true;
			alertMsg.append("\n");
			alertMsg.append(addAlert);
		}
		if (alertMsg.length() > 0) {
			if (alert) {
				setString(getView(), getResources().getString(R.string.sdfAlertNameLabel), R.id.sdfAlertName);
			} else {
				setString(getView(), getResources().getString(R.string.sdfStatusNameLabel), R.id.sdfAlertName);
			}
			setString(getView(), alertMsg.toString(), R.id.sdfAlertText);
		}
		if (study.getSmaWeek() != 0 && !study.hasInsufficientHistory()) {
			setString(getView(), rules.inCash(), R.id.sdfInCashText);
			setString(getView(), rules.inCashAndPut(), R.id.sdfInCashAndPutText);
			setString(getView(), rules.inStock(), R.id.sdfInStockText);
			setString(getView(), rules.inStockAndCall(), R.id.sdfInStockAndCallText);
		}
	}

	TextView setDouble(View view, double value, int viewId) {
		TextView textView = (TextView) view.findViewById(viewId);
		textView.setText(Study.format(value));
		return textView;
	}
	TextView setString(View view, String value, int viewId) {
		TextView textView = (TextView) view.findViewById(viewId);
		textView.setText(value);
		return textView;
	}
}
