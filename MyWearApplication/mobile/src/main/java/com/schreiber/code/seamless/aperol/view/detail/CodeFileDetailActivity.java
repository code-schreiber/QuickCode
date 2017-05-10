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
import com.schreiber.code.seamless.aperol.db.DatabaseReferenceWrapper;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileCreator;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.view.base.BaseActivity;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;
import com.schreiber.code.seamless.aperol.view.fullscreen.FullscreenImageActivity;

import java.util.ArrayList;


public class CodeFileDetailActivity extends BaseActivity {

    private static final String EXTRA_CODE_FILE_VIEW_MODEL = "EXTRA_CODE_FILE_VIEW_MODEL";


    public static void start(BaseActivity context, CodeFileViewModel codeFileViewModel) {
        Intent intent = new Intent(context, CodeFileDetailActivity.class);
        intent.putExtra(EXTRA_CODE_FILE_VIEW_MODEL, codeFileViewModel);
        context.startActivity(intent);
        context.overridePendingTransitionEnter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCodeFileDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_code_file_detail);
        CodeFileViewModel codeFileViewModel = getIntent().getParcelableExtra(EXTRA_CODE_FILE_VIEW_MODEL);
        binding.setCodeFileViewModel(codeFileViewModel);
        binding.activityCodeFileDetailContent.setCodeFileViewModel(codeFileViewModel);

        setSupportActionBar(binding.toolbar);
        setDisplayHomeAsUpEnabled(true);

        initViews(binding, codeFileViewModel);
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

    private void initViews(ActivityCodeFileDetailBinding binding, final CodeFileViewModel codeFileViewModel) {
        setTitle(codeFileViewModel.getDisplayName());
        binding.activityCodeFileDetailHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String objectAsNiceJson = codeFileViewModel.toString().replace("{", "{\n").replace(",", ",\n");
                Bitmap original = codeFileViewModel.getOriginalImage(view.getContext());
                Bitmap thumbnail = codeFileViewModel.getOriginalThumbnailImage(view.getContext());
                Bitmap code = codeFileViewModel.getCodeImage(view.getContext());
                Bitmap codeThumb = codeFileViewModel.getCodeThumbnailImage(view.getContext());
                ImageDialogFragment dialog = ImageDialogFragment.newInstance(objectAsNiceJson, original, thumbnail, code, codeThumb);
                showDialog(dialog);
            }
        });
        initFab(binding, codeFileViewModel);
    }

    private void initFab(final ActivityCodeFileDetailBinding binding, final CodeFileViewModel codeFileViewModel) {
        final FloatingActionButton fab = binding.activityCodeFileDetailFab;
        View codeLayout = binding.activityCodeFileDetailContent.contentCodeFileDetailCodeLayout;
        if (codeFileViewModel.isCodeAvailable(this)) {
            codeLayout.setVisibility(View.VISIBLE);
            fab.setImageBitmap(codeFileViewModel.getCodeThumbnailImage(this));
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
                        showSimpleDialog("No code could be found, make sure it is one of the supported formats: " + CodeFileCreator.getSupportedBarcodeFormatsAsString() + ".");
                    } else {
                        for (CodeFile codeFile : codeFiles) {
                            CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                            if (newCodeFileViewModel.isCodeAvailable(view.getContext())) {
                                DatabaseReferenceWrapper.addListItemAuthFirst(codeFile); // TODO check that this handles multiple codes
                            } else {
                                showSimpleDialog("No code could be found, make sure it is one of the supported formats: " + CodeFileCreator.getSupportedBarcodeFormatsAsString() + ".");
                            }
                        }
                        CodeFile codeFile = codeFiles.get(0);
                        CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                        if (newCodeFileViewModel.isCodeAvailable(view.getContext())) {
                            initFab(binding, newCodeFileViewModel);
                        }
                    }
                }
            });
        }
    }

}
