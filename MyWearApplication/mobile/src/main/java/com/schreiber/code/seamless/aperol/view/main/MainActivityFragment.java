package com.schreiber.code.seamless.aperol.view.main;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.db.SharedPreferencesWrapper;
import com.schreiber.code.seamless.aperol.model.ListItem;
import com.schreiber.code.seamless.aperol.util.UriUtils;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;
import java.util.List;


public class MainActivityFragment extends BaseFragment implements OnViewClickedListener {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MyCustomAdapter adapter;

    private static final int READ_REQUEST_CODE = 111;

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

        List<ListItem> data = SharedPreferencesWrapper.getListItem(getActivity());
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == READ_REQUEST_CODE) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        ListItem item = itemFromUri(uri);
                        SharedPreferencesWrapper.addListItem(getContext(), item);
                        adapter.replaceData(SharedPreferencesWrapper.getListItem(getContext()));
                    }
                } else {
                    showSnack("resultData: " + resultData);
                }
            } else {
                showSnack("requestCode: " + requestCode);
            }
        } else {
            showSnack("resultCode: " + resultCode);
        }
    }

    @NonNull
    private ListItem itemFromUri(@NonNull Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        String filename = UriUtils.getDisplayName(contentResolver, uri);
        String type = contentResolver.getType(uri);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getExtensionFromMimeType(type);
        if (mimeType == null) {
            mimeType = "";
        }
        String size = UriUtils.getSize(contentResolver, uri);
        return ListItem.create(filename, type, mimeType, size, uri);
    }

    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(ListItem item) {
        Uri uri = item.getUri();
        String type = item.type();
        handleFile(uri, type);
    }

    void handleFile(Uri uri, String type) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (UriUtils.fileExists(resolver, uri)) {
            if (UriUtils.isPdf(resolver, uri)) {
                showPdf(uri);
            } else if (UriUtils.isImage(resolver, uri)) {
                showImage(uri);
            } else if (UriUtils.isText(resolver, uri)) {
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                Toast.makeText(getActivity(), textContent, Toast.LENGTH_SHORT).show();
            } else if (type.equals("application/octet-stream")) {
                String text = UriUtils.readTextFromUri(resolver, uri);
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            } else {
                showSnack("No known type: " + type);
            }
        } else {
            showSnack("File doesn't exist: " + uri);
        }
    }

    private void showImage(Uri uri) {
        Bitmap bitmap = UriUtils.getBitmapFromUri(getActivity().getContentResolver(), uri);
        showDialog(ImageDialogFragment.newInstance(bitmap));
    }

    private void showPdf(Uri uri) {

        try {
            ParcelFileDescriptor fd = getActivity().getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            int pageNum = 0;
            PdfiumCore pdfiumCore = new PdfiumCore(getActivity());

            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

            pdfiumCore.openPage(pdfDocument, pageNum);

            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);

            printInfo(pdfiumCore, pdfDocument);

            pdfiumCore.closeDocument(pdfDocument);

            showDialog(ImageDialogFragment.newInstance(bitmap));
        } catch (IOException e) {
            logException(e);
        }
    }

    private void printInfo(PdfiumCore core, PdfDocument doc) {
        PdfDocument.Meta meta = core.getDocumentMeta(doc);
        logDebug("title = " + meta.getTitle());
        logDebug("author = " + meta.getAuthor());
        logDebug("subject = " + meta.getSubject());
        logDebug("keywords = " + meta.getKeywords());
        logDebug("creator = " + meta.getCreator());
        logDebug("producer = " + meta.getProducer());
        logDebug("creationDate = " + meta.getCreationDate());
        logDebug("modDate = " + meta.getModDate());

        printBookmarksTree(core.getTableOfContents(doc), "-");

    }

    private void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        if (tree.isEmpty()) {
            logDebug("tree is empty");
        } else {
            for (PdfDocument.Bookmark b : tree) {

                logDebug(String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

                if (b.hasChildren()) {
                    printBookmarksTree(b.getChildren(), sep + "-");
                }
            }
        }
    }

}