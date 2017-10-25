package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.util.BitmapUtils;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CodeFileCreator {

    private static final Integer[] SUPPORTED_BARCODE_FORMATS = {
//            Barcode.CODE_128,
//            Barcode.CODE_39,
//            Barcode.CODE_93,
//            Barcode.CODABAR,
//            Barcode.DATA_MATRIX,
//            Barcode.EAN_13,
//            Barcode.EAN_8,
//            Barcode.ITF,
            Barcode.QR_CODE,
//            Barcode.UPC_A,
//            Barcode.UPC_E,
            Barcode.PDF417,
            Barcode.AZTEC,
    };

    private CodeFileCreator() {
        // Hide utility class constructor
    }

    @NonNull
    static List<CodeFile> createCodeFiles(Context context, String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom) {
        List<CodeFile> codeFiles = new ArrayList<>();
        if (originalImage != null) {
            SparseArray<Barcode> barcodes = getCodesFromBitmap(context, originalImage);
            int barcodesSize = barcodes.size();
            if (barcodesSize > 0) {
                for (int i = 0; i < barcodesSize; i++) {
                    Barcode barcode = barcodes.get(barcodes.keyAt(i));
                    if (barcodesSize > 1) {
                        // append Code Index To Filename
                        originalFilename += " (Code " + i + ")";
                    }
                    CodeFile codeFile = getCodeFileFromBarcode(originalFilename, fileType, size, originalImage, importedFrom, barcode);
                    if (codeFile != null) {
                        codeFiles.add(codeFile);
                    }
                }
            }
        }
        return codeFiles;
    }

    private static boolean isBarcodeFormatSupported(int barcodeFormat) {
        return Arrays.asList(SUPPORTED_BARCODE_FORMATS).contains(barcodeFormat);
    }

    @NonNull
    public static String getSupportedBarcodeFormatsAsString() {
        List<String> supportedFormats = new ArrayList<>();
        for (Integer supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            String encodingFormatName = BarcodeFormatMapper.getEncodingFormatName(supportedBarcodeFormat);
            supportedFormats.add(encodingFormatName);
        }
        return TypeUtils.getCommaSeparatedStringsFromList(supportedFormats);
    }

    @Nullable
    private static CodeFile getCodeFileFromBarcode(String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom, Barcode barcode) {
        String encodingFormatName = BarcodeFormatMapper.getEncodingFormatName(barcode.format);
        String codeContentType = BarcodeFormatMapper.getContentType(barcode.valueFormat);
        String codeDisplayValue = barcode.displayValue;
        String codeRawValue = barcode.rawValue;

        if (isBarcodeFormatSupported(barcode.format)) {
            Bitmap codeImage = EncodingUtils.encode(BarcodeFormatMapper.getEncodingFormat(barcode.format), codeRawValue, barcode.getBoundingBox().width(), barcode.getBoundingBox().height());
            if (codeImage == null) {
                Logger.logError("Couldn't encode bitmap from barcode:" + codeRawValue);
            } else {
                OriginalCodeFile originalCodeFile = OriginalCodeFile.create(originalFilename, fileType, size, importedFrom);
                return createCodeFile(originalImage, encodingFormatName, codeContentType, codeDisplayValue, codeRawValue, codeImage, originalCodeFile);
            }
        } else {
            Logger.logError("Code format not supported: " + barcode.format + " - " + encodingFormatName + ". " + "Currently supported: " + getSupportedBarcodeFormatsAsString());
        }
        return null;
    }

    private static CodeFile createCodeFile(Bitmap originalImage, String encodingFormatName, String codeContentType, String codeDisplayValue, String codeRawValue, Bitmap codeImage, OriginalCodeFile originalCodeFile) {
        return CodeFile.create(originalCodeFile, originalImage, codeImage, encodingFormatName, codeContentType, codeDisplayValue, codeRawValue);
    }

    private static SparseArray<Barcode> getCodesFromBitmap(Context context, Bitmap bitmap) {
        BarcodeDetector detector = setupBarcodeDetector(context);
        if (detector == null) {
            return new SparseArray<>();
        }
        SparseArray<Barcode> detectedCodes = detectCodes(bitmap, detector);
        detector.release();
        return detectedCodes;
    }

    private static SparseArray<Barcode> detectCodes(Bitmap bitmap, BarcodeDetector detector) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> detectedCodes = detector.detect(frame);

        if (detectedCodes.size() == 0) {
            // Try again with a smaller image, which seems to work sometimes
            Bitmap smallerBitmap = BitmapUtils.scaleDownImage500Pixels(bitmap);
            if (smallerBitmap.getWidth() < bitmap.getWidth()) {
                Logger.logDebug("Trying to detect again with a smaller image, this time " + smallerBitmap.getWidth() + "x" + smallerBitmap.getHeight() + " instead of " + bitmap.getWidth() + "x" + bitmap.getHeight());
                return detectCodes(smallerBitmap, detector);
            } else {
                Logger.logDebug("Trying to detect again with a smaller images did not work.");
            }
        }
        Logger.logDebug(detectedCodes.size() + " detected codes");
        return detectedCodes;
    }

    @Nullable
    public static BarcodeDetector setupBarcodeDetector(Context context) {
        int supportedBarcodeFormats = getSupportedBarcodeFormats();
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(supportedBarcodeFormats)
                .build();
        if (!detector.isOperational()) {
            Logger.logError("BarcodeDetector is not operational.");
            return null;
        }
        return detector;
    }

    @VisibleForTesting
    static int getSupportedBarcodeFormats() {
        int supportedBarcodeFormats = 0;
        for (int supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedBarcodeFormats |= supportedBarcodeFormat;
        }
        return supportedBarcodeFormats;
    }

}
