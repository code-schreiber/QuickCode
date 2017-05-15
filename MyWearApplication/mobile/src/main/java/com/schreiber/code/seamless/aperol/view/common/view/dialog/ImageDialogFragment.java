package com.schreiber.code.seamless.aperol.view.common.view.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
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


    public static ImageDialogFragment newInstance(Bitmap bitmap) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(IMAGE_ORIGINAL, bitmap);
        fragment.setArguments(args);
        return fragment;
    }

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
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        addTextViewIfGiven(layout, INFO);
        addImageViewToLayout(layout, IMAGE_ORIGINAL);
        addImageViewIfGiven(layout, IMAGE_ORIGINAL_THUMBNAIL);
        addImageViewIfGiven(layout, IMAGE_CODE);
        addImageViewIfGiven(layout, IMAGE_CODE_THUMBNAIL);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(layout);
        return new AlertDialog.Builder(getActivity())
                .setView(scrollView)
                .create();
    }

    private void addTextViewIfGiven(LinearLayout layout, String key) {
        if (getArguments().containsKey(INFO)) {
            TextView textView = new TextView(getActivity());
            textView.setText(getArguments().getString(key));
            layout.addView(textView);
        }
    }

    private void addImageViewIfGiven(LinearLayout layout, String key) {
        if (getArguments().containsKey(key)) {
            addImageViewToLayout(layout, key);
        }
    }

    private void addImageViewToLayout(LinearLayout layout, String key) {
        ImageView codeThumb = new ImageView(getActivity());
        codeThumb.setImageBitmap((Bitmap) getArguments().getParcelable(key));
        layout.addView(codeThumb);
    }

}
