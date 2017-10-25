package com.schreiber.code.seamless.aperol.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class TypefaceProvider {

    private static final String FONTS_PATH = "fonts";
    private final Map<String, Font> fonts = new HashMap<>();

    private static final boolean RANDOM_MODE = true;
    private String randomKey;

    private static TypefaceProvider instance;
    private Map<String, Font> allFonts = new HashMap<>();


    public static TypefaceProvider getInstance(Context context) {
        if (instance == null) {
            instance = new TypefaceProvider();
            instance.initialize(context);
        }
        return instance;
    }

    private void initialize(Context context) {
        AssetManager assets = context.getAssets();

        List<String> paths = new AssetPathLoader(assets, FONTS_PATH).getPaths();
        initializeTypefaces(assets, paths);

        if (RANDOM_MODE && !fonts.isEmpty()) {
            for (String s : fonts.keySet()) {
                allFonts.put(s, fonts.get(s));
            }
        }
    }

    private void initializeTypefaces(AssetManager assets, List<String> paths) {
        for (String path : paths) {
            if (path.endsWith(".ttf") || path.endsWith(".otf")) {// TODO extract constants
                if (fontExists(assets, path)) {
                    addTypefaceToFonts(assets, path);
                } else {
                    Logger.logError("Not adding not existant " + path);
                }
            } else {
                Logger.logError("Not adding " + path);
            }
        }
    }

    private void addTypefaceToFonts(AssetManager assets, String path) {
        Typeface typeface = Typeface.createFromAsset(assets, path);
        String fontName = (new File(path)).getParent();
        Font font = fonts.get(fontName);
        if (font == null) {
            fonts.put(fontName, new Font(fontName, typeface));
        } else {
            font.addStyle(typeface);
        }
    }

    private boolean fontExists(AssetManager assets, String fontPath) {
        try {
            Typeface.createFromAsset(assets, fontPath);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void setTypeface(TextView textView) {
        String defaultTypeface;
        if (RANDOM_MODE) {
            defaultTypeface = getARandomTypeface();
        }
        setTypeface(defaultTypeface, textView);
    }

    public void setTypeface(TextView textView, int style) {
        String defaultTypeface;
        if (RANDOM_MODE) {
            defaultTypeface = getARandomTypeface();
        }
        setTypeface(defaultTypeface, textView, style);
    }

    private String getARandomTypeface() {
        if (randomKey == null) {
            int randomIndex = new Random().nextInt(fonts.size());
            randomKey = (String) fonts.keySet().toArray()[randomIndex];
        }
        return randomKey;
    }

    private void setTypeface(String fontName, TextView textView) {
        int style = textView.getTypeface().getStyle();
        setTypeface(fontName, textView, style);
    }

    private void setTypeface(String fontName, TextView textView, int style) {
        Font font = fonts.get(fontName);
        if (font != null) {
            Typeface typeface = font.getStyle(style);
            if (typeface != null) {
                textView.setTypeface(typeface);
                return;
            }
        }
        Logger.logWarning("No Typeface for " + style + " found in " + fonts);
    }

    public void resetRandomKey() {
        randomKey = null;
    }

    @Nullable
    public String getCurrentFontName() {
        getARandomTypeface();
        if (randomKey != null) {
            return fonts.get(randomKey).getName();
        }
        return null;
    }

    public Set<String> getAllFontNames() {
        return allFonts.keySet();
    }
}
