package com.toolslab.quickcode.view.main;


import android.app.Activity;
import android.content.ContentResolver;
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

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.FragmentCodesListBinding;
import com.toolslab.quickcode.databinding.ItemLoadingBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.model.CodeFile;
import com.toolslab.quickcode.model.CodeFileCreator;
import com.toolslab.quickcode.model.CodeFileFactory;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.util.AssetPathLoader;
import com.toolslab.quickcode.util.Tracker;
import com.toolslab.quickcode.util.UriUtils;
import com.toolslab.quickcode.view.base.BaseActivity;
import com.toolslab.quickcode.view.base.BaseFragment;
import com.toolslab.quickcode.view.common.view.OnViewClickedListener;
import com.toolslab.quickcode.view.detail.CodeFileDetailActivity;
import com.toolslab.quickcode.view.fullscreen.FullscreenImageActivity;

import java.util.List;


public class CodesListFragment extends BaseFragment implements OnViewClickedListener {

    private ItemLoadingBinding loadingViewBinding;
    private RecyclerView recyclerView;
    private CodeFileViewModelsAdapter adapter;

    private static final int READ_REQUEST_CODE = 111;
    private ValueEventListener onCodeFilesChangedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentCodesListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_codes_list, container, false);
        loadingViewBinding = binding.fragmentCodesListLoadingView;
        recyclerView = binding.fragmentCodesListRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new CodeFileViewModelsAdapter(this);
        recyclerView.setAdapter(adapter);

        CodeFileCreator.setupBarcodeDetector(getActivity());

        onCodeFilesChangedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CodeFileViewModel> models = DatabaseReferenceWrapper.getCodeFilesFromDataSnapshot(dataSnapshot);
                replaceListData(models);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logException("onCancelled", databaseError.toException());
            }
        };
        DatabaseReferenceWrapper.addValueEventListenerAuthFirst(onCodeFilesChangedListener);
        return binding.getRoot();
    }

    @Override
    public void onStop() {
        DatabaseReferenceWrapper.removeEventListenerAuthFirst(onCodeFilesChangedListener);
        super.onStop();
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
    }

    @Override
    public void onItemClicked(CodeFileViewModel item) {
        CodeFileDetailActivity.start((BaseActivity) getActivity(), item.getCodeFile().id());
        Tracker.trackOnClick(getActivity(), "onItemClicked");
    }

    @Override
    public void onCodeInItemClicked(CodeFileViewModel item) {
        FullscreenImageActivity.start((BaseActivity) getActivity(), item.getCodeFile().id());
        Tracker.trackOnClick(getActivity(), "onCodeInItemClicked");
    }

    @Override
    public boolean onItemLongClicked(CodeFileViewModel item) {
        // TODO [UI nice to have] swipe to delete
        final CodeFile codeFile = item.getCodeFile();
        DatabaseReferenceWrapper.deleteListItemAuthFirst(codeFile, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(final DatabaseError databaseError, final DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // TODO [UI nice to have] make Snackbar implementation more elegant
                            Snackbar.make(recyclerView, codeFile.displayName() + " was deleted", Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            addCodeFileToDatabase(codeFile);
                                        }
                                    })
                                    .show();
                        } else {
                            showSnack("Problem deleting " + codeFile.displayName());
                            logException("Problem deleting " + codeFile.displayName(), databaseError.toException());
                        }
                    }
                }
        );
        Tracker.trackOnClick(getActivity(), "onItemLongClicked - deleteListItemAuthFirst");
        return true;
    }

    private void replaceListData(List<CodeFileViewModel> adapterData) {
        adapter.replaceData(adapterData);

        CodesListActivity codesListActivity = (CodesListActivity) getActivity();
        if (codesListActivity != null) {
            if (adapter.getItemCount() > 0) {
                codesListActivity.setVisibilityOfFabHint(View.GONE);
            } else {
                codesListActivity.setVisibilityOfFabHint(View.VISIBLE);
            }
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
        logDebug("performing file search on " + typeFilter);
        BarcodeDetector detector = CodeFileCreator.setupBarcodeDetector(getActivity());
        if (detector == null) {
            // Not able to scan, so why bother the user
            final String message = "Could not set up the barcode detector!";
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

    public void handleFile(List<Uri> uris) {
        for (Uri uri : uris) {
            handleFile(uri);
        }
    }

    void handleFile(Uri uri) {
        if (UriUtils.isSupportedImportFile(getActivity().getContentResolver(), uri)) {
            loadFileInBackground(uri);
        } else {
            showSimpleDialog(R.string.error_file_not_added_unsupported_type, getActivity().getContentResolver().getType(uri), UriUtils.getSupportedImportFormatsAsString());
        }
    }

    void importAssets() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<String> paths = new AssetPathLoader(getActivity().getAssets(), "test code images").getPaths();
                for (String path : paths) {
                    showLoadingView(path);
                    final List<CodeFile> items = CodeFileFactory.createCodeFileFromAssets(getActivity(), path);
                    hideLoadingView(items);
                }
            }
        }).start();
    }

    void loadSharedTextInBackground(String text) {
        final int maxCharacters = 2953;
        if (text.length() > maxCharacters) {
            String message = getString(R.string.error_shared_text_too_long);
            logWarning(message + " Length: " + text.length());
            showSimpleDialog(message);
            text = text.substring(0, maxCharacters - 3) + "...";
        }

        String originalFilename = text.length() > 20 ? text.substring(0, 20) + "…" : text;
        showLoadingView(originalFilename);
        final String finalText = text;
        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<CodeFile> items = CodeFileFactory.createCodeFilesFromText(getActivity(), finalText);
                hideLoadingView(items);
            }
        }).start();
    }

    private void loadFileInBackground(final Uri uri) {
        showLoadingView(uri);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // create code files in background
                final List<CodeFile> items = CodeFileFactory.createCodeFilesFromUri(getActivity(), uri);
                hideLoadingView(items);
            }
        }).start();
    }

    private void hideLoadingView(final List<CodeFile> items) {
        loadingViewBinding.itemLoadingText.post(new Runnable() {

            @Override
            public void run() {
                addCodeFilesToDatabase(items);
                loadingViewBinding.getRoot().setVisibility(View.GONE);
            }
        });
    }

    private void showLoadingView(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        String originalFilename = UriUtils.getDisplayName(contentResolver, uri);
        showLoadingView(originalFilename);
    }

    private void showLoadingView(final String originalFilename) {
        loadingViewBinding.itemLoadingText.post(new Runnable() {

            @Override
            public void run() {
                loadingViewBinding.itemLoadingText.setText(originalFilename);
                loadingViewBinding.getRoot().setVisibility(View.VISIBLE);
            }
        });
    }

    private void addCodeFilesToDatabase(List<CodeFile> items) {
        if (items.isEmpty()) {
            showSimpleDialog(R.string.error_file_not_added, CodeFileCreator.getSupportedBarcodeFormatsAsString());
        } else {
            for (CodeFile codeFile : items) {
                addCodeFileToDatabase(codeFile);
            }
        }
    }

    private void addCodeFileToDatabase(CodeFile codeFile) {
        DatabaseReferenceWrapper.addCodeFileAuthFirst(codeFile);
    }

    @Deprecated
    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(recyclerView, m, Snackbar.LENGTH_SHORT).show();
    }

}