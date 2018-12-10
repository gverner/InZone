package com.codeworks.pai;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.codeworks.pai.db.model.MaType;

public class PaiUtils {
	public static String    PREF_FILE = "com.codeworks.inzone.preferences";
	//public static String    PREF_PORTFOLIO = "com.codeworks.pai.preference.portfolio.name";
	//public static String PREF_PORTFOLIO_KEY = "pref_portfolio_key";
	public static String PREF_PORTFOLIO_KEY = "pref_portfolio_name";
	
	//public static final String PREF_PORTFOLIO_MA_TYPE = "com.codeworks.inzone.portfolio_ma_type";
	public static final String PREF_PORTFOLIO_MA_TYPE = "pref_portfolio_type";
	public static final String PREF_RINGTONE = "pref_ringtone";
	public static final String PREF_VIBRATE_ON = "pref_vibrateOn";
	
	public static final String MA_TYPE_EMA = "E";
	public static final String MA_TYPE_SMA = "S";
	
	public static double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static double round(double value, int scale) {
		return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	public static String getDefaultPortfolioName(Resources resources, int portfolioId) {
		int tab_id = R.string.pref_portfolio_name1_defaultValue;
		switch (portfolioId) {
		case 1:
			tab_id = R.string.pref_portfolio_name1_defaultValue;
			break;
		case 2:
			tab_id = R.string.pref_portfolio_name2_defaultValue;
			break;
		case 3:
			tab_id = R.string.pref_portfolio_name3_defaultValue;
			break;
		default:
			tab_id = R.string.pref_portfolio_name1_defaultValue;
			break;
		}
		String preferenceName = resources.getString(tab_id);
		return preferenceName;
	}
	
	public static String getPortfolioName(Resources resources, SharedPreferences sharedPreferences, int portfolioId) {
		String preferenceName;
		preferenceName = getDefaultPortfolioName(resources, portfolioId);
		return sharedPreferences.getString(PREF_PORTFOLIO_KEY+portfolioId, preferenceName);
	}

	public static String getPortfolioName(Activity activity, int portfolioId) {
		SharedPreferences sharedPreferences = getSharedPreferences(activity);
		Resources resources = activity.getResources(); // default portfolio name stored in resources
		return getPortfolioName(resources, sharedPreferences, portfolioId);
	}
	
	public static SharedPreferences getSharedPreferences(Activity activity) {
		//SharedPreferences sharedPreferences = activity.getSharedPreferences(PaiUtils.PREF_FILE, Activity.MODE_PRIVATE);
		return PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
	}
	
	public static String getStrategy(Activity activity, int portfolioId) {
		SharedPreferences sharedPreferences = getSharedPreferences(activity);
		return getStrategy(sharedPreferences, portfolioId);
	}
	
	public static String getStrategy(SharedPreferences sharedPreferences, int portfolioId) {
		return sharedPreferences.getString(PaiUtils.PREF_PORTFOLIO_MA_TYPE + portfolioId, portfolioId == 1 ? "E" : "S");
	}
	
	public static void savePortfolioName(Resources resources, SharedPreferences sharedPreferences, int portfolioId, String portfolioName) {
		Editor editor = sharedPreferences.edit();
		editor.putString(PREF_PORTFOLIO_KEY+portfolioId, portfolioName);
		editor.apply();
	}
}
