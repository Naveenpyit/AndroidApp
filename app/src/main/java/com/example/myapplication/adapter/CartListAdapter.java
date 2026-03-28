package com.example.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.CartItemModel;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteCartRequest;
import com.example.myapplication.model.UpdateCartRequest;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder> {

    private static final String TAG = "CartListAdapter";
    private static final String USER_ID = "10";

    public interface CartUpdateListener {
        void onCartUpdated();
        void onItemDeleted(int position);
    }

    private final Context context;
    private final ArrayList<CartItemModel> list;
    private final CartUpdateListener listener;
    private final ApiService apiService;
    private String userId;
    public CartListAdapter(Context context, ArrayList<CartItemModel> list,
                           CartUpdateListener listener, String userId) {
        this.context    = context;
        this.list       = list;
        this.listener   = listener;
        this.userId     = userId;
        this.apiService = RetrofitClient.getClient(context);
        Log.d(TAG, "✅ CartListAdapter initialized with User ID: " + userId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItemModel item = list.get(position);

        Log.d(TAG, "\n📦 Binding item at position: " + position);
        Log.d(TAG, "   Name: " + item.getItemName());
        Log.d(TAG, "   Cart ID: " + item.getCartId());
        Log.d(TAG, "   Current Qty: " + item.getQuantityInt());

        h.tvName.setText(item.getItemName() != null ? item.getItemName() : "");
        h.tvMeta.setText(
                "Pack : " + item.getPackName()
                        + "  Category : " + item.getCategoryName()
                        + "  SKU : " + item.getItemCode());

        updatePriceLabel(h, item);
        h.tvQty.setText(String.valueOf(item.getQuantityInt()));

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(h.ivImage);

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CartItemModel currentItem = list.get(pos);
            int newQty = currentItem.getQuantityInt() + 1;

            Log.d(TAG, "\n➕ PLUS clicked");
            Log.d(TAG, "   Position: " + pos);
            Log.d(TAG, "   Cart ID: " + currentItem.getCartId());
            Log.d(TAG, "   Old Qty: " + currentItem.getQuantityInt());
            Log.d(TAG, "   New Qty: " + newQty);

            h.tvQty.setText(String.valueOf(newQty));
            callUpdate(currentItem, newQty, h, pos);
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CartItemModel currentItem = list.get(pos);
            int newQty = currentItem.getQuantityInt() - 1;

            Log.d(TAG, "\n➖ MINUS clicked");
            Log.d(TAG, "   Position: " + pos);
            Log.d(TAG, "   Cart ID: " + currentItem.getCartId());
            Log.d(TAG, "   Old Qty: " + currentItem.getQuantityInt());

            if (newQty <= 0) {
                Log.d(TAG, "   Action: DELETE (qty would be 0)");
                callDelete(currentItem, pos);
            } else {
                Log.d(TAG, "   New Qty: " + newQty);
                h.tvQty.setText(String.valueOf(newQty));
                callUpdate(currentItem, newQty, h, pos);
            }
        });

        h.tvRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            CartItemModel currentItem = list.get(pos);

            Log.d(TAG, "\n🗑️ REMOVE clicked");
            Log.d(TAG, "   Position: " + pos);
            Log.d(TAG, "   Cart ID: " + currentItem.getCartId());
            Log.d(TAG, "   Item: " + currentItem.getItemName());

            callDelete(currentItem, pos);
        });
    }

    private void callUpdate(CartItemModel item, int newQty,
                            ViewHolder h, int position) {
        Log.d(TAG, "\n📡 Calling UPDATE-CART API");
        Log.d(TAG, "   Request: {");
        Log.d(TAG, "     n_user: \"" + USER_ID + "\",");
        Log.d(TAG, "     n_cart: \"" + item.getCartId() + "\",");
        Log.d(TAG, "     n_product: \"" + item.getProductId() + "\",");
        Log.d(TAG, "     n_pack: \"" + item.getPack() + "\",");
        Log.d(TAG, "     n_qty: \"" + newQty + "\"");
        Log.d(TAG, "   }");

        apiService.updateCart(new UpdateCartRequest(
                USER_ID,
                item.getCartId(),
                item.getProductId(),
                item.getPack(),
                String.valueOf(newQty)
        )).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                Log.d(TAG, "✅ UPDATE-CART Response: Code " + r.code());

                if (r.isSuccessful() && r.body() != null && r.body().getStatus() == 1) {
                    Log.d(TAG, "✅ UPDATE SUCCESS");
                    Log.d(TAG, "   Message: " + r.body().getMessage());
                    setItemQty(item, newQty);
                    updatePriceLabel(h, item);
                    listener.onCartUpdated();

                    Toast.makeText(context, "Quantity updated ✓", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, " UPDATE FAILED");
                    if (r.body() != null) {
                        Log.e(TAG, "   Message: " + r.body().getMessage());
                    }

                    h.tvQty.setText(String.valueOf(item.getQuantityInt()));
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(TAG, "UPDATE-CART Network Error: " + t.getMessage());
                h.tvQty.setText(String.valueOf(item.getQuantityInt()));
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void callDelete(CartItemModel item, int position) {
        Log.d(TAG, "\n📡 Calling DELETE-CART API");
        Log.d(TAG, "   Request: {");
        Log.d(TAG, "     n_cart: \"" + item.getCartId() + "\",");
        Log.d(TAG, "     n_user: \"" + USER_ID + "\"");
        Log.d(TAG, "   }");

        apiService.deleteCart(new DeleteCartRequest(item.getCartId(), USER_ID))
                .enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> r) {
                        Log.d(TAG, "✅ DELETE-CART Response: Code " + r.code());

                        if (r.isSuccessful() && r.body() != null && r.body().getStatus() == 1) {
                            Log.d(TAG, "✅ DELETE SUCCESS");
                            Log.d(TAG, "   Message: " + r.body().getMessage());

                            listener.onItemDeleted(position);
                            Toast.makeText(context, "Item removed ✓", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "❌ DELETE FAILED");
                            if (r.body() != null) {
                                Log.e(TAG, "   Message: " + r.body().getMessage());
                            }
                            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Log.e(TAG, "❌ DELETE-CART Network Error: " + t.getMessage());
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePriceLabel(ViewHolder h, CartItemModel item) {
        double grandTotal = item.getGrandTotal();
        double lineTotal = item.getLineTotal();
        double gstTotal = item.getGstValueDouble() * item.getQuantityInt();

        h.tvPrice.setText("₹" + String.format("%.2f", grandTotal)
                + "  (₹" + String.format("%.2f", lineTotal)
                + " + " + String.format("%.2f", gstTotal) + " GST)");
    }

    private void setItemQty(CartItemModel item, int qty) {
        item.setLocalQty(qty);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvName, tvMeta, tvPrice, tvQty, tvRemove, btnMinus, btnPlus;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImage   = v.findViewById(R.id.iv_cart_image);
            tvName    = v.findViewById(R.id.tv_cart_name);
            tvMeta    = v.findViewById(R.id.tv_cart_meta);
            tvPrice   = v.findViewById(R.id.tv_cart_price);
            tvQty     = v.findViewById(R.id.tv_qty);
            tvRemove  = v.findViewById(R.id.tv_remove);
            btnMinus  = v.findViewById(R.id.btn_minus);
            btnPlus   = v.findViewById(R.id.btn_plus);
        }
    }
}