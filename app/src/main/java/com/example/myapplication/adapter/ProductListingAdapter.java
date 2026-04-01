package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.ProductDetailScreen;
import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.AddWishlistRequest;
import com.example.myapplication.model.AddWishlistResponse;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.WishlistManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int    TYPE_PRODUCT = 0;
    private static final int    TYPE_LOADING = 1;
    private static final String TAG          = "WishlistDebug";

    public interface BadgeListener {
        void onWishlistCountChanged(int delta);
        void onCartCountChanged(int delta);
    }

    private final Context                 context;
    private final ArrayList<ProductModel> list;
    private       boolean                 isLoadingVisible = false;
    private final ApiService              apiService;
    private final WishlistManager         wishlistManager;
    private       BadgeListener           badgeListener;

    public ProductListingAdapter(Context context, ArrayList<ProductModel> list) {
        this.context        = context;
        this.list           = list;
        this.apiService     = RetrofitClient.getClient(context);
        this.wishlistManager = WishlistManager.getInstance(context);
    }

    public void setBadgeListener(BadgeListener listener) {
        this.badgeListener = listener;
    }

    // ── Loading footer ────────────────────────────────────────────────────────

    public void showLoading() {
        if (!isLoadingVisible) { isLoadingVisible = true;  notifyItemInserted(getItemCount()); }
    }
    public void hideLoading() {
        if (isLoadingVisible)  { isLoadingVisible = false; notifyItemRemoved(list.size()); }
    }

    @Override public int getItemCount() { return list.size() + (isLoadingVisible ? 1 : 0); }
    @Override public int getItemViewType(int pos) {
        return (isLoadingVisible && pos == getItemCount() - 1) ? TYPE_LOADING : TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        if (viewType == TYPE_LOADING)
            return new LoadingViewHolder(inf.inflate(R.layout.item_loading_footer, parent, false));
        return new ProductViewHolder(inf.inflate(R.layout.item_product_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_LOADING) return;

        ProductModel    p = list.get(position);
        ProductViewHolder h = (ProductViewHolder) holder;

        // ── Bind text fields ──────────────────────────────────────────────────
        h.tvName.setText(p.getName()         != null ? p.getName()         : "");
        h.tvSubtitle.setText(p.getCategoryName() != null ? p.getCategoryName() : "");
        h.tvPrice.setText("₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMrp.setText("₹"   + (p.getMrp()          != null ? p.getMrp()          : "0"));
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        h.tvDiscount.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Off");
        h.tvMoq.setText("MRP : ₹" + p.getMrp() + " | MOQ : " + p.getMoq());
        h.tvBuyPrice.setText("Buy for ₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMargin.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Margin");

        // ── Image ─────────────────────────────────────────────────────────────
        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(h.ivImage);

        // ── Wishlist state ────────────────────────────────────────────────────
        boolean isWishlisted = wishlistManager.isWishlisted(p.getItemId());
        p.setWishlisted(isWishlisted);
        p.setWishlistId(wishlistManager.getWishlistId(p.getItemId()));
        updateWishlistIcon(h, isWishlisted);

        // ── Cart state ────────────────────────────────────────────────────────
        String  savedQty = p.getCartQty();
        boolean inCart   = !isEmpty(savedQty) && !savedQty.equals("0");
        if (inCart) {
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText(savedQty);
        } else {
            h.btnAddToCart.setVisibility(View.VISIBLE);
            h.layoutQty.setVisibility(View.GONE);
            h.tvQtyCount.setText("1");
        }

        // ── Cart listeners ────────────────────────────────────────────────────
        h.btnAddToCart.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText("1");
            callAddCartApi(item, 1, h, 0);
        });

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            int newQty = safeInt(h.tvQtyCount.getText().toString()) + 1;
            h.tvQtyCount.setText(String.valueOf(newQty));
            callAddCartApi(item, newQty, h, +1);
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            int current = safeInt(h.tvQtyCount.getText().toString());
            if (current > 1) {
                int newQty = current - 1;
                h.tvQtyCount.setText(String.valueOf(newQty));
                callAddCartApi(item, newQty, h, -1);
            } else {
                h.layoutQty.setVisibility(View.GONE);
                h.btnAddToCart.setVisibility(View.VISIBLE);
                item.setCartQty("0");
                callAddCartApi(item, 0, h, -1);
            }
        });

        // ── Item click ────────────────────────────────────────────────────────
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailScreen.class);
            intent.putExtra("c_random", p.getRandom());
            context.startActivity(intent);
        });

        // ── Wishlist toggle ───────────────────────────────────────────────────
        h.ivWishlist.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            // ✅ Use ACTUAL product fields — NOT hardcoded values
            String category = item.getCategory();   // n_category from API e.g. "1"
            String product  = item.getItemId();      // id from API e.g. "6"
            String pack     = item.getPackId();      // n_pack_id from API e.g. "5"
            String userId   = "10";                  // TODO: replace with TokenManager.getUserId()

            Log.d(TAG, "Wishlist click → category=" + category
                    + " product=" + product + " pack=" + pack);

            if (isEmpty(category) || isEmpty(product) || isEmpty(pack)) {
                Toast.makeText(context, "Missing product info", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "MISSING: category=" + category
                        + " product=" + product + " pack=" + pack);
                return;
            }

            if (wishlistManager.isWishlisted(product)) {
                removeFromWishlist(item, h);
            } else {
                // ✅ Pass actual values — category, product, pack, userId
                addToWishlist(category, product, pack, userId, item, h);
            }
        });
    }

    // ── Wishlist: Add ─────────────────────────────────────────────────────────

    private void addToWishlist(String category, String product, String pack,
                               String userId, ProductModel item, ProductViewHolder holder) {

        Log.d(TAG, "addToWishlist → category=" + category
                + " product=" + product + " pack=" + pack + " user=" + userId);

        AddWishlistRequest request = new AddWishlistRequest(category, product, pack, userId);

        apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {
            @Override
            public void onResponse(Call<AddWishlistResponse> call,
                                   Response<AddWishlistResponse> response) {

                Log.d(TAG, "addWishlist HTTP=" + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "addWishlist failed: HTTP " + response.code());
                    return;
                }

                AddWishlistResponse body = response.body();
                Log.d(TAG, "n_status=" + body.getNStatus()
                        + " n_wishlist_count=" + body.getNWishlistCount());

                if (body.getNStatus() != 1) {
                    Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ API returns n_wishlist_count as the NEW total count,
                //    NOT the individual wishlist ID. We store productId→packId
                //    and use list-wishlist to get the real n_id later.
                //    For now store n_wishlist_count as a reference key.
                String wishlistCount = body.getNWishlistCount(); // e.g. "3"

                item.setWishlisted(true);
                item.setWishlistId(wishlistCount); // temporary; overwritten after list call

                // ✅ Store productId + packId so WishlistScreen can use them for add-to-cart
                wishlistManager.addWishlist(item.getItemId(), wishlistCount, item.getPackId());

                updateWishlistIcon(holder, true);

                if (badgeListener != null) badgeListener.onWishlistCountChanged(+1);

                Toast.makeText(context, "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show();

                // ✅ Fetch actual n_id from list API and update WishlistManager
                fetchAndStoreWishlistId(item, userId);
            }

            @Override
            public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "addWishlist onFailure: " + t.getMessage());
            }
        });
    }

    /**
     * After adding, call list-wishlist to get the real n_id for this product.
     * This n_id is what delete-wishlist needs as "n_wishlist".
     */
    private void fetchAndStoreWishlistId(ProductModel item, String userId) {
        com.example.myapplication.model.ListWishlistRequest req =
                new com.example.myapplication.model.ListWishlistRequest(userId);

        apiService.listWishlist(req).enqueue(
                new Callback<com.example.myapplication.model.ListWishlistResponse>() {
                    @Override
                    public void onResponse(Call<com.example.myapplication.model.ListWishlistResponse> call,
                                           Response<com.example.myapplication.model.ListWishlistResponse> response) {

                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getNStatus() != 1
                                || response.body().getJData() == null) return;

                        for (com.example.myapplication.model.ListWishlistResponse.WishlistItem w
                                : response.body().getJData()) {

                            if (item.getItemId().equals(w.getNProduct())) {
                                // ✅ n_id is what delete-wishlist needs
                                String realWishlistId = w.getNId();
                                item.setWishlistId(realWishlistId);
                                wishlistManager.addWishlist(
                                        item.getItemId(), realWishlistId, item.getPackId());
                                Log.d(TAG, "Stored real wishlistId=" + realWishlistId
                                        + " for product=" + item.getItemId());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.myapplication.model.ListWishlistResponse> call,
                                          Throwable t) {
                        Log.e(TAG, "fetchAndStoreWishlistId failed: " + t.getMessage());
                    }
                });
    }

    // ── Wishlist: Remove ──────────────────────────────────────────────────────

    private void removeFromWishlist(ProductModel item, ProductViewHolder holder) {
        String userId     = "10";
        String wishlistId = wishlistManager.getWishlistId(item.getItemId());

        Log.d(TAG, "removeFromWishlist → wishlistId=" + wishlistId
                + " product=" + item.getItemId());

        if (isEmpty(wishlistId)) {
            Toast.makeText(context, "Cannot remove: ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DeleteWishlistRequest request = new DeleteWishlistRequest(userId, wishlistId);

        apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    item.setWishlisted(false);
                    item.setWishlistId(null);
                    wishlistManager.removeWishlist(item.getItemId());
                    updateWishlistIcon(holder, false);

                    if (badgeListener != null) badgeListener.onWishlistCountChanged(-1);

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

    private void updateWishlistIcon(ProductViewHolder h, boolean isWishlisted) {
        h.ivWishlist.setImageResource(isWishlisted
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);
    }

    // ── Cart API ──────────────────────────────────────────────────────────────

    private void callAddCartApi(ProductModel item, int quantity,
                                ProductViewHolder holder, int cartDelta) {
        String category = item.getCategory();
        String packId   = item.getPackId();
        String itemId   = item.getItemId();

        if (isEmpty(category)) { showMissingToast("category", holder, item); return; }
        if (isEmpty(packId))   { showMissingToast("pack ID",  holder, item); return; }
        if (isEmpty(itemId))   { showMissingToast("item ID",  holder, item); return; }

        final int deltaToFire = (cartDelta == 0 && quantity == 1) ? +1 : cartDelta;

        AddCartRequest request = new AddCartRequest(
                category, packId, itemId, String.valueOf(quantity), "10");

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse body = response.body();
                    if (body.getStatus() == 1) {
                        item.setCartQty(String.valueOf(quantity));
                        if (badgeListener != null && deltaToFire != 0)
                            badgeListener.onCartCountChanged(deltaToFire);
                        Toast.makeText(context,
                                quantity > 0 ? "Added to cart ✓" : "Removed from cart",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                body.getMessage() != null ? body.getMessage() : "Failed",
                                Toast.LENGTH_LONG).show();
                        resetToAddButton(holder, item);
                    }
                } else {
                    Toast.makeText(context, "Request failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    resetToAddButton(holder, item);
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                resetToAddButton(holder, item);
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showMissingToast(String field, ProductViewHolder h, ProductModel item) {
        Toast.makeText(context, "Missing: " + field, Toast.LENGTH_LONG).show();
        resetToAddButton(h, item);
    }

    private void resetToAddButton(ProductViewHolder h, ProductModel item) {
        if (h == null) return;
        h.layoutQty.setVisibility(View.GONE);
        h.btnAddToCart.setVisibility(View.VISIBLE);
        item.setCartQty("0");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    private int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 1; }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView    ivImage, ivWishlist;
        TextView     tvName, tvSubtitle, tvPrice, tvMrp, tvDiscount;
        TextView     tvMoq, tvBuyPrice, tvMargin, tvQtyCount;
        Button       btnAddToCart;
        LinearLayout layoutQty;
        TextView     btnMinus, btnPlus;

        ProductViewHolder(@NonNull View v) {
            super(v);
            ivImage      = v.findViewById(R.id.iv_product_image);
            ivWishlist   = v.findViewById(R.id.iv_wishlist);
            tvName       = v.findViewById(R.id.tv_product_name);
            tvSubtitle   = v.findViewById(R.id.tv_product_subtitle);
            tvPrice      = v.findViewById(R.id.tv_selling_price);
            tvMrp        = v.findViewById(R.id.tv_mrp);
            tvDiscount   = v.findViewById(R.id.tv_discount);
            tvMoq        = v.findViewById(R.id.tv_moq);
            tvBuyPrice   = v.findViewById(R.id.tv_buy_price);
            tvMargin     = v.findViewById(R.id.tv_margin);
            btnAddToCart = v.findViewById(R.id.btn_add_to_cart);
            layoutQty    = v.findViewById(R.id.layout_qty);
            btnMinus     = v.findViewById(R.id.btn_minus);
            btnPlus      = v.findViewById(R.id.btn_plus);
            tvQtyCount   = v.findViewById(R.id.tv_qty_count);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        LoadingViewHolder(@NonNull View v) {
            super(v);
            progressBar = v.findViewById(R.id.progress_bar);
        }
    }
}