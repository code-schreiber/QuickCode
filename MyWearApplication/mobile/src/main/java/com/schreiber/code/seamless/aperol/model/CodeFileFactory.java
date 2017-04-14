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
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public abstract class CodeFileFactory {

    private static final boolean PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT = true;
    private static final boolean PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT = true;

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

    @NonNull
    public static ArrayList<CodeFile> createCodeFilesFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String fileType = contentResolver.getType(uri);
        String size = UriUtils.getSize(contentResolver, uri);

        ArrayList<CodeFile> codeFiles = new ArrayList<>();
        ArrayList<Bitmap> originalImages = getBitmapsFromUri(context, uri);
        if (originalImages != null && !originalImages.isEmpty()) {
            if (originalImages.size() > 1 && !PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT) {
                Logger.logWarning("PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT is disabled, returning only one codefile out of " + originalImages.size());
                CodeFile codeFile = createItem(context, originalFilename, fileType, size, originalImages.get(0), uri.toString());
                return createListFromSingleItem(codeFile);
            }
            for (Bitmap originalImage : originalImages) {
                CodeFile codeFile = createItem(context, originalFilename, fileType, size, originalImage, uri.toString());
                codeFiles.add(codeFile);
            }
        }
        return codeFiles;
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

                    encodingFormatName = BarcodeFormatMapper.getEncodingFormatName(barcode.format);
                    codeContentType = BarcodeFormatMapper.getContentType(barcode.valueFormat);
                    codeDisplayValue = barcode.displayValue;
                    codeRawValue = barcode.rawValue;

                    if (isBarcodeFormatSupported(barcode.format)) {
                        codeImage = EncodingUtils.encode(BarcodeFormatMapper.getEncodingFormat(barcode.format), codeRawValue, barcode.getBoundingBox().width(), barcode.getBoundingBox().height());
                        if (codeImage == null) {
                            Logger.logError("Couldn't encode bitmap from barcode:" + codeRawValue);
                        }
                    } else {
                        Logger.logError("Code format not supported: " + barcode.format + " - " + encodingFormatName + ". " + "Currently supported: " + getSupportedBarcodeFormatsAsString());
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
    private static ArrayList<Bitmap> getBitmapsFromUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        if (UriUtils.fileExists(resolver, uri)) {
            if (UriUtils.isPdf(resolver, uri)) {
                return pdfToBitmaps(context, uri);
            } else if (UriUtils.isImage(resolver, uri)) {
                Bitmap bitmap = UriUtils.getBitmapFromUri(context.getContentResolver(), uri);
                return createListFromSingleItem(bitmap);
            } else if (UriUtils.isText(resolver, uri)) {
                // TODO
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                Bitmap bitmap = EncodingUtils.encodeQRCode(textContent);
                return createListFromSingleItem(bitmap);
            } else {
                Logger.logError("No known file type: " + uri);
            }
        } else {
            Logger.logError("File doesn't exist: " + uri);
        }
        return null;
    }

    @NonNull
    private static <T> ArrayList<T> createListFromSingleItem(T t) {
        if (t == null) {
            return (ArrayList<T>) Collections.EMPTY_LIST;
        } else {
            return (ArrayList<T>) Collections.singletonList(t);
        }
    }

// TODO when E and when T
//    @NonNull
//    private static <E> ArrayList<E> createSingleBitmapListWithE(E t) {
//        if (t == null) {
//            return (ArrayList<E>) Collections.EMPTY_LIST;
//        } else {
//            return (ArrayList<E>) Collections.singletonList(t);
//        }
//    }

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

    private static boolean isBarcodeFormatSupported(int barcodeFormat) {
        return Arrays.asList(SUPPORTED_BARCODE_FORMATS).contains(barcodeFormat);
    }

    @NonNull
    public static String getSupportedBarcodeFormatsAsString() {
        String supportedFormats = "";
        for (Integer supportedBarcodeFormat : SUPPORTED_BARCODE_FORMATS) {
            supportedFormats += (supportedBarcodeFormat) + ", ";
        }
        return supportedFormats.substring(0, supportedFormats.length() - 2);
    }

    @NonNull
    private static ArrayList<Bitmap> pdfToBitmaps(Context context, Uri uri) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            // create a new renderer
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                int pageCount = renderer.getPageCount();
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    PdfRenderer.Page page = renderer.openPage(pageIndex);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    bitmaps.add(bitmap);
                }
                renderer.close();
            }
        } catch (IOException e) {
            Logger.logException(e);
        }
        return bitmaps;
    }

}