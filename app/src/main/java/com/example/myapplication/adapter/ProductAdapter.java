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

    private static final String TAG = "ProductAdapter";

    private final Context         context;
    private final ArrayList<ProductModel> list;
    private final WishlistManager wishlistManager;
    private final ApiService      apiService;
    private final TokenManager    tokenManager;

    public ProductAdapter(Context context, ArrayList<ProductModel> list, TokenManager tokenManager) {
        this.context         = context;
        this.list            = list;
        this.wishlistManager = WishlistManager.getInstance(context);
        this.apiService      = RetrofitClient.getClient(context);
        this.tokenManager    = tokenManager;
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
        h.tvName.setText(p.getName()         != null ? p.getName()         : "");
        h.tvSubtitle.setText(p.getCategoryName() != null ? p.getCategoryName() : "");
        h.tvPrice.setText("₹" + (p.getSellingPrice() != null ? p.getSellingPrice() : "0"));
        h.tvMrp.setText("₹"   + (p.getMrp()          != null ? p.getMrp()          : "0"));
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        h.tvDiscount.setText((p.getDiscount() != null ? p.getDiscount() : "0") + "% Off");
        h.tvMoq.setText("MOQ: " + p.getMoq());

        // ─── IMAGE ───
        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.logo)
                .error(R.drawable.arrivalimgg)
                .into(h.ivImage);

        // ─── WISHLIST STATE ───
        boolean isWishlisted = wishlistManager.isWishlisted(p.getItemId());
        p.setWishlisted(isWishlisted);
        p.setWishlistId(wishlistManager.getWishlistId(p.getItemId()));
        updateWishlistIcon(h, isWishlisted);

        // ─── WISHLIST CLICK ───
        h.ivWishlist.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            String category = item.getCategory();
            String product  = item.getItemId();
            String pack     = item.getPackId();

            Log.d(TAG, "Wishlist click → category=" + category
                    + " product=" + product + " pack=" + pack);

            if (isEmpty(category) || isEmpty(product) || isEmpty(pack)) {
                Toast.makeText(context, "Missing product info", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "MISSING → category=" + category
                        + " product=" + product + " pack=" + pack);
                return;
            }

            if (wishlistManager.isWishlisted(product)) {
                removeWishlist(item, h);
            } else {
                addWishlist(item, h);
            }
        });

        // ─── CART STATE ───
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

        // ─── ADD TO CART ───
        h.btnAddToCart.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText("1");
            callAddCartApi(item, 1, h);
        });

        // ─── PLUS ───
        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            int newQty = safeInt(h.tvQtyCount.getText().toString()) + 1;
            h.tvQtyCount.setText(String.valueOf(newQty));
            callAddCartApi(item, newQty, h);
        });

        // ─── MINUS ───
        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            ProductModel item = list.get(pos);

            int current = safeInt(h.tvQtyCount.getText().toString());
            if (current > 1) {
                int newQty = current - 1;
                h.tvQtyCount.setText(String.valueOf(newQty));
                callAddCartApi(item, newQty, h);
            } else {
                h.layoutQty.setVisibility(View.GONE);
                h.btnAddToCart.setVisibility(View.VISIBLE);
                item.setCartQty("0");
                callAddCartApi(item, 0, h);
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

    private void addWishlist(ProductModel item, ViewHolder h) {
        String userId = tokenManager.getUserId();

        if (isEmpty(userId)) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        h.ivWishlist.setEnabled(false);

        AddWishlistRequest request = new AddWishlistRequest(
                item.getCategory(),
                item.getItemId(),
                item.getPackId(),
                userId
        );

        apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {
            @Override
            public void onResponse(Call<AddWishlistResponse> call,
                                   Response<AddWishlistResponse> response) {

                h.ivWishlist.setEnabled(true);

                Log.d(TAG, "addWishlist HTTP=" + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                AddWishlistResponse body = response.body();
                Log.d(TAG, "n_status=" + body.getNStatus()
                        + " n_wishlist_count=" + body.getNWishlistCount());

                if (body.getNStatus() != 1) {
                    Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tempId = body.getNWishlistCount();

                item.setWishlisted(true);
                item.setWishlistId(tempId);

                wishlistManager.addWishlist(item.getItemId(), tempId, item.getPackId());

                updateWishlistIcon(h, true);
                notifyItemChanged(h.getAdapterPosition());

                Toast.makeText(context, "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show();

                // ✅ Real n_id fetch பண்ணி store பண்ணு (delete-க்கு தேவை)
                fetchAndStoreWishlistId(item, userId);
            }

            @Override
            public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                h.ivWishlist.setEnabled(true);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "addWishlist onFailure: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // FETCH REAL WISHLIST ID (n_id) AFTER ADD
    // ─────────────────────────────────────────────

    private void fetchAndStoreWishlistId(ProductModel item, String userId) {
        ListWishlistRequest req = new ListWishlistRequest(userId);

        apiService.listWishlist(req).enqueue(new Callback<ListWishlistResponse>() {
            @Override
            public void onResponse(Call<ListWishlistResponse> call,
                                   Response<ListWishlistResponse> response) {

                if (!response.isSuccessful() || response.body() == null
                        || response.body().getNStatus() != 1
                        || response.body().getJData() == null) return;

                for (ListWishlistResponse.WishlistItem w : response.body().getJData()) {
                    if (item.getItemId().equals(w.getNProduct())) {
                        String realWishlistId = w.getNId(); // ✅ real n_id
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
            public void onFailure(Call<ListWishlistResponse> call, Throwable t) {
                Log.e(TAG, "fetchAndStoreWishlistId failed: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // REMOVE WISHLIST
    // ─────────────────────────────────────────────

    private void removeWishlist(ProductModel item, ViewHolder h) {
        String userId     = tokenManager.getUserId();
        String wishlistId = wishlistManager.getWishlistId(item.getItemId());

        Log.d(TAG, "removeWishlist → wishlistId=" + wishlistId
                + " product=" + item.getItemId());

        if (isEmpty(userId)) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmpty(wishlistId)) {
            Toast.makeText(context, "Cannot remove: ID not found", Toast.LENGTH_SHORT).show();
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

                    item.setWishlisted(false);
                    item.setWishlistId(null);
                    wishlistManager.removeWishlist(item.getItemId());

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
                Log.e(TAG, "removeWishlist onFailure: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // CART API
    // ─────────────────────────────────────────────

    private void callAddCartApi(ProductModel item, int quantity, ViewHolder h) {
        String userId   = tokenManager.getUserId();
        String category = item.getCategory();
        String packId   = item.getPackId();
        String itemId   = item.getItemId();

        if (isEmpty(category) || isEmpty(packId) || isEmpty(itemId)) {
            Toast.makeText(context, "Missing product info", Toast.LENGTH_SHORT).show();
            resetToAddButton(h, item);
            return;
        }

        if (isEmpty(userId)) {
            Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show();
            resetToAddButton(h, item);
            return;
        }

        AddCartRequest request = new AddCartRequest(
                category,
                packId,
                itemId,
                String.valueOf(quantity),
                userId
        );

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse body = response.body();
                    if (body.getStatus() == 1) {
                        item.setCartQty(String.valueOf(quantity));
                        Toast.makeText(context,
                                quantity > 0 ? "Added to cart ✓" : "Removed from cart",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                body.getMessage() != null ? body.getMessage() : "Cart failed",
                                Toast.LENGTH_SHORT).show();
                        resetToAddButton(h, item);
                    }
                } else {
                    Toast.makeText(context,
                            "Request failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    resetToAddButton(h, item);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                resetToAddButton(h, item);
                Log.e(TAG, "callAddCartApi onFailure: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private void resetToAddButton(ViewHolder h, ProductModel item) {
        if (h == null) return;
        h.layoutQty.setVisibility(View.GONE);
        h.btnAddToCart.setVisibility(View.VISIBLE);
        item.setCartQty("0");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    private int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); }
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



    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView    ivImage, ivWishlist;
        TextView     tvName, tvSubtitle, tvPrice, tvMrp, tvDiscount, tvMoq;
        Button       btnAddToCart;
        LinearLayout layoutQty;
        TextView     btnMinus, btnPlus, tvQtyCount;

        public ViewHolder(@NonNull View v) {
            super(v);
            ivImage      = v.findViewById(R.id.iv_product_image);
            ivWishlist   = v.findViewById(R.id.iv_wishlist);
            tvName       = v.findViewById(R.id.tv_product_name);
            tvSubtitle   = v.findViewById(R.id.tv_product_subtitle);
            tvPrice      = v.findViewById(R.id.tv_selling_price);
            tvMrp        = v.findViewById(R.id.tv_mrp);
            tvDiscount   = v.findViewById(R.id.tv_discount);
            tvMoq        = v.findViewById(R.id.tv_moq);
            btnAddToCart = v.findViewById(R.id.btn_add_to_cart);
            layoutQty    = v.findViewById(R.id.layout_qty);
            btnMinus     = v.findViewById(R.id.btn_minus);
            btnPlus      = v.findViewById(R.id.btn_plus);
            tvQtyCount   = v.findViewById(R.id.tv_qty_count);
        }
    }
}