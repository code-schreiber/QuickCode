package com.schreiber.code.seamless.aperol.view.main;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;

import java.io.IOException;
import java.util.ArrayList;


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
        setContentView(R.layout.activity_code_file_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setDisplayHomeAsUpEnabled(true);
        initViews();
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

    private void initViews() {
        final CodeFileViewModel codeFileViewModel = getIntent().getParcelableExtra(EXTRA_CODE_FILE_VIEW_MODEL);

        setTitle(codeFileViewModel.codeFile().filename());
        final Bitmap originalImage = codeFileViewModel.getOriginalImage(this);
        ((ImageView) findViewById(R.id.activity_code_file_detail_header_image)).setImageBitmap(originalImage);
        ((TextView) findViewById(R.id.content_code_file_detail_text)).setText(codeFileViewModel.toString());
        findViewById(R.id.content_code_file_detail_text).setOnClickListener(new View.OnClickListener() {
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
                    ArrayList<Bitmap> barcodesAsBitmap = CodeFileFactory.getBarcodesAsBitmapFromImage(view.getContext(), originalImage);
                    if (barcodesAsBitmap.isEmpty()) {
                        showSimpleDialog("Could't get code from " + codeFileViewModel.codeFile().originalFilename());
                    } else {
                        if (barcodesAsBitmap.size() > 1) {
                            showSimpleDialog("Error: " + barcodesAsBitmap.size() + " codes found, saving only one.");
                        }
                        Bitmap code = barcodesAsBitmap.get(0);
                        try {
                            codeFileViewModel.saveCodeImage(view.getContext(), code);
                        } catch (IOException e) {
                            Logger.logException(e);
                        }
                        initFab(codeFileViewModel, originalImage);
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
