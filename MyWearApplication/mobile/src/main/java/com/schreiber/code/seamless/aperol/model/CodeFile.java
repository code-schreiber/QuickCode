package com.schreiber.code.seamless.aperol.model;


import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;

import java.util.Date;


// https://github.com/google/auto/blob/master/value/userguide/index.md
@AutoValue
public abstract class CodeFile {

    private static final long NOT_ON_WATCH = 0L;

    public static CodeFile create(String filename, String type, String size, Date creationDate, String source) {
        return new AutoValue_CodeFile(filename, filename, type, size, creationDate.getTime(), source, NOT_ON_WATCH);
    }

    public abstract String filename();

    public abstract String originalFilename();

    public abstract String type();

    public abstract String size();

    public abstract long creationDate();

    public abstract String source();

    public abstract long onWatchUntil();

    public Date getCreationDate() {
        return new Date(creationDate());
    }

    public String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(type());
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

}

