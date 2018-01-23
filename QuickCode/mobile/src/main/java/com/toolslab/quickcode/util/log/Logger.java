package com.toolslab.quickcode.util.log;


import com.google.firebase.crash.FirebaseCrash;
import com.toolslab.quickcode.util.TypeUtils;

import timber.log.Timber;


// TODO [Before V1] add tags to logs
public class Logger {

    private Logger() {
        // Hide utility class constructor
    }

    public static void logInfo(String message) {
        if (isMessageOk(message)) {
            FirebaseCrash.log(message);
            Timber.i(message);
        }
    }

    public static void logDebug(String message) {
        if (isMessageOk(message)) {
            FirebaseCrash.log(message);
            Timber.d(message);
        }
    }

    public static void logWarning(String message) {
        if (isMessageOk(message)) {
            FirebaseCrash.report(new Exception("Warning: " + message));
            Timber.w(message);
        }
    }

    public static void logError(String message) {
        if (isMessageOk(message)) {
            FirebaseCrash.report(new Exception("Error: " + message));
            Timber.e(message);
        }
    }

    public static void logException(Exception e) {
        logException(e.getMessage(), e);
    }

    public static void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            FirebaseCrash.log(message);
            FirebaseCrash.report(e);
            Timber.e(e, message);
        }
    }

    private static boolean isMessageOk(String message) {
        boolean isMessageOk = !TypeUtils.isEmpty(message);
        if (!isMessageOk) {
            logException(new IllegalArgumentException("No message provided to log! Message: \"" + message + "\""));
        }
        return isMessageOk;
    }

}
