package com.schreiber.code.seamless.aperol.model;


import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;


// https://github.com/google/auto/blob/master/value/userguide/index.md
@AutoValue
public abstract class ListItem {

    public static ListItem create(String filename, String type, String size) {
        return new AutoValue_ListItem(filename, type, size);
    }

    public abstract String filename();

    public abstract String type();

    public abstract String size();

    public String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(type());
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

}

