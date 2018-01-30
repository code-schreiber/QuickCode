package com.toolslab.quickcode.view.base;


import android.annotation.SuppressLint;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.toolslab.quickcode.R;
import com.toolslab.quickcode.util.TypeUtils;
import com.toolslab.quickcode.view.common.view.dialog.SimpleDialogFragment;

import timber.log.Timber;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    public void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void overridePendingTransitionFadeIn() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void overridePendingTransitionFadeOut() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    protected void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    protected void enableDisplayHomeAsUp() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showSimpleDialog(@StringRes int resId) {
        showSimpleDialog(getString(resId));
    }

    protected void showSimpleDialog(String message) {
        logDebug("Showing dialog with message: " + message);
        showDialog(SimpleDialogFragment.newInstance(message));
    }

    protected void showSimpleError(@StringRes int resId, Object... formatArgs) {
        showSimpleError(getString(resId, formatArgs));
    }

    protected void showSimpleError(String message) {
        logError("Showing error with message: " + message);
        showDialog(SimpleDialogFragment.newInstance(message));
    }

    protected void showDialog(DialogFragment dialog) {
        if (isFinishing()) {
            logWarning("Not showing dialog, activity finishing.");
        } else if (getSupportFragmentManager().isStateSaved()) {
            logWarning("Not showing dialog, Can not perform this action after onSaveInstanceState.");
        } else {
            dialog.show(getSupportFragmentManager(), dialog.toString());
        }
    }

    public void logInfo(String message) {
        if (isMessageOk(message)) {
            Timber.i(message);
        }
    }

    public void logDebug(String message) {
        if (isMessageOk(message)) {
            Timber.d(message);
        }
    }

    public void logWarning(String message) {
        if (isMessageOk(message)) {
            Timber.w(message);
        }
    }

    public void logError(String message) {
        if (isMessageOk(message)) {
            Timber.e(message);
        }
    }

    public void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            Timber.e(e, message);
        }
    }

    public void logException(Exception e) {
        logException(e.getMessage(), e);
    }

    private boolean isMessageOk(String message) {
        boolean isMessageOk = !TypeUtils.isEmpty(message);
        if (!isMessageOk) {
            Timber.e(new Exception(), "No message provided to log! Message: \"" + message + "\"");
        }
        return isMessageOk;
    }

}


