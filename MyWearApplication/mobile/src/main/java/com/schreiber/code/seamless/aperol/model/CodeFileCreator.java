package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;


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
    static ArrayList<CodeFile> createCodeFiles(Context context, String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom) {
        ArrayList<CodeFile> codeFiles = new ArrayList<>();
        if (originalImage != null) {
            SparseArray<Barcode> barcodes = getCodesFromBitmap(context, originalImage);
            if (barcodes.size() < 1) {
                // FIXME bug when unsuported code is still detected
                Logger.logError("No barcodes detected, creating CodeFile without barcode: " + originalFilename);
                OriginalCodeFile originalCodeFile = OriginalCodeFile.create(originalFilename, fileType, size, importedFrom);
                CodeFile codeFile = CodeFile.create(originalCodeFile, originalImage);
                codeFiles.add(codeFile);
            } else {
                for (int i = 0; i < barcodes.size(); i++) {
                    int key = barcodes.keyAt(i);
                    Barcode barcode = barcodes.get(key);
                    if (barcodes.size() > 1) {
                        // append Code Index To Filename
                        originalFilename += " ( Code " + key + ")";
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
        ArrayList<String> supportedFormats = new ArrayList<>();
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
                return CodeFile.create(originalCodeFile, originalImage, codeImage, encodingFormatName, codeContentType, codeDisplayValue, codeRawValue);
            }
        } else {
            Logger.logError("Code format not supported: " + barcode.format + " - " + encodingFormatName + ". " + "Currently supported: " + getSupportedBarcodeFormatsAsString());
        }
        return null;
    }

    private static SparseArray<Barcode> getCodesFromBitmap(Context context, Bitmap bitmap) {
        BarcodeDetector detector = setupBarcodeDetector(context);
        if (detector == null) {
            return new SparseArray<>();
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> detectedCodes = detector.detect(frame);
        detector.release();
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

    private static int getSupportedBarcodeFormats() {
        int supportedBarcodeFormats = 0;
        for (int supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedBarcodeFormats |= supportedBarcodeFormat;
        }
        return supportedBarcodeFormats;
    }

}