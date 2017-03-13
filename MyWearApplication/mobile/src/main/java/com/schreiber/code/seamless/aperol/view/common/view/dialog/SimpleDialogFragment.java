package com.schreiber.code.seamless.aperol.view.common.view.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


public class SimpleDialogFragment extends DialogFragment {

    private static final String MESSAGE = "MESSAGE";


    public static SimpleDialogFragment newInstance(String message) {
        SimpleDialogFragment fragment = new SimpleDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        return new AlertDialog.Builder(context)
                .setMessage(getArguments().getString(MESSAGE))
                .create();
    }

}
