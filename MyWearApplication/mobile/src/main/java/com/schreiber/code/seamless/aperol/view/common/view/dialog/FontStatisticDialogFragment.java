package com.schreiber.code.seamless.aperol.view.common.view.dialog;


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

import java.util.Set;


public class FontStatisticDialogFragment extends DialogFragment {

    public interface DialogFragmentListener {

        void onOkClicked(boolean showDialogAgain);

    }

    private DialogFragmentListener mListener;

    public static FontStatisticDialogFragment newInstance() {
        return new FontStatisticDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof DialogFragmentListener) {
            mListener = (DialogFragmentListener) context;
        } else {
            throw new ClassCastException("Activity must implement " + DialogFragmentListener.class + ": " + context.getClass());
        }
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        TypefaceProvider.getInstance(context).resetRandomKey();
        return new AlertDialog.Builder(context)
                .setMessage("¿¡do you like this font?!" + " \uD83D\uDD25\uD83D\uDCA5 \uD83D\uDD25 ️" + TypefaceProvider.getInstance(context).getCurrentFontName() +
                        "\nEstimado/a.\n" +
                        "Como continuación a la notificación que habrá recibido de la DGT (Dirección General de Tráfico) por vía postal, " +
                        "relativa a la Campaña de Seguridad 16-C-004, le recordamos que ....\n" +
                        "En dicha comunicación le informamos que es necesario revisar la instalación eléctrica de la batería (Como recordatorio, le adjuntamos modelo de carta informativa sobre esta campaña.)" +
                        "\nSymbols ö ä ü ß à á â æ ã å ā ė ī œ ō û")
                .setNegativeButton("it's ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToStatistic(" -", context);
                        mListener.onOkClicked(true);
                    }
                })
                .setNeutralButton("statistic", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fontstatistic = SharedPreferencesWrapper.getFontStatistic(context);
                        initStatistic(context, fontstatistic);
                        Toast.makeText(context, SharedPreferencesWrapper.getFontStatistic(context), Toast.LENGTH_LONG).show();
                        mListener.onOkClicked(false);
                    }
                })
                .setPositiveButton("oh yeah!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToStatistic(" |", context);
                        mListener.onOkClicked(true);
                    }
                })
                .create();
    }

    private void addToStatistic(String symbol, Context context) {
        String fontstatistic = SharedPreferencesWrapper.getFontStatistic(context);
        initStatistic(context, fontstatistic);
        String currentFontName = TypefaceProvider.getInstance(context).getCurrentFontName();
        if (currentFontName != null) {
            SharedPreferencesWrapper.setFontStatistic(context, fontstatistic.replace(currentFontName, currentFontName + symbol));
        }
    }

    private void initStatistic(Context context, String fontstatistic) {
        if (fontstatistic.isEmpty()) {
            final Set<String> allFontNames = TypefaceProvider.getInstance(context).getAllFontNames();
            String initial = "";
            int i = 1;
            for (String font : allFontNames) {
                initial += i++ + " " + font + "\n";
            }
            SharedPreferencesWrapper.setFontStatistic(context, initial);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        Context context = getContext();
        TypefaceProvider provider = TypefaceProvider.getInstance(context);

        TextView view = (TextView) dialog.findViewById(android.R.id.message);
        provider.setTypeface(view, Typeface.NORMAL);
        view = (TextView) dialog.findViewById(android.R.id.button1);
        provider.setTypeface(view, Typeface.BOLD);
        view = (TextView) dialog.findViewById(android.R.id.button2);
        provider.setTypeface(view, Typeface.ITALIC);
        view = (TextView) dialog.findViewById(android.R.id.button3);
        provider.setTypeface(view, Typeface.BOLD_ITALIC);
    }

}
