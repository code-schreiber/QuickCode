package com.toolslab.quickcode.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtil {

    private ClipboardUtil() {
        // Hide utility class constructor
    }

    public static boolean copyToClipboard(Context context, String textLabel, String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(textLabel, textToCopy);
            clipboard.setPrimaryClip(clip);
            return true;
        }
        return false;
    }

}
