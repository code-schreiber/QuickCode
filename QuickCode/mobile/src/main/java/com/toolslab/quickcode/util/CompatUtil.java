package com.toolslab.quickcode.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

public class CompatUtil {

    private CompatUtil() {
        // Hide utility class constructor
    }

    @Nullable
    public static Drawable getDrawableCompat(@NonNull Context context, @DrawableRes int id) {
        return ActivityCompat.getDrawable(context, id);
    }

}
