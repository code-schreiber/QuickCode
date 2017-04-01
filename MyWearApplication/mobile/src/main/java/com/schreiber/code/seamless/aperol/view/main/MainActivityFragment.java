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
import com.schreiber.code.seamless.aperol.util.android.NetworkUtils;
import com.schreiber.code.seamless.aperol.view.base.BaseActivity;
import com.schreiber.code.seamless.aperol.view.base.BaseFragment;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;
import com.schreiber.code.seamless.aperol.view.detail.CodeFileDetailActivity;

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

        adapter = new MyCustomAdapter(getAdapterData(), this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == READ_REQUEST_CODE) {
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    handleFile(uri);
                    return;
                }
            }
        }
        showSnack("onActivityResult not handling result: resultCode: " + resultCode + " requestCode: " + requestCode + " resultData: " + resultData);
    }

    @Override
    public void onItemClicked(CodeFileViewModel item) {
        CodeFileDetailActivity.start((BaseActivity) getActivity(), item);
    }

    @Override
    public boolean onItemLongClicked(CodeFileViewModel item) {
        final CodeFile codeFile = item.getCodeFile();
        if (SharedPreferencesWrapper.deleteListItem(getActivity(), codeFile)) {
            Snackbar.make(recyclerView, codeFile.displayName() + " was deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addItemToAdapter(codeFile);
                        }
                    })
                    .show();
        } else {
            Snackbar.make(recyclerView, "Problem deleting " + codeFile.displayName(), Snackbar.LENGTH_LONG)
                    .show();
        }
        adapter.replaceData(getAdapterData());
        return true;
    }

    void performFileSearch() {

        BarcodeDetector detector = CodeFileFactory.setupBarcodeDetector(getActivity());
        if (detector == null) {
            // Not able to scan, so why bother the user
            showSimpleDialog("Could not set up the detector! Was offline: " + !NetworkUtils.isOnline(getActivity()));
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
        // Fires an intent to spin up the "file chooser" UI and select a file.
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    void importAssets() {
        ArrayList<String> paths = new AssetPathLoader(getActivity().getAssets(), "test code images").getPaths();
        ArrayList<CodeFile> assets = new ArrayList<>();
        for (String path : paths) {
            CodeFile item = CodeFileFactory.createCodeFileFromPath(getActivity(), path);
            if (item != null) {
                assets.add(item);
            } else {
                showSimpleDialog("Error: See backstrace. Not adding " + path);
            }
        }

        if (assets.isEmpty()) {
            showSimpleDialog("No Assets to import");
        } else {
            for (CodeFile codeFile : assets) {
                addItemToAdapter(codeFile);
            }

        }
    }

    private void addItemToAdapter(CodeFile codeFile) {
        ArrayList<CodeFile> itemsBefore = SharedPreferencesWrapper.getListItems(getActivity());
        if (!itemsBefore.contains(codeFile)) {
            SharedPreferencesWrapper.addListItem(getActivity(), codeFile);
            ArrayList<CodeFileViewModel> adapterData = getAdapterData();
            if (adapterData.isEmpty()) {
//                showSimpleDialog("adapterData is empty after adding file, showing item that was not persisted");
                adapter.addData(CodeFileViewModel.create(codeFile));
            } else {
                adapter.replaceData(adapterData);
            }
        } else {
            showSnack(codeFile.displayName() + " already exists");
        }
    }

    private ArrayList<CodeFileViewModel> getAdapterData() {
        ArrayList<CodeFile> data = SharedPreferencesWrapper.getListItems(getActivity());
        return CodeFileViewModel.createList(data);
    }

    void handleFile(Uri uri) {
        if (uri != null) {
            CodeFile item = CodeFileFactory.createCodeFileFromUri(getActivity(), uri);
            if (item != null) {
                addItemToAdapter(item);
            }
        }
    }

    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

}