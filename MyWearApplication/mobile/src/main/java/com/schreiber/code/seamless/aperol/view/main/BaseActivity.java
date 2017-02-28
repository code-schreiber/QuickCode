package com.schreiber.code.seamless.aperol.view.main;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.schreiber.code.seamless.aperol.util.TypeUtils;

import timber.log.Timber;


public class BaseActivity extends AppCompatActivity {

    void showDialog(DialogFragment dialog) {
        dialog.show(getSupportFragmentManager(), dialog.toString());
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


