package com.toolslab.quickcode.util.log;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.StringRes;

import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.annotation.Nonnull;


public class Tracker {

    private static final String SEMICOLON = ";";
    private static final String EQUALS = "=";

    private Tracker() {
        // Hide utility class constructor
    }

    public static void trackOnClick(Context context, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(TrackerParam.ITEM_NAME, itemName);
        logEvent(context, TrackerEvent.VIEW_ITEM, bundle);
    }

    public static void trackOnClick(Context context, @StringRes int resId) {
        trackOnClick(context, context.getString(resId));
    }

    public static void trackOnLongClick(Context context, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(TrackerParam.ITEM_NAME, itemName);
        logEvent(context, TrackerEvent.LONG_CLICK, bundle);
    }

    public static void trackOnLongClick(Context context, @StringRes int resId) {
        trackOnLongClick(context, context.getString(resId));
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
            logEvent(context, TrackerEvent.INTENT, intentToBundle(intent));
        } else {
            Logger.logError("No intent for trackIntent");
        }
    }

    private static void logEvent(Context context, String event, Bundle bundle) {
        Logger.logDebug("Logging event " + event + ": " + bundle2string(bundle));
        FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
    }

    @CheckResult
    @Nonnull
    private static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return "Bundle is null";
        }
        StringBuilder string = new StringBuilder("Bundle {");
        for (String key : bundle.keySet()) {
            string.append(" ").append(key).append(EQUALS).append(bundle.get(key)).append(SEMICOLON);
        }
        string.append(" }");
        return string.toString();
    }

    @CheckResult
    @Nonnull
    private static Bundle intentToBundle(@Nonnull Intent intent) {
        Bundle bundle = new Bundle();
        String[] items = intent.toUri(0).split(SEMICOLON);
        if (items.length == 0) {
            bundle.putString("intent_as_string", intent.toString());
        } else {
            for (String item : items) {
                if (item.contains(EQUALS)) {
                    String key = item.substring(0, item.indexOf(EQUALS)).trim().replace(" ", "_");
                    bundle.putString(key, item.replace(key + EQUALS, ""));
                } else {
                    bundle.putString("item_" + item, item);
                }
            }
        }
        if (intent.getExtras() != null) {
            bundle.putBoolean(TrackerParam.HAS_EXTRAS, true);
            bundle.putAll(intent.getExtras());
        } else {
            bundle.putBoolean(TrackerParam.HAS_EXTRAS, false);
        }
        return bundle;
    }


    private static class TrackerEvent extends FirebaseAnalytics.Event {
        private static final String FIRST_LAUNCH = "first_launch";
        private static final String REFERRER_DETAILS = "referrer_details";
        private static final String INTENT = "intent";
        private static final String LONG_CLICK = "long_click";
    }

    private static class TrackerParam extends FirebaseAnalytics.Param {
        private static final String INSTALL_REFERRER = "install_referrer";
        private static final String REFERRER_CLICK_TIMESTAMP = "referrer_click_timestamp";
        private static final String INSTALL_BEGIN_TIMESTAMP = "install_begin_timestamp";
        private static final String HAS_EXTRAS = "has_extras";
    }

}
