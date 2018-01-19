package com.toolslab.quickcode.util.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.toolslab.quickcode.util.log.Tracker;


public class InstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Tracker.trackInstallReferrer(context, intent);
    }

}
