package com.toolslab.quickcode.util.bitmap;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.toolslab.quickcode.util.log.Logger;

import java.io.ByteArrayOutputStream;


public final class BitmapUtils {

    private static final int PIXELS_200 = 200;
    private static final int PIXELS_500 = 500;
    private static final int PIXELS_1000 = 1000;

    public static final class Dimensions {

        private final int width;
        private final int height;

        Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @CheckResult
        public int getWidth() {
            return width;
        }

        @CheckResult
        public int getHeight() {
            return height;
        }
    }

    private BitmapUtils() {
        // Hide utility class constructor
    }

    @CheckResult
    public static boolean isScalingDown500PixelsPossible(Bitmap bitmap) {
        return isScalingDownPossible(bitmap, PIXELS_500);
    }

    @Nullable
    @CheckResult
    public static Bitmap scaleDownImage200Pixels(Bitmap bitmap) {
        return scaleDownImage(bitmap, PIXELS_200);
    }

    @Nullable
    @CheckResult
    public static Bitmap scaleDownImage500Pixels(Bitmap bitmap) {
        int scaleSize = Math.max(bitmap.getWidth(), bitmap.getHeight()) - PIXELS_500;
        return scaleDownImage(bitmap, scaleSize);
    }

    @Nullable
    @CheckResult
    public static Bitmap scaleDownImage1000Pixels(Bitmap bitmap) {
        return scaleDownImage(bitmap, PIXELS_1000);
    }

    @Nullable
    @CheckResult
    private static Bitmap scaleDownImage(Bitmap bitmap, int scaleSize) {
        if (bitmap != null) {
            if (scaleSize > 0) {
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();
                if (originalWidth > scaleSize || originalHeight > scaleSize) {
                    Dimensions dimensions = getNewDimensions(scaleSize, originalWidth, originalHeight);
                    return Bitmap.createScaledBitmap(bitmap, dimensions.width, dimensions.height, false);
                } else {
                    // Image not bigger than scaleSize, no need to scale down
                    return bitmap;
                }
            } else {
                Logger.logWarning("Invalid scaleSize: " + scaleSize);
                return bitmap;
            }
        }
        return null;
    }

    @Nullable
    @CheckResult
    public static Bitmap createBitmapFromBase64EncodedString(String bitmapBase64Encoded) {
        byte[] bitmapBytes = Base64.decode(bitmapBase64Encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }

    @Nullable
    @CheckResult
    public static String getBase64encodedBitmap(@Nullable Bitmap originalImage) {
        if (originalImage != null) {
            byte[] bytes = getBytes(originalImage);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }
        return null;
    }

    @NonNull
    @CheckResult
    public static Dimensions getNewDimensions(int scaleSize, int originalWidth, int originalHeight) {
        int newWidth;
        int newHeight;
        float ratio;
        if (originalHeight > originalWidth) {
            newHeight = scaleSize;
            ratio = (float) originalWidth / (float) originalHeight;
            newWidth = Math.round(newHeight * ratio);
        } else if (originalWidth > originalHeight) {
            newWidth = scaleSize;
            ratio = (float) originalHeight / (float) originalWidth;
            newHeight = Math.round(newWidth * ratio);
        } else {
            // originalHeight and originalWidth are equal
            newHeight = scaleSize;
            newWidth = scaleSize;
        }
        if (newWidth < 1) {
            newWidth = scaleSize;
        }
        if (newHeight < 1) {
            newHeight = scaleSize;
        }
        return new Dimensions(newWidth, newHeight);
    }

    @NonNull
    @CheckResult
    public static Bitmap createWhiteBitmap(int width, int height) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return bitmap;
    }

    @NonNull
    @CheckResult
    public static Bitmap putWhiteBackground(Bitmap bitmap) {
        return putBackground(bitmap, Color.WHITE);
    }

    @NonNull
    @CheckResult
    public static Bitmap putBlackBackground(Bitmap bitmap) {
        return putBackground(bitmap, Color.BLACK);
    }

    @NonNull
    @CheckResult
    private static Bitmap putBackground(Bitmap bitmap, int color) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(color);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return newBitmap;
    }

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    private static boolean isScalingDownPossible(Bitmap bitmap, int howMuch) {
        int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
        return max > howMuch;
    }

}
