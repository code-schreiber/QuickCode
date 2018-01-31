package com.toolslab.quickcode.util;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;


public class TypeUtils {

    private static final String NULL = "null";

    private TypeUtils() {
        // Hide utility class constructor
    }

    @NonNull
    @CheckResult
    public static String createCommaSeparatedStringFromList(List<String> strings) {
        StringBuilder commaSeparatedStrings = new StringBuilder();
        if (strings != null) {
            for (String string : strings) {
                commaSeparatedStrings.append(string).append(", ");
            }
        }
        if (commaSeparatedStrings.length() == 0) {
            return commaSeparatedStrings.toString();
        } else {
            return commaSeparatedStrings.substring(0, commaSeparatedStrings.length() - 2);
        }
    }

    @CheckResult
    public static boolean isValid(@Nullable String s) {
        return !isEmpty(s) && !s.equals(NULL);
    }

    @CheckResult
    public static boolean isEmpty(@Nullable String s) {
        return s == null || s.length() == 0;
    }

}
