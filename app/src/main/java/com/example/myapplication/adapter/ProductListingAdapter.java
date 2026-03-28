package com.example.myapplication.adapter;

import android.content.Context;
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
import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PRODUCT = 0;
    private static final int TYPE_LOADING = 1;
    private static final String TAG = "ProductAdapter";

    private final Context context;
    private final ArrayList<ProductModel> list;
    private boolean isLoadingVisible = false;
    private ApiService apiService;

    public ProductListingAdapter(Context context, ArrayList<ProductModel> list) {
        this.context = context;
        this.list = list;
        this.apiService = RetrofitClient.getClient(context);
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        if (viewType == TYPE_LOADING) {
            return new LoadingViewHolder(inf.inflate(R.layout.item_loading_footer, parent, false));
        }
        return new ProductViewHolder(inf.inflate(R.layout.item_product_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_LOADING) return;

        ProductModel p = list.get(position);
        ProductViewHolder h = (ProductViewHolder) holder;

        // ── Bind Data ──
        h.tvName.setText(p.getName());
        h.tvSubtitle.setText(p.getCategoryName());
        h.tvPrice.setText("₹" + p.getSellingPrice());
        h.tvMrp.setText("₹" + p.getMrp());
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        h.tvDiscount.setText(p.getDiscount() + "% Off");
        h.tvMoq.setText("MRP : ₹" + p.getMrp() + " | MOQ : " + p.getMoq());
        h.tvBuyPrice.setText("Buy for ₹" + p.getSellingPrice());
        h.tvMargin.setText(p.getDiscount() + "% Margin");

        Glide.with(context)
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(h.ivImage);

        // ── Initial State: Only "Add to Cart" button visible ──
        h.btnAddToCart.setVisibility(View.VISIBLE);
        h.layoutQty.setVisibility(View.GONE);
        h.tvQtyCount.setText("1");

        // ── ADD TO CART Button Click ──
        h.btnAddToCart.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            ProductModel item = list.get(pos);

            // Hide "Add to Cart", Show Qty Selector
            h.btnAddToCart.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);
            h.tvQtyCount.setText("1");

            // Send API with qty = 1
            addToCartAPI(item, 1, h);
        });

        // ── PLUS Button ──
        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            ProductModel item = list.get(pos);
            int currentQty = safeInt(h.tvQtyCount.getText().toString());
            int newQty = currentQty + 1;

            h.tvQtyCount.setText(String.valueOf(newQty));
            item.setCartQty(String.valueOf(newQty));

            // Send API with new qty
            addToCartAPI(item, newQty, h);
        });

        // ── MINUS Button ──
        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            ProductModel item = list.get(pos);
            int currentQty = safeInt(h.tvQtyCount.getText().toString());

            if (currentQty > 1) {
                int newQty = currentQty - 1;
                h.tvQtyCount.setText(String.valueOf(newQty));
                item.setCartQty(String.valueOf(newQty));

                // Send API with new qty
                addToCartAPI(item, newQty, h);
            } else if (currentQty == 1) {
                // Remove from cart
                h.layoutQty.setVisibility(View.GONE);
                h.btnAddToCart.setVisibility(View.VISIBLE);
                item.setCartQty("0");

                // Send API with qty = 0 to remove
                addToCartAPI(item, 0, h);
            }
        });
    }

    private void addToCartAPI(ProductModel item, int quantity, ProductViewHolder holder) {
        // ── DEBUG: Log Request Data ──
        Log.d(TAG, "=== ADD TO CART REQUEST ===");
        Log.d(TAG, "Product: " + item.getName());
        Log.d(TAG, "Category: " + item.getCategory());
        Log.d(TAG, "Pack ID: " + item.getPackId());
        Log.d(TAG, "Item ID: " + item.getItemId());
        Log.d(TAG, "Quantity: " + quantity);
        Log.d(TAG, "User: 10");
        // Prepare request
        AddCartRequest request = new AddCartRequest(
                item.getCategory(),           // n_category
                item.getPackId(),             // n_pack
                item.getItemId(),             // n_product
                String.valueOf(quantity),
                "10"// n_qty
               // userId                    // n_user
        );

        Log.d(TAG, "Request: " + request.toString());

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                Log.d(TAG, "=== API RESPONSE ===");
                Log.d(TAG, "Status Code: " + response.code());
                Log.d(TAG, "Is Successful: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    CommonResponse body = response.body();
                    Log.d(TAG, "Response Body: " + (body != null ? body.toString() : "NULL"));

                    if (body != null) {
                        Log.d(TAG, "Status: " + body.getStatus());
                        Log.d(TAG, "Message: " + body.getMessage());

                        if (body.getStatus() == 1) {
                            Log.d(TAG, "✅ SUCCESS - Item added to cart");
                            item.setCartQty(String.valueOf(quantity));

                            if (quantity > 0) {
                                Toast.makeText(context, quantity + " added to cart ✓", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Removed from cart ✓", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "❌ API Error - Status: " + body.getStatus());
                            Toast.makeText(context, "Error: " + body.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Response body is NULL");
                        Toast.makeText(context, "Invalid response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Request Failed - Code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error details";
                        Log.e(TAG, "Error Body: " + errorBody);
                        Toast.makeText(context, "Failed: Code " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network Error", t);
                Log.e(TAG, "Error Message: " + t.getMessage());
                Log.e(TAG, "Error Type: " + t.getClass().getSimpleName());

                Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int safeInt(String v) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 1;
        }
    }

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

    // ── ViewHolders ──

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvSubtitle, tvPrice, tvMrp, tvDiscount;
        TextView tvMoq, tvBuyPrice, tvMargin, tvQtyCount;
        Button btnAddToCart;
        LinearLayout layoutQty;
        TextView btnMinus, btnPlus;

        public ProductViewHolder(@NonNull View v) {
            super(v);

            ivImage = v.findViewById(R.id.iv_product_image);
            tvName = v.findViewById(R.id.tv_product_name);
            tvSubtitle = v.findViewById(R.id.tv_product_subtitle);
            tvPrice = v.findViewById(R.id.tv_selling_price);
            tvMrp = v.findViewById(R.id.tv_mrp);
            tvDiscount = v.findViewById(R.id.tv_discount);
            tvMoq = v.findViewById(R.id.tv_moq);
            tvBuyPrice = v.findViewById(R.id.tv_buy_price);
            tvMargin = v.findViewById(R.id.tv_margin);

            btnAddToCart = v.findViewById(R.id.btn_add_to_cart);

            layoutQty = v.findViewById(R.id.layout_qty);
            btnMinus = v.findViewById(R.id.btn_minus);
            btnPlus = v.findViewById(R.id.btn_plus);
            tvQtyCount = v.findViewById(R.id.tv_qty_count);
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