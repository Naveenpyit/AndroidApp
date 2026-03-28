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
    private static final String TAG          = "CartDebug";

    // ── Callback interface so the Activity can update its badge TextViews ──
    public interface BadgeListener {
        void onWishlistCountChanged(int delta);   // +1 added, -1 removed
        void onCartCountChanged(int delta);        // +qty added, -qty removed
    }

    private final Context                 context;
    private final ArrayList<ProductModel> list;
    private       boolean                 isLoadingVisible = false;
    private final ApiService              apiService;
    private final WishlistManager         wishlistManager;
    private       BadgeListener           badgeListener;   // ← NEW

    public ProductListingAdapter(Context context, ArrayList<ProductModel> list) {
        this.context         = context;
        this.list            = list;
        this.apiService      = RetrofitClient.getClient(context);
        this.wishlistManager = WishlistManager.getInstance(context);
    }

    /** Call this from the Activity after creating the adapter. */
    public void setBadgeListener(BadgeListener listener) {
        this.badgeListener = listener;
    }

    // ── Loading footer helpers ────────────────────────────────────────────────

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

        ProductModel p  = list.get(position);
        ProductViewHolder h = (ProductViewHolder) holder;

        h.tvName.setText(p.getName()         != null ? p.getName()         : "");
        h.tvSubtitle.setText(p.getCategoryName() != null ? p.getCategoryName() : "");
        h.tvPrice.setText("₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMrp.setText("₹"   + (p.getMrp()          != null ? p.getMrp()          : "0"));
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        h.tvDiscount.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Off");
        h.tvMoq.setText("MRP : ₹" + p.getMrp() + " | MOQ : " + p.getMoq());
        h.tvBuyPrice.setText("Buy for ₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMargin.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Margin");

        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(h.ivImage);

        // Sync wishlist state from persistent manager
        boolean isWishlisted    = wishlistManager.isWishlisted(p.getItemId());
        String  storedWishlistId = wishlistManager.getWishlistId(p.getItemId());
        p.setWishlisted(isWishlisted);
        p.setWishlistId(storedWishlistId);
        updateWishlistIcon(h, isWishlisted);

        // Restore cart state
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

        // ── Add to cart ──────────────────────────────────────────────────────
        h.btnAddToCart.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText("1");
            callAddCartApi(item, 1, h, 0);   // delta = 0 first; API callback fires it
        });

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            int newQty = safeInt(h.tvQtyCount.getText().toString()) + 1;
            h.tvQtyCount.setText(String.valueOf(newQty));
            callAddCartApi(item, newQty, h, +1);   // added 1 unit
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);
            int current = safeInt(h.tvQtyCount.getText().toString());
            if (current > 1) {
                int newQty = current - 1;
                h.tvQtyCount.setText(String.valueOf(newQty));
                callAddCartApi(item, newQty, h, -1);   // removed 1 unit
            } else {
                h.layoutQty.setVisibility(View.GONE);
                h.btnAddToCart.setVisibility(View.VISIBLE);
                item.setCartQty("0");
                callAddCartApi(item, 0, h, -1);   // removed last unit
            }
        });

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailScreen.class);
            intent.putExtra("c_random", p.getRandom());
            context.startActivity(intent);
        });

        // ── Wishlist toggle ──────────────────────────────────────────────────
        h.ivWishlist.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            String category = item.getCategory();
            String product  = item.getItemId();
            String pack     = item.getPackId();
            String userId   = "10";

            if (isEmpty(category) || isEmpty(product) || isEmpty(pack)) {
                Toast.makeText(context, "Missing product info", Toast.LENGTH_SHORT).show();
                return;
            }

            if (wishlistManager.isWishlisted(product)) {
                removeFromWishlist(item, h);
            } else {
                addToWishlist(category, product, pack, userId, item, h);
            }
        });
    }

    // ── Wishlist helpers ─────────────────────────────────────────────────────

    private void updateWishlistIcon(ProductViewHolder h, boolean isWishlisted) {
        h.ivWishlist.setImageResource(isWishlisted
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);
    }

    private void addToWishlist(String category, String product, String pack,
                               String userId, ProductModel item, ProductViewHolder holder) {
        AddWishlistRequest request = new AddWishlistRequest(category, product, pack, userId);

        apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {
            @Override
            public void onResponse(Call<AddWishlistResponse> call, Response<AddWishlistResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getNStatus() == 1) {

                    item.setWishlisted(true);
                    String wishlistId = response.body().getNWishlistCount();
                    item.setWishlistId(wishlistId);
                    wishlistManager.addWishlist(item.getItemId(), wishlistId);

                    updateWishlistIcon(holder, true);

                    // ── Notify Activity: +1 wishlist ──────────────────────
                    if (badgeListener != null) badgeListener.onWishlistCountChanged(+1);

                    Toast.makeText(context, "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromWishlist(ProductModel item, ProductViewHolder holder) {
        String userId     = "10";
        String wishlistId = wishlistManager.getWishlistId(item.getItemId());

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
                    wishlistManager.removeWishlist(item.getItemId());

                    updateWishlistIcon(holder, false);

                    // ── Notify Activity: -1 wishlist ──────────────────────
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

    // ── Cart helpers ─────────────────────────────────────────────────────────

    /**
     * @param cartDelta  Pass +1 when user taps '+', -1 when taps '-', 0 for first "Add to Cart"
     *                   (the API success callback fires the actual +1 for first-add).
     */
    private void callAddCartApi(ProductModel item, int quantity,
                                ProductViewHolder holder, int cartDelta) {
        String category = item.getCategory();
        String packId   = item.getPackId();
        String itemId   = item.getItemId();

        Log.d(TAG, "━━━━ ADD TO CART ━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "name     = " + item.getName());
        Log.d(TAG, "category = " + category);
        Log.d(TAG, "packId   = " + packId);
        Log.d(TAG, "itemId   = " + itemId);
        Log.d(TAG, "qty      = " + quantity);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (isEmpty(category)) { showMissingToast("category", holder, item); return; }
        if (isEmpty(packId))   { showMissingToast("pack ID",  holder, item); return; }
        if (isEmpty(itemId))   { showMissingToast("item ID",  holder, item); return; }

        // For first "Add to Cart" (quantity==1, cartDelta==0) we send +1 after API confirms.
        final int deltaToFire = (cartDelta == 0 && quantity == 1) ? +1 : cartDelta;

        AddCartRequest request = new AddCartRequest(
                category, packId, itemId, String.valueOf(quantity), "10");

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                Log.d(TAG, "HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse body = response.body();
                    Log.d(TAG, "status=" + body.getStatus() + " | " + body.getMessage());

                    if (body.getStatus() == 1) {
                        item.setCartQty(String.valueOf(quantity));

                        // ── Notify Activity: cart count changed ───────────
                        if (badgeListener != null && deltaToFire != 0) {
                            badgeListener.onCartCountChanged(deltaToFire);
                        }

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
                    try {
                        String err = response.errorBody() != null
                                ? response.errorBody().string() : "unknown";
                        Log.e(TAG, "HTTP error " + response.code() + ": " + err);
                    } catch (Exception ignored) {}
                    Toast.makeText(context, "Request failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    resetToAddButton(holder, item);
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                resetToAddButton(holder, item);
            }
        });
    }

    private void showMissingToast(String field, ProductViewHolder h, ProductModel item) {
        Log.e(TAG, "FAIL: " + field + " is null");
        Toast.makeText(context, "Missing: " + field, Toast.LENGTH_LONG).show();
        resetToAddButton(h, item);
    }

    private void resetToAddButton(ProductViewHolder h, ProductModel item) {
        if (h == null) return;
        h.layoutQty.setVisibility(View.GONE);
        h.btnAddToCart.setVisibility(View.VISIBLE);
        item.setCartQty("0");
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    private int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 1; }
    }

    // ── ViewHolders ──────────────────────────────────────────────────────────

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