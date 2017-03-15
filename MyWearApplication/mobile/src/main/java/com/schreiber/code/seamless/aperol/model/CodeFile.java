package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.Date;


@AutoValue
public abstract class CodeFile implements Parcelable {

    private static final long NOT_ON_WATCH = 0L;

    public static CodeFile create(String filename, String originalFilename, String type, String size, String source) {
        return new AutoValue_CodeFile(filename, originalFilename, type, size, new Date().getTime(), source, NOT_ON_WATCH);
    }

    public abstract String filename();

    public abstract String originalFilename();

    public abstract String type();

    public abstract String size();

    public abstract long creationDate();

    public abstract String source();

    public abstract long onWatchUntil();

}

