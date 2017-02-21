package com.schreiber.code.seamless.aperol.util;

import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * TODO make inmutable
 * Created by sebas on 17.02.17.
 */

class Font {

    private final String name;
    private final SparseArray<Typeface> styles;

    public Font(String name, Typeface typeface) {
        this.name = name;
        SparseArray<Typeface> styles = new SparseArray<>();
        styles.put(typeface.getStyle(), typeface);
        this.styles = styles;
    }

    public void addStyle(Typeface typeface) {
        this.styles.put(typeface.getStyle(), typeface);//TODO make inmutable
    }

    public Typeface getStyle(int style) {
        return this.styles.get(style);
    }

    public String getName() {
        return this.name;
    }
}
