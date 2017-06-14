package com.schreiber.code.seamless.aperol.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.schreiber.code.seamless.aperol.db.PremiumPreferences;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class CodeFileFactory {

    private CodeFileFactory() {
        // Hide utility class constructor
    }

    public static ArrayList<CodeFile> createCodeFileFromAssets(Context context, String path) {
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
        String importedFrom = uri.toString();

        ArrayList<CodeFile> codeFiles = new ArrayList<>();
        ArrayList<Bitmap> originalImages = getBitmapsFromUri(context, uri);
        if (originalImages != null && !originalImages.isEmpty()) {
            if (originalImages.size() > 1 && !PremiumPreferences.allowMultiplePagesImport(context)) {
                Logger.logWarning("allowMultiplePagesImport is disabled, returning only one codefile out of " + originalImages.size());
                return createCodeFiles(context, originalFilename, fileType, size, originalImages.get(0), importedFrom);
            }
            for (Bitmap originalImage : originalImages) {
                String filename = originalFilename;
                if (originalImages.size() > 1) {
                    // append index To filename on multiple pages
                    filename += " (" + originalImages.indexOf(originalImage) + ")";
                }
                codeFiles.addAll(createCodeFiles(context, filename, fileType, size, originalImage, importedFrom));
            }
        }
        return codeFiles;
    }

    @NonNull
    public static ArrayList<CodeFile> createCodeFilesFromText(Context context, String text) {
        String originalFilename = "A shared text";//TODO what to use for the name of a text file?
        String fileType = UriUtils.getTextTypeName();
        String importedFrom = "Imported from a shared text";//TODO what to use

        ArrayList<CodeFile> codeFiles = new ArrayList<>();
        ArrayList<Bitmap> originalImages = getBitmapFromText(text);
        if (originalImages != null && !originalImages.isEmpty()) {
            if (originalImages.size() > 1 && !PremiumPreferences.allowMultiplePagesImport(context)) {
                Logger.logWarning("allowMultiplePagesImport is disabled, returning only one codefile out of " + originalImages.size());
                final Bitmap originalImage = originalImages.get(0);
                int size = originalImage.getByteCount();
                return createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
            }
            for (Bitmap originalImage : originalImages) {
                int size = originalImage.getByteCount();
                codeFiles.addAll(createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom));
            }
        }
        return codeFiles;
    }

    public static ArrayList<CodeFile> createCodeFileFromCodeFile(Context context, CodeFile codeFile) {
        String originalFilename = codeFile.originalCodeFile().filename();
        String fileType = codeFile.originalCodeFile().fileType();
        int size = codeFile.originalCodeFile().size();
        Bitmap originalImage = CodeFileViewModel.create(codeFile).getOriginalImage();
        return createCodeFiles(context, originalFilename, fileType, size, originalImage, "CodeFile of " + originalFilename);
    }

    @NonNull
    static String getFileSuffix(String originalFilename) {
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
        if (suffix.contains(" ")) {
            // handle pdf (1) case, removing the " (1) "
            suffix = suffix.substring(0, suffix.indexOf(" "));
        }
        return suffix;
    }

    @NonNull
    private static ArrayList<CodeFile> createCodeFiles(Context context, String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom) {
        ArrayList<CodeFile> codeFiles = CodeFileCreator.createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
        if (codeFiles.size() > 1 && !PremiumPreferences.allowMultipleCodesInImageImport(context)) {
            Logger.logError(codeFiles.size() + " barcodes found in bitmap! Saving only one because of allowMultipleCodesInImageImport.");
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
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                return getBitmapFromText(textContent);
            } else {
                Logger.logError("No known file type: " + uri);
            }
        } else {
            Logger.logError("File doesn't exist: " + uri);
        }
        return null;
    }

    @NonNull
    private static ArrayList<Bitmap> getBitmapFromText(String textContent) {
        Bitmap bitmap = EncodingUtils.encodeQRCode(textContent);
        return createListFromSingleItem(bitmap);
    }

    @NonNull
    private static <T> ArrayList<T> createListFromSingleItem(T t) {
        if (t == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Collections.singletonList(t));
        }
    }

}