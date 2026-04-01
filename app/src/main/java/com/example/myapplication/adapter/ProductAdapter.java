package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.ProductDetailScreen;
import com.example.myapplication.model.AddWishlistRequest;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.model.ProductModel;
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
        h.btnBuy.setText("Buy ₹" + p.getSellingPrice());

        // ─── IMAGE ───
        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.logo)
                .error(R.drawable.arrivalimgg)
                .into(h.ivImage);

        // ─── WISHLIST STATE ───
        boolean isWishlisted = wishlistManager.isWishlisted(p.getItemId());
        updateWishlistIcon(h, isWishlisted);

        // ─── CLICK WISHLIST ───
        h.ivWishlist.setOnClickListener(v -> {

            boolean currentState = wishlistManager.isWishlisted(p.getItemId());

            if (!currentState) {
                addWishlist(p, h);
            } else {
                removeWishlist(p, h);
            }
        });

        // ─── CLICK ITEM → DETAIL ───
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

        Log.e("API_CALL", "Add Wishlist Called");

        apiService.addWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                h.ivWishlist.setEnabled(true);

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    String wishlistId = null;

                    try {
                        wishlistId = response.body().getWishlistId();
                    } catch (Exception e) {
                        wishlistId = p.getItemId();
                    }

                    if (wishlistId == null || wishlistId.isEmpty()) {
                        wishlistId = p.getItemId();
                    }

                    wishlistManager.addWishlist(p.getItemId(), wishlistId, p.getPackId());

                    updateWishlistIcon(h, true);

                    int pos = h.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        notifyItemChanged(pos);
                    }

                    Toast.makeText(context, "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Failed to add wishlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                h.ivWishlist.setEnabled(true);
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

        if (wishlistId == null) {
            wishlistManager.removeWishlist(p.getItemId());
            updateWishlistIcon(h, false);
            notifyItemChanged(h.getAdapterPosition());
            return;
        }

        h.ivWishlist.setEnabled(false);

        DeleteWishlistRequest request = new DeleteWishlistRequest(userId, wishlistId);

        apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                h.ivWishlist.setEnabled(true);

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    wishlistManager.removeWishlist(p.getItemId());

                    updateWishlistIcon(h, false);
                    notifyItemChanged(h.getAdapterPosition());

                    Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                h.ivWishlist.setEnabled(true);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // ICON UPDATE
    // ─────────────────────────────────────────────

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
        Button btnBuy;

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

            btnBuy = v.findViewById(R.id.btn_buy);
        }
    }
}