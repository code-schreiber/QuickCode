package com.schreiber.code.seamless.aperol.util;


import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class UriUtils {

    public static final String MODE_READ = "r";

    public static final String TYPE_ABSOLUTE_APPLICATION_PDF = "application/pdf";
    public static final String TYPE_ABSOLUTE_TEXT_PLAIN = "text/plain";
    public static final String TYPE_RELATIVE_IMAGE = "image/";

    private UriUtils() {
        // Hide utility class constructor
    }

    @CheckResult
    public static String readTextFromUri(ContentResolver contentResolver, Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
        } catch (IOException e) {
            Logger.logException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return stringBuilder.toString();
    }

    @Nullable
    @CheckResult
    public static Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, MODE_READ);
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                return BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
        } catch (FileNotFoundException e) {
            Logger.logException(e);
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return null;
    }

    @CheckResult
    public static String getDisplayName(ContentResolver contentResolver, Uri uri) {
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                Logger.logWarning(cursor.getCount() + " items in cursor!");
            }
            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor.moveToFirst()) {

                    // Note it's called "Display Name".  This is
                    // provider-specific, and might not necessarily be the file name.
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    return cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    @CheckResult
    public static String getSize(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                Logger.logWarning(cursor.getCount() + " items in cursor!");
            }
            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since an
                    // int can't be null in Java, the behavior is implementation-specific,
                    // which is just a fancy term for "unpredictable".  So as
                    // a rule, check if it's null before assigning to an int.  This will
                    // happen often:  The storage API allows for remote files, whose
                    // size might not be locally known.
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString()
                        // will do the conversion automatically.
                        return cursor.getString(sizeIndex) + " bytes";
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return "Unknown";
    }

    @CheckResult
    public static boolean fileExists(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cursor.close();
                return true;
            } else {
                cursor.close();
            }
        }
        return false;
    }

    public static boolean isPdf(ContentResolver contentResolver, Uri uri) {
        return TYPE_ABSOLUTE_APPLICATION_PDF.equals(contentResolver.getType(uri));
    }

    public static boolean isImage(ContentResolver contentResolver, Uri uri) {
        String type = contentResolver.getType(uri);
        return !TypeUtils.isEmpty(type) && type.contains(TYPE_RELATIVE_IMAGE);
    }

    public static boolean isText(ContentResolver contentResolver, Uri uri) {
        return TYPE_ABSOLUTE_TEXT_PLAIN.equals(contentResolver.getType(uri));
    }

}
