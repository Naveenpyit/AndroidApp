package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistScreen extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private ArrayList<ListWishlistResponse.WishlistItem> wishlistItems;
    private ApiService apiService;
    private TokenManager tokenManager;
    private ProgressDialog progressDialog;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        setupStatusBar();
        initViews();
        fetchWishlist();
    }

    private void initViews() {
        rvWishlist = findViewById(R.id.rv_wishlist);
        btnBack = findViewById(R.id.btn_back);

        wishlistItems = new ArrayList<>();
        apiService = RetrofitClient.getClient(this);
        tokenManager = new TokenManager(this);

        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new WishlistAdapter(this, wishlistItems, this::removeFromWishlistUI);
        rvWishlist.setAdapter(adapter);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void fetchWishlist() {
        showLoader();
        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) userId = "10";

        apiService.listWishlist(new ListWishlistRequest(userId))
                .enqueue(new Callback<ListWishlistResponse>() {
                    @Override
                    public void onResponse(Call<ListWishlistResponse> call, Response<ListWishlistResponse> response) {
                        dismissLoader();
                        if (response.isSuccessful() && response.body() != null && response.body().getNStatus() == 1) {
                            if (response.body().getJData() != null) {
                                wishlistItems.clear();
                                wishlistItems.addAll(response.body().getJData());
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(WishlistScreen.this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ListWishlistResponse> call, Throwable t) {
                        dismissLoader();
                        Toast.makeText(WishlistScreen.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ Called by adapter when item is removed
    private void removeFromWishlistUI(int position, String wishlistId) {
        if (position >= 0 && position < wishlistItems.size()) {
            wishlistItems.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    // ✅ Called by adapter when cart is updated
    public void onCartUpdated() {
        // Cart was updated on server, CartScreen will fetch fresh data
        // You can optionally show a snackbar or refresh indicator
        Toast.makeText(this, "Item added to cart!", Toast.LENGTH_SHORT).show();
    }

    private void showLoader() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage("Loading wishlist...");
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
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}