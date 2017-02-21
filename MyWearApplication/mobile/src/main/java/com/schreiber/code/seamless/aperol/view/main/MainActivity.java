package com.schreiber.code.seamless.aperol.view.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.util.TypefaceProvider;
import com.schreiber.code.seamless.aperol.view.common.view.dialog.FontStatisticDialogFragment;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;

    private static final String COUNT_KEY = "TODO_FIND_A_WAY_TO_READ_FROM_ONE_SOURCE";//TODO


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.activity_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.app_bar_main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main_fragment);
                if (fragment != null && fragment instanceof MainActivityFragment)
                    ((MainActivityFragment) fragment).performFileSearch();
                else {
                    showSnack(fragment + " is not " + MainActivityFragment.class);
                }
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO show() in base activity
                FontStatisticDialogFragment dialog = FontStatisticDialogFragment.newInstance();
                dialog.show(getSupportFragmentManager(), dialog.toString());
            }
        }, 1000 * 3);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        showSnack("MenuItem: " + id);

        if (id == R.id.menu_global_camera) {

        } else if (id == R.id.menu_global_manage) {

        } else if (id == R.id.menu_global_share) {

        } else if (id == R.id.menu_global_send) {

        }
    }

    private void showSnack(String m) {
        Log.i(TAG, m);
        Snackbar.make(findViewById(R.id.app_bar_main_fab), m, Snackbar.LENGTH_SHORT).show();
    }

    public void onSectionAttached(int number) {
        Snackbar.make(findViewById(R.id.app_bar_main_fab), "onSectionAttached: " + number, Snackbar.LENGTH_SHORT).show();
        switch (number) {
            case 1:
                Log.i(TAG, getString(R.string.title_section1));
                break;
            case 2:
                Log.i(TAG, getString(R.string.title_section2));
                break;
            case 3:
                Log.i(TAG, getString(R.string.title_section3));
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume ");
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected " + bundle);
        Log.d(TAG, "mGoogleApiClient " + mGoogleApiClient);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, dataEventBuffer + " onDataChanged");
        for (DataEvent event : dataEventBuffer) {
            Log.d(TAG, "event.getType() " + event.getType());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended " + i);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed " + connectionResult);
    }

    // Create a data map and put data in it
    public void increaseCounter(int count) {
        String path = "/count";
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        // For sending text 100KB of data is 102400 characters
        putDataMapReq.getDataMap().putInt(COUNT_KEY, count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d(TAG, count + " putDataItem " + putDataReq);
        // FIXME devices not connected http://stackoverflow.com/questions/22524760/not-able-to-connect-android-wear-emulator-with-device
//        React to when it sends the data
// developer.android.com/training/wearables/data-layer/events.html#Wait
//        pendingResult.addBatchCallback(new PendingResult.BatchCallback() {
//            @Override
//            public void zzs(Status status) {
//                Log.d(TAG, "zzs " + status);
//            }
//        });
    }

}


