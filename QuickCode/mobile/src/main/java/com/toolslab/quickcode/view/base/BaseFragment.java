package com.toolslab.quickcode.view.base;


import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.toolslab.quickcode.util.TypeUtils;
import com.toolslab.quickcode.util.log.Logger;
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
        Logger.logInfo(message);
    }

    protected static void logDebug(String message) {
        Logger.logDebug(message);
    }

    protected static void logWarning(String message) {
        Logger.logWarning(message);
    }

    protected static void logError(String message) {
        Logger.logError(message);
    }

    protected static void logException(Exception e) {
        Logger.logException(e);
    }

    protected static void logException(String message, Throwable e) {
        Logger.logException(message,e);
    }

    private void showDialog(DialogFragment dialog) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showDialog(dialog);
        } else {
            throw new IllegalArgumentException(getActivity() + " is not BaseActivity!");
        }
    }

}
