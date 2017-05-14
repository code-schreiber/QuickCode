package com.schreiber.code.seamless.aperol.util;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;


public class Tracker {

    private Tracker() {
        // Hide utility class constructor
    }

    public static void trackOnClick(Context context, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

//    public static void trackReferrer(String tag, Intent intent) {
//        String utmSource = "utm_source";
//        String referrer = "referrer";
//        String params = "";
//        if (intent != null) {
//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                if (bundle.containsKey(utmSource)) {
//                    params += " Campaign: " + bundle.get(utmSource) + " ";
//                }
//                if (bundle.containsKey(referrer)) {
//                    params += referrer + ": " + bundle.get(referrer);
//                }
//            }
//        }
//        if (!params.isEmpty()) {
//            logEvent(tag, INSTALL_REFERRER, params);
//        }
//    }
//
//    /**
//     * @param intent the intent to take data out of.
//     * @return A string description of the intent.
//     */
//    public static String getIntentDescription(Intent intent) {
//        String intentDescription = "";
//        if (intent != null) {
//            if (intent.getExtras() != null) {
//                intentDescription = intent.getExtras().toString();
//            } else {
//                intentDescription = intent.toString();
//            }
//        }
//        return intentDescription;
//    }

}
