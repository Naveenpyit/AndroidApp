package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.ProductDetailScreen;
import com.example.myapplication.model.*;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.WishlistManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ProductModel> list;
    private final WishlistManager wishlistManager;
    private final ApiService apiService;
    private final TokenManager tokenManager;

    public ProductAdapter(Context context, ArrayList<ProductModel> list) {
        this.context = context;
        this.list = list;
        this.wishlistManager = WishlistManager.getInstance(context);
        this.apiService = RetrofitClient.getClient(context);
        this.tokenManager = new TokenManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_product_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        ProductModel p = list.get(position);

        // ─── TEXT ───
        h.tvName.setText(p.getName());
        h.tvSubtitle.setText(p.getCategoryName());

        h.tvPrice.setText("₹" + p.getSellingPrice());
        h.tvMrp.setText("₹" + p.getMrp());
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        h.tvDiscount.setText(p.getDiscount() + "% Off");
        h.tvMoq.setText("MOQ: " + p.getMoq());

        // ─── IMAGE ───
        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.logo)
                .error(R.drawable.arrivalimgg)
                .into(h.ivImage);

        // ─── WISHLIST ───
        boolean isWishlisted = wishlistManager.isWishlisted(p.getItemId());
        updateWishlistIcon(h, isWishlisted);

        h.ivWishlist.setOnClickListener(v -> {
            if (!wishlistManager.isWishlisted(p.getItemId())) {
                addWishlist(p, h);
            } else {
                removeWishlist(p, h);
            }
        });

        // ─── CART STATE ───
        String savedQty = p.getCartQty();
        boolean inCart = savedQty != null && !savedQty.equals("0");

        if (inCart) {
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText(savedQty);
        } else {
            h.btnAddToCart.setVisibility(View.VISIBLE);
            h.layoutQty.setVisibility(View.GONE);
            h.tvQtyCount.setText("1");
        }

        // ─── ADD TO CART ───
        h.btnAddToCart.setOnClickListener(v -> {
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText("1");

            callAddCartApi(p, 1, h);
        });

        // ─── PLUS ───
        h.btnPlus.setOnClickListener(v -> {
            int qty = safeInt(h.tvQtyCount.getText().toString()) + 1;
            h.tvQtyCount.setText(String.valueOf(qty));
            callAddCartApi(p, qty, h);
        });

        // ─── MINUS ───
        h.btnMinus.setOnClickListener(v -> {
            int qty = safeInt(h.tvQtyCount.getText().toString());

            if (qty > 1) {
                qty--;
                h.tvQtyCount.setText(String.valueOf(qty));
                callAddCartApi(p, qty, h);
            } else {
                h.layoutQty.setVisibility(View.GONE);
                h.btnAddToCart.setVisibility(View.VISIBLE);
                callAddCartApi(p, 0, h);
            }
        });

        // ─── ITEM CLICK ───
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProductDetailScreen.class);
            i.putExtra("c_random", p.getRandom());
            context.startActivity(i);
        });
    }

    // ─────────────────────────────────────────────
    // ADD WISHLIST
    // ─────────────────────────────────────────────

    private void addWishlist(ProductModel p, ViewHolder h) {

        String userId = tokenManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        h.ivWishlist.setEnabled(false);

        AddWishlistRequest request = new AddWishlistRequest(
                p.getCategory(),
                p.getItemId(),
                p.getPackId(),
                userId
        );

        apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {
            @Override
            public void onResponse(Call<AddWishlistResponse> call,
                                   Response<AddWishlistResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().getNStatus() == 1) {

                    String tempId = response.body().getNWishlistCount();

                    wishlistManager.addWishlist(
                            p.getItemId(),
                            tempId,
                            p.getPackId()
                    );

                    updateWishlistIcon(h, true);
                    notifyItemChanged(h.getAdapterPosition());

                    Toast.makeText(context,
                            "Added to Wishlist ❤️",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // REMOVE WISHLIST
    // ─────────────────────────────────────────────

    private void removeWishlist(ProductModel p, ViewHolder h) {

        String userId = tokenManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        String wishlistId = wishlistManager.getWishlistId(p.getItemId());

        DeleteWishlistRequest request = new DeleteWishlistRequest(userId, wishlistId);

        apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    wishlistManager.removeWishlist(p.getItemId());

                    updateWishlistIcon(h, false);
                    notifyItemChanged(h.getAdapterPosition());

                    Toast.makeText(context,
                            "Removed from Wishlist",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // CART API
    // ─────────────────────────────────────────────

    private void callAddCartApi(ProductModel item, int quantity, ViewHolder h) {

        String userId = tokenManager.getUserId();

        AddCartRequest request = new AddCartRequest(
                item.getCategory(),
                item.getPackId(),
                item.getItemId(),
                String.valueOf(quantity),
                userId
        );

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    item.setCartQty(String.valueOf(quantity));

                } else {
                    Toast.makeText(context, "Cart failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private int safeInt(String v) {
        try { return Integer.parseInt(v); }
        catch (Exception e) { return 1; }
    }

    private void updateWishlistIcon(ViewHolder h, boolean state) {
        h.ivWishlist.setImageResource(
                state ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ─────────────────────────────────────────────
    // VIEW HOLDER
    // ─────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage, ivWishlist;
        TextView tvName, tvSubtitle, tvPrice, tvMrp, tvDiscount, tvMoq;

        Button btnAddToCart;
        LinearLayout layoutQty;
        TextView btnMinus, btnPlus, tvQtyCount;

        public ViewHolder(@NonNull View v) {
            super(v);

            ivImage = v.findViewById(R.id.iv_product_image);
            ivWishlist = v.findViewById(R.id.iv_wishlist);

            tvName = v.findViewById(R.id.tv_product_name);
            tvSubtitle = v.findViewById(R.id.tv_product_subtitle);
            tvPrice = v.findViewById(R.id.tv_selling_price);
            tvMrp = v.findViewById(R.id.tv_mrp);
            tvDiscount = v.findViewById(R.id.tv_discount);
            tvMoq = v.findViewById(R.id.tv_moq);

            btnAddToCart = v.findViewById(R.id.btn_add_to_cart);
            layoutQty = v.findViewById(R.id.layout_qty);
            btnMinus = v.findViewById(R.id.btn_minus);
            btnPlus = v.findViewById(R.id.btn_plus);
            tvQtyCount = v.findViewById(R.id.tv_qty_count);
        }
    }
}