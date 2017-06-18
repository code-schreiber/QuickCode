package com.schreiber.code.seamless.aperol.view.main;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.databinding.ItemListBinding;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;

import java.util.ArrayList;
import java.util.Collections;


// TODO generalize ViewHolder https://medium.com/google-developers/android-data-binding-recyclerview-db7c40d9f0e4
class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder> {

    private ArrayList<CodeFileViewModel> data;
    private final OnViewClickedListener clickListener;


    MyCustomAdapter(OnViewClickedListener listener) {
        this.data = new ArrayList<>();
        this.clickListener = listener;
    }

    void replaceData(ArrayList<CodeFileViewModel> items) {
        data.clear();
        data.addAll(items);
        Collections.sort(data);
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
        CodeFileViewModel item = data.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public boolean contains(CodeFile codeFile) {
        return data.contains(CodeFileViewModel.create(codeFile));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemListBinding binding;

        ViewHolder(ItemListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CodeFileViewModel item) {
            binding.setCodeFileViewModel(item);
            binding.setActionListener(clickListener);
            binding.executePendingBindings();
        }

    }

}


