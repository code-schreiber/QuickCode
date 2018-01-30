package com.toolslab.quickcode.view.base;


import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


public class BaseFragment extends Fragment {

    protected void showSimpleDialog(String message) {
        getBaseActivity().showSimpleDialog(message);
    }

    protected void showSimpleError(@StringRes int resId, Object... formatArgs) {
        getBaseActivity().showSimpleError(resId, formatArgs);
    }

    protected void showSimpleError(String message) {
        getBaseActivity().showSimpleError(message);
    }

    protected void logInfo(String message) {
        getBaseActivity().logInfo(message);
    }

    protected void logDebug(String message) {
        getBaseActivity().logDebug(message);
    }

    protected void logWarning(String message) {
        getBaseActivity().logWarning(message);
    }

    protected void logError(String message) {
        getBaseActivity().logError(message);
    }

    protected void logException(Exception e) {
        getBaseActivity().logException(e);
    }

    protected void logException(String message, Throwable e) {
        getBaseActivity().logException(message, e);
    }

    private BaseActivity getBaseActivity() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return (BaseActivity) activity;
        } else {
            throw new IllegalArgumentException(activity + " is not BaseActivity!");
        }
    }

}
