package com.toolslab.quickcode.view.main;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.toolslab.quickcode.BuildConfig;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.ActivityCodesListBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.util.GooglePlayServicesUtil;
import com.toolslab.quickcode.util.UriUtils;
import com.toolslab.quickcode.util.log.Tracker;
import com.toolslab.quickcode.view.base.BaseActivity;
import com.toolslab.quickcode.view.common.view.dialog.FontStatisticDialogFragment;

import java.util.List;
import java.util.Locale;


public class CodesListActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FontStatisticDialogFragment.DialogFragmentListener {

    @Nullable
    private DrawerLayout drawerLayout;

    @Nullable
    private ActivityCodesListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GooglePlayServicesUtil googlePlayServicesUtil = new GooglePlayServicesUtil(this);
        if (googlePlayServicesUtil.isAvailable()) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_codes_list);
            initViews(binding);
            handleIntent(getIntent());
        } else {
            logError("Play Services not available");
            googlePlayServicesUtil.showUpdateDialog();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        onMenuItemSelected(item);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        onMenuItemSelected(item);

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onOkClicked(boolean showDialogAgain) {
        if (showDialogAgain) {
            showFontsDialog();
        }
    }

    public void setVisibilityOfFabHint(int visibility) {
        if (binding != null) {
            binding.activityCodesListAppBarMain.appBarMainFabHint.setVisibility(visibility);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void initViews(ActivityCodesListBinding binding) {
        Toolbar toolbar = binding.activityCodesListAppBarMain.appBarMainToolbar;
        setSupportActionBar(toolbar);
        drawerLayout = binding.activityCodesListDrawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.activityCodesListNavView;
        navigationView.setNavigationItemSelectedListener(this);

        final String footerText = createNavigationFooterText();
        binding.activityCodesListNavFooter.navFooterMainText.setText(footerText);
        binding.activityCodesListNavFooter.navFooterMainText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Navigation Footer Text", footerText);
                    clipboard.setPrimaryClip(clip);
                    showSnack(getString(R.string.copied_to_clipboard));
                    return true;
                }
                return false;
            }
        });

        FloatingActionButton actionButton = binding.activityCodesListAppBarMain.appBarMainFab;
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CodesListFragment fragment = getCodesListFragment();
                if (fragment != null) {
                    fragment.performFileSearch();
                }
            }
        });

        actionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CodesListFragment fragment = getCodesListFragment();
                if (fragment != null) {
                    fragment.performFileSearchForImages();
                    return true;
                }
                return false;
            }
        });
    }

    private String createNavigationFooterText() {
        String format = "Version: %1$s (%2$s)%3$s";
        return String.format(Locale.getDefault(), format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                DatabaseReferenceWrapper.getUser());
    }

    // TODO [Refactoring] handle intent in fragment
    private void handleIntent(Intent intent) {
        if (intent.getExtras() != null) {
            Tracker.trackIntent(this, intent);
        }
        CodesListFragment fragment = getCodesListFragment();
        if (fragment == null) {
            logError("CodesListFragment is null");
            showSimpleDialog(R.string.error_generic);
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri linkData = intent.getData();
            if (linkData != null) {
                fragment.handleFile(linkData);
            } else {
                showUnknownTypeDialog(type, "ACTION_VIEW");
            }
        } else if (Intent.ACTION_SEND.equals(action)) {
            if (UriUtils.isText(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                fragment.loadSharedTextInBackground(sharedText);
            } else if (UriUtils.isImage(type)) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                fragment.handleFile(imageUri);
            } else if (UriUtils.isPdf(type)) {
                Uri pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                fragment.handleFile(pdfUri);
            } else {
                showUnknownTypeDialog(type, "ACTION_SEND");
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (UriUtils.isImage(type)) {
                List<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                fragment.handleFile(imageUris);
            } else {
                showUnknownTypeDialog(type, "ACTION_SEND_MULTIPLE");
            }
        } else if (!Intent.ACTION_MAIN.equals(action)) {
            logError("Activity started with unknown action: " + action);
        }
    }

    private void showUnknownTypeDialog(String type, String action) {
        logError("Activity started with " + action + " has unknown type: " + type);
        showSimpleDialog(R.string.error_file_not_added_unsupported_type, type, UriUtils.getSupportedImportFormatsAsString());
    }

    @Deprecated
    private void onMenuItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.menu_global_import_assets) {
//            importAssets();
//        } else if (id == R.id.menu_global_show_font_dialog) {
//            showFontsDialog();
//        } else if (id == R.id.menu_global_debug_reset_app) {
//            SharedPreferencesWrapper.clearAll(this);
//            DatabaseReferenceWrapper.clearAll();
//            finish();
//        }
//    }
//
//    private void importAssets() {
//        CodesListFragment fragment = getCodesListFragment();
//        if (fragment != null) {
//            fragment.importAssets();
//        }
    }

    @Deprecated
    private void showFontsDialog() {
//        showDialog(FontStatisticDialogFragment.newInstance());
    }

    @Nullable
    private CodesListFragment getCodesListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main_fragment);
        if (fragment != null && fragment instanceof CodesListFragment) {
            return (CodesListFragment) fragment;
        }
        showSnack(CodesListFragment.class + " not found");
        logError(CodesListFragment.class + " not found");
        return null;
    }

    private void showSnack(String m) {
        logDebug("Showing Snack: " + m);
        if (drawerLayout != null) {
            Snackbar.make(drawerLayout, m, Snackbar.LENGTH_SHORT).show();
        }
    }

}
