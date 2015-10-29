package com.example.sguillen.mywearapplication.view.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sguillen.mywearapplication.NavigationDrawerFragment;
import com.example.sguillen.mywearapplication.R;
import com.example.sguillen.mywearapplication.view.common.view.OnViewClickedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder> {

    private final static String TAG = MyCustomAdapter.class.getSimpleName();

    private String[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener { // FIXME make static

        private final OnViewClickedListener mListener;

        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(View v, OnViewClickedListener listener) {
            super(v);
            this.mListener = listener;
            this.mTextView = (TextView) v.findViewById(R.id.item_list_text_view);
        }

        @Override
        public void onClick(View v) {
            // TODO make this get triggered
            Log.d(TAG, "onClick" + this.mTextView.getText());
            boolean a = true;
            if (a){
                mListener.onTomato(v, getAdapterPosition());
            } else {
                mListener.onPotato(v);
            }
        }



    }



    // Provide a suitable constructor (depends on the kind of dataset)
    public MyCustomAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyCustomAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(v, getOnViewClickedListener());
    }

    @NonNull
    private OnViewClickedListener getOnViewClickedListener() {
        return new OnViewClickedListener() {
            @Override
            public void onPotato(View caller) {
                // Here just as an example
            }

            @Override
            public void onTomato(View caller, int position) {
//                item = mDataset.get(position); TODO
                ((MainActivity) caller.getContext()).increaseCounter(position);
            }
        };
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}


