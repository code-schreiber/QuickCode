package com.example.sguillen.mywearapplication;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;


public class MainWearActivity extends WearableActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "MainWearActivity";


    private static final String COUNT_KEY = "TODO_FIND_A_WAY_TO_READ_FROM_ONE_SOURCE";

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.activity_main_wear_container);
        mTextView = (TextView) findViewById(R.id.activity_main_wear_text);
        mClockView = (TextView) findViewById(R.id.activity_main_wear_clock);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed " + connectionResult);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, dataEvents + " onDataChanged");
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "event.getType() " + event.getType());
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                String path = "/count";
                if (item.getUri().getPath().compareTo(path) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateCount(dataMap.getInt(COUNT_KEY));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted ");
                // DataItem deleted
            }
        }
    }

    // Our method to update the count
    private void updateCount(final int c) {
        //TODO
        Log.d(TAG, "Got " + c);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//stuff that updates ui
                mTextView.setText("Got " + c);
            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(new Date().toString());
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

}
