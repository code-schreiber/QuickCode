package com.toolslab.quickcode.util;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class PdfToBitmapConverter {

    private PdfToBitmapConverter() {
        // Hide utility class constructor
    }

    @NonNull
    static List<Bitmap> pdfUriToBitmaps(ContentResolver contentResolver, Uri uri) {
        List<Bitmap> bitmaps = new ArrayList<>();
        PdfRenderer renderer = null;
        try {
            ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(uri, UriUtils.MODE_READ);
            if (fileDescriptor != null) {
                renderer = new PdfRenderer(fileDescriptor);
                int pageCount = renderer.getPageCount();
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    bitmaps.add(getBitmapFromPage(renderer, pageIndex));
                }
            }
        } catch (IOException e) {
            Logger.logException(e);
        } finally {
            if (renderer != null) {
                renderer.close();
            }
        }
        return bitmaps;
    }

    @NonNull
    private static Bitmap getBitmapFromPage(PdfRenderer renderer, int pageIndex) {
        PdfRenderer.Page page = renderer.openPage(pageIndex);
        Bitmap bitmap = createWhiteBitmap(page.getWidth(), page.getHeight());
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        return bitmap;
    }

    @NonNull
    private static Bitmap createWhiteBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return bitmap;
    }

}
