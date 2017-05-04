package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.Date;

import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;


@AutoValue
@FirebaseValue
public abstract class OriginalCodeFile implements Parcelable {

    public static OriginalCodeFile create(String filename, String fileType, int size, String importedFrom) {
        long importedOn = new Date().getTime();
        return new AutoValue_OriginalCodeFile(filename, fileType, size, importedOn, importedFrom);
    }

    public abstract String filename();

    public abstract String fileType();

    public abstract int size();

    public abstract long importedOn();

    public abstract String importedFrom();

}

