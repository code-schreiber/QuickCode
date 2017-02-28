package com.schreiber.code.seamless.aperol.view.common.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.ImageView;


public class ImageDialogFragment extends DialogFragment {

    private static final String IMAGE = "IMAGE";
    private static final String IMAGE_URI = "IMAGE_URI";


    public static ImageDialogFragment newInstance(Bitmap image) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageDialogFragment newInstance(Uri imageUri) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(IMAGE_URI, imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        ImageView imageView = new ImageView(context);
        if (getArguments().containsKey(IMAGE_URI))
            imageView.setImageURI((Uri) getArguments().getParcelable(IMAGE_URI));
        else if (getArguments().containsKey(IMAGE))
            imageView.setImageBitmap((Bitmap) getArguments().getParcelable(IMAGE));
        return new AlertDialog.Builder(context)
                .setView(imageView)
                .create();
    }

}
