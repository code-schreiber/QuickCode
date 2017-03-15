package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Date;


@AutoValue
public abstract class CodeFileViewModel implements Parcelable {

    public static CodeFileViewModel create(CodeFile codeFile) {
        return new AutoValue_CodeFileViewModel(codeFile);
    }

    public abstract CodeFile codeFile();


    public String getCreationDate(Context context) {
        return DateFormat.getDateFormat(context).format(new Date(codeFile().creationDate()));
    }

    public String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(codeFile().type());
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

    public static ArrayList<CodeFileViewModel> createList(ArrayList<CodeFile> data) {
        ArrayList<CodeFileViewModel> list = new ArrayList<>();
        for (CodeFile codeFile : data) {
            list.add(create(codeFile));
        }
        return list;
    }
}

