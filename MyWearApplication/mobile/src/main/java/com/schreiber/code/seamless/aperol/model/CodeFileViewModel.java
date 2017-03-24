package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
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

    private static final String SUFFIX_CODE = "code";
    private static final String SUFFIX_THUMBNAIL = "thumbnail";
    private static final String SUFFIX_ORIGINAL = "original";


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
        Collections.sort(list);
        return list;
    }

    public CodeFile getCodeFile() {
        return codeFile();
    }

    public String getCreationDateShort(Context context) {
        return getCreationDate(context);
    }

    public String getCreationDateLong(Context context) {
        return "Imported on " + getCreationDate(context);
    }

    @NonNull
    private String getCreationDate(Context context) {
        // TODO extract to dateutils
        Date creationDate = new Date(codeFile().originalCodeFile().importedOn());
        String date = DateFormat.getDateFormat(context).format(creationDate);
        String time = DateFormat.getTimeFormat(context).format(creationDate);
        return date + " " + time;
    }

    public String getOriginalFilePath() {
        return "Original File " + codeFile().originalCodeFile().importedFrom() + " " + codeFile().originalCodeFile().filename();
    }

    public String getOriginalFileSize() {
        return "Original File Size " + codeFile().originalCodeFile().size();
    }


    public String getOriginalFileType() {
        String mimeType = getMimeType();
        if (!TypeUtils.isEmpty(mimeType)) {
            mimeType = " (" + mimeType + ")";
        }
        return "Original File Type " + codeFile().originalCodeFile().fileType() + mimeType;
    }

    public String getDisplayName() {
        return codeFile().displayName();
    }

    public String getCodeType() {
        return "Code type: " + codeFile().codeType();
    }

    public String getCodeContentType() {
        return "Code content type: " + codeFile().codeContentType();
    }

    public String getCodeDisplayContent() {
        return "Code display content: " + codeFile().codeDisplayContent();
    }

    public String getCodeRawContent() {
        return "Code raw content: " + codeFile().codeRawContent();
    }

    @DrawableRes
    public int getHasCodeResource() {
        boolean hasCode = !TypeUtils.isEmpty(codeFile().codeRawContent());
        return hasCode ? R.drawable.ic_visibility_black_24dp : 0;
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
        // TODO do off the UI thread
        return IOUtils.saveBitmapToFile(context, image, codeFile().originalCodeFile().filename(), fileSuffix);
    }

    private Bitmap getBitmapFromFile(Context context, String fileSuffix) {
        // TODO do off the UI thread
        return IOUtils.getBitmapFromFile(context, codeFile().originalCodeFile().filename(), fileSuffix);
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

