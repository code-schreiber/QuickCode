package com.schreiber.code.seamless.aperol.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.IOUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class CodeFileFactory {

    public static CodeFile createItemFromPath(Context context, String path) {
        Uri uri = Uri.parse(path);
        String originalFilename = uri.getLastPathSegment();
        String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
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

    public static CodeFile createItemFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String type = contentResolver.getType(uri);
        String size = UriUtils.getSize(contentResolver, uri);
        Bitmap fileAsImage = getBitmapFromUri(context, uri);
        return createItem(context, originalFilename, type, size, fileAsImage, uri.toString());
    }

    @Nullable
    private static CodeFile createItem(Context context, String originalFilename, String type, String size, Bitmap fileAsImage, String source) {
        if (fileAsImage != null) {
            int thumbnailSize = 128;
            Bitmap thumbnail = Bitmap.createScaledBitmap(fileAsImage, thumbnailSize, thumbnailSize, false);
            if (thumbnail != null) {
                try {
                    IOUtils.saveBitmapToFile(context, fileAsImage, originalFilename, "original");
                    IOUtils.saveBitmapToFile(context, thumbnail, originalFilename, "thumbnail");
                    ArrayList<Bitmap> codes = getCodesFromBitmap(context, fileAsImage);
                    if (codes != null && !codes.isEmpty()) {
                        if (codes.size() > 1) {
                            Logger.logError("Error: " + codes.size() + " codes found in bitmap! Saving only one.");
                        }
                        Bitmap code = codes.get(0);
                        IOUtils.saveBitmapToFile(context, code, originalFilename, "code");
                    } else {
                        Logger.logError("Couldn't get code from " + originalFilename);
                    }
                    String fileName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());//TODO extract
                    return CodeFile.create(fileName, originalFilename, type, size, source);
                } catch (IOException e) {
                    Logger.logException(e);
                    return null;
                }
            }
        }
        return null;
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
                return EncodingUtils.encodeAsQrCode(textContent);
            } else {
                Logger.logError("No known file type: " + uri);
            }
        } else {
            Logger.logError("File doesn't exist: " + uri);
        }
        return null;
    }

    private static ArrayList<Bitmap> getCodesFromBitmap(Context context, Bitmap bitmap) {
        ArrayList<Bitmap> codes = new ArrayList<>();

        BarcodeDetector detector = setupBarcodeDetector(context);
        if (detector == null) {
            return codes;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        for (int i = 0; i < barcodes.size(); i++) {
            int key = barcodes.keyAt(i);
            Barcode barcode = barcodes.get(key);

            Bitmap code;
            if (barcode.format == Barcode.QR_CODE) {
                code = EncodingUtils.encodeAsQrCode(barcode.rawValue);
            } else if (barcode.format == Barcode.PDF417) {
                code = EncodingUtils.encodeAsPdf417(barcode.rawValue);
            } else {
                // TODO
                Logger.logError("Unknown code: " + barcode.format);
                code = EncodingUtils.encodeAsQrCode(barcode.rawValue);
            }
            if (code != null) {
                codes.add(code);
            } else {
                Logger.logError("Coulnt encode bitmap from barcode");
            }
        }
        return codes;
    }

    @Nullable
    public static BarcodeDetector setupBarcodeDetector(Context context) {
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                // TODO choose formars a la setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            return null;
        }
        return detector;
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

