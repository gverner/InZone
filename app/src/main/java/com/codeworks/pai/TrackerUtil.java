package com.codeworks.pai;

import android.app.Activity;
import android.content.res.Resources;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by glennverner on 4/27/15.
 */
public class TrackerUtil {

    public static Tracker getAppTracker(InZone application) {
        Tracker t = application.getTracker(
                InZone.TrackerName.APP_TRACKER);
        return t;
    }
    public static Tracker getAppTracker(Activity activity) {
        Tracker t = ((InZone) activity.getApplication()).getTracker(
                InZone.TrackerName.APP_TRACKER);
        return t;
    }

    public static void sendExtendedMarket(Activity activity) {
        Tracker tracker = getAppTracker(activity);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getResources().getString(R.string.trackScreensCategoryId))
                .setAction(activity.getResources().getString(R.string.trackViewActionId))
                .setLabel(activity.getResources().getString(R.string.trackExtendedMarketLabelId))
                .build());
    }

    public static void sendScreenView(Activity activity, int screenStringId) {
        Tracker tracker = getAppTracker(activity);
        tracker.setScreenName(activity.getResources().getString(screenStringId));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendTiming(InZone application, String name, String variable, long ms) {
        if (application != null) {
            Tracker t = getAppTracker(application);
            if (t != null) {
                t.send(new HitBuilders.TimingBuilder()
                                .setCategory("Service")
                                .setValue(ms)
                                .setVariable(variable)
                                .setLabel(name)
                                .build()
                );
            }
        }
    }
}
