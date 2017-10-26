package com.toolslab.quickcode.model;


import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.auto.value.AutoValue;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.util.TypeUtils;

import java.util.Date;
import java.util.List;


@AutoValue
public abstract class CodeFileViewModel implements Parcelable, Comparable<CodeFileViewModel> {

    public static CodeFileViewModel create(CodeFile codeFile) {
        return new AutoValue_CodeFileViewModel(codeFile);
    }

    abstract CodeFile codeFile();


    @Override
    public int compareTo(@NonNull CodeFileViewModel o) {
        // Last created items first
        return Long.compare(o.codeFile().originalCodeFile().importedOn(), this.codeFile().originalCodeFile().importedOn());
    }

    public CodeFile getCodeFile() {
        return codeFile();
    }

    public String getDisplayName() {
        return codeFile().displayName();
    }

    public String getTags() {
        List<String> tags = codeFile().tags();
        return TypeUtils.getCommaSeparatedStringsFromList(tags);
    }

    public String getOriginalFilename() {
        return codeFile().originalCodeFile().filename();
    }

    public String getCreationDateLong(Context context) {
        return getCreationDate(context);
    }

    public String getCreationDateShort(Context context) {
        return getCreationDate(context);
    }

    public String getOriginalFileSizeInMegabytes(Context context) {
        int sizeInBytes = codeFile().originalCodeFile().size();
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public String getOriginalFileType() {
        String mimeType = getMimeType();
        if (!TypeUtils.isEmpty(mimeType)) {
            mimeType = " (" + mimeType + ")";
        }
        return codeFile().originalCodeFile().fileType() + mimeType;
    }

    public String getCodeType() {
        return codeFile().codeType();
    }

    public String getCodeContentType() {
        return codeFile().codeContentType();
    }

    public String getCodeDisplayContent() {
        return getCodeDisplayContentSimple();
    }

    public String getCodeDisplayContentSimple() {
        return codeFile().codeDisplayContent();
    }

    public String getCodeRawContent() {
        return codeFile().codeRawContent();
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
    public Bitmap getOriginalImageThumbnail() {
        return codeFile().originalImageThumbnail();
    }

    @Nullable
    public Bitmap getCodeImageThumbnail() {
        return codeFile().codeImageThumbnail();
    }

    @Nullable
    public Bitmap getOriginalImage() {
        return codeFile().originalImage();
    }

    @Nullable
    public Bitmap getCodeImage() {
        return codeFile().codeImage();
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

