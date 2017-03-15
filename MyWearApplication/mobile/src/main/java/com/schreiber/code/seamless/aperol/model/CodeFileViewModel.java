package com.schreiber.code.seamless.aperol.model;


import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Date;


@AutoValue
public abstract class CodeFileViewModel implements Parcelable, Comparable<CodeFileViewModel> {

    public static CodeFileViewModel create(CodeFile codeFile) {
        return new AutoValue_CodeFileViewModel(codeFile);
    }

    public abstract CodeFile codeFile();


    @Override
    public int compareTo(@NonNull CodeFileViewModel o) {
        // Last created items first
        return Long.compare(o.codeFile().creationDate(), this.codeFile().creationDate());
    }

    public String getCreationDate(Context context) {
        Date creationDate = new Date(codeFile().creationDate());// TODO extract to dateutils
        return DateFormat.getDateFormat(context).format(creationDate) + " " + DateFormat.getTimeFormat(context).format(creationDate);
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

