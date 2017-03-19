package com.schreiber.code.seamless.aperol.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class CodeFileFactory {

    private static final int SUPPORTED_BARCODE_FORMATS = Barcode.ALL_FORMATS; // TODO choose supported and unit tested formats a la setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)


    public static CodeFile createItemFromPath(Context context, String path) {
        Uri uri = Uri.parse(path);
        String originalFilename = uri.getLastPathSegment();
        String type = getFileSuffix(originalFilename);
        String size = "-1";// TODO
        Bitmap fileAsImage = null;
        try {
            fileAsImage = BitmapFactory.decodeStream(context.getAssets().open(path));
        } catch (IOException e) {
            Logger.logException(e);
        }
        String source = "app assets/" + (new File(path)).getParent();
        return createItem(context, originalFilename, type, size, fileAsImage, source);
    }

    @NonNull
    private static String getFileSuffix(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
    }

    public static CodeFile createItemFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String type = contentResolver.getType(uri);
        String size = UriUtils.getSize(contentResolver, uri);
        Bitmap fileAsImage = getBitmapFromUri(context, uri);
        return createItem(context, originalFilename, type, size, fileAsImage, uri.toString());
    }

    @Nullable
    private static CodeFile createItem(Context context, String originalFilename, String type, String size, Bitmap originalImage, String source) {
        if (originalImage != null) {
            int thumbnailSize = 200;
            Bitmap thumbnail = Bitmap.createScaledBitmap(originalImage, thumbnailSize, thumbnailSize, false);
            if (thumbnail != null) {
                try {
                    String suffix = getFileSuffix(originalFilename);
                    String fileName = originalFilename.replace("." + suffix, "");
                    CodeFile codeFile = CodeFile.create(fileName, originalFilename, type, size, source);
                    if (saveBitmapsToFile(context, codeFile, originalImage, thumbnail)) {
                        return codeFile;
                    } else {
                        Logger.logError("Couldn't save images from " + originalFilename);
                    }
                } catch (IOException e) {
                    Logger.logException(e);
                }
            }
        }
        return null;
    }

    @CheckResult
    private static boolean saveBitmapsToFile(Context context, CodeFile codeFile, Bitmap originalImage, Bitmap thumbnail)
            throws IOException {
        CodeFileViewModel codeFileViewModel = CodeFileViewModel.create(codeFile);
        boolean allSaved = codeFileViewModel.saveOriginalImage(context, originalImage);
        if (allSaved) {
            allSaved = codeFileViewModel.saveThumbnailImage(context, thumbnail);
            if (allSaved) {
                ArrayList<Bitmap> barcodesAsBitmap = getBarcodesAsBitmapFromImage(context, codeFileViewModel);
                Bitmap codeImage = null;
                if (barcodesAsBitmap.isEmpty()) {
                    Logger.logError("Couldn't get code from " + codeFile.originalFilename());
                } else {
                    if (barcodesAsBitmap.size() > 1) {
                        Logger.logError(barcodesAsBitmap.size() + " codes found in bitmap! Saving only one.");
                    }
                    codeImage = barcodesAsBitmap.get(0);
                }
                if (codeImage != null) {
                    allSaved = codeFileViewModel.saveThumbnailImage(context, codeImage);
                }
            }
        }
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
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                return EncodingUtils.encode(BarcodeFormat.QR_CODE, textContent, 1);
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
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(SUPPORTED_BARCODE_FORMATS)
                .build();

        if (!detector.isOperational()) {
            return null;
        }
        return detector;
    }

    @NonNull
    public static ArrayList<Bitmap> getBarcodesAsBitmapFromImage(Context context, CodeFileViewModel codeFileViewModel) {
        SparseArray<Barcode> barcodes = getCodesFromBitmap(context, codeFileViewModel.getOriginalImage(context));
        ArrayList<Bitmap> barcodesAsBitmap = new ArrayList<>();

        if (barcodes.size() < 1) {
            Logger.logError("No barcodes detected.");
        } else {
            for (int i = 0; i < barcodes.size(); i++) {
                int key = barcodes.keyAt(i);
                Barcode barcode = barcodes.get(key);
                Logger.logDebug("Barcode found, valueformat: " + barcode.valueFormat);// TODO use this is CONTACT_INFO, EMAIL, ISBN, PHONE, PRODUCT, SMS, TEXT, URL, WIFI, GEO , CALENDAR_EVENT , DRIVER_LICENSE
                Logger.logDebug("Barcode found, cornerPoints: " + barcode.cornerPoints);
                Logger.logDebug("Barcode found, BoundingBox: " + barcode.getBoundingBox());
                Logger.logDebug("Barcode found, displayValue: " + barcode.displayValue);
                Logger.logDebug("Barcode found, rawValue: " + barcode.rawValue);
// Logger.logDebug("Barcode founde, BoundingBox "+barcode.//valueFormat);
                int barcodeFormat = barcode.format;
                BarcodeFormat encodingFormat = getEncodingFormat(barcodeFormat);
                if (encodingFormat != null) {
                    Bitmap code = EncodingUtils.encode(encodingFormat, barcode.rawValue, 1);// TODO pass ratio taken from barcode instead of 1
                    if (code != null) {
                        barcodesAsBitmap.add(code);
                    } else {
                        Logger.logError("Couldn't encode bitmap from barcode:" + barcode.rawValue);
                    }
                } else {
                    Logger.logError("Code format not supported: " + getEncodingFormatName(barcodeFormat) + ". " + "Currenty supported:" + SUPPORTED_BARCODE_FORMATS);//TODO loop through and get names
                }
            }
        }
        return barcodesAsBitmap;
    }

    @Nullable
    private static BarcodeFormat getEncodingFormat(int barcodeFormat) {
// Barcode can be:
// CODE_128
// CODE_39
// CODE_93
// CODABAR
// DATA_MATRIX
// EAN_13 Typical grocery barcode
// EAN_8
// ITF
// QR_CODE
// UPC_A
// UPC_E
// PDF417
// AZTEC
        switch (barcodeFormat) {
            case Barcode.DATA_MATRIX:
                return BarcodeFormat.DATA_MATRIX;
            case Barcode.EAN_13:
                return BarcodeFormat.EAN_13;
            case Barcode.QR_CODE:
                return BarcodeFormat.QR_CODE;
            case Barcode.PDF417:
                return BarcodeFormat.PDF_417;
            case Barcode.AZTEC:
                return BarcodeFormat.AZTEC;
            default:
                return null;
        }
    }

    @Nullable
    private static String getEncodingFormatName(int barcodeFormat) {
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
                Logger.logError("Code format not supported:" + barcodeFormat);
                return "Unknown";
        }
    }

    @Nullable
    private static Bitmap pdfToBitmap(Context context, Uri uri) {
        int pageNum = 0;// TODO
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            PdfDocument pdfDocument = pdfiumCore.newDocument(fileDescriptor);
            pdfiumCore.openPage(pdfDocument, pageNum);
            if (pdfiumCore.getPageCount(pdfDocument) != 1) {
                Logger.logError("Pdf has " + pdfiumCore.getPageCount(pdfDocument) + " pages.");
            }
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);

            printInfo(pdfiumCore, pdfDocument);
            pdfiumCore.closeDocument(pdfDocument);

            return bitmap;
        } catch (IOException e) {
            Logger.logException(e);
        }
        return null;
    }

    private static void printInfo(PdfiumCore core, PdfDocument doc) {
        PdfDocument.Meta meta = core.getDocumentMeta(doc);
        Logger.logDebug("title = " + meta.getTitle());
        Logger.logDebug("author = " + meta.getAuthor());
        Logger.logDebug("subject = " + meta.getSubject());
        Logger.logDebug("keywords = " + meta.getKeywords());
        Logger.logDebug("creator = " + meta.getCreator());
        Logger.logDebug("producer = " + meta.getProducer());
        Logger.logDebug("creationDate = " + meta.getCreationDate());
        Logger.logDebug("modDate = " + meta.getModDate());

        printBookmarksTree(core.getTableOfContents(doc), "-");

    }

    private static void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        if (tree.isEmpty()) {
            Logger.logDebug("tree is empty");
        } else {
            for (PdfDocument.Bookmark b : tree) {

                Logger.logDebug(String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

                if (b.hasChildren()) {
                    printBookmarksTree(b.getChildren(), sep + "-");
                }
            }
        }
    }

}