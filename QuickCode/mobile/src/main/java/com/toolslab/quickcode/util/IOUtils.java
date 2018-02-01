package com.toolslab.quickcode.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.toolslab.quickcode.util.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public final class IOUtils {

    public static final String SLASH = "/";
    public static final String FILE_PATH_PREFIX = "file:///";

    private static final String EMPTY_STRING = "";
    private static final char LINE_BREAK = '\n';

    private IOUtils() {
        // Hide utility class constructor
    }

    @CheckResult
    public static boolean saveBitmapToFile(Context context, Bitmap fileAsImage, String filename, String suffix) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename + "." + suffix, Context.MODE_PRIVATE);
        boolean saved = fileAsImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        return saved;
    }

    @Nullable
    public static Bitmap getBitmapFromFile(Context context, String originalFilename, String suffix) {
        try {
            FileInputStream fis = context.openFileInput(originalFilename + "." + suffix);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (IOException e) {
            Logger.logException(e);
        }
        return null;
    }

    public static void writeToFileOutputStream(InputStream inputStream, String fileName, int bufferSize)
            throws IOException {
        writeToFileOutputStream(inputStream, new File(fileName), bufferSize);
    }

    public static void writeToFileOutputStream(InputStream inputStream, File file, int bufferSize)
            throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        writeToOutputStream(inputStream, bufferSize, out);
    }

    public static void writeToOutputStream(InputStream inputStream, int bufferSize, OutputStream out) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int readBytes = inputStream.read(buffer);
        while (readBytes != -1) {
            out.write(buffer, 0, readBytes);
            readBytes = inputStream.read(buffer);
        }
        out.flush();
        out.close();
    }

    /**
     * Recursively deletes a file/directory.
     *
     * @param file The file to be deleted.
     */
    public static void delete(@NonNull File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    delete(f);
                }
            } else {
                boolean deleted = file.delete();
                if (!deleted) {
                    Logger.logError("Couldn't delete " + file.getAbsolutePath());
                }
            }
        }
    }

    @NonNull
    public static String inputStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder content = new StringBuilder(inputStream.available());
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(LINE_BREAK);
            }
            reader.close();
            inputStream.close();
            return content.toString().trim();
        } catch (IOException e) {
            Logger.logException(e);
            return EMPTY_STRING;
        }
    }

}
