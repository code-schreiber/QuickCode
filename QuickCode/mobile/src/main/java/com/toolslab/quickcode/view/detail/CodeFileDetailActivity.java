package com.toolslab.quickcode.view.detail;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.ActivityCodeFileDetailBinding;
import com.toolslab.quickcode.databinding.ContentCodeFileDetailBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.view.base.BaseActivity;
import com.toolslab.quickcode.view.common.view.OnImageClickedListener;
import com.toolslab.quickcode.view.common.view.dialog.ImageDialogFragment;
import com.toolslab.quickcode.view.fullscreen.FullscreenImageActivity;


public class CodeFileDetailActivity extends BaseActivity implements OnImageClickedListener {

    private static final String CODE_FILE_ID = "CODE_FILE_ID";

    private ActivityCodeFileDetailBinding binding;
    private CodeFileViewModel codeFileViewModel;
    private String codeFileId;
    private ValueEventListener onCodeFilesChangedListener;

    public static void start(BaseActivity context, String codeFileId) {
        Intent intent = new Intent(context, CodeFileDetailActivity.class);
        intent.putExtra(CODE_FILE_ID, codeFileId);
        context.startActivity(intent);
        context.overridePendingTransitionEnter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        codeFileId = getIntent().getStringExtra(CODE_FILE_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_code_file_detail);
        binding.setClickListener(this);
        binding.activityCodeFileDetailContent.setClickListener(this);

        setSupportActionBar(binding.toolbar);
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        addListeners();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionExit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void onStop() {
        removeListeners();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageClicked() {
        showDialog(ImageDialogFragment.newInstance(codeFileViewModel.getOriginalImage()));
    }

    private void addListeners() {
        onCodeFilesChangedListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                codeFileViewModel = DatabaseReferenceWrapper.getCodeFileFromDataSnapshot(dataSnapshot);
                if (codeFileViewModel != null) {
                    binding.setCodeFileViewModel(codeFileViewModel);
                    binding.activityCodeFileDetailContent.setCodeFileViewModel(codeFileViewModel);
                    binding.executePendingBindings();
                    binding.activityCodeFileDetailContent.executePendingBindings();
                    binding.toolbarLayout.setTitle(codeFileViewModel.getDisplayName());
                    initViews();
                    initDebugViews();
                } else {
                    logError("codeFileViewModel is null");
                    showSimpleDialog(R.string.error_generic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logException("onCancelled " + databaseError.getCode(), databaseError.toException());
                showSimpleDialog(R.string.error_generic);
            }
        };
        DatabaseReferenceWrapper.addValueEventListenerForCodeFileId(codeFileId, onCodeFilesChangedListener);
    }

    private void removeListeners() {
        DatabaseReferenceWrapper.removeEventListenerForCodeFileId(codeFileId, onCodeFilesChangedListener);
    }

    private void initViews() {
        final ContentCodeFileDetailBinding content = binding.activityCodeFileDetailContent;
        content.contentCodeFileDetailDebugTags.setVisibility(View.GONE);
        content.contentCodeFileDetailDebugSize.setVisibility(View.GONE);
        content.contentCodeFileDetailDebugType.setVisibility(View.GONE);
        handleAllowClickingLinks(content.contentCodeFileDetailCodeDisplayContentTextview);
        if (codeFileViewModel.getCodeDisplayContent().equals(codeFileViewModel.getCodeRawContent())) {
            // Don't display double infos
            binding.activityCodeFileDetailContent.contentCodeFileDetailCodeRawContentLayout.setVisibility(View.GONE);
        } else {
            handleAllowClickingLinks(content.contentCodeFileDetailCodeRawContentTextview);
        }
        initFab();
    }

    private void initFab() {
        FloatingActionButton fab = binding.activityCodeFileDetailFab;
        View codeLayout = binding.activityCodeFileDetailContent.contentCodeFileDetailCodeLayout;
        codeLayout.setVisibility(View.VISIBLE);
        fab.setImageBitmap(codeFileViewModel.getCodeImageThumbnail());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenImageActivity.start((BaseActivity) view.getContext(), codeFileId);
            }
        });
    }

    private void initDebugViews() {
        // TODO [Before beta] use BuildConfig.DEBUG
        if (true) {
            binding.activityCodeFileDetailContent.contentCodeFileDetailDebugTags.setVisibility(View.VISIBLE);
            binding.activityCodeFileDetailContent.contentCodeFileDetailDebugSize.setVisibility(View.VISIBLE);
            binding.activityCodeFileDetailContent.contentCodeFileDetailDebugType.setVisibility(View.VISIBLE);
            binding.activityCodeFileDetailContent.contentCodeFileDetailDebugTags.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String objectAsNiceJson = codeFileViewModel.toString().replace("{", "{\n").replace(",", ",\n");
                    Bitmap original = codeFileViewModel.getOriginalImage();
                    Bitmap thumbnail = codeFileViewModel.getOriginalImageThumbnail();
                    Bitmap code = codeFileViewModel.getCodeImage();
                    Bitmap codeThumb = codeFileViewModel.getCodeImageThumbnail();
                    ImageDialogFragment dialog = ImageDialogFragment.newInstance(objectAsNiceJson, original, thumbnail, code, codeThumb);
                    showDialog(dialog);
                    return true;
                }
            });
        }
    }

    private void handleAllowClickingLinks(TextView textView) {
        FullscreenImageActivity.handleAllowClickingLinks(this, textView);
    }

}
