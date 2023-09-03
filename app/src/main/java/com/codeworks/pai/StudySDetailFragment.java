package com.codeworks.pai;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class StudySDetailFragment extends StudyDetailFragmentBase {
	private static final String	TAG				= StudySDetailFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.study_s_detail_fragment, container, false);

		return view;
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
        lookupOption(study, rules);
		setDouble(study.getPrice(), R.id.sdfPrice);
		setDouble(study.getLow(), R.id.sdfLow);
		setDouble(study.getHigh(), R.id.sdfHigh);
		setDouble(study.getAverageTrueRange() / 4, R.id.sdfAtr25);
		setDouble(study.getEmaWeek() + (study.getAverageTrueRange() / 4), R.id.sdfPricePlusAtr25);
		
		if (study.isValidWeek()) {
			setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand);
			setDouble(study.getSmaWeek(), R.id.sdfMaWeekly);
			setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand);
			if (rules.isUpTrend(Period.Week)) {
				setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand).setBackgroundColor(Color.LTGRAY);
				setDouble(study.getSmaWeek(), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand);
				setString( getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfZoneUpperType).setBackgroundColor(Color.LTGRAY);
				setString( getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfZoneMidType).setBackgroundColor(Color.LTGRAY);
				setString( "" , R.id.sdfZoneLowerType);
			} else {
				setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperBand);
				setDouble(study.getSmaWeek(), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBand).setBackgroundColor(Color.LTGRAY);
				setString( "" , R.id.sdfZoneUpperType);
				setString( getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfZoneMidType).setBackgroundColor(Color.LTGRAY);
				setString( getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfZoneLowerType).setBackgroundColor(Color.LTGRAY);
			}
		}
		
		if (study.isValidMonth()) {
			setDouble(rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperBand);
			setDouble(rules.calcUpperBuyZoneBottom(Period.Month), R.id.sdfMaMonthly);
			setDouble(rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBand);
			//setDouble(rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyPDL2);
		}
		setDouble(study.getStochasticK(), R.id.sdfStochasticK);
		setDouble(study.getStochasticD(), R.id.sdfStochasticD);
		
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
				setString(getResources().getString(R.string.sdfAlertNameLabel), R.id.sdfAlertName);
			} else {
				setString(getResources().getString(R.string.sdfStatusNameLabel), R.id.sdfAlertName);
			}
			setString(alertMsg.toString(), R.id.sdfAlertText);
		}
		if (study.getSmaWeek() != 0 && !study.hasInsufficientHistory()) {
			setString(rules.inCash(), R.id.sdfInCashText);
			setString(rules.inCashAndPut(), R.id.sdfInCashAndPutText);
			setString(rules.inStock(), R.id.sdfInStockText);
			setString(rules.inStockAndCall(), R.id.sdfInStockAndCallText);
		}
	}

}
