package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.LogInterceptor;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.util.Logger;

import java.util.ArrayList;


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

    public static boolean addListItem(Context context, CodeFile codeFile) {
        initHawk(context);
        ArrayList<CodeFile> items = getListItems(context);
        items.add(codeFile);
        return Hawk.put(LIST_ITEMS_KEY, items);
    }

    public static ArrayList<CodeFile> getListItems(Context context) {
        initHawk(context);
        ArrayList<CodeFile> codeFiles = Hawk.get(LIST_ITEMS_KEY);
        if (codeFiles == null) {
            codeFiles = new ArrayList<>();
        }
        return codeFiles;
    }

    public static <T> boolean deleteListItem(Context context, T t) {
        initHawk(context);
        return Hawk.delete(LIST_ITEMS_KEY);
    }

    public static <T> boolean containsListItem(Context context, T t) {
        initHawk(context);
        return Hawk.contains(LIST_ITEMS_KEY);
    }

    private static void initHawk(Context context) {
        if (!Hawk.isBuilt()) {
            Hawk.init(context)
                    .setLogInterceptor(new LogInterceptor() {
                        @Override
                        public void onLog(String message) {
                            Logger.logDebug("Hawk says: " + message);
                        }
                    })
                    .build();
        }
    }

    public static void clearAll(Context context) {
        getDefaultPreferences(context).edit().clear().commit();
        Hawk.deleteAll();
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
