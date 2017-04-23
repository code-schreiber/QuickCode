package com.schreiber.code.seamless.aperol.view.common.view.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class ImageDialogFragment extends DialogFragment {

    private static final String INFO = "INFO";
    private static final String IMAGE_ORIGINAL = "IMAGE_ORIGINAL";
    private static final String IMAGE_ORIGINAL_THUMBNAIL = "IMAGE_ORIGINAL_THUMBNAIL";
    private static final String IMAGE_CODE = "IMAGE_CODE";
    private static final String IMAGE_CODE_THUMBNAIL = "IMAGE_CODE_THUMBNAIL";


    public static ImageDialogFragment newInstance(String info, Bitmap fileAsImage, Bitmap thumbnail, Bitmap code, Bitmap codeThumbnail) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(INFO, info);
        args.putParcelable(IMAGE_ORIGINAL, fileAsImage);
        args.putParcelable(IMAGE_ORIGINAL_THUMBNAIL, thumbnail);
        args.putParcelable(IMAGE_CODE, code);
        args.putParcelable(IMAGE_CODE_THUMBNAIL, codeThumbnail);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        textView.setText(getArguments().getString(INFO));
        layout.addView(textView);
        ImageView original = new ImageView(context);
        original.setImageBitmap((Bitmap) getArguments().getParcelable(IMAGE_ORIGINAL));
        layout.addView(original);
        ImageView thumb = new ImageView(context);
        thumb.setImageBitmap((Bitmap) getArguments().getParcelable(IMAGE_ORIGINAL_THUMBNAIL));
        layout.addView(thumb);
        ImageView code = new ImageView(context);
        code.setImageBitmap((Bitmap) getArguments().getParcelable(IMAGE_CODE));
        layout.addView(code);
        ImageView codeThumb = new ImageView(context);
        codeThumb.setImageBitmap((Bitmap) getArguments().getParcelable(IMAGE_CODE_THUMBNAIL));
        layout.addView(codeThumb);
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(layout);
        return new AlertDialog.Builder(context)
                .setView(scrollView)
                .create();
    }

}
