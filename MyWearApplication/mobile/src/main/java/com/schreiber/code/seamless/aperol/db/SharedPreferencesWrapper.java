package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SharedPreferencesWrapper {

    private static final String PREFERENCES_KEY = "PREFERENCES_KEY";

    private static final String FontStatistic_KEY = "FontStatistic_KEY";


    private SharedPreferencesWrapper() {
        // Hide utility class constructor
    }

    public static String getFontStatistic(Context context) {
        return getDefaultPreferences(context).getString(FontStatistic_KEY, "");
    }

    public static void setFontStatistic(Context context, String fontStatistic) {
        getEditor(context).putString(FontStatistic_KEY, fontStatistic);
    }

    public static void registerPrefObserver(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unRegisterPrefObserver(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getDefaultPreferences(context).edit();
    }

    private static SharedPreferences getDefaultPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    static SharedPreferences getDefaultPreferences(Context context, String preferencesKey) {
        return context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE);
    }

}
