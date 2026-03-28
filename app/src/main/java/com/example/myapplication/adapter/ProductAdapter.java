package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.myapplication.ProductDetailScreen;
import com.example.myapplication.R;
import com.example.myapplication.model.ProductModel;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ProductModel> productList;

    public ProductAdapter(Context context, ArrayList<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.tvProductName.setText(product.getName() != null ? product.getName() : "");
        holder.tvSubtitle.setText(product.getCategoryName() != null ? product.getCategoryName() : "");
        holder.tvSellingPrice.setText("₹" + (product.getSellingPrice() != null ? product.getSellingPrice() : "0"));
        holder.tvDiscount.setText((product.getDiscount() != null ? product.getDiscount() : "0") + "% Off");
        holder.tvMoq.setText("MRP : ₹" + product.getMrp() + " | MOQ : " + product.getMoq());
        holder.btnBuy.setText("Buy for ₹" + (product.getSellingPrice() != null ? product.getSellingPrice() : "0"));
        holder.tvMargin.setText((product.getDiscount() != null ? product.getDiscount() : "0") + "% Margin");

        // ✅ Strikethrough set in Java — NOT in XML
        holder.tvMrp.setText("₹" + (product.getMrp() != null ? product.getMrp() : "0"));
        holder.tvMrp.setPaintFlags(holder.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Load image
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .centerCrop()
                .into(holder.ivProductImage);

        // Wishlist icon state
        holder.ivWishlist.setImageResource(
                product.isWishlisted() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );

        // Wishlist toggle
        holder.ivWishlist.setOnClickListener(v -> {
            product.setWishlisted(!product.isWishlisted());
            holder.ivWishlist.setImageResource(
                    product.isWishlisted() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
            );
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailScreen.class);
            intent.putExtra("c_random", product.getRandom());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivWishlist;
        TextView tvProductName, tvSubtitle, tvSellingPrice, tvMrp, tvDiscount, tvMoq, tvMargin;
        Button btnBuy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivWishlist     = itemView.findViewById(R.id.iv_wishlist);
            tvProductName  = itemView.findViewById(R.id.tv_product_name);
            tvSubtitle     = itemView.findViewById(R.id.tv_product_subtitle);
            tvSellingPrice = itemView.findViewById(R.id.tv_selling_price);
            tvMrp          = itemView.findViewById(R.id.tv_mrp);
            tvDiscount     = itemView.findViewById(R.id.tv_discount);
            tvMoq          = itemView.findViewById(R.id.tv_moq);
            btnBuy         = itemView.findViewById(R.id.btn_buy);
            tvMargin       = itemView.findViewById(R.id.tv_margin);
        }
    }
}