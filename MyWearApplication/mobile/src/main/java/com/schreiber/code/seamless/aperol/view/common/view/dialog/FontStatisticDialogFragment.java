package com.schreiber.code.seamless.aperol.view.common.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.Toast;

import com.schreiber.code.seamless.aperol.db.SharedPreferencesWrapper;
import com.schreiber.code.seamless.aperol.util.TypefaceProvider;


public class FontStatisticDialogFragment extends DialogFragment {

    public interface DialogFragmentListener {

        void onDialogCancelled();

        void onDialogDismissed();
    }

    private DialogFragmentListener mListener;

    public static FontStatisticDialogFragment newInstance() {
        return new FontStatisticDialogFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        return new AlertDialog.Builder(context)
                .setMessage("do you like this font?")
                .setNegativeButton("it's ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToStatistic(" -", context);
                    }
                })
                .setNeutralButton("statistic", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initStatistic(context);
                        Toast.makeText(context, SharedPreferencesWrapper.getFontStatistic(context), Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("oh yea", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToStatistic(" |", context);
                    }
                })
                .create();
    }

    private void addToStatistic(String symbol, Context context) {
        initStatistic(context);
        String fontstatistic = SharedPreferencesWrapper.getFontStatistic(context);
        String currentFontName = TypefaceProvider.getInstance(context).getCurrentFontName();
        if (currentFontName != null) {
            SharedPreferencesWrapper.setFontStatistic(context, fontstatistic.replace(currentFontName, currentFontName + symbol));
        }
    }

    private void initStatistic(Context context) {
        if (SharedPreferencesWrapper.getFontStatistic(context).isEmpty()) {
            String initial = "";
            for (String font : TypefaceProvider.getInstance(context).getAllFontNames()) {
                initial += font + "\n";
            }
            SharedPreferencesWrapper.setFontStatistic(context, initial);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        Context context = getContext();
        TextView view = (TextView) dialog.findViewById(android.R.id.message);
        TypefaceProvider.getInstance(context).setTypeface(view, Typeface.NORMAL);
        view = (TextView) dialog.findViewById(android.R.id.button1);
        TypefaceProvider.getInstance(context).setTypeface(view, Typeface.ITALIC);
        view = (TextView) dialog.findViewById(android.R.id.button2);
        TypefaceProvider.getInstance(context).setTypeface(view, Typeface.BOLD);
        view = (TextView) dialog.findViewById(android.R.id.button3);
        TypefaceProvider.getInstance(context).setTypeface(view, Typeface.BOLD_ITALIC);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DialogFragmentListener) {
            mListener = (DialogFragmentListener) activity;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onDialogCancelled();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onDialogDismissed();
        }
    }

}
