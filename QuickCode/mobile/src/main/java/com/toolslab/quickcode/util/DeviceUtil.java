package com.toolslab.quickcode.util;

import android.os.Build;

public class DeviceUtil {

    private DeviceUtil() {
        // Hide utility class constructor
    }

    public static boolean isEmulator() {
        boolean emulatorHardware = Build.HARDWARE != null && (Build.HARDWARE.contains("vbox") || Build.HARDWARE.contains("goldfish"));
        boolean emulatorFingerprint = Build.FINGERPRINT != null && Build.FINGERPRINT.startsWith("generic");
        return emulatorHardware || emulatorFingerprint;
    }

}
