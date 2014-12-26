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
import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.processor.DownloadOptionTask;
import com.codeworks.pai.processor.Notice;
import com.codeworks.pai.study.Period;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

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

    void lookupOption(Study study, Rules rules) {
        Option call = new Option(study.getSymbol(), OptionType.C, rules.AOACall(), DateTime.now());
        Option put = new Option(study.getSymbol(), OptionType.P, rules.AOBPut(), DateTime.now());

        DownloadOptionTask task = new DownloadOptionTask() {
            @Override
            protected void onPostExecute(List<Option> options) {
                if (!isAdded()) {
                    return;
                }
                SimpleDateFormat mmdd = new SimpleDateFormat("MM/dd");

                int row = 0;
                for (Option option : options) {
                    row++;
                    // Type Expires  Days Strike  Bid  ROI
                    // Call 12/20/14 15   211.00 3.00 4.5%
                    // Call 01/17/15 45   211.00 6.00 3.0%
                    // Put  12/20/14 15   202.00 0.41 4.5%
                    // Put  01/17/15 45   202.00 1.58 3.0%
                    //

                    double optionPrice = 0;
                    if (option.getBid() > 0) {
                        optionPrice = option.getBid();
                        setString(getResources().getString(R.string.sdfBidLabel) , R.id.row0Bid);
                    } else {
                        optionPrice = option.getPrice();
                        setString(getResources().getString(R.string.sdfPriceLabel) , R.id.row0Bid);
                    }

                    LocalDate expires = option.getExpires().toLocalDate();
                    if (expires.getDayOfWeek() == DateTimeConstants.SATURDAY) {
                        expires = expires.minusDays(1);
                    }
                    double days = Days.daysBetween(new DateTime().toLocalDate(), expires).getDays();
                    double roi = 0;
                    if (days > 0 && option.getStrike() > 0) {
                        roi = (((optionPrice / days) * 365) / option.getStrike()) * 100;
                    }
                    Log.d(TAG, option.getSymbol() + " " + option.getType() + " " + option.getStrike() + " " + option.getBid() + " " + option.getExpires() + " " + roi);
                    if (isAdded()) {
                        switch (row) {
                            case 1:
                                setString(option.getType().getValue(), R.id.row1Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row1Expire);
                                setDouble(option.getStrike(), R.id.row1Strike);
                                setDouble(days, R.id.row1Days, 0);
                                setDouble(optionPrice, R.id.row1Bid);
                                setDouble(roi, R.id.row1Roi, 2);
                                break;
                            case 3:
                                setString(option.getType().getValue(), R.id.row2Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row2Expire);
                                setDouble(option.getStrike(), R.id.row2Strike);
                                setDouble(days, R.id.row2Days, 0);
                                setDouble(optionPrice, R.id.row2Bid);
                                setDouble(roi, R.id.row2Roi, 2);
                                break;
                            case 2:
                                setString(option.getType().getValue(), R.id.row3Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row3Expire);
                                setDouble(option.getStrike(), R.id.row3Strike);
                                setDouble(days, R.id.row3Days, 0);
                                setDouble(optionPrice, R.id.row3Bid);
                                setDouble(roi, R.id.row3Roi, 2);
                                break;
                            case 4:
                                setString(option.getType().getValue(), R.id.row4Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row4Expire);
                                setDouble(option.getStrike(), R.id.row4Strike);
                                setDouble(days, R.id.row4Days, 0);
                                setDouble(optionPrice, R.id.row4Bid);
                                setDouble(roi, R.id.row4Roi, 2);
                                break;
                        }

                    }
                }
            }
        };
        task.execute(call,put);
    }

    TextView setBackground(int viewId, int color) {
		TextView textView = (TextView) getView().findViewById(viewId);
		textView.setBackgroundColor(color);
		return textView;
	}
	TextView setDouble(double value, int viewId) {
		TextView textView = (TextView) getView().findViewById(viewId);
		textView.setText(Study.format(value));
		return textView;
	}
    TextView setDouble(double value, int viewId, int scale) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setText(new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString());
        return textView;
    }
	TextView setString(String value, int viewId) {
		TextView textView = (TextView) getView().findViewById(viewId);
		textView.setText(value);
		return textView;
	}
}
