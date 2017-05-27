package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.content.SharedPreferences;


public final class PremiumPreferences {


    private static final String PREMIUM_PREFERENCES_KEY = "PREMIUM_PREFERENCES_KEY";

    private static final String PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT_KEY = "PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT_KEY";
    private static final String PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT_KEY = "PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT_KEY";
    private static final String PREMIUM_ALLOW_CLICKING_LINKS_KEY = "PREMIUM_ALLOW_CLICKING_LINKS_KEY";

    private static final boolean DEFAULT = true; // TODO remove and default to false

    private PremiumPreferences() {
        // Hide utility class constructor
    }

    public static boolean allowMultiplePagesImport(Context context) {
        return getSharedPreferences(context).getBoolean(PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT_KEY, DEFAULT);
    }

    public static boolean allowMultipleCodesInImageImport(Context context) {
        return getSharedPreferences(context).getBoolean(PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT_KEY, DEFAULT);
    }


    public static boolean allowClickingLinks(Context context) {
        return getSharedPreferences(context).getBoolean(PREMIUM_ALLOW_CLICKING_LINKS_KEY, DEFAULT);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return SharedPreferencesWrapper.getDefaultPreferences(context, PREMIUM_PREFERENCES_KEY);
    }

}
