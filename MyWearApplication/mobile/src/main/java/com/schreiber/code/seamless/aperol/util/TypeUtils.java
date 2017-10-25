package com.schreiber.code.seamless.aperol.util;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;


public class TypeUtils {

    private static final String NULL = "null";

    private TypeUtils() {
        // Hide utility class constructor
    }

    @NonNull
    public static String getCommaSeparatedStringsFromList(List<String> strings) {
        String commaSeparatedStrings = "";
        for (String string : strings) {
            commaSeparatedStrings += string + ", ";
        }
        return commaSeparatedStrings.substring(0, commaSeparatedStrings.length() - 2);
    }

    @CheckResult
    public static boolean isValid(String s) {
        return !isEmpty(s) && !s.equals(NULL);
    }

    @CheckResult
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
