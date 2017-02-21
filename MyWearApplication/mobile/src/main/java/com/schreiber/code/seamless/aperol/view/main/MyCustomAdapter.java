package com.schreiber.code.seamless.aperol.view.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.schreiber.code.seamless.aperol.R;
import com.schreiber.code.seamless.aperol.model.ListItem;
import com.schreiber.code.seamless.aperol.view.common.view.OnAdapterItemClickedListener;
import com.schreiber.code.seamless.aperol.view.common.view.OnViewClickedListener;

import java.util.List;


class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder> {

    private List<ListItem> data;
    private final OnViewClickedListener listener;


    MyCustomAdapter(List<ListItem> data, OnViewClickedListener listener) {
        this.data = data;
        this.listener = listener;
    }

    void addAnItem(ListItem item) {
        data.add(item);
        notifyDataSetChanged();
    }

    @Override
    public MyCustomAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(convertView, new OnAdapterItemClickedListener() {
            @Override
            public void onAdapterItemClicked(int position) {
                listener.onItemClicked(data.get(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = data.get(position);
        holder.textView.setText(item.s);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final OnAdapterItemClickedListener listener;

        private final TextView textView;

        ViewHolder(View convertView, OnAdapterItemClickedListener listener) {
            super(convertView);
            this.textView = (TextView) convertView.findViewById(R.id.item_list_text_view);
            this.listener = listener;
            convertView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onAdapterItemClicked(getAdapterPosition());
        }

    }


}


