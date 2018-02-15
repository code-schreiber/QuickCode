package com.toolslab.quickcode.view.main;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.FragmentCodesListBinding;
import com.toolslab.quickcode.databinding.ItemLoadingBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.model.CodeFile;
import com.toolslab.quickcode.model.CodeFileCreator;
import com.toolslab.quickcode.model.CodeFileFactory;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.util.AssetPathLoader;
import com.toolslab.quickcode.util.DeviceUtil;
import com.toolslab.quickcode.util.UriUtils;
import com.toolslab.quickcode.util.bitmap.PdfToBitmapConverter;
import com.toolslab.quickcode.util.log.Tracker;
import com.toolslab.quickcode.view.base.BaseActivity;
import com.toolslab.quickcode.view.base.BaseFragment;
import com.toolslab.quickcode.view.common.view.OnViewClickedListener;
import com.toolslab.quickcode.view.detail.CodeFileDetailActivity;
import com.toolslab.quickcode.view.fullscreen.FullscreenImageActivity;

import java.util.List;


public class CodesListFragment extends BaseFragment
        implements OnViewClickedListener, InstallReferrerStateListener {

    private static final String INTENT_TYPE_FILTER_ALL = "*/*";
    private static final String INTENT_TYPE_FILTER_IMAGE = "image/*";
    private static final int READ_REQUEST_CODE = 111;

    private ItemLoadingBinding loadingViewBinding;
    private RecyclerView recyclerView;
    private CodeFileViewModelsAdapter adapter;
    private ChildEventListener onCodeFilesChildListener;
    private InstallReferrerClient referrerClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentCodesListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_codes_list, container, false);
        loadingViewBinding = binding.fragmentCodesListLoadingView;
        recyclerView = binding.fragmentCodesListRecyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new CodeFileViewModelsAdapter(this);
        recyclerView.setAdapter(adapter);

        CodeFileCreator.setupBarcodeDetector(getActivity());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        addListeners();
    }

    @Override
    public void onStop() {
        removeListeners();
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
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // User clicked back
            return;
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
        DatabaseReferenceWrapper.removeCodeFile(codeFile);
        Tracker.trackOnLongClick(getActivity(), "removeCodeFile");
        return true;
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        switch (responseCode) {
            case InstallReferrerResponse.OK:
                logDebug("InstallReferrer connected");
                handleReferrer();
                break;
            case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                if (!DeviceUtil.isEmulator()) {
                    logWarning("InstallReferrer not supported");
                }
                break;
            case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                logWarning("Unable to connect to the service");
                break;
            default:
                logWarning("responseCode not found:" + responseCode);
                break;
        }
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        logDebug("onInstallReferrerServiceDisconnected");
    }

    void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case Intent.ACTION_VIEW:
                    handleActionViewIntent(intent);
                    return;
                case Intent.ACTION_SEND:
                    handleActionSendIntent(intent);
                    return;
                case Intent.ACTION_SEND_MULTIPLE:
                    handleActionSendMultipleIntent(intent);
                    return;
                case Intent.ACTION_MAIN:
                    // Just an app start
                    return;
                default:
                    break;
            }
        }
        logError("Activity started with unknown action: " + action + ", " + intent.getType());
    }

    void performFileSearch() {
        if (PdfToBitmapConverter.deviceSupportsPdfToBitmap()) {
            performFileSearch(INTENT_TYPE_FILTER_ALL);
        } else {
            performFileSearchForImages();
        }
    }

    void performFileSearchForImages() {
        performFileSearch(INTENT_TYPE_FILTER_IMAGE);
    }

    private void handleActionViewIntent(Intent intent) {
        Uri linkData = intent.getData();
        if (linkData != null) {
            handleFile(linkData);
        } else {
            showUnknownTypeDialog(intent);
        }
    }

    private void handleActionSendIntent(Intent intent) {
        String type = intent.getType();
        if (UriUtils.isSupportedImportFile(type)) {
            if (UriUtils.isText(type) && (intent.hasExtra(Intent.EXTRA_TEXT))) {
                loadSharedTextInBackground(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else {
                handleFile(intent);
            }
        } else {
            showUnknownTypeDialog(intent);
        }
    }

    private void handleActionSendMultipleIntent(Intent intent) {
        String type = intent.getType();
        if (UriUtils.isImage(type)) {
            List<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            handleFile(imageUris);
        } else {
            showUnknownTypeDialog(intent);
        }
    }

    private void showUnknownTypeDialog(Intent intent) {
        String type = intent.getType();
        logError("Unable to handle intent: " + intent.toString() +
                ", action: " + intent.getAction() +
                ", type: " + type +
                ", extras: " + intent.getExtras());
        showSimpleError(R.string.error_file_not_added_unsupported_type, UriUtils.describeFileType(type), UriUtils.getSupportedImportFormatsAsString());
    }

    private void handleReferrer() {
        try {
            Tracker.trackInstallReferrer(getActivity(), referrerClient.getInstallReferrer());
            referrerClient.endConnection();
        } catch (RemoteException e) {
            logException(e);
        }
    }

    private void addListeners() {
        DatabaseReferenceWrapper.signInAnonymously(new DatabaseReferenceWrapper.OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                // Great! Now we can save stuff in the database
                onCodeFilesChildListener = new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        addCodeFileViewModel(dataSnapshot);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeCodeFileViewModel(dataSnapshot);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                        addCodeFileViewModel(dataSnapshot);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                        addCodeFileViewModel(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        logException("ChildEventListener onCancelled. DatabaseError code " + databaseError.getCode(), databaseError.toException());
                        showSimpleError(R.string.error_generic);
                    }
                };
                updateFabHint();
                DatabaseReferenceWrapper.addEventListeners(onCodeFilesChildListener);
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                // Oh no! We can't save stuff in the database quite yet
                logException("onSignedInFailed", exception);
                showSimpleError(R.string.error_not_signed_in);
            }
        });

        referrerClient = InstallReferrerClient.newBuilder(getActivity()).build();
        referrerClient.startConnection(this);
    }

    private void removeListeners() {
        DatabaseReferenceWrapper.removeEventListeners(onCodeFilesChildListener);
        if (referrerClient != null && referrerClient.isReady()) {
            referrerClient.endConnection();
        }
    }

    private void addCodeFileViewModel(DataSnapshot dataSnapshot) {
        CodeFileViewModel codeFileViewModel = DatabaseReferenceWrapper.getCodeFileFromDataSnapshot(dataSnapshot);
        if (codeFileViewModel != null) {
            adapter.addCodeFileViewModel(codeFileViewModel);
            updateFabHint();
        }
    }

    private void removeCodeFileViewModel(DataSnapshot dataSnapshot) {
        final CodeFileViewModel codeFileViewModel = DatabaseReferenceWrapper.getCodeFileFromDataSnapshot(dataSnapshot);
        if (codeFileViewModel != null) {
            adapter.removeCodeFileViewModel(codeFileViewModel);
            updateFabHint();

            // TODO [UI nice to have] make Snackbar implementation more elegant
            Snackbar.make(recyclerView, codeFileViewModel.getCodeFile().displayName() + " was deleted", Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            addCodeFileToDatabase(codeFileViewModel.getCodeFile());
                        }
                    })
                    .show();
        }
    }

    private void updateFabHint() {
        CodesListActivity codesListActivity = (CodesListActivity) getActivity();
        if (codesListActivity != null) {
            if (adapter.getItemCount() > 0) {
                codesListActivity.setVisibilityOfFabHint(View.GONE);
            } else {
                codesListActivity.setVisibilityOfFabHint(View.VISIBLE);
            }
        }
    }

    private void performFileSearch(String typeFilter) {
        logDebug("performing file search on " + typeFilter);
        BarcodeDetector detector = CodeFileCreator.setupBarcodeDetector(getActivity());
        if (detector == null) {
            // Not able to scan, so why bother the user
            showSimpleError(R.string.error_barcode_detector_not_operational);
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

    private void handleFile(Intent intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            handleFile(uri);
        } else {
            showUnknownTypeDialog(intent);
        }
    }

    private void handleFile(List<Uri> uris) {
        for (Uri uri : uris) {
            handleFile(uri);
        }
    }

    private void handleFile(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        if (UriUtils.isSupportedImportFile(contentResolver, uri)) {
            loadFileInBackground(uri);
        } else {
            String fileType = UriUtils.describeFileType(contentResolver, uri);
            showSimpleError(R.string.error_file_not_added_unsupported_type, fileType, UriUtils.getSupportedImportFormatsAsString());
        }
    }

    private void importAssets() {
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

    private void loadSharedTextInBackground(@NonNull String text) {
        if (text.length() > CodeFileFactory.MAX_CHARACTERS) {
            String message = getString(R.string.error_shared_text_too_long);
            logWarning(message + " Length: " + text.length());
            showSimpleError(message);
            text = text.substring(0, CodeFileFactory.MAX_CHARACTERS - 3) + "...";
        }

        final String originalFilename;
        if (text.length() <= CodeFileFactory.FILENAME_MAX_CHARACTERS) {
            originalFilename = text;
        } else {
            originalFilename = text.substring(0, CodeFileFactory.FILENAME_MAX_CHARACTERS) + "…";
        }
        showLoadingView(originalFilename);
        final String finalText = text;
        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<CodeFile> items = CodeFileFactory.createCodeFilesFromText(getActivity(), originalFilename, finalText);
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
            showSimpleError(R.string.error_file_not_added, CodeFileCreator.getSupportedBarcodeFormatsAsString());
        } else {
            for (CodeFile codeFile : items) {
                addCodeFileToDatabase(codeFile);
            }
        }
    }

    private void addCodeFileToDatabase(CodeFile codeFile) {
        DatabaseReferenceWrapper.addCodeFile(codeFile);
    }

}
