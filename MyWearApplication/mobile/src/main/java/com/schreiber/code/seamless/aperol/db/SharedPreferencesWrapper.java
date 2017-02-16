package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesWrapper {

    private static final String PREFERENCES_KEY = "PREFERENCES_KEY";

    private static final String FontStatistic_KEY = "FontStatistic_KEY";


    private SharedPreferencesWrapper() {
        // Hide utility class constructor
    }

    public static String getFontStatistic(Context context) {
        return getSharedPreferences(context).getString(FontStatistic_KEY, "");
    }

    public static void setFontStatistic(Context context, String fontStatistic) {
        getEditor(context).putString(FontStatistic_KEY, fontStatistic);
    }

    public static void registerPrefObserver(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unRegisterPrefObserver(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return getSharedPreferences(context, PREFERENCES_KEY);
    }

    static SharedPreferences getSharedPreferences(Context context, String preferencesKey) {
        return context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE);
    }

}
