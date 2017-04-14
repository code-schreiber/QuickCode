package com.schreiber.code.seamless.aperol.util;


import android.graphics.Bitmap;
import android.support.annotation.NonNull;


public final class BitmapUtils {

    static final class Dimensions {

        int width;
        int height;

        Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private BitmapUtils() {
        // Hide utility class constructor
    }

    public static Bitmap resizeImage(Bitmap bitmap, int scaleSize) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        Dimensions dimensions = getNewDimensions(scaleSize, originalWidth, originalHeight);
        return Bitmap.createScaledBitmap(bitmap, dimensions.width, dimensions.height, false);
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

}
