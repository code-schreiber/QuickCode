package com.schreiber.code.seamless.aperol.view.main;


import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.util.TypeUtils;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.SimpleDialogFragment;

import timber.log.Timber;


public class BaseActivity extends AppCompatActivity {

    void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    void overridePendingTransitionExit() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    void overridePendingTransitionFadeIn() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    void overridePendingTransitionFadeOut() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
        }
    }

    void showSimpleDialog(String message) {
        logDebug("Showing dialog with message: " + message);
        showDialog(SimpleDialogFragment.newInstance(message));
    }

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


