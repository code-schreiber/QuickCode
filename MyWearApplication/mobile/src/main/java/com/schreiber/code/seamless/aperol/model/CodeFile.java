package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class CodeFile implements Parcelable {

    static final long NOT_ON_WATCH = 0L;

    public static CodeFile create(OriginalCodeFile originalCodeFile) {
        String originalFilename = originalCodeFile.filename();
        String suffix = CodeFileFactory.getFileSuffix(originalFilename);
        String displayName = originalFilename.replace("." + suffix, "");
        return new AutoValue_CodeFile(displayName, "", "", "", "", originalCodeFile, NOT_ON_WATCH);
    }

    public static CodeFile create(OriginalCodeFile originalCodeFile, String codeType, String codeContentType, String codeDisplayContent, String codeRawContent) {
        String originalFilename = originalCodeFile.filename();
        String suffix = CodeFileFactory.getFileSuffix(originalFilename);
        String displayName = originalFilename.replace("." + suffix, "");
        return new AutoValue_CodeFile(displayName, codeType, codeContentType, codeDisplayContent, codeRawContent, originalCodeFile, NOT_ON_WATCH);
    }

    public abstract String displayName();

    public abstract String codeType();

    public abstract String codeContentType();

    public abstract String codeDisplayContent();

    public abstract String codeRawContent();

    public abstract OriginalCodeFile originalCodeFile();

    public abstract long onWatchUntil();

}

