package com.schreiber.code.seamless.aperol.model;


import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.firebase.database.DataSnapshot;
import com.schreiber.code.seamless.aperol.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import me.mattlogan.auto.value.firebase.annotation.FirebaseValue;


@AutoValue
@FirebaseValue
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
        List<String> tags = createTags(originalCodeFile, suffix, codeType, codeContentType);
        return new AutoValue_CodeFile(displayName, tags, codeType, codeContentType, codeDisplayContent, codeRawContent, originalCodeFile, NOT_ON_WATCH);
    }

    public static CodeFile create(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(AutoValue_CodeFile.FirebaseValue.class).toAutoValue();
    }

    public Object toFirebaseValue() {
        return new AutoValue_CodeFile.FirebaseValue(this);
    }

    private static List<String> createTags(OriginalCodeFile originalCodeFile, String suffix, String codeType, String codeContentType) {
        List<String> tags = new ArrayList<>();
        tags.add(originalCodeFile.filename());
        tags.add(suffix);
        tags.add(originalCodeFile.fileType());
        tags.add(originalCodeFile.importedFrom());
        if (!TypeUtils.isEmpty(codeType)) {
            tags.add(codeType);
        }
        if (!TypeUtils.isEmpty(codeContentType)) {
            tags.add(codeContentType);
        }
        return tags;
    }

    public abstract String displayName();

    public abstract List<String> tags();

    public abstract String codeType();

    public abstract String codeContentType();

    public abstract String codeDisplayContent();

    public abstract String codeRawContent();

    public abstract OriginalCodeFile originalCodeFile();

    public abstract long onWatchUntil();

    public String id() {
        return String.valueOf(hashCode());
    }
}

