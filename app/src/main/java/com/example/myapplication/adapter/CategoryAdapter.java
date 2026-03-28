package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.myapplication.activity.ProductListingScreen;
import com.example.myapplication.R;
import com.example.myapplication.model.CategoryModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<CategoryModel> list;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categorylist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = list.get(position);

        holder.txtCategory.setText(category.getName());

        Glide.with(context)
                .load(category.getImage())
                .transform(new CircleCrop())
                .placeholder(R.drawable.new_arrival)
                .error(R.drawable.new_arrival)
                .into(holder.imgCategory);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductListingScreen.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("sectionId",  "");
            intent.putExtra("title",      category.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgCategory;
        TextView txtCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}