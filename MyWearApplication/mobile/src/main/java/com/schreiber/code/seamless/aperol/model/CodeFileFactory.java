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

    private static final int[] SUPPORTED_BARCODE_FORMATS = {Barcode.DATA_MATRIX, Barcode.QR_CODE, Barcode.AZTEC, Barcode.PDF417};


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
            Bitmap thumbnail = getThumbnail(originalImage);
            if (thumbnail != null) {
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
//                            TODO create multiple files for premium
                    }
                    for (int i = 0; i < barcodes.size(); i++) {
                        int key = barcodes.keyAt(i);
                        barcode = barcodes.get(key);

                        encodingFormatName = getEncodingFormatName(barcode.format);
                        codeContentType = getContentType(barcode.valueFormat);
                        codeDisplayValue = barcode.displayValue;
                        codeRawValue = barcode.rawValue;

                        Logger.logDebug("Barcode found, valueformat: " + barcode.valueFormat);
                        Logger.logDebug("Barcode found, cornerPoints: " + barcode.cornerPoints[0] + "," + barcode.cornerPoints[1]);
                        Logger.logDebug("Barcode found, BoundingBox: " + barcode.getBoundingBox());
                        Logger.logDebug("Barcode found, displayValue: " + codeDisplayValue);
                        Logger.logDebug("Barcode found, rawValue: " + codeRawValue);

                        BarcodeFormat encodingFormat = getEncodingFormat(barcode.format);
                        Logger.logDebug("Barcode found, encodingFormat: " + encodingFormat);

                        if (encodingFormat != null) {
                            codeImage = EncodingUtils.encode(encodingFormat, codeRawValue, barcode.getBoundingBox().width(), barcode.getBoundingBox().height());
                            if (codeImage == null) {
                                Logger.logError("Couldn't encode bitmap from barcode:" + codeRawValue);
                            }
                            Logger.logError("Code format not supported: " + encodingFormatName + ". " + "Currenty supported:" + SUPPORTED_BARCODE_FORMATS);//TODO loop through and get names
                        }
                    }
                }
                OriginalCodeFile originalCodeFile = OriginalCodeFile.create(originalFilename, fileType, size, importedFrom);
                CodeFile codeFile = CodeFile.create(originalCodeFile, encodingFormatName, codeContentType, codeDisplayValue, codeRawValue);
                try {
                    if (saveBitmapsToFile(context, codeFile, originalImage, thumbnail, codeImage)) {
                        return codeFile;
                    } else {
                        Logger.logError("Couldn't save images from " + originalFilename);
                    }
                } catch (IOException e) {
                    Logger.logException(e);
                }
            } else {
                Logger.logError("Couldn't create thumbnail of " + originalFilename);
            }
        }
        return null;
    }

    private static Bitmap getThumbnail(Bitmap originalImage) {
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
    private static boolean saveBitmapsToFile(Context context, CodeFile codeFile, Bitmap originalImage, Bitmap thumbnail, Bitmap codeImage)
            throws IOException {
        CodeFileViewModel codeFileViewModel = CodeFileViewModel.create(codeFile);
        boolean allSaved = codeFileViewModel.saveOriginalImage(context, originalImage);
        if (allSaved) {
            allSaved = codeFileViewModel.saveThumbnailImage(context, thumbnail);
            if (allSaved) {
                if (codeImage != null) {
                    allSaved = codeFileViewModel.saveCodeImage(context, codeImage);
                }
            }
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
        int supportedBarcodeFormats = 0;
        for (int supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedBarcodeFormats |= supportedBarcodeFormat;
        }
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(supportedBarcodeFormats)
                .build();

        if (!detector.isOperational()) {
            return null;
        }
        return detector;
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
        if (Arrays.asList(SUPPORTED_BARCODE_FORMATS).contains(barcodeFormat)) {
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
        return null;
    }

    private static String getEncodingFormatName(int barcodeFormat) {
        if (Arrays.asList(SUPPORTED_BARCODE_FORMATS).contains(barcodeFormat)) {
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
        Logger.logError("Code format not supported:" + barcodeFormat);
        return "Unknown";
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
                Logger.logError("barcodeValueFormat not supported:" + barcodeValueFormat);
                return "Unknown barcodeValueFormat";
        }
    }

    @Nullable
    private static Bitmap pdfToBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        int pageNum = 0;// TODO
        try {
            // create a new renderer
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            // let us just render all pages
            int pageCount = renderer.getPageCount();
            if (pageCount > 0) {
                if (pageCount != 1) {
                    Logger.logError("Pdf has " + pageCount + " pages.");
                }
                PdfRenderer.Page page = renderer.openPage(pageNum);
                bitmap = Bitmap.createBitmap(
                        page.getWidth(),
                        page.getHeight(),
                        Bitmap.Config.ARGB_8888);

                // say we render for showing on the screen
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                // close the page
                page.close();
            }

            // close the renderer
            renderer.close();
        } catch (IOException e) {
            Logger.logException(e);
        }
        return bitmap;
    }

//    @Nullable
//    private static Bitmap pdfToBitmap(Context context, Uri uri) {
//        int pageNum = 0;// TODO
//        try {
//            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
//            PdfiumCore pdfiumCore = new PdfiumCore(context);
//            PdfDocument pdfDocument = pdfiumCore.newDocument(fileDescriptor);
//            pdfiumCore.openPage(pdfDocument, pageNum);
//            if (pdfiumCore.getPageCount(pdfDocument) != 1) {
//                Logger.logError("Pdf has " + pdfiumCore.getPageCount(pdfDocument) + " pages.");
//            }
//            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
//            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);
//
//            printInfo(pdfiumCore, pdfDocument);
//            pdfiumCore.closeDocument(pdfDocument);
//
//            return bitmap;
//        } catch (IOException e) {
//            Logger.logException(e);
//        }
//        return null;
//    }
//
//    private static void printInfo(PdfiumCore core, PdfDocument doc) {
//        PdfDocument.Meta meta = core.getDocumentMeta(doc);
//        Logger.logDebug("title = " + meta.getTitle());
//        Logger.logDebug("author = " + meta.getAuthor());
//        Logger.logDebug("subject = " + meta.getSubject());
//        Logger.logDebug("keywords = " + meta.getKeywords());
//        Logger.logDebug("creator = " + meta.getCreator());
//        Logger.logDebug("producer = " + meta.getProducer());
//        Logger.logDebug("creationDate = " + meta.getCreationDate());
//        Logger.logDebug("modDate = " + meta.getModDate());
//
//        printBookmarksTree(core.getTableOfContents(doc), "-");
//
//    }
//
//    private static void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
//        if (tree.isEmpty()) {
//            Logger.logDebug("tree is empty");
//        } else {
//            for (PdfDocument.Bookmark b : tree) {
//
//                Logger.logDebug(String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));
//
//                if (b.hasChildren()) {
//                    printBookmarksTree(b.getChildren(), sep + "-");
//                }
//            }
//        }
//    }

}