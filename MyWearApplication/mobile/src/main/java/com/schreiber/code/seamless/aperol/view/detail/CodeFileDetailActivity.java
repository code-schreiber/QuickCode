package com.schreiber.code.seamless.aperol.view.detail;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.ActivityCodeFileDetailBinding;
import com.schreiber.code.seamless.aperol.databinding.ContentCodeFileDetailBinding;
import com.schreiber.code.seamless.aperol.db.DatabaseReferenceWrapper;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileCreator;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.view.base.BaseActivity;
import com.schreiber.code.seamless.aperol.view.common.view.OnImageClickedListener;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;
import com.schreiber.code.seamless.aperol.view.fullscreen.FullscreenImageActivity;

import java.util.ArrayList;


public class CodeFileDetailActivity extends BaseActivity implements OnImageClickedListener {

    private static final String EXTRA_CODE_FILE_VIEW_MODEL = "EXTRA_CODE_FILE_VIEW_MODEL";

    private ActivityCodeFileDetailBinding binding;
    private CodeFileViewModel codeFileViewModel;


    public static void start(BaseActivity context, CodeFileViewModel codeFileViewModel) {
        Intent intent = new Intent(context, CodeFileDetailActivity.class);
        intent.putExtra(EXTRA_CODE_FILE_VIEW_MODEL, codeFileViewModel);
        context.startActivity(intent);
        context.overridePendingTransitionEnter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        codeFileViewModel = getIntent().getParcelableExtra(EXTRA_CODE_FILE_VIEW_MODEL);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_code_file_detail);
        binding.setClickListener(this);
        binding.setCodeFileViewModel(codeFileViewModel);
        binding.activityCodeFileDetailContent.setClickListener(this);
        binding.activityCodeFileDetailContent.setCodeFileViewModel(codeFileViewModel);

        setSupportActionBar(binding.toolbar);
        setDisplayHomeAsUpEnabled(true);

        initViews();
        initDebugViews();
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

    private void initViews() {
        setTitle(codeFileViewModel.getDisplayName());
        final ContentCodeFileDetailBinding content = binding.activityCodeFileDetailContent;
        content.contentCodeFileDetailDebugTags.setVisibility(View.GONE);
        content.contentCodeFileDetailDebugSize.setVisibility(View.GONE);
        content.contentCodeFileDetailDebugType.setVisibility(View.GONE);
        FullscreenImageActivity.handleAllowClickingLinks(this, content.contentCodeFileDetailCodeDisplayContentTextview);
        FullscreenImageActivity.handleAllowClickingLinks(this, content.contentCodeFileDetailCodeRawContentTextview);
        initFab();
    }

    private void initFab() {
        final FloatingActionButton fab = binding.activityCodeFileDetailFab;
        View codeLayout = binding.activityCodeFileDetailContent.contentCodeFileDetailCodeLayout;
        if (codeFileViewModel.isCodeAvailable()) {
            codeLayout.setVisibility(View.VISIBLE);
            fab.setImageBitmap(codeFileViewModel.getCodeImageThumbnail());
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenImageActivity.start((BaseActivity) view.getContext(), codeFileViewModel);
                }
            });
            if (codeFileViewModel.getCodeDisplayContent().equals(codeFileViewModel.getCodeRawContent())) {
                // Don't display double infos
                binding.activityCodeFileDetailContent.contentCodeFileDetailCodeRawContentLayout.setVisibility(View.GONE);
            }
        } else {
            // No code, let the user try again
            codeLayout.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.ic_retry_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleFile(view);
                }

                private void handleFile(View view) {
                    ArrayList<CodeFile> codeFiles = CodeFileFactory.createCodeFileFromCodeFile(view.getContext(), codeFileViewModel.getCodeFile());
                    if (codeFiles.isEmpty()) {
                        showSimpleDialog(R.string.error_file_no_code, CodeFileCreator.getSupportedBarcodeFormatsAsString());
                    } else {
                        for (CodeFile codeFile : codeFiles) {
                            CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                            if (newCodeFileViewModel.isCodeAvailable()) {
                                DatabaseReferenceWrapper.addListItemAuthFirst(codeFile); // TODO check that this handles multiple codes
                            } else {
                                showSimpleDialog(R.string.error_file_no_code, CodeFileCreator.getSupportedBarcodeFormatsAsString());
                            }
                        }
                        CodeFile codeFile = codeFiles.get(0);
                        CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                        if (newCodeFileViewModel.isCodeAvailable()) {
                            codeFileViewModel = newCodeFileViewModel;
                            initFab();
                        } else {
                            logError("Code image not available after extracting.");
                        }
                    }
                }
            });
        }
    }

    private void initDebugViews() {
        // TODO use BuildConfig.DEBUG
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
}
