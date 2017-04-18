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
        final Bitmap originalImage = codeFileViewModel.getOriginalImage(this);
        binding.activityCodeFileDetailHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ImageDialogFragment.newInstance(codeFileViewModel.toString(), originalImage, codeFileViewModel.getCodeImage(v.getContext()), codeFileViewModel.getOriginalThumbnailImage(v.getContext())));
            }
        });
        initFab(binding, codeFileViewModel, originalImage);
    }

    private void initFab(final ActivityCodeFileDetailBinding binding, final CodeFileViewModel codeFileViewModel, final Bitmap originalImage) {
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
        } else {
            // No code, let the user try again
            codeLayout.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.ic_add_image_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<CodeFile> codeFiles = CodeFileFactory.createCodeFileFromCodeFile(view.getContext(), codeFileViewModel.getCodeFile(), originalImage);
                    if (!codeFiles.isEmpty()) {
                        CodeFile codeFile = codeFiles.get(0);// TODO handle multiple codes
                        CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                        if (newCodeFileViewModel.getCodeImage(view.getContext()) != null) {
                            // TODO persist
                            initFab(binding, newCodeFileViewModel, originalImage);
                        } else {
                            showSimpleDialog("No code could be found, make sure it is one of the supported formats: " + CodeFileCreator.getSupportedBarcodeFormatsAsString() + ".");
                        }
                    } else {
                        showSimpleDialog("No Code could be created");
                    }
                }
            });
        }
    }

}
