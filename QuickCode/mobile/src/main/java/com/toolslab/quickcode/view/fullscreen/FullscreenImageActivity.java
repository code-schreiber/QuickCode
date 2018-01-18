package com.toolslab.quickcode.view.fullscreen;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.ActivityFullscreenImageBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.db.PremiumPreferences;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.view.base.BaseActivity;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenImageActivity extends BaseActivity {

    private static final String CODE_FILE_ID = "CODE_FILE_ID";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int DELAY_MILLIS = 100;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };

    private ActivityFullscreenImageBinding binding;
    private View mTextLayout;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            showActionBar();
            mTextLayout.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private ValueEventListener onCodeFilesChangedListener;
    private String codeFileId;

    public static void start(BaseActivity context, String codeFileId) {
        Intent intent = new Intent(context, FullscreenImageActivity.class);
        intent.putExtra(CODE_FILE_ID, codeFileId);
        context.startActivity(intent);// TODO [UI nice to have] animate code in button getting bigger
        context.overridePendingTransitionFadeIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_fullscreen_image);

        setDisplayHomeAsUpEnabled(true);

        mVisible = true;
        codeFileId = getIntent().getStringExtra(CODE_FILE_ID);
        mTextLayout = binding.activityFullscreenImageContentTextLayout;
        mContentView = binding.activityFullscreenImageContent;
        handleAllowClickingLinks(this, binding.activityFullscreenImageContentText);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        addListeners();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionFadeOut();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionFadeOut();
    }

    @Override
    public void onStop() {
        removeListeners();
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
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

    public static void handleAllowClickingLinks(final BaseActivity context, TextView textview) {
        if (PremiumPreferences.allowClickingLinks(context)) {
            Linkify.addLinks(textview, Linkify.ALL);
        } else {
            textview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.showSimpleDialog(R.string.error_premium_links_not_allowed);
                }
            });
        }
    }

    private void addListeners() {
        onCodeFilesChangedListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CodeFileViewModel codeFileViewModel = DatabaseReferenceWrapper.getCodeFileFromDataSnapshot(dataSnapshot);
                if (codeFileViewModel != null) {
                    setTitle(codeFileViewModel.getDisplayName());
                    binding.setCodeFileViewModel(codeFileViewModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logException("onCancelled. DatabaseError code " + databaseError.getCode(), databaseError.toException());
                showSimpleDialog(R.string.error_generic);
            }
        };
        DatabaseReferenceWrapper.addValueEventListenerForCodeFileId(codeFileId, onCodeFilesChangedListener);
    }

    private void removeListeners() {
        DatabaseReferenceWrapper.removeEventListenerForCodeFileId(codeFileId, onCodeFilesChangedListener);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        hideActionBar();
        mTextLayout.setVisibility(View.GONE);// TODO [UI nice to have] use animation
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, DELAY_MILLIS);
    }
}
