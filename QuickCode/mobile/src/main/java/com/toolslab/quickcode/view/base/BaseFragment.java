package com.toolslab.quickcode.view.base;


import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.toolslab.quickcode.util.TypeUtils;
import com.toolslab.quickcode.view.common.view.dialog.SimpleDialogFragment;

import timber.log.Timber;


public class BaseFragment extends Fragment {

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

    protected static void logInfo(String message) {
        if (isMessageOk(message)) {
            Timber.i(message);
        }
    }

    protected static void logDebug(String message) {
        if (isMessageOk(message)) {
            Timber.d(message);
        }
    }

    protected static void logWarning(String message) {
        if (isMessageOk(message)) {
            Timber.w(message);
        }
    }

    protected static void logError(String message) {
        if (isMessageOk(message)) {
            Timber.e(message);
        }
    }

    protected static void logException(Exception e) {
        logException(e.getMessage(), e);
    }

    protected static void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            Timber.e(e, message);
        }
    }

    private static boolean isMessageOk(String message) {
        boolean isMessageOk = !TypeUtils.isEmpty(message);
        if (!isMessageOk) {
            Timber.e(new Exception(), "No message provided to log! Message: \"" + message + "\"");
        }
        return isMessageOk;
    }

    private void showDialog(DialogFragment dialog) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showDialog(dialog);
        } else {
            throw new IllegalArgumentException(getActivity() + " is not BaseActivity!");
        }
    }

}
