package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.orhanobut.hawk.Hawk;
import com.schreiber.code.seamless.aperol.model.ListItem;

import java.util.ArrayList;

public final class SharedPreferencesWrapper {

    private static final String FONT_STATISTIC_KEY = "FONT_STATISTIC_KEY";
    private static final String QUESTION_mark_KEY = "QUESTION_mark_KEY";


    private SharedPreferencesWrapper() {
        // Hide utility class constructor
    }

    public static String getFontStatistic(Context context) {
        return getDefaultPreferences(context).getString(FONT_STATISTIC_KEY, "");
    }

    public static void setFontStatistic(Context context, String fontStatistic) {
        getEditor(context).putString(FONT_STATISTIC_KEY, fontStatistic).apply();
    }

    public static void addQuestionMark(Context context, ListItem listItem) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context).build();
        }
        ArrayList<ListItem> items = getQuestionMark(context);
        items.add(listItem);
        Hawk.put(QUESTION_mark_KEY, items);
    }

    public static ArrayList<ListItem> getQuestionMark(Context context) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context).build();
        }
        ArrayList<ListItem> list = Hawk.get(QUESTION_mark_KEY);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static <T> boolean deleteQuestionMark(Context context, T t) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context).build();
        }
        return Hawk.delete(QUESTION_mark_KEY);
    }

    public static <T> boolean containsQuestionMark(Context context, T t) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context).build();
        }
        return Hawk.contains(QUESTION_mark_KEY);
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
