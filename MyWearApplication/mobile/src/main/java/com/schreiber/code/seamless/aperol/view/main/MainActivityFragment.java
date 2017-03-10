package com.schreiber.code.seamless.aperol.view.main;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.db.SharedPreferencesWrapper;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.util.AssetPathLoader;
import com.schreiber.code.seamless.aperol.util.EncodingUtils;
import com.schreiber.code.seamless.aperol.util.IOUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.util.UriUtils;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

        List<CodeFile> data = SharedPreferencesWrapper.getListItems(getActivity());
        ArrayList<String> paths = new AssetPathLoader(getActivity().getAssets(), "test code images").getPaths();
        for (String path : paths) {
            CodeFile item = createItemFromPath(path);
            if (item != null) {
                data.add(item);
            } else {
                showSnack("Error: Not adding " + path);
            }
        }
        adapter = new MyCustomAdapter(data, this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        BarcodeDetector detector = setupBarcodeDetector();
        if (detector == null) {
            // Not able to scan, so why bother the user
            return;
        }

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only a file type, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be "*/*".
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == READ_REQUEST_CODE) {
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        CodeFile item = createItemFromUri(uri);
                        if (item != null) {
                            if (!SharedPreferencesWrapper.getListItems(getActivity()).contains(item)) {
                                SharedPreferencesWrapper.addListItem(getActivity(), item);
                                adapter.replaceData(SharedPreferencesWrapper.getListItems(getActivity()));
                            } else {
                                showSnack(item.filename() + " already exists");
                            }
                        }
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

    private CodeFile createItemFromPath(String path) {
        return createItemFromUri(Uri.parse(path));
    }

    private CodeFile createItemFromUri(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        String filename = UriUtils.getDisplayName(contentResolver, uri);
        String type = contentResolver.getType(uri);
        String size = UriUtils.getSize(contentResolver, uri);
        Bitmap fileAsImage = getBitmapFromUri(uri);
        return createItem(filename, type, size, fileAsImage, uri.toString());
    }

    private CodeFile createItem(String filename, String type, String size, Bitmap fileAsImage, String source) {
        if (fileAsImage != null) {
            Bitmap code = getCodeFromBitmap(fileAsImage);
            if (code != null) {
                int thumbnailSize = 64;
                Bitmap thumbnail = Bitmap.createScaledBitmap(code, thumbnailSize, thumbnailSize, false);
                if (thumbnail != null) {
                    try {
                        IOUtils.saveBitmapToFile(getActivity(), fileAsImage, filename, "original");
                        IOUtils.saveBitmapToFile(getActivity(), code, filename, "code");
                        IOUtils.saveBitmapToFile(getActivity(), thumbnail, filename, "thumbnail");
                    } catch (IOException e) {
                        Logger.logException(e);
                        return null;
                    }
                    return CodeFile.create(filename, type, size, new Date(), source);
                }
            }
        }
        return null;
    }

    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClicked(CodeFile item) {
        String originalFilename = item.originalFilename();
        Bitmap fileAsImage = IOUtils.getBitmapFromFile(getActivity(), originalFilename, "original");
        Bitmap code = IOUtils.getBitmapFromFile(getActivity(), originalFilename, "code");
        Bitmap thumbnail = IOUtils.getBitmapFromFile(getActivity(), originalFilename, "thumbnail");
        showImages(item.toString(), fileAsImage, code, thumbnail);
    }

    private void showImages(String info, Bitmap fileAsImage, Bitmap code, Bitmap thumbnail) {
        showDialog(ImageDialogFragment.newInstance(info, fileAsImage, code, thumbnail));
    }

    @Nullable
    Bitmap getBitmapFromUri(Uri uri) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (UriUtils.fileExists(resolver, uri)) {
            if (UriUtils.isPdf(resolver, uri)) {
                return pdfToBitmap(uri);
            } else if (UriUtils.isImage(resolver, uri)) {
                return UriUtils.getBitmapFromUri(getActivity().getContentResolver(), uri);
            } else if (UriUtils.isText(resolver, uri)) {
                String textContent = UriUtils.readTextFromUri(resolver, uri);
                return EncodingUtils.encodeAsQrCode(textContent);
            } else {
                showSnack("No known file type: " + uri);
            }
        } else {
            showSnack("File doesn't exist: " + uri);
        }
        return null;
    }


    private Bitmap getCodeFromBitmap(Bitmap bitmap) {
        ArrayList<Bitmap> codes = new ArrayList<>();

        BarcodeDetector detector = setupBarcodeDetector();
        if (detector == null) {
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        for (int i = 0; i < barcodes.size(); i++) {
            int key = barcodes.keyAt(i);
            Barcode barcode = barcodes.get(key);

            Bitmap code;
            if (barcode.format == Barcode.QR_CODE) {
                code = EncodingUtils.encodeAsQrCode(barcode.rawValue);
            } else if (barcode.format == Barcode.PDF417) {
                code = EncodingUtils.encodeAsPdf417(barcode.rawValue);
            } else {
                // TODO
                code = EncodingUtils.encodeAsQrCode(barcode.rawValue);
            }
            if (code != null) {
                codes.add(code);
            }
        }
        if (codes.isEmpty()) {
            return null;
        } else {
            if (codes.size() > 1) {
                Logger.logError(codes.size() + " codes found in bitmap!");
            }
            return codes.get(0);
        }
    }

    @Nullable
    private BarcodeDetector setupBarcodeDetector() {
        BarcodeDetector detector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            showSnack("Could not set up the detector!");
            return null;
        }
        return detector;
    }

    @Nullable
    private Bitmap pdfToBitmap(Uri uri) {
        int pageNum = 0;// TODO
        try {
            ParcelFileDescriptor fileDescriptor = getActivity().getContentResolver().openFileDescriptor(uri, UriUtils.MODE_READ);
            PdfiumCore pdfiumCore = new PdfiumCore(getActivity());
            PdfDocument pdfDocument = pdfiumCore.newDocument(fileDescriptor);
            pdfiumCore.openPage(pdfDocument, pageNum);
            if (pdfiumCore.getPageCount(pdfDocument) != 1) {
                showSnack("Error: Pdf has " + pdfiumCore.getPageCount(pdfDocument) + " pages.");
            }

            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);

            printInfo(pdfiumCore, pdfDocument);
            pdfiumCore.closeDocument(pdfDocument);

            return bitmap;
        } catch (IOException e) {
            logException(e);
        }
        return null;
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