package com.example.myapplication.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.FilterModel;

import java.util.List;

public class FilterOptionAdapter extends RecyclerView.Adapter<FilterOptionAdapter.VH> {

    private final Context context;
    private final List<FilterModel.FilterOption> options;

    public FilterOptionAdapter(Context context, List<FilterModel.FilterOption> options) {
        this.context = context;
        this.options = options;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_filter_option, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FilterModel.FilterOption opt = options.get(position);

        holder.tvName.setText(opt.getOptionName());

        if (opt.getCount() > 0) {
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText("(" + opt.getCount() + ")");
        } else {
            holder.tvCount.setVisibility(View.GONE);
        }

        // Check state
        updateCheckState(holder, opt.isSelected());

        holder.itemView.setOnClickListener(v -> {
            opt.setSelected(!opt.isSelected());
            updateCheckState(holder, opt.isSelected());
        });
    }

    private void updateCheckState(VH holder, boolean selected) {
        if (selected) {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.red_primary));
            holder.checkBox.setBackgroundResource(R.drawable.checkbox_checked_bg);
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.drawable.filter_item_selected_bg);
        } else {
            holder.tvName.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.checkBox.setBackgroundResource(R.drawable.checkbox_unchecked_bg);
            holder.checkIcon.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        View checkBox;
        View checkIcon;

        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tv_option_name);
            tvCount   = v.findViewById(R.id.tv_option_count);
            checkBox  = v.findViewById(R.id.view_checkbox);
            checkIcon = v.findViewById(R.id.iv_check_icon);
        }
    }
}