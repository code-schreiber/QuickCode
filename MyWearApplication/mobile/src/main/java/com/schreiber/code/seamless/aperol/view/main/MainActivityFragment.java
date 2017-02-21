package com.schreiber.code.seamless.aperol.view.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.model.ListItem;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements OnViewClickedListener {

    private final static String TAG = "MainActivityFragment";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MyCustomAdapter adapter;

    private static final int READ_REQUEST_CODE = 111;
    private static final int READ_PDF_REQUEST_CODE = 222;
    private static final int READ_IMAGE_REQUEST_CODE = 333;
    private static final int READ_TEXT_REQUEST_CODE = 444;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainActivityFragment newInstance(int sectionNumber) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_activity_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        List<ListItem> data = Arrays.asList(new ListItem("uno"), new ListItem("dos"), new ListItem("three"));
        adapter = new MyCustomAdapter(data, this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only a file type, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be "*/*".
        intent.setType("*/*");
//        intent.setType("pdf/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == READ_REQUEST_CODE) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    String s = "File name " + getDisplayName(uri) +
                            "\ntype " + requestCode +
                            "\nsize " + getSize(uri) +
                            "\nUri " + uri.toString();
                    adapter.addAnItem(new ListItem(s));
                }
            } else if (requestCode == READ_PDF_REQUEST_CODE)
                showSnack("requestCode: " + requestCode);
            else if (requestCode == READ_IMAGE_REQUEST_CODE)
                showSnack("requestCode: " + requestCode);
            else showSnack("resultCode: " + resultCode);
    }

    private void showSnack(String m) {
        Log.i(TAG, m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

    private String readTextFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
        } catch (IOException e) {
            Logger.logException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return stringBuilder.toString();
    }

    @Nullable
    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                return BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
        } catch (FileNotFoundException e) {
            Logger.logException(e);
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return null;
    }

    public String getDisplayName(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);
        if (cursor != null)
            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor.moveToFirst()) {

                    // Note it's called "Display Name".  This is
                    // provider-specific, and might not necessarily be the file name.
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    return cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        return "";
    }

    public String getSize(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);
        if (cursor != null)
            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since an
                    // int can't be null in Java, the behavior is implementation-specific,
                    // which is just a fancy term for "unpredictable".  So as
                    // a rule, check if it's null before assigning to an int.  This will
                    // happen often:  The storage API allows for remote files, whose
                    // size might not be locally known.
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString()
                        // will do the conversion automatically.
                        return cursor.getString(sizeIndex) + " bytes";
                    }
                }
            } finally {
                cursor.close();
            }
        return "Unknown";
    }

    @Override
    public void onItemClicked(ListItem item) {
        Uri uri = Uri.parse(item.s);
        if (item.s.contains("\ntype " + READ_REQUEST_CODE)) {
            // Handle file
        } else if (item.s.contains("\ntype " + READ_IMAGE_REQUEST_CODE)) {
            // Handle image file
            Bitmap b = getBitmapFromUri(uri);
            // TODO show(b);
        } else if (item.s.contains("\ntype " + READ_PDF_REQUEST_CODE)) {
            // Handle PDF file
            // TODO PDF pdf = getPdfFromUri(uri);
            // TODO show(pdf);
        } else if (item.s.contains("\ntype " + READ_TEXT_REQUEST_CODE)) {
            // Handle Text file
            String text = readTextFromUri(uri);
            showSnack(text);
        } else {
            showSnack("No known type: " + item.s);
        }
    }

}