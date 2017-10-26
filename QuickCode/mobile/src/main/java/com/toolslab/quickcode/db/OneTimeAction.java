package com.toolslab.quickcode.db;


import android.content.Context;
import android.content.SharedPreferences;


public final class OneTimeAction {


    private static final String ONE_TIME_ACTION_PREFERENCES_KEY = "ONE_TIME_ACTION_PREFERENCES_KEY";


    private OneTimeAction() {
        // Hide utility class constructor
    }

    public static void setHappened(Context context, String key, String value) {
        getEditor(context).putBoolean(key, true).apply();
    }

    public static boolean hasHappened(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return SharedPreferencesWrapper.getDefaultPreferences(context, ONE_TIME_ACTION_PREFERENCES_KEY);
    }


}
