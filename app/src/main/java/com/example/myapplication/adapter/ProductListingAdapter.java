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

public class ProductListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PRODUCT = 0;
    private static final int TYPE_LOADING = 1;

    public interface BadgeListener {
        void onWishlistCountChanged(int delta);
        void onCartCountChanged(int delta);
    }

    private Context context;
    private ArrayList<ProductModel> list;
    private boolean isLoadingVisible = false;

    private ApiService apiService;
    private WishlistManager wishlistManager;
    private TokenManager tokenManager;
    private BadgeListener badgeListener;

    public ProductListingAdapter(Context context, ArrayList<ProductModel> list) {
        this.context = context;
        this.list = list;
        this.apiService = RetrofitClient.getClient(context);
        this.wishlistManager = WishlistManager.getInstance(context);
        this.tokenManager = new TokenManager(context);
    }

    public void setBadgeListener(BadgeListener listener) {
        this.badgeListener = listener;
    }

    // ───────────── Loading ─────────────

    public void showLoading() {
        if (!isLoadingVisible) {
            isLoadingVisible = true;
            notifyItemInserted(getItemCount());
        }
    }

    public void hideLoading() {
        if (isLoadingVisible) {
            isLoadingVisible = false;
            notifyItemRemoved(list.size());
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + (isLoadingVisible ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (isLoadingVisible && position == getItemCount() - 1)
                ? TYPE_LOADING : TYPE_PRODUCT;
    }

    // ───────────── ViewHolder ─────────────

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_loading_footer, parent, false));
        }

        return new ProductViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_product_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_LOADING) return;

        ProductViewHolder h = (ProductViewHolder) holder;
        ProductModel p = list.get(position);

        h.tvName.setText(p.getName());
        h.tvSubtitle.setText(p.getCategoryName());
        h.tvPrice.setText("₹" + p.getSellingPrice());

        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(h.ivImage);

        // ───── Wishlist ─────
        boolean isWishlisted = wishlistManager.isWishlisted(p.getItemId());
        updateWishlistIcon(h, isWishlisted);

        h.ivWishlist.setOnClickListener(v -> {

            String userId = tokenManager.getUserId();
            if (userId == null) {
                Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isWishlisted) {

                AddWishlistRequest req = new AddWishlistRequest(
                        p.getCategory(), p.getItemId(), p.getPackId(), userId
                );

                apiService.addWishlist(req).enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> res) {

                        if (res.isSuccessful() && res.body() != null && res.body().getStatus() == 1) {

                            wishlistManager.addWishlist(p.getItemId(), p.getItemId(), p.getPackId());

                            if (badgeListener != null)
                                badgeListener.onWishlistCountChanged(+1);

                            updateWishlistIcon(h, true);

                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {}
                });

            } else {

                String wishlistId = wishlistManager.getWishlistId(p.getItemId());

                if (wishlistId == null) return;

                DeleteWishlistRequest req = new DeleteWishlistRequest(userId, wishlistId);

                apiService.deleteWishlist(req).enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> res) {

                        if (res.isSuccessful() && res.body() != null && res.body().getStatus() == 1) {

                            wishlistManager.removeWishlist(p.getItemId());

                            if (badgeListener != null)
                                badgeListener.onWishlistCountChanged(-1);

                            updateWishlistIcon(h, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {}
                });
            }
        });

        // Click → Detail
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProductDetailScreen.class);
            i.putExtra("c_random", p.getRandom());
            context.startActivity(i);
        });
    }

    private void updateWishlistIcon(ProductViewHolder h, boolean state) {
        h.ivWishlist.setImageResource(
                state ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );
    }

    // ───────────── ViewHolders ─────────────

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage, ivWishlist;
        TextView tvName, tvSubtitle, tvPrice;

        public ProductViewHolder(@NonNull View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_product_image);
            ivWishlist = v.findViewById(R.id.iv_wishlist);
            tvName = v.findViewById(R.id.tv_product_name);
            tvSubtitle = v.findViewById(R.id.tv_product_subtitle);
            tvPrice = v.findViewById(R.id.tv_selling_price);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View v) {
            super(v);
            progressBar = v.findViewById(R.id.progress_bar);
        }
    }
}