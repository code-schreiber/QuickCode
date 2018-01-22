package com.toolslab.quickcode.model;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.toolslab.quickcode.db.PremiumPreferences;
import com.toolslab.quickcode.util.Logger;
import com.toolslab.quickcode.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CodeFileFactory {

    public static final int FILENAME_MAX_CHARACTERS = 20;
    public static final int MAX_CHARACTERS = 2953;

    private CodeFileFactory() {
        // Hide utility class constructor
    }

    public static List<CodeFile> createCodeFileFromAssets(Context context, String path) {
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
    public static List<CodeFile> createCodeFilesFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        String fileType = contentResolver.getType(uri);
        int size = UriUtils.getSizeInBytes(contentResolver, uri);
        String importedFrom = uri.toString();

        List<CodeFile> codeFiles = new ArrayList<>();
        List<Bitmap> originalImages = getBitmapsFromUri(context, uri);
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
    public static List<CodeFile> createCodeFilesFromText(Context context,
                                                         @Size(min = 0, max = FILENAME_MAX_CHARACTERS) String originalFilename,
                                                         @Size(min = 0, max = MAX_CHARACTERS) String text) {
        String fileType = UriUtils.getTextTypeName();
        String importedFrom = "Imported from shared text: " + originalFilename;
        Bitmap originalImage = UriUtils.getBitmapFromText(text);
        int size = originalImage.getByteCount();
        return createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
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
    private static List<CodeFile> createCodeFiles(Context context, String originalFilename, String fileType, int size, Bitmap originalImage, String importedFrom) {
        List<CodeFile> codeFiles = CodeFileCreator.createCodeFiles(context, originalFilename, fileType, size, originalImage, importedFrom);
        if (codeFiles.size() > 1 && !PremiumPreferences.allowMultipleCodesInImageImport(context)) {
            Logger.logError(codeFiles.size() + " barcodes found in bitmap! Saving only one because of allowMultipleCodesInImageImport.");
            return createListFromSingleItem(codeFiles.get(0));
        }
        return codeFiles;
    }

    @Nullable
    private static List<Bitmap> getBitmapsFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        if (UriUtils.fileExists(contentResolver, uri)) {
            if (UriUtils.isPdf(contentResolver, uri)) {
                return UriUtils.pdfUriToBitmaps(context.getContentResolver(), uri);
            } else if (UriUtils.isImage(contentResolver, uri)) {
                Bitmap bitmap = UriUtils.getBitmapFromUri(contentResolver, uri);
                return createListFromSingleItem(bitmap);
            } else if (UriUtils.isText(contentResolver, uri)) {
                Bitmap bitmap = UriUtils.getBitmapFromText(contentResolver, uri);
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
    private static <T> List<T> createListFromSingleItem(T t) {
        if (t == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Collections.singletonList(t));
        }
    }

}
