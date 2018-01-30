package com.toolslab.quickcode.util.log;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.analytics.FirebaseAnalytics;


public class Tracker {

    private Tracker() {
        // Hide utility class constructor
    }

    public static void trackOnClick(Context context, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(TrackerParam.ITEM_NAME, itemName);
        logEvent(context, TrackerEvent.VIEW_ITEM, bundle);
    }

    public static void trackInstallReferrer(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                logEvent(context, TrackerEvent.FIRST_LAUNCH, bundle);
            } else {
                Logger.logError("No bundle for trackInstallReferrer");
            }
        } else {
            Logger.logError("No intent for trackInstallReferrer");
        }
    }

    public static void trackInstallReferrer(Context context, ReferrerDetails referrerDetails) {
        if (referrerDetails != null) {
            Bundle bundle = new Bundle();
            bundle.putString(TrackerParam.INSTALL_REFERRER, referrerDetails.getInstallReferrer());
            bundle.putLong(TrackerParam.INSTALL_BEGIN_TIMESTAMP, referrerDetails.getInstallBeginTimestampSeconds());
            bundle.putLong(TrackerParam.REFERRER_CLICK_TIMESTAMP, referrerDetails.getReferrerClickTimestampSeconds());
            logEvent(context, TrackerEvent.REFERRER_DETAILS, bundle);
        } else {
            Logger.logError("No referrerDetails for trackInstallReferrer");
        }
    }

    public static void trackIntent(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                logEvent(context, TrackerEvent.INTENT, bundle);
            } else {
                Logger.logError("No bundle for trackIntent");
            }
        } else {
            Logger.logError("No intent for trackIntent");
        }
    }

    private static void logEvent(Context context, String event, Bundle bundle) {
        Logger.logDebug("Logging event " + event + ": " + bundle2string(bundle));
        FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
    }

    @Nullable
    private static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder string = new StringBuilder("Bundle {");
        for (String key : bundle.keySet()) {
            string.append(" ").append(key).append(" -> ").append(bundle.get(key)).append(";");
        }
        string.append(" }");
        return string.toString();
    }


    private static class TrackerEvent extends FirebaseAnalytics.Event {
        private static final String FIRST_LAUNCH = "first_launch";
        private static final String REFERRER_DETAILS = "referrer_details";
        private static final String INTENT = "intent";
    }

    private static class TrackerParam extends FirebaseAnalytics.Param {
        private static final String INSTALL_REFERRER = "install_referrer";
        private static final String REFERRER_CLICK_TIMESTAMP = "referrer_click_timestamp";
        private static final String INSTALL_BEGIN_TIMESTAMP = "install_begin_timestamp";
    }

}
