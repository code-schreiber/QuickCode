package com.schreiber.code.seamless.aperol.model;


import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;

import java.util.Date;


// https://github.com/google/auto/blob/master/value/userguide/index.md
@AutoValue
public abstract class ListItem {

    public static ListItem create(String filename, String type, String size, Date creationDate) {
        return new AutoValue_ListItem(filename, filename, type, size, creationDate.getTime());
    }

    public abstract String filename();

    public abstract String originalFilename();

    public abstract String type();

    public abstract String size();

    public abstract long creationDate();

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

