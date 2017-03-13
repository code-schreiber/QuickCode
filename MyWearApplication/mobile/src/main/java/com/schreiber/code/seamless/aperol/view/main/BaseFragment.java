package com.schreiber.code.seamless.aperol.view.main;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.schreiber.code.seamless.aperol.util.TypeUtils;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.SimpleDialogFragment;

import timber.log.Timber;


public class BaseFragment extends Fragment {


    public BaseFragment() {

    }

    void showSimpleDialog(String message) {
        showDialog(SimpleDialogFragment.newInstance(message));
    }

    void showDialog(DialogFragment dialog) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showDialog(dialog);
        }
    }

    static void logInfo(String message) {
        if (isMessageOk(message)) {
            Timber.i(message);
        }
    }

    static void logDebug(String message) {
        if (isMessageOk(message)) {
            Timber.d(message);
        }
    }

    static void logWarning(String message) {
        if (isMessageOk(message)) {
            Timber.w(message);
        }
    }

    static void logError(String message) {
        if (isMessageOk(message)) {
            Timber.e(message);
        }
    }

    static void logException(String message, Throwable e) {
        if (isMessageOk(message)) {
            Timber.e(e, message);
        }
    }

    static void logException(Exception e) {
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