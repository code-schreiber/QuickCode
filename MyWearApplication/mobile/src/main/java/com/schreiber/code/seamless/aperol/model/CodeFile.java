package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.auto.value.AutoValue;

import java.util.Date;


@AutoValue
public abstract class CodeFile implements Parcelable {

    static final long NOT_ON_WATCH = 0L;

    public static CodeFile create(OriginalCodeFile originalCodeFile) {
        String originalFilename = originalCodeFile.filename();
        String suffix = CodeFileFactory.getFileSuffix(originalFilename);
        String displayName = originalFilename.replace("." + suffix, "");
        return new AutoValue_CodeFile(displayName, originalCodeFile, NOT_ON_WATCH);
    }

    public abstract String displayName();

    public abstract OriginalCodeFile originalCodeFile();

//    public abstract Barcode barcode();// TODO pass important data from barcode

    public abstract long onWatchUntil();

}

