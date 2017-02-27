package com.schreiber.code.seamless.aperol.util;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public final class IOUtils {

    public static final String SLASH = "/";
    public static final String FILE_PATH_PREFIX = "file:///";

    private IOUtils() {
        // Hide utility class constructor
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
                file.delete();
            }
        }
    }

}
