package com.schreiber.code.seamless.aperol.model;


import android.net.Uri;

import com.google.auto.value.AutoValue;


// https://github.com/google/auto/blob/master/value/userguide/index.md
@AutoValue
public abstract class ListItem {

    public static ListItem create(String filename, String type, String mimeType, String size, Uri uri) {
        return new AutoValue_ListItem(filename, type, mimeType, size, uri.toString());
    }

    public abstract String filename();

    public abstract String type();

    public abstract String mimeType();

    public abstract String size();

    public abstract String uri();

    public Uri getUri() {
        return Uri.parse(uri());
    }

}

