package com.toolslab.quickcode.view.main;


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

import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.ActivityCodesListBinding;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.db.SharedPreferencesWrapper;
import com.toolslab.quickcode.util.Tracker;
import com.toolslab.quickcode.util.UriUtils;
import com.toolslab.quickcode.view.base.BaseActivity;
import com.toolslab.quickcode.view.common.view.dialog.FontStatisticDialogFragment;

import java.util.List;


public class CodesListActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FontStatisticDialogFragment.DialogFragmentListener {

    private DrawerLayout drawerLayout;

    private ActivityCodesListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_codes_list);
        initViews();

        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
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

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onOkClicked(boolean showDialogAgain) {
        if (showDialogAgain) {
            showFontsDialog();
        }
    }

    @Override
    protected void onResume() {
        logDebug("onResume ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        logDebug("onPause");
        super.onPause();
    }

    public void setVisibilityOfFabHint(int visibility) {
        binding.activityCodesListAppBarMain.appBarMainFabHint.setVisibility(visibility);
    }

    private void initViews() {
        Toolbar toolbar = binding.activityCodesListAppBarMain.appBarMainToolbar;
        setSupportActionBar(toolbar);
        drawerLayout = binding.activityCodesListDrawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.activityCodesListNavView;
        navigationView.setNavigationItemSelectedListener(this);

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

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        logDebug("Handling intent: " + Tracker.getIntentDescription(intent));
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

    private void onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_global_import_assets) {
            importAssets();
        } else if (id == R.id.menu_global_show_font_dialog) {
            showFontsDialog();
        } else if (id == R.id.menu_global_debug_reset_app) {
            SharedPreferencesWrapper.clearAll(this);
            DatabaseReferenceWrapper.clearAllAuthFirst();
            finish();
        }
    }

    private void importAssets() {
        CodesListFragment fragment = getCodesListFragment();
        if (fragment != null) {
            fragment.importAssets();
        }
    }

    private void showFontsDialog() {
        showDialog(FontStatisticDialogFragment.newInstance());
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
        logInfo(m);
        Snackbar.make(drawerLayout, m, Snackbar.LENGTH_SHORT).show();
    }

}
