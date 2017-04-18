package com.schreiber.code.seamless.aperol.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class CodeFileFactory {

    private static final boolean PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT = true;
    private static final boolean PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT = true;

    private CodeFileFactory() {
        // Hide utility class constructor
    }

    public static ArrayList<CodeFile> createCodeFileFromPath(Context context, String path) {
        Uri uri = Uri.parse(path);
        String originalFilename = uri.getLastPathSegment();
        String fileType = getFileSuffix(originalFilename);
        int size = UriUtils.SIZE_UNKNOWN;// TODO get size from uri or path
        Bitmap originalImage = null;
        try {
            originalImage = BitmapFactory.decodeStream(context.getAssets().open(path));
        } catch (IOException e) {
            Logger.logException(e);
        }
        String importedFrom = "app assets/" + (new File(path)).getParent();
        return createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
    }

    @NonNull
    public static ArrayList<CodeFile> createCodeFilesFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String fileType = contentResolver.getType(uri);
        int size = UriUtils.getSizeInBytes(contentResolver, uri);

        ArrayList<CodeFile> codeFiles = new ArrayList<>();
        ArrayList<Bitmap> originalImages = getBitmapsFromUri(context, uri);
        if (originalImages != null && !originalImages.isEmpty()) {
            if (originalImages.size() > 1 && !PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT) {
                Logger.logWarning("PREMIUM_ALLOW_MULTIPLE_PAGES_IMPORT is disabled, returning only one codefile out of " + originalImages.size());
                return createCodeFiles(context, originalFilename, fileType, size, originalImages.get(0), uri.toString());
            }
            for (Bitmap originalImage : originalImages) {
                codeFiles.addAll(createCodeFiles(context, originalFilename, fileType, size, originalImage, uri.toString()));
            }
        }
        return codeFiles;
    }

    public static ArrayList<CodeFile> createCodeFileFromCodeFile(Context context, CodeFile codeFile, Bitmap originalImage) {
        String originalFilename = codeFile.originalCodeFile().filename();
        String fileType = codeFile.originalCodeFile().fileType();
        int size = codeFile.originalCodeFile().size();
        return createCodeFiles(context, originalFilename, fileType, size, originalImage, "CodeFile of " + originalFilename);
    }

    @NonNull
    static String getFileSuffix(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
    }

    @NonNull
    private static ArrayList<CodeFile> createCodeFiles(Context context, String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom) {
        ArrayList<CodeFile> codeFiles = CodeFileCreator.createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
        if (codeFiles.size() > 1 && !PREMIUM_ALLOW_MULTIPLE_CODES_IN_IMAGE_IMPORT) {
            Logger.logError(codeFiles.size() + " barcodes found in bitmap! Saving only one.");
            return createListFromSingleItem(codeFiles.get(0));
        }
        return codeFiles;
    }

    @Nullable
    private static ArrayList<Bitmap> getBitmapsFromUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        if (UriUtils.fileExists(resolver, uri)) {
            if (UriUtils.isPdf(resolver, uri)) {
                return PdfToBitmapConverter.pdfUriToBitmaps(context.getContentResolver(), uri);
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
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Collections.singletonList(t));
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

}