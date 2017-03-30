package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.auto.value.AutoValue;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.util.IOUtils;
import com.schreiber.code.seamless.aperol.util.TypeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


@AutoValue
public abstract class CodeFileViewModel implements Parcelable, Comparable<CodeFileViewModel> {

    private static final String SUFFIX_ORIGINAL_THUMBNAIL = "original.thumbnail";
    private static final String SUFFIX_CODE_THUMBNAIL = "code.thumbnail";
    private static final String SUFFIX_ORIGINAL = "original";
    private static final String SUFFIX_CODE = "code";


    public static CodeFileViewModel create(CodeFile codeFile) {
        return new AutoValue_CodeFileViewModel(codeFile);
    }

    abstract CodeFile codeFile();


    @Override
    public int compareTo(@NonNull CodeFileViewModel o) {
        // Last created items first
        return Long.compare(o.codeFile().originalCodeFile().importedOn(), this.codeFile().originalCodeFile().importedOn());
    }

    public static ArrayList<CodeFileViewModel> createList(ArrayList<CodeFile> data) {
        ArrayList<CodeFileViewModel> list = new ArrayList<>();
        for (CodeFile codeFile : data) {
            list.add(create(codeFile));
        }
        return list;
    }

    public CodeFile getCodeFile() {
        return codeFile();
    }

    public String getDisplayName() {
        return codeFile().displayName();
    }

    public String getOriginalFilePath() {
        return "Original File: " + codeFile().originalCodeFile().importedFrom() + " " + codeFile().originalCodeFile().filename();
    }

    public String getCreationDateLong(Context context) {
        return "Imported on " + getCreationDate(context);
    }

    public String getCreationDateShort(Context context) {
        return getCreationDate(context);
    }

    public String getOriginalFileSize() {
        return "Original File Size: " + codeFile().originalCodeFile().size();
    }


    public String getOriginalFileType() {
        String mimeType = getMimeType();
        if (!TypeUtils.isEmpty(mimeType)) {
            mimeType = " (" + mimeType + ")";
        }
        return "Original File Type: " + codeFile().originalCodeFile().fileType() + mimeType;
    }

    public String getCodeType() {
        return createStringWithPrefix("Code type: ", codeFile().codeType());
    }

    public String getCodeContentType() {
        return createStringWithPrefix("Code content type: ", codeFile().codeContentType());
    }

    public String getCodeDisplayContent() {
        return createStringWithPrefix("Code display content: ", getCodeDisplayContentSimple());
    }

    public String getCodeDisplayContentSimple() {
        return codeFile().codeDisplayContent();
    }

    public String getCodeRawContent() {
        return createStringWithPrefix("Code raw content: ", codeFile().codeRawContent());
    }

    @NonNull
    private String createStringWithPrefix(String prefix, String s) {
        return TypeUtils.isEmpty(s) ? "" : prefix + s;
    }

    public boolean isCodeAvailable(Context context) {
        return getCodeImage(context) != null;
    }

    @DrawableRes
    public int getIsOnWatchResource() {
        boolean isOnWatch = codeFile().onWatchUntil() != CodeFile.NOT_ON_WATCH;
        return isOnWatch ? R.drawable.ic_watch_black_24dp : 0;
    }

    @BindingAdapter("imageBitmap")
    public static void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @BindingAdapter("imageResource")
    public static void setImageResource(ImageView imageView, @DrawableRes int resId) {
        imageView.setImageResource(resId);
    }

    @Nullable
    public Bitmap getOriginalThumbnailImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_ORIGINAL_THUMBNAIL);
    }

    @Nullable
    public Bitmap getCodeThumbnailImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_CODE_THUMBNAIL);
    }

    @Nullable
    public Bitmap getOriginalImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_ORIGINAL);
    }

    @Nullable
    public Bitmap getCodeImage(Context context) {
        return getBitmapFromFile(context, SUFFIX_CODE);
    }

    public boolean saveOriginalThumbnailImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_ORIGINAL_THUMBNAIL);
    }

    public boolean saveCodeThumbnailImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_CODE_THUMBNAIL);
    }

    public boolean saveOriginalImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_ORIGINAL);
    }

    public boolean saveCodeImage(Context context, Bitmap image) throws IOException {
        return saveBitmapToFile(context, image, SUFFIX_CODE);
    }

    private boolean saveBitmapToFile(Context context, Bitmap image, String fileSuffix) throws IOException {
        // TODO do off the UI thread
        return IOUtils.saveBitmapToFile(context, image, codeFile().originalCodeFile().filename(), fileSuffix);
    }

    @Nullable
    private Bitmap getBitmapFromFile(Context context, String fileSuffix) {
        // TODO do off the UI thread
        return IOUtils.getBitmapFromFile(context, codeFile().originalCodeFile().filename(), fileSuffix);
    }

    @NonNull
    private String getCreationDate(Context context) {
        // TODO [Refactoring] extract to dateutils
        Date creationDate = new Date(codeFile().originalCodeFile().importedOn());
        String date = DateFormat.getDateFormat(context).format(creationDate);
        String time = DateFormat.getTimeFormat(context).format(creationDate);
        return date + " " + time;
    }

    private String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(codeFile().originalCodeFile().fileType());
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

}

