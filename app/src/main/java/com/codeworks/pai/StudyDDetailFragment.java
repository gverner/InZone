package com.codeworks.pai;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.model.EmaDRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

public class StudyDDetailFragment extends StudyDetailFragmentBase {
	private static final String	TAG				= StudyDDetailFragment.class.getSimpleName();

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_item_zone).setVisible(true);
        super.onPrepareOptionsMenu(menu);

    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.menu_item_done:
				// Not implemented here
				return false;

			case R.id.menu_item_zone:
				// Do Fragment menu item stuff here
                showDemandZoneInputDialog();
				return true;

			default:
				break;
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.study_d_detail_fragment, container, false);


        return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        TrackerUtil.sendScreenView(getActivity(),R.string.trackEMADetail);
    }

	void populateView(Study study) {

		Log.i(TAG,"PopulateView");
		EmaDRules rules;
		if (MaType.D.equals(study.getMaType())) {
			rules = new EmaDRules(study);
			Log.d(TAG,"Populate EMA Detail Page");
		} else {
			rules = new EmaDRules(study);
			Log.e(TAG,"INVALID EMA MA TYPE="+study.getMaType().name());
		}

		final long securityId = study.getSecurityId();
		final long portfolioId = study.getPortfolioId();

        lookupOption(study, rules);
		setDouble(study.getPrice(), R.id.sdfPrice);
		setDouble(study.getLow(), R.id.sdfLow);
		setDouble(study.getHigh(), R.id.sdfHigh);
		if (study.isValidWeek()) {
			setDouble(study.getAverageTrueRange() / 4, R.id.sdfAtr25);
			setDouble(study.getEmaWeek() + (study.getAverageTrueRange() / 4), R.id.sdfPricePlusAtr25);
			setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
			setDouble(rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop);
			setDouble(study.getEmaWeek(), R.id.sdfMaWeekly);
			setDouble(rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeeklyDemand);
            setDouble(rules.calcUpperBuyZoneStoploss(Period.Week), R.id.sdfMaWeeklyStop);
			setDouble(rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom);
			setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
			setDouble(rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);

			if (rules.isUpTrend(Period.Week)) {
				if (rules.isWeeklyUpperSellZoneExpandedByMonthly()) {
					setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
				} else {
					setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom).setBackgroundColor(Color.LTGRAY);
				}
				setDouble(rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(study.getEmaWeek(), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeeklyDemand).setBackgroundColor(Color.LTGRAY);

				setDouble(rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom);
				setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
				setDouble(rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);

				TextView sellZone = setString( getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfUpperWS);
				TextView buyZone = setString( getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfUpperWB);
				sellZone.setBackgroundColor(Color.LTGRAY);
				buyZone.setBackgroundColor(Color.LTGRAY);
//               	getView().findViewById(R.id.sdfUpperWB2).setBackgroundColor(Color.LTGRAY);


				((LayoutParams)buyZone.getLayoutParams()).weight = 2;
				((LayoutParams)setString( "" , R.id.sdfLowerWS).getLayoutParams()).weight = 1;
				setString( "" , R.id.sdfLowerWB);

			} else {
				setDouble(rules.calcUpperSellZoneBottom(Period.Week), R.id.sdfWeeklyUpperSellBottom);
				setDouble(rules.calcUpperBuyZoneTop(Period.Week), R.id.sdfWeeklyUpperBuyTop);
				//setDouble(rules.calcUpperBuyZoneBottom(Period.Week), R.id.sdfMaWeekly).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcLowerSellZoneBottom(Period.Week), R.id.sdfWeeklyLowerSellBottom).setBackgroundColor(Color.LTGRAY);
				TextView buyZone;
				if (rules.isWeeklyLowerBuyZoneCompressedByMonthly()) {
					setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop);
					setDouble(rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom);
					buyZone = setString( getResources().getString(R.string.sdfZoneTypePDL) , R.id.sdfLowerWB);
				} else {
					setDouble(rules.calcLowerBuyZoneTop(Period.Week), R.id.sdfWeeklyLowerBuyTop).setBackgroundColor(Color.LTGRAY);
					setDouble(rules.calcLowerBuyZoneBottom(Period.Week), R.id.sdfWeeklyLowerBuyBottom).setBackgroundColor(Color.LTGRAY);
					buyZone = setString( getResources().getString(R.string.sdfZoneTypeBuyer) , R.id.sdfLowerWB);
				}
				TextView sellZone = setString( getResources().getString(R.string.sdfZoneTypeSeller) , R.id.sdfLowerWS);
				sellZone.setBackgroundColor(Color.LTGRAY);
				buyZone.setBackgroundColor(Color.LTGRAY);

				((LayoutParams)sellZone.getLayoutParams()).weight = 2;
				((LayoutParams)setString( "" , R.id.sdfUpperWB).getLayoutParams()).weight = 1;
				setString( "" , R.id.sdfUpperWS);
			}
			
		}
		if (study.isValidMonth()) {
			if (rules.isWeeklyUpperSellZoneExpandedByMonthly()) {
				setDouble(rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperSellBottom).setBackgroundColor(Color.LTGRAY);
			} else {
				setDouble(rules.calcUpperSellZoneBottom(Period.Month), R.id.sdfMonthlyUpperSellBottom);
			}
			setDouble(rules.calcUpperBuyZoneTop(Period.Month), R.id.sdfMonthlyUpperBuyTop);
			setDouble(study.getEmaMonth(), R.id.sdfMaMonthly);
			setDouble(rules.calcLowerSellZoneBottom(Period.Month), R.id.sdfMonthlyLowerSellBottom);
			if (rules.isWeeklyLowerBuyZoneCompressedByMonthly()) {
				setDouble(rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBuyTop).setBackgroundColor(Color.LTGRAY);
				setDouble(rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyLowerBuyBottom).setBackgroundColor(Color.LTGRAY);
			} else {
				setDouble(rules.calcLowerBuyZoneTop(Period.Month), R.id.sdfMonthlyLowerBuyTop);
				setDouble(rules.calcLowerBuyZoneBottom(Period.Month), R.id.sdfMonthlyLowerBuyBottom);
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
				setString(getResources().getString(R.string.sdfAlertNameLabel), R.id.sdfAlertName);
			} else {
				setString(getResources().getString(R.string.sdfStatusNameLabel), R.id.sdfAlertName);
			}
			setString(alertMsg.toString(), R.id.sdfAlertText);
		}
		if (study.isValidWeek() && !study.hasInsufficientHistory()) {
			setString(rules.inCash(), R.id.sdfInCashText);
			setString(rules.inCashAndPut(), R.id.sdfInCashAndPutText);
			setString(rules.inStock(), R.id.sdfInStockText);
			setString(rules.inStockAndCall(), R.id.sdfInStockAndCallText);
		}

	}

}
