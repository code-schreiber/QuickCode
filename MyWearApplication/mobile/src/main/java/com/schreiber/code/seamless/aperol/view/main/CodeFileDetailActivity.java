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
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileFactory;
import com.schreiber.code.seamless.aperol.util.IOUtils;
import com.schreiber.code.seamless.aperol.util.Logger;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;

import java.io.IOException;
import java.util.ArrayList;


public class CodeFileDetailActivity extends BaseActivity {

    private static final String EXTRA_CODE_FILE = "EXTRA_CODE_FILE";


    static void start(BaseActivity context, CodeFile codeFile) {
        Intent intent = new Intent(context, CodeFileDetailActivity.class);
        intent.putExtra(EXTRA_CODE_FILE, codeFile);
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
        final CodeFile codeFile = getIntent().getParcelableExtra(EXTRA_CODE_FILE);
        final String originalFilename = codeFile.originalFilename();
        final Bitmap fileAsImage = IOUtils.getBitmapFromFile(this, originalFilename, "original");
        final Bitmap thumbnail = IOUtils.getBitmapFromFile(this, originalFilename, "thumbnail");// TODO extract getBitmapFromFile()s
        setTitle(codeFile.filename());
        ((ImageView) findViewById(R.id.activity_code_file_detail_header_image)).setImageBitmap(fileAsImage);
        ((TextView) findViewById(R.id.content_code_file_detail_text)).setText(codeFile.toString());
        findViewById(R.id.content_code_file_detail_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap code = IOUtils.getBitmapFromFile(v.getContext(), originalFilename, "code");
                showDialog(ImageDialogFragment.newInstance(codeFile.toString(), fileAsImage, code, thumbnail));
            }
        });
        initFab(originalFilename, fileAsImage);
    }

    private void initFab(final String originalFilename, final Bitmap fileAsImage) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_code_file_detail_fab);
        Bitmap code = IOUtils.getBitmapFromFile(this, originalFilename, "code");
        if (code == null) {
            // No code, let the user try again
            // TODO test trying again
            fab.setImageResource(R.drawable.ic_add_image_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<Bitmap> barcodesAsBitmap = CodeFileFactory.getBarcodesAsBitmapFromImage(view.getContext(), fileAsImage);
                    if (barcodesAsBitmap.isEmpty()) {
                        showSimpleDialog("Could't get code from " + originalFilename);
                    } else {
                        if (barcodesAsBitmap.size() > 1) {
                            showSimpleDialog("Error: " + barcodesAsBitmap.size() + " codes found, saving only one.");
                        }
                        Bitmap code = barcodesAsBitmap.get(0);
                        try {
                            IOUtils.saveBitmapToFile(view.getContext(), code, originalFilename, "code");
                        } catch (IOException e) {
                            Logger.logException(e);
                        }
                        initFab(originalFilename, fileAsImage);
                    }
                }
            });
        } else {
            fab.setImageResource(R.drawable.ic_visibility_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CodeFile codeFile = getIntent().getParcelableExtra(EXTRA_CODE_FILE);
                    FullscreenImageActivity.start((BaseActivity) view.getContext(), codeFile);
                }
            });
        }
    }

}
