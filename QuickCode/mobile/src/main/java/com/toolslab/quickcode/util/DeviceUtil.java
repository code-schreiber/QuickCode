package com.toolslab.quickcode.util;

import android.os.Build;

public class DeviceUtil {

    private DeviceUtil() {
        // Hide utility class constructor
    }

    public static boolean isEmulator() {
        return Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86");
    }

}
