package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.ProductDetailScreen;
import com.example.myapplication.R;
import com.example.myapplication.model.ProductModel;

import java.util.List;

public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.ViewHolder> {

    private final Context           context;
    private final List<ProductModel> list;

    public RelatedProductAdapter(Context context, List<ProductModel> list) {
        this.context = context;
        this.list    = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_related_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ProductModel p = list.get(position);

        h.tvName.setText(p.getName() != null ? p.getName() : "");
        h.tvFabric.setText(p.getCategoryName() != null ? p.getCategoryName() : "");
        h.tvPrice.setText("₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMrp.setText("MRP : ₹" + (p.getMrp() != null ? p.getMrp() : "0"));
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        h.tvDiscount.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Off");
        h.tvMoq.setText("MOQ : " + (p.getMoq() != null ? p.getMoq() : ""));

        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .centerCrop()
                .into(h.ivImage);

        // Wishlist icon
        h.ivWishlist.setImageResource(p.isWishlisted()
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);

        h.ivWishlist.setOnClickListener(v -> {
            p.setWishlisted(!p.isWishlisted());
            h.ivWishlist.setImageResource(p.isWishlisted()
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite_border);
        });

        // Navigate to ProductDetailActivity on card click
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailScreen.class);
            intent.putExtra("c_random", p.getRandom());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivWishlist;
        TextView  tvName, tvFabric, tvPrice, tvMrp, tvDiscount, tvMoq;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImage    = v.findViewById(R.id.iv_related_image);
            ivWishlist = v.findViewById(R.id.iv_related_wishlist);
            tvName     = v.findViewById(R.id.tv_related_name);
            tvFabric   = v.findViewById(R.id.tv_related_fabric);
            tvPrice    = v.findViewById(R.id.tv_related_price);
            tvMrp      = v.findViewById(R.id.tv_related_mrp);
            tvDiscount = v.findViewById(R.id.tv_related_discount);
            tvMoq      = v.findViewById(R.id.tv_related_moq);
        }
    }
}