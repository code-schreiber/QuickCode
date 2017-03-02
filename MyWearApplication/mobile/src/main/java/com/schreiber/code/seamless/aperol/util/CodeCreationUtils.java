package com.schreiber.code.seamless.aperol.util;


import android.graphics.Bitmap;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class CodeCreationUtils {


    private CodeCreationUtils() {
        // Hide utility class constructor
    }

    @Nullable
    @CheckResult
    public static Bitmap encodeAsPdf417(String rawContent) {
        BarcodeFormat format = BarcodeFormat.PDF_417;
        int width = 400;
        int height = 400;
        return encodeAsBitmap(rawContent, format, width, height);
    }

    @Nullable
    @CheckResult
    public static Bitmap encodeAsQrCode(String rawContent) {
        BarcodeFormat format = BarcodeFormat.QR_CODE;
        int width = 400;
        int height = 400;
        return encodeAsBitmap(rawContent, format, width, height);
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
