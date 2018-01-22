package com.toolslab.quickcode.util.log;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;


public class Tracker {

    private Tracker() {
        // Hide utility class constructor
    }

    public static void trackOnClick(Context context, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_NAME, itemName);
        logEvent(context, Event.VIEW_ITEM, bundle);
    }

    public static void trackInstallReferrer(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                logEvent(context, Event.FIRST_LAUNCH, bundle);
            } else {
                Logger.logError("No bundle for trackInstallReferrer");
            }
        } else {
            Logger.logError("No intent for trackInstallReferrer");
        }
    }

    public static void trackIntent(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                logEvent(context, Event.INTENT, bundle);
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


    private static class Event extends FirebaseAnalytics.Event {
        private static final String FIRST_LAUNCH = "first_launch";
        private static final String INTENT = "intent";
    }

    private static class Param extends FirebaseAnalytics.Param {
    }

}
