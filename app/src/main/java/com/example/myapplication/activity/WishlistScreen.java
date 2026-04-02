package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.WishlistAdapter;
import com.example.myapplication.model.ListWishlistRequest;
import com.example.myapplication.model.ListWishlistResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.WishlistManager;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistScreen extends AppCompatActivity {

    private static final String TAG = "WishlistScreen";

    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private ArrayList<ListWishlistResponse.WishlistItem> wishlistItems;

    private ApiService apiService;
    private TokenManager tokenManager;
    private ProgressDialog progressDialog;

    private ImageView btnBack;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        setupStatusBar();
        initViews();
        fetchWishlist();
    }

    // ─────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────

    private void initViews() {
        rvWishlist     = findViewById(R.id.rv_wishlist);
        btnBack        = findViewById(R.id.btn_back);
        layoutEmpty    = findViewById(R.id.layout_empty);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        wishlistItems = new ArrayList<>();
        apiService    = RetrofitClient.getClient(this);
        tokenManager  = new TokenManager(this);

        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new WishlistAdapter(this, wishlistItems, this::removeFromWishlistUI);
        rvWishlist.setAdapter(adapter);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void fetchWishlist() {
        showLoader();

        String userId = tokenManager.getUserId();

        if (userId == null || userId.trim().isEmpty()) {
            dismissLoader();
            showError("Session expired. Please login again.");
            return;
        }

        Log.d(TAG, "fetchWishlist → userId = " + userId);

        final String finalUserId = userId;

        apiService.listWishlist(new ListWishlistRequest(finalUserId))
                .enqueue(new Callback<ListWishlistResponse>() {

                    @Override
                    public void onResponse(Call<ListWishlistResponse> call,
                                           Response<ListWishlistResponse> response) {
                        dismissLoader();

                        // ── Step 1: HTTP error check ──────────────────────
                        if (!response.isSuccessful()) {
                            String errBody = "";
                            try {
                                if (response.errorBody() != null)
                                    errBody = response.errorBody().string();
                            } catch (IOException e) {
                                errBody = "unreadable";
                            }
                            Log.e(TAG, "HTTP " + response.code() + " → " + errBody);
                            showError("Server error (" + response.code() + ")");
                            return;
                        }

                        // ── Step 2: Null body check ───────────────────────
                        if (response.body() == null) {
                            Log.e(TAG, "Response body is null");
                            showError("Empty response from server");
                            return;
                        }

                        ListWishlistResponse body = response.body();
                        Log.d(TAG, "n_status = " + body.getNStatus());
                        Log.d(TAG, "c_message = " + body.getJData());

                        // ── Step 3: API status check ──────────────────────
                        if (body.getNStatus() != 1) {
                            Log.e(TAG, "API status != 1");
                            showError("Failed to load wishlist");
                            return;
                        }

                        // ── Step 4: Empty list check ──────────────────────
                        if (body.getJData() == null || body.getJData().isEmpty()) {
                            Log.d(TAG, "Wishlist is empty");
                            showEmptyState("Your wishlist is empty");
                            return;
                        }

                        // ── Step 5: Populate list ─────────────────────────
                        wishlistItems.clear();
                        wishlistItems.addAll(body.getJData());

                        // ✅ Fix n_pack from local WishlistManager
                        // (API doesn't return n_pack, only c_pack_name)
                        WishlistManager wm = WishlistManager.getInstance(WishlistScreen.this);
                        for (ListWishlistResponse.WishlistItem item : wishlistItems) {
                            String savedPackId = wm.getPackId(item.getNProduct());
                            if (savedPackId != null && !savedPackId.isEmpty()) {
                                item.setNPack(savedPackId);
                            }
                            Log.d(TAG, "Item → product=" + item.getNProduct()
                                    + " | pack=" + item.getNPack()
                                    + " | packName=" + item.getCPackName()
                                    + " | wishlistId=" + item.getNId());
                        }

                        adapter.notifyDataSetChanged();
                        showList();
                        Log.d(TAG, "✅ Loaded " + wishlistItems.size() + " wishlist items");
                    }

                    @Override
                    public void onFailure(Call<ListWishlistResponse> call, Throwable t) {
                        dismissLoader();
                        Log.e(TAG, "onFailure → " + t.getMessage(), t);
                        showError("Network error: " + t.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Remove from UI (called after API success in adapter)
    // ─────────────────────────────────────────────

    private void removeFromWishlistUI(int position, String wishlistId) {
        if (position >= 0 && position < wishlistItems.size()) {
            wishlistItems.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, wishlistItems.size());

            if (wishlistItems.isEmpty()) {
                showEmptyState("Your wishlist is empty");
            }
        }
    }

    // ─────────────────────────────────────────────
    // Cart updated callback (from WishlistAdapter)
    // ─────────────────────────────────────────────

    public void onCartUpdated() {
        Toast.makeText(this, "Item added to cart!", Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────
    // Visibility Helpers
    // ─────────────────────────────────────────────

    private void showList() {
        rvWishlist.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        rvWishlist.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyState(message);
    }

    // ─────────────────────────────────────────────
    // Loader
    // ─────────────────────────────────────────────

    private void showLoader() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading wishlist...");
        }
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    private void dismissLoader() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}