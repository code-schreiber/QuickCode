package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public final class SharedPreferencesWrapper {

    private static final String FONT_STATISTIC_KEY = "FONT_STATISTIC_KEY";
    private static final String LIST_ITEMS_KEY = "LIST_ITEMS_KEY";


    private SharedPreferencesWrapper() {
        // Hide utility class constructor
    }

    public static String getFontStatistic(Context context) {
        return getDefaultPreferences(context).getString(FONT_STATISTIC_KEY, "");
    }

    public static void setFontStatistic(Context context, String fontStatistic) {
        getEditor(context).putString(FONT_STATISTIC_KEY, fontStatistic).apply();
    }

    public static void clearAll(Context context) {
        getEditor(context).clear().commit();
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
