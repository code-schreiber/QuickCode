package com.schreiber.code.seamless.aperol.view.main;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.util.IOUtils;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.ImageDialogFragment;


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

    private void initViews() {
        final CodeFile codeFile = getIntent().getParcelableExtra(EXTRA_CODE_FILE);
        final String originalFilename = codeFile.originalFilename();
        final Bitmap fileAsImage = IOUtils.getBitmapFromFile(this, originalFilename, "original");
        final Bitmap code = IOUtils.getBitmapFromFile(this, originalFilename, "code");
        final Bitmap thumbnail = IOUtils.getBitmapFromFile(this, originalFilename, "thumbnail");// TODO extract getBitmapFromFile()s
        setTitle(codeFile.filename());
        ((ImageView) findViewById(R.id.activity_code_file_detail_header_image)).setImageBitmap(fileAsImage);
        ((TextView) findViewById(R.id.content_code_file_detail_text)).setText(codeFile.toString());
        ((TextView) findViewById(R.id.content_code_file_detail_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ImageDialogFragment.newInstance(codeFile.toString(), fileAsImage, code, thumbnail));
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_code_file_detail_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenImageActivity.start((BaseActivity) view.getContext(), codeFile);
            }
        });
    }

}
