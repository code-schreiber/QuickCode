package com.toolslab.quickcode.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;


public final class BitmapUtils {

    static final class Dimensions {

        final int width;
        final int height;

        Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private BitmapUtils() {
        // Hide utility class constructor
    }

    public static Bitmap scaleDownImage500Pixels(Bitmap bitmap) {
        int scaleSize = Math.max(bitmap.getWidth(), bitmap.getHeight()) - 500;
        return scaleDownImage(bitmap, scaleSize);
    }

    @Nullable
    public static Bitmap scaleDownImage(Bitmap bitmap, int scaleSize) {
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
    public static Bitmap createBitmapFromBase64EncodedString(String bitmapBase64Encoded) {
        byte[] bitmapBytes = Base64.decode(bitmapBase64Encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }

    @Nullable
    public static String getBase64encodedBitmap(@Nullable Bitmap originalImage) {
        if (originalImage != null) {
            byte[] bytes = getBytes(originalImage);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }
        return null;
    }

    @NonNull
    static Dimensions getNewDimensions(int scaleSize, int originalWidth, int originalHeight) {
        int newWidth = -1;
        int newHeight = -1;
        float ratio;
        if (originalHeight > originalWidth) {
            newHeight = scaleSize;
            ratio = (float) originalWidth / (float) originalHeight;
            newWidth = Math.round(newHeight * ratio);
        } else if (originalWidth > originalHeight) {
            newWidth = scaleSize;
            ratio = (float) originalHeight / (float) originalWidth;
            newHeight = Math.round(newWidth * ratio);
        } else if (originalHeight == originalWidth) {
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

    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

}
