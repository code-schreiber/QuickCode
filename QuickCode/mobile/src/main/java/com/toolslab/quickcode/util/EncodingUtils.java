package com.toolslab.quickcode.util;


import android.graphics.Bitmap;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;


public class EncodingUtils {

    private static final int WIDTH = 500;

    private EncodingUtils() {
        // Hide utility class constructor
    }

    @Nullable
    @CheckResult
    @SuppressWarnings("SuspiciousNameCombination")
    public static Bitmap encode(BarcodeFormat format, String rawContent, int originalWidth, int originalHeight) {
        if (originalWidth < originalHeight) {
            // Most codes are wide
            int tempWidth = originalWidth;
            originalWidth = originalHeight;
            originalHeight = tempWidth;
        }
        BitmapUtils.Dimensions dimensions = BitmapUtils.getNewDimensions(WIDTH, originalWidth, originalHeight);
        return encodeAsBitmap(rawContent, format, dimensions.width, dimensions.height);
    }

    @Nullable
    @CheckResult
    public static Bitmap encodeQRCode(String rawContent) {
        //noinspection SuspiciousNameCombination
        return encodeAsBitmap(rawContent, BarcodeFormat.QR_CODE, WIDTH, WIDTH);
    }

    @Nullable
    @CheckResult
    private static Bitmap encodeAsBitmap(String rawContent, BarcodeFormat format, int width, int height) {
        return new Encoder().encodeAsBitmap(rawContent, format, width, height);
    }

}
