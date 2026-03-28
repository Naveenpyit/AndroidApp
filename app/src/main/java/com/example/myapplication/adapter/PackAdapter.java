package com.example.myapplication.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ProductDetailModel;

import java.util.List;

public class PackAdapter extends RecyclerView.Adapter<PackAdapter.ViewHolder> {

    public interface OnPackSelectedListener {
        void onPackSelected(ProductDetailModel.PackModel pack);
    }

    private final Context                           context;
    private final List<ProductDetailModel.PackModel> packList;
    private final OnPackSelectedListener            listener;
    private int selectedIndex = 0;

    public PackAdapter(Context context,
                       List<ProductDetailModel.PackModel> packList,
                       OnPackSelectedListener listener) {
        this.context  = context;
        this.packList = packList;
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        int prev = selectedIndex;
        selectedIndex = index;
        notifyItemChanged(prev);
        notifyItemChanged(index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_pack_chip, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ProductDetailModel.PackModel pack = packList.get(position);
        h.tvPackName.setText(pack.getPackName());

        boolean isSelected = (position == selectedIndex);
        h.tvPackName.setBackgroundResource(isSelected
                ? R.drawable.pack_selected_bg
                : R.drawable.pack_unselected_bg);
        h.tvPackName.setTextColor(isSelected
                ? context.getResources().getColor(R.color.white)
                : context.getResources().getColor(R.color.red_primary));

        h.itemView.setOnClickListener(v -> {
            setSelectedIndex(position);
            listener.onPackSelected(pack);
        });
    }

    @Override
    public int getItemCount() { return packList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPackName;

        ViewHolder(@NonNull View v) {
            super(v);
            tvPackName = v.findViewById(R.id.tv_pack_name);
        }
    }
}