package com.toolslab.quickcode.util.log;


import com.google.firebase.crash.FirebaseCrash;
import com.toolslab.quickcode.BuildConfig;
import com.toolslab.quickcode.util.TypeUtils;

import timber.log.Timber;


// TODO [Before V1] add tags to logs
public class Logger {

    private Logger() {
        // Only use crash reporting on non-debug builds
        FirebaseCrash.setCrashCollectionEnabled(!BuildConfig.DEBUG);
    }

    public static void logInfo(String message) {
        if (isMessageOk(message)) {
            logToCrashLogger(message);
            Timber.i(message);
        }
    }

    public static void logDebug(String message) {
        if (isMessageOk(message)) {
            logToCrashLogger(message);
            Timber.d(message);
        }
    }

    public static void logWarning(String message) {
        if (isMessageOk(message)) {
            reportToCrashLogger(new WarningException(message));
            Timber.w(message);
        }
    }

    public static void logError(String message) {
        if (isMessageOk(message)) {
            reportToCrashLogger(new ErrorException(message));
            Timber.e(message);
        }
    }

    public static void logException(Exception e) {
        logException(e.getMessage(), e);
    }

    public static void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            logToCrashLogger(message);
            Timber.e(e, message);
        } else {
            Timber.e(e);
        }
        FirebaseCrash.report(e);
    }

    private static boolean isMessageOk(String message) {
        boolean isMessageOk = !TypeUtils.isEmpty(message);
        if (!isMessageOk) {
            logException(new IllegalArgumentException("No message provided to log! Message: \"" + message + "\""));
        }
        return isMessageOk;
    }

    private static void logToCrashLogger(String message) {
        FirebaseCrash.log(message);
    }

    private static void reportToCrashLogger(Throwable throwable) {
        FirebaseCrash.report(throwable);
    }

    private static class WarningException extends Throwable {
        WarningException(String message) {
            super("Warning: " + message);
        }
    }

    private static class ErrorException extends Throwable {
        ErrorException(String message) {
            super("Error: " + message);
        }
    }

}
