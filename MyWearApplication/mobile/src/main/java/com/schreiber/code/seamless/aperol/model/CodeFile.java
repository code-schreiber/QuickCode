package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@AutoValue
public abstract class CodeFile implements Parcelable {

    static final long NOT_ON_WATCH = 0L;

    public static CodeFile create(OriginalCodeFile originalCodeFile) {
        String codeType = "";
        String codeContentType = "";
        String codeDisplayContent = "";
        String codeRawContent = "";
        return create(originalCodeFile, codeType, codeContentType, codeDisplayContent, codeRawContent);
    }

    public static CodeFile create(OriginalCodeFile originalCodeFile, String codeType, String codeContentType, String codeDisplayContent, String codeRawContent) {
        String originalFilename = originalCodeFile.filename();
        String suffix = CodeFileFactory.getFileSuffix(originalFilename);
        String displayName = originalFilename.replace("." + suffix, "");
        ArrayList<String> tags = createTags(originalCodeFile, suffix);
        return new AutoValue_CodeFile(displayName, tags, codeType, codeContentType, codeDisplayContent, codeRawContent, originalCodeFile, NOT_ON_WATCH);
    }

    private static ArrayList<String> createTags(OriginalCodeFile originalCodeFile, String suffix) {
        List<String> tags = Arrays.asList(
                originalCodeFile.filename(),
                suffix,
                originalCodeFile.fileType(),
                originalCodeFile.importedFrom());
        return new ArrayList<>(tags);
    }

    public abstract String displayName();

    public abstract ArrayList<String> tags();

    public abstract String codeType();

    public abstract String codeContentType();

    public abstract String codeDisplayContent();

    public abstract String codeRawContent();

    public abstract OriginalCodeFile originalCodeFile();

    public abstract long onWatchUntil();

}

