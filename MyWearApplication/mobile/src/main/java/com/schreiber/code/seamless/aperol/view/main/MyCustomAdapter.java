package com.schreiber.code.seamless.aperol.view.main;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.ItemListBinding;
import com.schreiber.code.seamless.aperol.model.ListItem;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;

import java.util.ArrayList;
import java.util.List;

// TODO generalize ViewHolder https://medium.com/google-developers/android-data-binding-recyclerview-db7c40d9f0e4
class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder> {

    private List<ListItem> data;
    private final OnViewClickedListener listener;


    MyCustomAdapter(List<ListItem> data, OnViewClickedListener listener) {
        this.data = data;
        this.listener = listener;
    }

    void replaceData(ArrayList<ListItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public MyCustomAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemListBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_list, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = data.get(position);
        holder.bind(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemListBinding binding;

        ViewHolder(ItemListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ListItem item) {
            binding.setListItem(item);
            binding.setActionListener(listener);
            binding.executePendingBindings();
        }

    }


}


