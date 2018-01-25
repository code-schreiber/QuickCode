package com.toolslab.quickcode.util;


import android.graphics.Typeface;
import android.util.SparseArray;

import com.toolslab.quickcode.util.log.Logger;


class Font {

    private final String name;
    private final SparseArray<Typeface> styles;

    Font(String name, Typeface typeface) {
        SparseArray<Typeface> styles = new SparseArray<>();
        styles.put(typeface.getStyle(), typeface);
        this.styles = styles;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void addStyle(Typeface typeface) {
        Typeface exitsting = styles.get(typeface.getStyle());
        if (exitsting != null) {
            Logger.logError("Replacing " + exitsting.getStyle() + " with " + typeface.getStyle());
        }
        styles.put(typeface.getStyle(), typeface);
    }

    Typeface getStyle(int style) {
        return styles.get(style);
    }

}
