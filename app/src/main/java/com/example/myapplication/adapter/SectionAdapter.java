package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.activity.ProductListingScreen;
import com.example.myapplication.R;
import com.example.myapplication.model.SectionModel;

import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SectionModel> list;

    // ✅ Pass the parent categoryId so we can filter correctly
    private String parentCategoryId = "1";

    public SectionAdapter(Context context, ArrayList<SectionModel> list) {
        this.context = context;
        this.list    = list;
    }

    public SectionAdapter(Context context, ArrayList<SectionModel> list, String parentCategoryId) {
        this.context          = context;
        this.list             = list;
        this.parentCategoryId = parentCategoryId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trending_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SectionModel section = list.get(position);

        holder.txtSection.setText(section.getName());

        Glide.with(context)
                .load(section.getImage())
                .placeholder(R.drawable.new_arrival)
                .error(R.drawable.new_arrival)
                .centerCrop()
                .into(holder.imgSection);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductListingScreen.class);
            intent.putExtra("categoryId", parentCategoryId);
            intent.putExtra("sectionId",  String.valueOf(section.getId()));
            intent.putExtra("title",      section.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSection;
        TextView  txtSection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSection = itemView.findViewById(R.id.imgTrending);
            txtSection = itemView.findViewById(R.id.txtTrending);
        }
    }
}