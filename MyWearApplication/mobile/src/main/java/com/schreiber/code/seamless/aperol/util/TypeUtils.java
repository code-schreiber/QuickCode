package com.schreiber.code.seamless.aperol.util;


import android.support.annotation.CheckResult;


public class TypeUtils {

    private static final String NULL = "null";

    private TypeUtils() {
        // Hide utility class constructor
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
