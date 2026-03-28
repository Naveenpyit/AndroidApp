package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.WishlistScreen;
import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.model.ListWishlistResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.WishlistManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private final Context context;
    private final ArrayList<ListWishlistResponse.WishlistItem> list;
    private final OnRemoveListener removeListener;
    private final ApiService apiService;
    private final WishlistManager wishlistManager;

    public interface OnRemoveListener {
        void onRemove(int position, String wishlistId);
    }

    public WishlistAdapter(Context context, ArrayList<ListWishlistResponse.WishlistItem> list, OnRemoveListener removeListener) {
        this.context = context;
        this.list = list;
        this.removeListener = removeListener;
        this.apiService = RetrofitClient.getClient(context);
        this.wishlistManager = WishlistManager.getInstance(context);
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist_card, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        ListWishlistResponse.WishlistItem item = list.get(position);

        holder.tvName.setText(item.getCItemCode() != null ? item.getCItemCode() : "");
        holder.tvSubtitle.setText(item.getCFabric() != null ? item.getCFabric() : "");
        holder.tvPrice.setText("₹" + (item.getNSellingPrice() != null ? item.getNSellingPrice() : "0"));
        holder.tvMrp.setText("₹" + (item.getNMrp() != null ? item.getNMrp() : "0"));
        holder.tvMrp.setPaintFlags(holder.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvPackName.setText(item.getCPackName() != null ? item.getCPackName() : "");

        Glide.with(context)
                .load(item.getCImage())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivImage);

        // ✅ Initialize quantity to 1
        int currentQty = safeInt(holder.tvQtyCount.getText().toString());
        if (currentQty == 0) currentQty = 1;
        holder.tvQtyCount.setText(String.valueOf(currentQty));

        // ✅ Remove from Wishlist button
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                removeFromWishlist(item, holder, pos);
            }
        });

        // ✅ Add to Cart button (first time)
        holder.btnAddToCart.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                holder.btnAddToCart.setVisibility(View.GONE);
                holder.layoutQty.setVisibility(View.VISIBLE);
                addToCart(item, 1);
            }
        });

        // ✅ Plus button (increase qty)
        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                int newQty = safeInt(holder.tvQtyCount.getText().toString()) + 1;
                holder.tvQtyCount.setText(String.valueOf(newQty));
                addToCart(item, newQty);
            }
        });

        // ✅ Minus button (decrease qty)
        holder.btnMinus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                int current = safeInt(holder.tvQtyCount.getText().toString());
                if (current > 1) {
                    int newQty = current - 1;
                    holder.tvQtyCount.setText(String.valueOf(newQty));
                    addToCart(item, newQty);
                } else {
                    // Remove from cart (qty = 0)
                    holder.layoutQty.setVisibility(View.GONE);
                    holder.btnAddToCart.setVisibility(View.VISIBLE);
                    addToCart(item, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ✅ Add to Cart with quantity parameter
    private void addToCart(ListWishlistResponse.WishlistItem item, int quantity) {
        String category = item.getNCategory();
        String product = item.getNProduct();
        String pack = item.getCPackName();
        String userId = "10";

        if (isEmpty(category) || isEmpty(product) || isEmpty(pack)) {
            Toast.makeText(context, "Missing product information", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Pass quantity in the request
        AddCartRequest request = new AddCartRequest(category, pack, product, String.valueOf(quantity), userId);

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    if (quantity > 0) {
                        Toast.makeText(context, "Added to cart ✓", Toast.LENGTH_SHORT).show();
                        // ✅ NOTIFY ACTIVITY TO REFRESH CART
                        if (context instanceof WishlistScreen) {
                            ((WishlistScreen) context).onCartUpdated();
                        }
                    } else {
                        Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Remove from Wishlist
    private void removeFromWishlist(ListWishlistResponse.WishlistItem item, WishlistViewHolder holder, int position) {
        String userId = "10";
        String wishlistId = item.getNId();

        if (isEmpty(wishlistId)) {
            Toast.makeText(context, "Cannot remove: ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DeleteWishlistRequest request = new DeleteWishlistRequest(userId, wishlistId);
        apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    // ✅ Remove from persistent storage
                    wishlistManager.removeWishlist(item.getNProduct());

                    // ✅ Notify listener to remove from list
                    removeListener.onRemove(position, wishlistId);
                    Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    private int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 1; }
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnRemove;
        TextView tvName, tvSubtitle, tvPrice, tvMrp, tvPackName;
        LinearLayout btnAddToCart, layoutQty;
        TextView tvQtyCount, btnMinus, btnPlus;

        WishlistViewHolder(@NonNull View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_product_image);
            btnRemove = v.findViewById(R.id.btn_remove_wishlist);
            tvName = v.findViewById(R.id.tv_product_name);
            tvSubtitle = v.findViewById(R.id.tv_product_subtitle);
            tvPrice = v.findViewById(R.id.tv_selling_price);
            tvMrp = v.findViewById(R.id.tv_mrp);
            tvPackName = v.findViewById(R.id.tv_pack_name);
            btnAddToCart = v.findViewById(R.id.btn_add_to_cart);
            layoutQty = v.findViewById(R.id.layout_qty);
            tvQtyCount = v.findViewById(R.id.tv_qty_count);
            btnMinus = v.findViewById(R.id.btn_minus);
            btnPlus = v.findViewById(R.id.btn_plus);
        }
    }
}