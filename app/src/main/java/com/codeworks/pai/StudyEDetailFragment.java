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
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class StudyEDetailFragment extends Fragment {
	private static final String	TAG				= StudyEDetailFragment.class.getSimpleName();
	public static final String	ARG_STUDY_ID	= "arg_study_id";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.study_e_detail_fragment, container, false);
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
		Log.i(TAG,"PopulateView");
		Rules rules;
		if (MaType.E.equals(study.getMaType())) {
			rules = new EmaRules(study);
			Log.d(TAG,"Populate EMA Detail Page");
		} else {
			rules = new SmaRules(study);
			Log.e(TAG,"INVALID EMA MA TYPE="+study.getMaType().name());
		}
		setDouble(getView(), study.getPrice(), R.id.sdfPrice);
		setDouble(getView(), study.getLow(), R.id.sdfLow);
		setDouble(getView(), study.getHigh(), R.id.sdfHigh);
		if (study.isValidWeek()) {
			setDouble(getView(), study.getAverageTrueRange() / 4, R.id.sdfAtr25);
			setDouble(getView(), study.getEmaWeek() + (study.getAverageTrueRange() / 4), R.id.sdfPricePlusAtr25);
			setDouble(getView(), rules.calcUpperSellZoneTop(Period.Week), R.id.sdfWeeklyUpperSellTop);
			setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
			setDouble(getView(), rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop);
			setDouble(getView(), rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly);
			setDouble(getView(), rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom);
			setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
			setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);

			if (rules.isUpTrend(Period.Week)) {
				setDouble(getView(), rules.calcUpperSellZoneTop(Period.Week), R.id.sdfWeeklyUpperSellTop).setBackgroundColor(Color.LTGRAY);
				if (rules.isWeeklyUpperSellZoneExpandedByMonthly()) {
					setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
				} else {
					setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom).setBackgroundColor(Color.LTGRAY);
				}
				setDouble(getView(), rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom);
				setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
				setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);

				TextView sellZone = setString(getView(),  getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfUpperWS);
				TextView buyZone = setString(getView(),  getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfUpperWB);
				sellZone.setBackgroundColor(Color.LTGRAY);
				buyZone.setBackgroundColor(Color.LTGRAY);

				((LayoutParams)buyZone.getLayoutParams()).weight = 2;
				((LayoutParams)setString(getView(),  "" , R.id.sdfLowerWS).getLayoutParams()).weight = 1;
				setString(getView(),  "" , R.id.sdfLowerWB);

			} else {
				setDouble(getView(), rules.calcUpperSellZoneTop(Period.Week), R.id.sdfWeeklyUpperSellTop);
				setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
				setDouble(getView(), rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop);
				setDouble(getView(), rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom).setBackgroundColor(Color.LTGRAY);
				TextView buyZone;
				if (rules.isWeeklyLowerBuyZoneCompressedByMonthly()) {
					setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
					setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);
					buyZone = setString(getView(),  getResources().getString(R.string.sdfZoneTypePDL) , R.id.sdfLowerWB);
				} else {
					setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop).setBackgroundColor(Color.LTGRAY);
					setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom).setBackgroundColor(Color.LTGRAY);
					buyZone = setString(getView(),  getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfLowerWB);
				}
				TextView sellZone = setString(getView(),  getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfLowerWS);
				sellZone.setBackgroundColor(Color.LTGRAY);
				buyZone.setBackgroundColor(Color.LTGRAY);

				((LayoutParams)sellZone.getLayoutParams()).weight = 2;
				((LayoutParams)setString(getView(),  "" , R.id.sdfUpperWB).getLayoutParams()).weight = 1;
				setString(getView(),  "" , R.id.sdfUpperWS);
			}
			
		}
		if (study.isValidMonth()) {
			setDouble(getView(), rules.calcUpperSellZoneTop(Period.Month), R.id.sdfMonthlyUpperSellTop);
			if (rules.isWeeklyUpperSellZoneExpandedByMonthly()) {
				setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperSellBottom).setBackgroundColor(Color.LTGRAY);
			} else {
				setDouble(getView(), rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperSellBottom);
			}
			setDouble(getView(), rules.calcUpperBuyZoneTop(Period.Month), R.id.sdfMonthlyUpperBuyTop);
			setDouble(getView(), study.getEmaMonth(), R.id.sdfMaMonthly);
			setDouble(getView(), rules.calcLowerSellZoneBottom(Period.Month), R.id.sdfMonthlyLowerSellBottom);
			if (rules.isWeeklyLowerBuyZoneCompressedByMonthly()) {
				setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBuyTop).setBackgroundColor(Color.LTGRAY);
				setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyLowerBuyBottom).setBackgroundColor(Color.LTGRAY);
			} else {
				setDouble(getView(), rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBuyTop);
				setDouble(getView(), rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyLowerBuyBottom);
			}
		}
		
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
		if (study.isValidWeek() && !study.hasInsufficientHistory()) {
			setString(getView(), rules.inCash(), R.id.sdfInCashText);
			setString(getView(), rules.inCashAndPut(), R.id.sdfInCashAndPutText);
			setString(getView(), rules.inStock(), R.id.sdfInStockText);
			setString(getView(), rules.inStockAndCall(), R.id.sdfInStockAndCallText);
		}

	}

	TextView setBackground(View view, int viewId, int color) {
		TextView textView = (TextView) view.findViewById(viewId);
		textView.setBackgroundColor(color);
		return textView;
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
