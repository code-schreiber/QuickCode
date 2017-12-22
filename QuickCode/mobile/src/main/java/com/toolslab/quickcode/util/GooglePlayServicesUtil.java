package com.toolslab.quickcode.util;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayServicesUtil {

    private static final int REQUEST_CODE = 1000;

    private final Activity activity;

    public GooglePlayServicesUtil(Activity activity) {
        this.activity = activity;
    }

    public boolean isAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        return errorCode == ConnectionResult.SUCCESS;
    }

    public void showUpdateDialog() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (googleApiAvailability.isUserResolvableError(errorCode)) {
            googleApiAvailability.getErrorDialog(activity, errorCode, REQUEST_CODE).show();
        }
    }

}
