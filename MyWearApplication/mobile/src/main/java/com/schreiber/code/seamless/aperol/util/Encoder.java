package com.schreiber.code.seamless.aperol.util;


import android.graphics.Bitmap;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


class Encoder {

    @Nullable
    @CheckResult
    Bitmap encodeAsBitmap(String rawContent, BarcodeFormat format, int width, int height) {
        BitMatrix encodedMatrix = getEncodedMatrix(rawContent, format, width, height);
        if (encodedMatrix != null) {
            int matrixWidth = encodedMatrix.getWidth();
            int matrixHeight = encodedMatrix.getHeight();
            if (matrixWidth != width) {
                Logger.logError("Matrix doesn't have the same width! Is " + matrixWidth + " and should be " + width);
            }
            if (matrixHeight != height) {
                Logger.logError("Matrix doesn't have the same height! Is " + matrixHeight + " and should be " + height);
            }
            int[] pixels = getPixels(encodedMatrix);
            return createBitmap(matrixWidth, matrixHeight, pixels);
        }
        return null;
    }

    @Nullable
    private BitMatrix getEncodedMatrix(String rawContent, BarcodeFormat format, int width, int height) {
        try {
            Map<EncodeHintType, ?> hints = createHints(format, width, height);
            return new MultiFormatWriter().encode(rawContent, format, width, height, hints);
        } catch (WriterException e) {
            Logger.logException(e);
            return null;
        }
    }

    @NonNull
    private Map<EncodeHintType, ?> createHints(BarcodeFormat format, int width, int height) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        switch (format) {
            case PDF_417:
                // zxing's PDF_417 encoding has a crash bug when charset is not set
                hints.put(EncodeHintType.CHARACTER_SET, Charset.defaultCharset().name());
                break;
            case DATA_MATRIX:
                // TODO image is too small
//                int minSize = Math.max(width, height);
//                hints.put(EncodeHintType.MIN_SIZE, new Dimension(minSize, minSize));
                break;
        }
        return hints;
    }

    @NonNull
    private Bitmap createBitmap(int matrixWidth, int matrixHeight, int[] pixels) {
        Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);
        int x = 0;
        int y = 0;
        int offset = 0;
        bitmap.setPixels(pixels, offset, matrixWidth, x, y, matrixWidth, matrixHeight);
        return bitmap;
    }

    @CheckResult
    private int[] getPixels(BitMatrix encodedMatrix) {
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
