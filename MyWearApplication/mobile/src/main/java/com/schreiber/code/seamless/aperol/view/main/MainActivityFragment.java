package com.schreiber.code.seamless.aperol.view.main;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.db.SharedPreferencesWrapper;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.util.AssetPathLoader;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;

import java.util.ArrayList;


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

        ArrayList<CodeFile> data = SharedPreferencesWrapper.getListItems(getActivity());
        adapter = new MyCustomAdapter(CodeFileViewModel.createList(data), this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    void performFileSearch() {

        BarcodeDetector detector = CodeFileFactory.setupBarcodeDetector(getActivity());
        if (detector == null) {
            // Not able to scan, so why bother the user
            showSimpleDialog("Could not set up the detector!");
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

    void importAssets() {
        ArrayList<String> paths = new AssetPathLoader(getActivity().getAssets(), "test code images").getPaths();
        ArrayList<CodeFile> assets = new ArrayList<>();
        for (String path : paths) {
            CodeFile item = CodeFileFactory.createItemFromPath(getActivity(), path);
            if (item != null) {
                assets.add(item);
            } else {
                showSimpleDialog("Error: See backstrace. Not adding " + path);
            }
        }

        if (assets.isEmpty()) {
            showSimpleDialog("No Assets to import");
        } else {
            ArrayList<CodeFile> itemsBefore = SharedPreferencesWrapper.getListItems(getActivity());
            for (CodeFile codeFile : assets) {
                if (!itemsBefore.contains(codeFile)) {
                    SharedPreferencesWrapper.addListItem(getActivity(), codeFile);
                } else {
                    showSnack(codeFile.filename() + " already exists");// TODO extract method
                }
            }
            ArrayList<CodeFile> data = SharedPreferencesWrapper.getListItems(getActivity());
            adapter.replaceData(CodeFileViewModel.createList(data));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == READ_REQUEST_CODE) {
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    handleFile(uri);
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

    void handleFile(Uri uri) {
        if (uri != null) {
            CodeFile item = CodeFileFactory.createItemFromUri(getActivity(), uri);
            if (item != null) {
                if (!SharedPreferencesWrapper.getListItems(getActivity()).contains(item)) {
                    SharedPreferencesWrapper.addListItem(getActivity(), item);
                    ArrayList<CodeFile> data = SharedPreferencesWrapper.getListItems(getActivity());
                    adapter.replaceData(CodeFileViewModel.createList(data));
                } else {
                    showSnack(item.filename() + " already exists");
                }
            }
        }
    }

    private void showSnack(String m) {
        logInfo(m);
        try {
            Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            logException(e);
        }
    }

    @Override
    public void onItemClicked(CodeFileViewModel item) {
        CodeFileDetailActivity.start((BaseActivity) getActivity(), item.codeFile());
    }

}