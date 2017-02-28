package com.schreiber.code.seamless.aperol.util;


import timber.log.Timber;

// TODO add tags
public class Logger {

    private Logger() {
        // Hide utility class constructor
    }

    public static void logInfo(String message) {
        if (isMessageOk(message)) {
            Timber.i(message);
        }
    }

    public static void logDebug(String message) {
        if (isMessageOk(message)) {
            Timber.d(message);
        }
    }

    public static void logWarning(String message) {
        if (isMessageOk(message)) {
            Timber.w(message);
        }
    }

    public static void logError(String message) {
        if (isMessageOk(message)) {
            Timber.e(message);
        }
    }

    public static void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            Timber.e(e, message);
        }
    }

    public static void logException(Exception e) {
        logException(e.getMessage(), e);
    }

    private static boolean isMessageOk(String message) {
        boolean isMessageOk = !TypeUtils.isEmpty(message);
        if (!isMessageOk) {
            Timber.e(new Exception(), "No message provided to log! Message: \"" + message + "\"");
        }
        return isMessageOk;
    }

}
