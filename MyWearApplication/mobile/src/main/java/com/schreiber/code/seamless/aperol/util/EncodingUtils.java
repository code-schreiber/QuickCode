package com.schreiber.code.seamless.aperol.util;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;


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
        Point dimensions = CodeFileFactory.getNewDimensions(WIDTH, originalWidth, originalHeight);
        return encodeAsBitmap(rawContent, format, dimensions.x, dimensions.y);
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
        try {
            BitMatrix encodedMatrix = new MultiFormatWriter().encode(rawContent, format, width, height, null);
            int matrixWidth = encodedMatrix.getWidth();
            int matrixHeight = encodedMatrix.getHeight();
            int[] pixels = getPixels(encodedMatrix);
            Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);
            int x = 0;
            int y = 0;
            int offset = 0;
            bitmap.setPixels(pixels, offset, matrixWidth, x, y, matrixWidth, matrixHeight);
            return bitmap;
        } catch (IllegalArgumentException | WriterException e) {
            Logger.logException(e);
            return null;
        }
    }

    @CheckResult
    private static int[] getPixels(BitMatrix encodedMatrix) {
        int white = 0xFFFFFFFF;
        int black = 0xFF000000;
        int width = encodedMatrix.getWidth();
        int height = encodedMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = encodedMatrix.get(x, y) ? black : white;
            }
        }
        return pixels;
    }

}
