package com.schreiber.code.seamless.aperol.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
        initializeTypefaces(assets, FONTS_PATH);

        if (RANDOM_MODE && !fonts.isEmpty()) {
            for (String s : fonts.keySet()) {
                allFonts.put(s, fonts.get(s));
            }

            int randomIndex = new Random().nextInt(fonts.size());
            randomKey = (String) fonts.keySet().toArray()[randomIndex];
            Font randomFont = fonts.get(randomKey);
            fonts.clear();
            fonts.put(randomKey, randomFont);
        }
    }

    private void initializeTypefaces(AssetManager assets, String path) {
        try {
            String[] list = assets.list(path);
            boolean isFolder = list.length > 0;
            if (isFolder) {
                for (String file : list) {
                    initializeTypefaces(assets, path + "/" + file);
                }
            } else {
                if (path.endsWith(".ttf") || path.endsWith(".otf")) {
                    if (fontExists(assets, path)) {
                        addTypefaceToFonts(assets, path);
                    } else
                        Logger.logError("Not adding not existant " + path);
                } else
                    Logger.logError("Not adding " + path);
            }
        } catch (IOException e) {
            Logger.logException(e);
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
        String defaultTypeface = "symbol";
        if (RANDOM_MODE) {
            defaultTypeface = randomKey;
        }
        setTypeface(defaultTypeface, textView);
    }

    public void setTypeface(TextView textView, int style) {
        String defaultTypeface = "symbol";
        if (RANDOM_MODE) {
            defaultTypeface = randomKey;
        }
        setTypeface(defaultTypeface, textView, style);
    }

    public void setTypeface(String fontName, TextView textView) {
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
        Logger.logError("No Typeface for " + style + " found in " + fonts);
    }

    @Nullable
    public String getCurrentFontName() {
        if (fonts.size() == 1) {
            String key = (String) fonts.keySet().toArray()[0];
            return fonts.get(key).getName();
        }
        return null;
    }

    public Set<String> getAllFontNames() {
        return allFonts.keySet();
    }
}
