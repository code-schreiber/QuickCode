package com.schreiber.code.seamless.aperol.view.main;


import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.FragmentMainBinding;
import com.schreiber.code.seamless.aperol.db.DatabaseReferenceWrapper;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileCreator;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.util.AssetPathLoader;
import com.schreiber.code.seamless.aperol.util.Tracker;
import com.schreiber.code.seamless.aperol.util.UriUtils;
import com.schreiber.code.seamless.aperol.util.android.NetworkUtils;
import com.schreiber.code.seamless.aperol.view.base.BaseActivity;
import com.schreiber.code.seamless.aperol.view.base.BaseFragment;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;
import com.schreiber.code.seamless.aperol.view.detail.CodeFileDetailActivity;
import com.schreiber.code.seamless.aperol.view.fullscreen.FullscreenImageActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivityFragment extends BaseFragment implements OnViewClickedListener {

    private TextView loadingView;
    private RecyclerView recyclerView;
    private MyCustomAdapter adapter;

    private static final int READ_REQUEST_CODE = 111;
    private DatabaseReferenceWrapper.OnCodeFilesChangedListener onCodeFilesChangedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        loadingView = binding.fragmentMainActivityLoadingView;
        recyclerView = binding.fragmentMainActivityRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new MyCustomAdapter(this);
        recyclerView.setAdapter(adapter);

        onCodeFilesChangedListener = new DatabaseReferenceWrapper.OnCodeFilesChangedListener() {
            @Override
            public void codeFilesChanged(ArrayList<CodeFile> codeFiles) {
                replaceListData(codeFiles);
            }
        };
        DatabaseReferenceWrapper.addOnCodeFilesChangedListener(onCodeFilesChangedListener);//TODO get reference to use later

        DatabaseReferenceWrapper.loadCodeFiles(new DatabaseReferenceWrapper.OnCodeFilesLoadedListener() {
            @Override
            public void codeFilesLoaded(ArrayList<CodeFile> codeFiles) {
                replaceListData(codeFiles);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        //TODO
//        DatabaseReferenceWrapper.removeOnCodeFilesChangedListener(onCodeFilesChangedListener); TODO
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
        logError("onActivityResult not handling result: resultCode: " + resultCode + " requestCode: " + requestCode + " resultData: " + resultData);
        showSnack("onActivityResult not handling result: resultCode: " + resultCode + " requestCode: " + requestCode + " resultData: " + resultData);
    }

    @Override
    public void onItemClicked(CodeFileViewModel item) {
        CodeFileDetailActivity.start((BaseActivity) getActivity(), item);
        Tracker.trackOnClick(getActivity(), "onItemClicked");
    }

    @Override
    public void onCodeInItemClicked(CodeFileViewModel item) {
        FullscreenImageActivity.start((BaseActivity) getActivity(), item);
        Tracker.trackOnClick(getActivity(), "onCodeInItemClicked");
    }

    @Override
    public boolean onItemLongClicked(CodeFileViewModel item) {
        final CodeFile codeFile = item.getCodeFile();
        DatabaseReferenceWrapper.deleteListItem(codeFile, new DatabaseReferenceWrapper.OnCodeFileDeletedListener() {
            @Override
            public void codeFileDeleted(Exception exception) {
                if (exception == null) {
                    Snackbar.make(recyclerView, codeFile.displayName() + " was deleted", Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addItemToAdapter(codeFile);
                                }
                            })
                            .show();
                } else {
                    showSnack("Problem deleting " + codeFile.displayName());
                    logException("Problem deleting " + codeFile.displayName(), exception);
                }
            }
        });
        Tracker.trackOnClick(getActivity(), "onItemLongClicked");
        return true;
    }

    private void replaceListData(ArrayList<CodeFile> codeFiles) {
        ArrayList<CodeFileViewModel> adapterData = CodeFileViewModel.createList(codeFiles);
        adapter.replaceData(adapterData);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (adapter.getItemCount() > 0) {
            mainActivity.setVisibilityOfFabHint(View.GONE);
        } else {
            mainActivity.setVisibilityOfFabHint(View.VISIBLE);
        }
    }

    void performFileSearch() {
        String typeFilter = "*/*";
        performFileSearch(typeFilter);
    }

    void performFileSearchForImages() {
        String typeFilter = "image/*";
        performFileSearch(typeFilter);
    }

    private void performFileSearch(String typeFilter) {
        BarcodeDetector detector = CodeFileCreator.setupBarcodeDetector(getActivity());
        if (detector == null) {
            // Not able to scan, so why bother the user
            final String message = "Could not set up the barcode detector! Was offline: " + !NetworkUtils.isOnline(getActivity());
            showSimpleDialog(message);
            logError(message);
            return;
        }

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only a file type, using the data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be "*/*".
        intent.setType(typeFilter);

        // Fires an intent to spin up the "file chooser" UI and select a file.
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    void importAssets() {
        ArrayList<String> paths = new AssetPathLoader(getActivity().getAssets(), "test code images").getPaths();
        ArrayList<CodeFile> assets = new ArrayList<>();
        for (String path : paths) {
            ArrayList<CodeFile> items = CodeFileFactory.createCodeFileFromAssets(getActivity(), path);
            if (items.isEmpty()) {
                showSimpleDialog("Error: Not adding " + path);
            } else {
                assets.addAll(items);
            }
        }

        for (CodeFile codeFile : assets) {
            addItemToAdapter(codeFile);
        }
    }

    private void addItemToAdapter(CodeFile codeFile) {
        DatabaseReferenceWrapper.addListItemAuthFirst(codeFile);
    }

    public void handleFile(List<Uri> uris) {
        for (Uri uri : uris) {
            handleFile(uri);
        }
    }

    void handleFile(Uri uri) {
        if (UriUtils.isSupportedImportFile(getActivity().getContentResolver(), uri)) {
            backgroundTaskStart(uri);
        } else {
            showSimpleDialog("No file added: File type not supported, " + getActivity().getContentResolver().getType(uri) + " is not one of the supported formats: " + UriUtils.getSupportedImportFormatsAsString() + ".");
        }
    }

    private void backgroundTaskStart(final Uri uri) {
        loadingView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // create code files in background
                final ArrayList<CodeFile> items = CodeFileFactory.createCodeFilesFromUri(getActivity(), uri);
                loadingView.post(new Runnable() {

                    @Override
                    public void run() {
                        addCodeFilesToAdapter(items);
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    void handleSharedText(String text) {
        final int maxCharacters = 2953;
        if (text.length() > maxCharacters) {
            logWarning("Text was too long so it has been cut. Length: " + text.length());
            showSimpleDialog("Text was too long so it has been cut.");
            text = text.substring(0, maxCharacters - 3) + "...";
        }
        ArrayList<CodeFile> items = CodeFileFactory.createCodeFilesFromText(getActivity(), text);
        addCodeFilesToAdapter(items);
    }

    private void addCodeFilesToAdapter(ArrayList<CodeFile> items) {
        if (items.isEmpty()) {
            showSimpleDialog("No file added: No code could be found, make sure it is one of the supported formats: " + CodeFileCreator.getSupportedBarcodeFormatsAsString() + ".");
        } else {
            for (CodeFile codeFile : items) {
                CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                if (!newCodeFileViewModel.isCodeAvailable()) {
                    showSimpleDialog("No code could be found, make sure it is one of the supported formats: " + CodeFileCreator.getSupportedBarcodeFormatsAsString() + ".");
                }
                addItemToAdapter(codeFile);
            }
        }
    }

    @Deprecated
    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

}