package com.schreiber.code.seamless.aperol.view.main;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.ActivityCodeFileDetailBinding;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;


public class CodeFileDetailActivity extends BaseActivity {

    private static final String EXTRA_CODE_FILE_VIEW_MODEL = "EXTRA_CODE_FILE_VIEW_MODEL";


    static void start(BaseActivity context, CodeFileViewModel codeFileViewModel) {
        Intent intent = new Intent(context, CodeFileDetailActivity.class);
        intent.putExtra(EXTRA_CODE_FILE_VIEW_MODEL, codeFileViewModel);
        context.startActivity(intent);
        context.overridePendingTransitionEnter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCodeFileDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_code_file_detail);
        final CodeFileViewModel codeFileViewModel = getIntent().getParcelableExtra(EXTRA_CODE_FILE_VIEW_MODEL);
        binding.setCodeFileViewModel(codeFileViewModel);
        binding.activityCodeFileDetailContent.setCodeFileViewModel(codeFileViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setDisplayHomeAsUpEnabled(true);

        initViews(codeFileViewModel);
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

    private void initViews(final CodeFileViewModel codeFileViewModel) {
        setTitle(codeFileViewModel.codeFile().displayName());
        final Bitmap originalImage = codeFileViewModel.getOriginalImage(this);
        findViewById(R.id.activity_code_file_detail_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ImageDialogFragment.newInstance(codeFileViewModel.toString(), originalImage, codeFileViewModel.getCodeImage(v.getContext()), codeFileViewModel.getThumbnailImage(v.getContext())));
            }
        });
        initFab(codeFileViewModel, originalImage);
    }

    private void initFab(final CodeFileViewModel codeFileViewModel, final Bitmap originalImage) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_code_file_detail_fab);
        if (codeFileViewModel.getCodeImage(this) == null) {
            // No code, let the user try again
            fab.setImageResource(R.drawable.ic_add_image_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CodeFile codeFile = CodeFileFactory.createCodeFileFromCodeFile(view.getContext(), codeFileViewModel.codeFile(), originalImage);
                    if (codeFile != null) {
                        CodeFileViewModel newCodeFileViewModel = CodeFileViewModel.create(codeFile);
                        if (newCodeFileViewModel.getCodeImage(view.getContext()) != null) {
                            // TODO persist
                            initFab(newCodeFileViewModel, originalImage);
                        } else {
                            showSimpleDialog("No code could be found");
                        }
                    } else {
                        showSimpleDialog("No CodeFile could be created");
                    }
                }
            });
        } else {
            fab.setImageResource(R.drawable.ic_visibility_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenImageActivity.start((BaseActivity) view.getContext(), codeFileViewModel);
                }
            });
        }
    }

}
