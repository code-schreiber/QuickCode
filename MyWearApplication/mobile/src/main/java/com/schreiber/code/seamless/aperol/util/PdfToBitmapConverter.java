package com.schreiber.code.seamless.aperol.util;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;


class PdfToBitmapConverter {

    private PdfToBitmapConverter() {
        // Hide utility class constructor
    }

    @NonNull
    static ArrayList<Bitmap> pdfUriToBitmaps(ContentResolver contentResolver, Uri uri) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(uri, UriUtils.MODE_READ);
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