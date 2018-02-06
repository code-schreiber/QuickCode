package com.toolslab.quickcode.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.toolslab.quickcode.util.TypeUtils;
import com.toolslab.quickcode.util.bitmap.BitmapUtils;
import com.toolslab.quickcode.util.encode.EncodingUtils;
import com.toolslab.quickcode.util.log.Logger;

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
        return TypeUtils.createCommaSeparatedStringFromList(supportedFormats);
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

    private static SparseArray<Barcode> getCodesFromBitmap(Context context, @NonNull Bitmap bitmap) {
        BarcodeDetector detector = setupBarcodeDetector(context);
        if (detector == null) {
            Logger.logError("getCodesFromBitmap: BarcodeDetector is not operational.");
            return new SparseArray<>();
        }
        SparseArray<Barcode> detectedCodes = detectCodes(bitmap, detector);
        detector.release();
        return detectedCodes;
    }

    private static SparseArray<Barcode> detectCodes(@NonNull Bitmap bitmap, @NonNull BarcodeDetector detector) {
        SparseArray<Barcode> detectedCodes = detectCodesScalingDownImage(bitmap, detector);
        if (detectedCodes.size() == 0) {
            detectedCodes = detectCodesPuttingBackground(bitmap, detector);
        }
        Logger.logDebug(detectedCodes.size() + " detected codes");
        return detectedCodes;
    }

    private static SparseArray<Barcode> detectCodesScalingDownImage(@NonNull Bitmap bitmap, @NonNull BarcodeDetector detector) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> detectedCodes = detector.detect(frame);
        if (detectedCodes.size() == 0) {
            // Try again with a smaller image, which works sometimes
            if (BitmapUtils.isScalingDown500PixelsPossible(bitmap)) {
                Bitmap smallerBitmap = BitmapUtils.scaleDownImage500Pixels(bitmap);
                if (smallerBitmap != null && smallerBitmap.getWidth() < bitmap.getWidth()) {
                    Logger.logDebug("Trying to detect again with a smaller image," +
                            " this time " + smallerBitmap.getWidth() + "x" + smallerBitmap.getHeight() +
                            " instead of " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    return detectCodesScalingDownImage(smallerBitmap, detector);
                }
            }
            Logger.logDebug("Trying to detect again with a smaller images did not work.");
        }
        return detectedCodes;
    }

    private static SparseArray<Barcode> detectCodesPuttingBackground(@NonNull Bitmap bitmap, @NonNull BarcodeDetector detector) {
        Logger.logDebug("Trying to detect again with a white background");
        SparseArray<Barcode> detectedCodes = detectCodesScalingDownImage(BitmapUtils.putWhiteBackground(bitmap), detector);
        if (detectedCodes.size() == 0) {
            Logger.logDebug("Trying to detect again with a black background");
            detectedCodes = detectCodesScalingDownImage(BitmapUtils.putBlackBackground(bitmap), detector);
            if (detectedCodes.size() == 0) {
                Logger.logDebug("Trying to detect again with a a white or black background did not work.");
            }
        }
        return detectedCodes;
    }

    @Nullable
    public static BarcodeDetector setupBarcodeDetector(Context context) {
        int supportedBarcodeFormats = getSupportedBarcodeFormats();
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(supportedBarcodeFormats)
                .build();
        if (!detector.isOperational()) {
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
