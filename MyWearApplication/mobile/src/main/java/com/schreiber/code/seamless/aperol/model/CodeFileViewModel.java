package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.auto.value.AutoValue;
import com.schreiber.code.seamless.aperol.util.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


@AutoValue
public abstract class CodeFileViewModel implements Parcelable, Comparable<CodeFileViewModel> {

    private static final String SUFFIX_CODE = "code";
    private static final String SUFFIX_THUMBNAIL = "thumbnail";
    private static final String SUFFIX_ORIGINAL = "original";


    public static CodeFileViewModel create(CodeFile codeFile) {
        return new AutoValue_CodeFileViewModel(codeFile);
    }

    public abstract CodeFile codeFile();


    @Override
    public int compareTo(@NonNull CodeFileViewModel o) {
        // Last created items first
        return Long.compare(o.codeFile().originalCodeFile().importedOn(), this.codeFile().originalCodeFile().importedOn());
    }

    // TODO extract to dateutils
    public String getCreationDate(Context context) {
        Date creationDate = new Date(codeFile().originalCodeFile().importedOn());
        String date = DateFormat.getDateFormat(context).format(creationDate);
        String time = DateFormat.getTimeFormat(context).format(creationDate);
        return date + " " + time;
    }

    public String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(codeFile().originalCodeFile().fileType());
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

    public static ArrayList<CodeFileViewModel> createList(ArrayList<CodeFile> data) {
        ArrayList<CodeFileViewModel> list = new ArrayList<>();
        for (CodeFile codeFile : data) {
            list.add(create(codeFile));
        }
        Collections.sort(list);
        return list;
    }

//    public boolean hasCode() {
//        return codeFile().barcode() != null; // TODO pass important data from barcode
//    }

    public boolean isOnWatch() {
        return codeFile().onWatchUntil() != CodeFile.NOT_ON_WATCH;
    }

    @BindingAdapter("imageBitmap")
    public static void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public Bitmap getOriginalImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_ORIGINAL);
    }

    public Bitmap getCodeImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_CODE);
    }

    public Bitmap getThumbnailImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_THUMBNAIL);
    }

    public boolean saveOriginalImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_ORIGINAL);
    }

    public boolean saveCodeImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_CODE);
    }

    public boolean saveThumbnailImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_THUMBNAIL);
    }

    private boolean saveBitmapToFile(Context context, Bitmap image, String fileSuffix) throws IOException {
        // TODO do of the UI thread
        return IOUtils.saveBitmapToFile(context, image, codeFile().originalCodeFile().filename(), fileSuffix);
    }

    private Bitmap getBitmapFromFile(Context context, String fileSuffix) {
        // TODO do of the UI thread
        return IOUtils.getBitmapFromFile(context, codeFile().originalCodeFile().filename(), fileSuffix);
    }

}

