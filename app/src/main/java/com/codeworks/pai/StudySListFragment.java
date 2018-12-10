package com.codeworks.pai;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.InZoneDateUtils;
import com.codeworks.pai.processor.UpdateService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class StudySListFragment extends StudyListFragmentBase {
    private static final String TAG = StudySListFragment.class.getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        TrackerUtil.sendScreenView(getActivity(),R.string.trackSMAList);
    }


    void fillData() {
        ListView list = getListView();

        headerView = View.inflate(getActivity(), R.layout.study_s_list_header, null);
        list.addHeaderView(headerView);

        getLoaderManager().initLoader(0, null, this);
        adapter = new PaiCursorAdapter(this.getActivity());
        setListAdapter(adapter);

    }

    class PaiCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        private boolean weeklyZoneModifiedByMonthly = false;

        public PaiCursorAdapter(Context context) {
            super(context, null, 0);
            // Log.d("TAG", "CursorAdapter Constr..");
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
		@Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Log.d("TAG", "CursorAdapter newView");
            final View customListView = mInflater.inflate(R.layout.study_s_list_row, parent, false);
            return customListView;
        }
		
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (null != cursor) {
                Study study = StudyTable.loadStudy(cursor);

                Rules rules = new SmaRules(study);
                // Set Symbol
                TextView symbol = (TextView) view.findViewById(R.id.quoteList_symbol);
                symbol.setText(study.getSymbol());
                // Price
                TextView price = (TextView) view.findViewById(R.id.quoteList_Price);
                price.setText(Study.format(study.getPrice()));

                if (study.isValidWeek()) {

                    setTrend(view, rules.isUpTrendMonthly(), R.id.quoteList_MonthyTrend);
                    setTrend(view, rules.isUpTrendWeekly(), R.id.quoteList_WeeklyTrend);
                    // Set EMA
                    TextView ema = (TextView) view.findViewById(R.id.quoteList_ema);
                    ema.setText(Study.format(study.getSmaMonth()));

                    double net = 0;
                    Calendar cal = GregorianCalendar.getInstance();
                    if ((study.getPriceDate() != null && InZoneDateUtils.isSameDay(study.getPriceDate(), new Date()))
                            || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        net = study.getPrice() - study.getLastClose();
                    }
                    TextView textNet = (TextView) view.findViewById(R.id.quoteList_net);
                    if (net < 0) {
                        textNet.setText(rules.formatNet(net));
                        textNet.setTextColor(getResources().getColor(R.color.net_negative));
                    } else {
                        textNet.setText(rules.formatNet(net));
                        textNet.setTextColor(getResources().getColor(R.color.net_positive));
                    }

                    if (rules.hasTradedBelowMAToday()) {
                        price.setTextColor(getResources().getColor(R.color.net_negative));
                    }

                    Configuration config = getResources().getConfiguration();
                    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        TextView extNetView = (TextView) view.findViewById(R.id.quoteList_extNet);
                        if (extNetView != null) {
                            TextView extPriceView = (TextView) view.findViewById(R.id.quoteList_extPrice);
                            TextView extTimeView = (TextView) view.findViewById(R.id.quoteList_extTime);
                            if (extendedMarket) {
                                if (study.getExtMarketPrice() > 0) {
                                    double extNet = study.getExtMarketPrice() - study.getPrice();
                                    setDouble(view, study.getExtMarketPrice(), R.id.quoteList_extPrice);
                                    extTimeView.setText(extendedFormat.format(study.getExtMarketDate()));
                                    if (extNet < 0) {
                                        extNetView.setText(rules.formatNet(extNet));
                                        extNetView.setTextColor(getResources().getColor(R.color.net_negative));
                                    } else {
                                        extNetView.setText(rules.formatNet(extNet));
                                        extNetView.setTextColor(getResources().getColor(R.color.net_positive));
                                    }
                                    TrackerUtil.sendExtendedMarket(getActivity());
                                } else {
                                    extPriceView.setText("");
                                    extNetView.setText("");
                                    extTimeView.setText("");
                                }
                                extPriceView.setVisibility(View.VISIBLE);
                                extNetView.setVisibility(View.VISIBLE);
                                extTimeView.setVisibility(View.VISIBLE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extPrice)).setVisibility(View.VISIBLE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extNet)).setVisibility(View.VISIBLE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extTime)).setVisibility(View.VISIBLE);
                            } else {
                                extPriceView.setVisibility(View.GONE);
                                extNetView.setVisibility(View.GONE);
                                extTimeView.setVisibility(View.GONE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extPrice)).setVisibility(View.GONE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extNet)).setVisibility(View.GONE);
                                ((TextView) headerView.findViewById(R.id.studyListHeader_extTime)).setVisibility(View.GONE);
                            }
                        }
                    }

                    //TextView textBuyZoneBot = setDouble(view, rules.calcBuyZoneBottom(), R.id.quoteList_BuyZoneBottom);
                    TextView textBuyZoneTop = setDouble(view, rules.calcBuyZoneTop(), R.id.quoteList_BuyZoneTop);

                    //textBuyZoneBot.setBackgroundColor(rules.getBuyZoneBackgroundColor());
                    textBuyZoneTop.setBackgroundColor(rules.getBuyZoneBackgroundColor());
                    //textBuyZoneBot.setTextColor(rules.getBuyZoneTextColor());
                    textBuyZoneTop.setTextColor(rules.getBuyZoneTextColor());

                    TextView textSellZoneBot = setDouble(view, rules.calcSellZoneBottom(), R.id.quoteList_SellZoneBottom);
                    //TextView textSellZoneTop = setDouble(view, rules.calcSellZoneTop(), R.id.quoteList_SellZoneTop);

                    textSellZoneBot.setBackgroundColor(rules.getSellZoneBackgroundColor());
                    textSellZoneBot.setTextColor(rules.getSellZoneTextColor());
                    //textSellZoneTop.setBackgroundColor(rules.getSellZoneBackgroundColor());
                    //textSellZoneTop.setTextColor(rules.getSellZoneTextColor());

                    TextView lastUpdated = (TextView) headerView.findViewById(R.id.studyList_lastUpdated);
                    if (study.getPriceDate() != null && lastUpdated != null) {
                        lastUpdated.setText(lastUpdatedFormat.format(study.getPriceDate()));
                    }
                } else {
                    setText(view, "", R.id.quoteList_net);
                    setText(view, "", R.id.quoteList_ema);
                    setText(view, "", R.id.quoteList_BuyZoneTop);
                    setText(view, "", R.id.quoteList_SellZoneBottom);
                }
            }
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

        void setTrend(View inView, boolean isUptrend, int viewId) {
            ImageView imageView = (ImageView) inView.findViewById(viewId);
            if (isUptrend) {
                imageView.setImageResource(R.drawable.ic_market_up);
            } else {
                imageView.setImageResource(R.drawable.ic_market_down);
            }
        }


    }

 }
