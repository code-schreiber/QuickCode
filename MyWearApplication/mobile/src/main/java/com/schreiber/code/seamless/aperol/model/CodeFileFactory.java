package com.schreiber.code.seamless.aperol.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public abstract class CodeFileFactory {

    private static final Integer[] SUPPORTED_BARCODE_FORMATS = {
            Barcode.CODE_128,
            Barcode.CODE_39,
            Barcode.CODE_93,
            Barcode.CODABAR,
            Barcode.DATA_MATRIX,
            Barcode.EAN_13,
            Barcode.EAN_8,
            // Barcode.ITF,
            Barcode.QR_CODE,
            Barcode.UPC_A,
            Barcode.UPC_E,
            Barcode.PDF417,
            Barcode.AZTEC,
    };


    public static CodeFile createCodeFileFromPath(Context context, String path) {
        Uri uri = Uri.parse(path);
        String originalFilename = uri.getLastPathSegment();
        String fileType = getFileSuffix(originalFilename);
        String size = "-1";// TODO
        Bitmap originalImage = null;
        try {
            originalImage = BitmapFactory.decodeStream(context.getAssets().open(path));
        } catch (IOException e) {
            Logger.logException(e);
        }
        String importedFrom = "app assets/" + (new File(path)).getParent();
        return createItem(context, originalFilename, fileType, size, originalImage, importedFrom);
    }

    public static CodeFile createCodeFileFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String fileType = contentResolver.getType(uri);
        String size = UriUtils.getSize(contentResolver, uri);
        Bitmap originalImage = getBitmapFromUri(context, uri);
        return createItem(context, originalFilename, fileType, size, originalImage, uri.toString());
    }

    public static CodeFile createCodeFileFromCodeFile(Context context, CodeFile codeFile, Bitmap originalImage) {
        String originalFilename = codeFile.originalCodeFile().filename();
        String fileType = codeFile.originalCodeFile().fileType();
        String size = codeFile.originalCodeFile().size();
        return createItem(context, originalFilename, fileType, size, originalImage, "CodeFile of " + originalFilename);
    }

    @NonNull
    static String getFileSuffix(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
    }

    @Nullable
    private static CodeFile createItem(Context context, String originalFilename, String fileType, String size, Bitmap originalImage, String importedFrom) {
        if (originalImage != null) {
            SparseArray<Barcode> barcodes = getCodesFromBitmap(context, originalImage);
            Bitmap codeImage = null;
            Barcode barcode;

            String encodingFormatName = "";
            String codeContentType = "";
            String codeDisplayValue = "";
            String codeRawValue = "";

            if (barcodes.size() < 1) {
                Logger.logError("No barcodes detected in " + originalFilename);
            } else {
                if (barcodes.size() > 1) {
                    Logger.logError(barcodes.size() + " barcodes found in bitmap! Saving only one.");
//                            TODO [Premium] create multiple files
                }
                for (int i = 0; i < barcodes.size(); i++) {
                    int key = barcodes.keyAt(i);
                    barcode = barcodes.get(key);

                    BarcodeFormat encodingFormat = getEncodingFormat(barcode.format);
                    encodingFormatName = getEncodingFormatName(barcode.format);
                    codeContentType = getContentType(barcode.valueFormat);
                    codeDisplayValue = barcode.displayValue;
                    codeRawValue = barcode.rawValue;

                    Logger.logDebug("Barcode found, encodingFormat: " + encodingFormat);
                    if (encodingFormat != null) {
                        codeImage = EncodingUtils.encode(encodingFormat, codeRawValue, barcode.getBoundingBox().width(), barcode.getBoundingBox().height());
                        if (codeImage == null) {
                            Logger.logError("Couldn't encode bitmap from barcode:" + codeRawValue);
                        }
                    } else {
                        Logger.logError("Code format not supported: " + barcode.format + " - " + encodingFormatName + ". " + "Currently supported: " + getSupportedFormats());
                    }
                }
            }
            OriginalCodeFile originalCodeFile = OriginalCodeFile.create(originalFilename, fileType, size, importedFrom);
            CodeFile codeFile = CodeFile.create(originalCodeFile, encodingFormatName, codeContentType, codeDisplayValue, codeRawValue);
            try {
                if (saveBitmapsToFile(context, codeFile, originalImage, codeImage)) {
                    return codeFile;
                } else {
                    Logger.logError("Couldn't save images from " + originalFilename);
                }
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return null;
    }

    private static Bitmap createThumbnail(Bitmap originalImage) {
        return resizeImage(originalImage, 200);
    }

    private static Bitmap resizeImage(Bitmap bitmap, int scaleSize) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        Point dimensions = getNewDimensions(scaleSize, originalWidth, originalHeight);
        return Bitmap.createScaledBitmap(bitmap, dimensions.x, dimensions.y, false);
    }

    @NonNull
    public static Point getNewDimensions(int scaleSize, int originalWidth, int originalHeight) {
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
        Point point = new Point();
        point.set(newWidth, newHeight);
        return point;
    }

    @CheckResult
    private static boolean saveBitmapsToFile(Context context, CodeFile codeFile, Bitmap originalImage, Bitmap codeImage)
            throws IOException {
        CodeFileViewModel codeFileViewModel = CodeFileViewModel.create(codeFile);
        boolean allSaved = codeFileViewModel.saveOriginalImage(context, originalImage);
        if (allSaved) {
            Bitmap thumbnail = createThumbnail(originalImage);
            if (thumbnail != null) {
                allSaved = codeFileViewModel.saveOriginalThumbnailImage(context, thumbnail);
                if (allSaved) {
                    if (codeImage != null) {
                        allSaved = codeFileViewModel.saveCodeImage(context, codeImage);
                        if (allSaved) {
                            thumbnail = createThumbnail(codeImage);
                            if (thumbnail != null) {
                                allSaved = codeFileViewModel.saveCodeThumbnailImage(context, thumbnail);
                            }// TODO else
                        }
                    }
                }
            }// TODO else
        }
        // TODO some type of rollback on fail
        return allSaved;
    }

    @Nullable
    private static Bitmap getBitmapFromUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        if (UriUtils.fileExists(resolver, uri)) {
            if (UriUtils.isPdf(resolver, uri)) {
                return pdfToBitmap(context, uri);
            } else if (UriUtils.isImage(resolver, uri)) {
                return UriUtils.getBitmapFromUri(context.getContentResolver(), uri);
            } else if (UriUtils.isText(resolver, uri)) {
                // TODO
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                return EncodingUtils.encodeQRCode(textContent);
            } else {
                Logger.logError("No known file type: " + uri);
            }
        } else {
            Logger.logError("File doesn't exist: " + uri);
        }
        return null;
    }

    private static SparseArray<Barcode> getCodesFromBitmap(Context context, Bitmap bitmap) {
        BarcodeDetector detector = setupBarcodeDetector(context);
        if (detector == null) {
            return new SparseArray<>();
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        return detector.detect(frame);
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

    private static int getSupportedBarcodeFormats() {
        int supportedBarcodeFormats = 0;
        for (int supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedBarcodeFormats |= supportedBarcodeFormat;
        }
        return supportedBarcodeFormats;
    }

    @Nullable
    private static BarcodeFormat getEncodingFormat(int barcodeFormat) {
        if (isBarcodeFormatSupported(barcodeFormat)) {
            switch (barcodeFormat) {
                case Barcode.CODE_128:
                    return BarcodeFormat.CODE_128;
                case Barcode.CODE_39:
                    return BarcodeFormat.CODE_39;
                case Barcode.CODE_93:
                    return BarcodeFormat.CODE_93;
                case Barcode.CODABAR:
                    return BarcodeFormat.CODABAR;
                case Barcode.DATA_MATRIX:
                    return BarcodeFormat.DATA_MATRIX;
                case Barcode.EAN_13:
                    return BarcodeFormat.EAN_13;
                case Barcode.EAN_8:
                    return BarcodeFormat.EAN_8;
                case Barcode.ITF:
                    return BarcodeFormat.ITF;
                case Barcode.QR_CODE:
                    return BarcodeFormat.QR_CODE;
                case Barcode.UPC_A:
                    return BarcodeFormat.UPC_A;
                case Barcode.UPC_E:
                    return BarcodeFormat.UPC_E;
                case Barcode.PDF417:
                    return BarcodeFormat.PDF_417;
                case Barcode.AZTEC:
                    return BarcodeFormat.AZTEC;
                default:
                    Logger.logError("Unknown code format:" + barcodeFormat);
                    return null;
            }
        }
        Logger.logError("Unsupported code format: " + barcodeFormat);
        return null;
    }

    private static String getEncodingFormatName(int barcodeFormat) {
        if (isBarcodeFormatSupported(barcodeFormat)) {
            switch (barcodeFormat) {
                case Barcode.CODE_128:
                    return "CODE 128";
                case Barcode.CODE_39:
                    return "CODE 39";
                case Barcode.CODE_93:
                    return "CODE 93";
                case Barcode.CODABAR:
                    return "CODABAR";
                case Barcode.DATA_MATRIX:
                    return "DATA MATRIX";
                case Barcode.EAN_13:
                    return "EAN 13 ";
                case Barcode.EAN_8:
                    return "EAN 8";
                case Barcode.ITF:
                    return "ITF";
                case Barcode.QR_CODE:
                    return "QR CODE";
                case Barcode.UPC_A:
                    return "UPC A";
                case Barcode.UPC_E:
                    return "UPC E";
                case Barcode.PDF417:
                    return "PDF 417";
                case Barcode.AZTEC:
                    return "AZTEC";
                default:
                    Logger.logError("Unknown code format:" + barcodeFormat);
                    return "Unknown code format: " + barcodeFormat;
            }
        }
        Logger.logError("Unsupported code format: " + barcodeFormat);
        return "Unsupported code format: " + barcodeFormat;
    }

    private static String getContentType(int barcodeValueFormat) {
        switch (barcodeValueFormat) {
            case Barcode.CONTACT_INFO:
                return "CONTACT_INFO";
            case Barcode.EMAIL:
                return "EMAIL";
            case Barcode.ISBN:
                return "ISBN";
            case Barcode.PHONE:
                return "PHONE";
            case Barcode.PRODUCT:
                return "PRODUCT";
            case Barcode.SMS:
                return "SMS";
            case Barcode.TEXT:
                return "TEXT";
            case Barcode.URL:
                return "URL";
            case Barcode.WIFI:
                return "WIFI";
            case Barcode.GEO:
                return "GEO";
            case Barcode.CALENDAR_EVENT:
                return "CALENDAR_EVENT";
            case Barcode.DRIVER_LICENSE:
                return "DRIVER_LICENSE";
            default:
                Logger.logError("Unknown barcodeValueFormat:" + barcodeValueFormat);
                return "Unknown barcodeValueFormat: " + barcodeValueFormat;
        }
    }

    private static boolean isBarcodeFormatSupported(int barcodeFormat) {
        return Arrays.asList(SUPPORTED_BARCODE_FORMATS).contains(barcodeFormat);
    }

    @NonNull
    public static String getSupportedFormats() {
        String supportedFormats = "";
        for (Integer supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedFormats += getEncodingFormatName(supportedBarcodeFormat) + "\n";
        }
        return supportedFormats.trim();
    }

    @Nullable
    private static Bitmap pdfToBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        int pageNum = 0;
        try {
            // create a new renderer
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);

                // let us just render all pages
                int pageCount = renderer.getPageCount();
                if (pageCount > 0) {
                    if (pageCount != 1) {
                        Logger.logError("Pdf has " + pageCount + " pages.");
                        // TODO [Premium] create multiple files
                    }
                    PdfRenderer.Page page = renderer.openPage(pageNum);
                    bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

                    // say we render for showing on the screen
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                }
                renderer.close();
            }
        } catch (IOException e) {
            Logger.logException(e);
        }
        return bitmap;
    }

}