package com.schreiber.code.seamless.aperol.view.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.schreiber.code.seamless.aperol.util.TypefaceProvider;


public class MyTextView extends TextView {

    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            TypefaceProvider.getInstance(getContext()).setTypeface(this);

        }
    }

}