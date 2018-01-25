package com.toolslab.quickcode.view.main;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.toolslab.quickcode.R;
import com.toolslab.quickcode.databinding.ItemListBinding;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.view.common.view.OnViewClickedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// TODO generalize ViewHolder https://medium.com/google-developers/android-data-binding-recyclerview-db7c40d9f0e4
class CodeFileViewModelsAdapter extends RecyclerView.Adapter<CodeFileViewModelsAdapter.ViewHolder> {

    private List<CodeFileViewModel> data;
    private final OnViewClickedListener clickListener;

    CodeFileViewModelsAdapter(OnViewClickedListener listener) {
        this.data = new ArrayList<>();
        this.clickListener = listener;
    }

    void addCodeFileViewModel(CodeFileViewModel viewModel) {
        if (!data.contains(viewModel)) {
            data.add(viewModel);
            Collections.sort(data);
            notifyDataSetChanged();
        }
    }

    void removeCodeFileViewModel(CodeFileViewModel viewModel) {
        if (data.contains(viewModel)) {
            data.remove(viewModel);
            Collections.sort(data);
            notifyDataSetChanged();
        }
    }

    @Override
    public CodeFileViewModelsAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
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


