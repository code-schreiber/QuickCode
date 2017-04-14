package com.schreiber.code.seamless.aperol.view.main;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.ActivityMainBinding;
import com.schreiber.code.seamless.aperol.db.SharedPreferencesWrapper;
import com.schreiber.code.seamless.aperol.view.base.BaseActivity;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.FontStatisticDialogFragment;


public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FontStatisticDialogFragment.DialogFragmentListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private static final String COUNT_KEY = "TODO_FIND_A_WAY_TO_READ_FROM_ONE_SOURCE";//TODO

    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = binding.activityMainAppBarMain.appBarMainToolbar;
        setSupportActionBar(toolbar);
        drawerLayout = binding.activityMainDrawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.activityMainNavView;
        navigationView.setNavigationItemSelectedListener(this);


        binding.activityMainAppBarMain.appBarMainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityFragment fragment = getMainActivityFragment();
                if (fragment != null) {
                    fragment.performFileSearch();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        handleIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Uri linkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && linkData != null) {
            String intentType = intent.getType();
            String type = getContentResolver().getType(linkData);
            showSnack(type + " and " + intentType);// TODO check
            MainActivityFragment fragment = getMainActivityFragment();
            if (fragment != null) {
                fragment.handleFile(linkData);
            }
        }
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

    private void onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_global_import_assets) {
            importAssets();
        } else if (id == R.id.menu_global_show_font_dialog) {
            showFontsDialog();
        } else if (id == R.id.menu_global_debug_reset_app) {
            SharedPreferencesWrapper.clearAll(this);
            finish();
        }
    }

    private void importAssets() {
        MainActivityFragment fragment = getMainActivityFragment();
        if (fragment != null) {
            fragment.importAssets();
        }
    }

    private void showFontsDialog() {
        showDialog(FontStatisticDialogFragment.newInstance());
    }

    @Override
    public void onOkClicked(boolean showDialogAgain) {
        if (showDialogAgain) {
            showFontsDialog();
        }
    }

    @Nullable
    private MainActivityFragment getMainActivityFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main_fragment);
        if (fragment != null && fragment instanceof MainActivityFragment) {
            return (MainActivityFragment) fragment;// TODO do this the right way
        }
        showSnack(MainActivityFragment.class + " not found");
        return null;
    }

    private void showSnack(String m) {
        logInfo(m);
        Snackbar.make(drawerLayout, m, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        logDebug("onResume ");
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        logDebug("onPause");
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        logDebug("onConnected " + bundle);
        logDebug("mGoogleApiClient " + mGoogleApiClient);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        logDebug(dataEventBuffer + " onDataChanged");
        for (DataEvent event : dataEventBuffer) {
            logDebug("event.getType() " + event.getType());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logDebug("onConnectionSuspended " + i);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logDebug("onConnectionFailed " + connectionResult);
    }

    // Create a data map and put data in it
    public void increaseCounter(int count) {
        String path = "/count";
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        // For sending text 100KB of data is 102400 characters
        putDataMapReq.getDataMap().putInt(COUNT_KEY, count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        logDebug(count + " putDataItem " + putDataReq);
//        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        // FIXME devices not connected http://stackoverflow.com/questions/22524760/not-able-to-connect-android-wear-emulator-with-device
//        React to when it sends the data
// developer.android.com/training/wearables/data-layer/events.html#Wait
//        pendingResult.addBatchCallback(new PendingResult.BatchCallback() {
//            @Override
//            public void zzs(Status status) {
//                logDebug("zzs " + status);
//            }
//        });
    }

}


