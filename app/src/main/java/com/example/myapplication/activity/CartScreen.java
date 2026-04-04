package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CartAdapter;
import com.example.myapplication.model.CartModel;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteCartRequest;
import com.example.myapplication.model.ListCartRequest;
import com.example.myapplication.model.ListCartResponse;
import com.example.myapplication.model.UpdateCartRequest;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.CartManager;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartScreen extends AppCompatActivity implements CartAdapter.CartChangeListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView          rvCart;
    private TextView              tvSubtotal, tvItemCount, tvMrpTotal,
            tvSavings, tvGst, tvGrandTotal;
    private TextView        btnProceed;
    private CheckBox              cbSelectAll;
    private ImageView             btnBack;
    private LinearLayout          layoutEmpty;
    private BottomNavigationView  bottomNav;
    private ProgressDialog        progressDialog;

   private MaterialButton btn_change_address;
    private CartAdapter           adapter;
    private ArrayList<CartModel>  cartItems;
    private ApiService            apiService;
    private String                userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_screen);

        setupStatusBar();
        initViews();
        setupBottomNav();

        TokenManager tokenManager = new TokenManager(this);
        userId = tokenManager.getUserId();
      //  if (userId == null || userId.isEmpty()) userId = "10"; // fallback

        loadCart();
    }
    @Override
    protected void onResume() {
        super.onResume();

        loadCart();
    }



    // ── Init ──────────────────────────────────────────────────────────────────
    private void initViews() {
        apiService    = RetrofitClient.getClient(this);
        cartItems     = CartManager.getInstance().getCartItems();

        btnBack       = findViewById(R.id.btn_back);
        rvCart        = findViewById(R.id.rv_card);
        tvSubtotal    = findViewById(R.id.tv_subtotal);
        btnProceed    = findViewById(R.id.btn_proceed);
        cbSelectAll   = findViewById(R.id.cb_select_all);
        btn_change_address = findViewById(R.id.btn_change_address);
      //  layoutEmpty   = findViewById(R.id.layout_empty);

        // Optional summary views — safe if not in layout
        tvItemCount   = findViewById(R.id.tv_item_count);
       // tvMrpTotal    = findViewById(R.id.tv_mrp_total);
       // tvSavings     = findViewById(R.id.tv_savings);
      //  tvGst         = findViewById(R.id.tv_gst);
       // tvGrandTotal  = findViewById(R.id.tv_grand_total);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // RecyclerView
        adapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        // Select all checkbox
        cbSelectAll.setOnCheckedChangeListener((btn, checked) -> {
            CartManager.getInstance().selectAll(checked);
            adapter.notifyDataSetChanged();
            updateSummary();
        });
        btn_change_address.setOnClickListener(v->{
            Intent i = new Intent(CartScreen.this,AddressActivity.class);
            startActivity(i);
        });

        // Proceed button
        btnProceed.setOnClickListener(v -> {
            if (CartManager.getInstance().getSelectedCount() == 0) {
                Toast.makeText(this, "Please select at least one item",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CheckoutScreen.class));
        });
    }

    // ── Load cart from API ────────────────────────────────────────────────────
    private void loadCart() {
        showLoader("Loading cart...");
        apiService.listCart(new ListCartRequest(userId))
                .enqueue(new Callback<ListCartResponse>() {
                    @Override
                    public void onResponse(Call<ListCartResponse> call,
                                           Response<ListCartResponse> response) {
                        hideLoader();
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getStatus() == 1
                                && response.body().getData() != null) {

                            ArrayList<CartModel> fresh =
                                    new ArrayList<>(response.body().getData());
                            CartManager.getInstance().setCartItems(fresh);

                            // cartItems is same reference — just notify
                            adapter.notifyDataSetChanged();
                            updateSummary();
                            showEmptyState(fresh.isEmpty());

                            // Sync select-all checkbox
                            cbSelectAll.setOnCheckedChangeListener(null);
                            cbSelectAll.setChecked(
                                    CartManager.getInstance().isAllSelected());
                            cbSelectAll.setOnCheckedChangeListener((btn, checked) -> {
                                CartManager.getInstance().selectAll(checked);
                                adapter.notifyDataSetChanged();
                                updateSummary();
                            });

                        } else {
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ListCartResponse> call, Throwable t) {
                        hideLoader();
                        Toast.makeText(CartScreen.this,
                                "Failed to load cart", Toast.LENGTH_SHORT).show();
                        showEmptyState(cartItems.isEmpty());
                    }
                });
    }

    // ── CartChangeListener: qty changed ──────────────────────────────────────
    @Override
    public void onCartChanged() {
        updateSummary();
    }

    // ── CartChangeListener: update qty via API ────────────────────────────────
    @Override
    public void onQtyUpdate(CartModel item, int newQty) {
        item.setUiQty(newQty);
        updateSummary();

        UpdateCartRequest req = new UpdateCartRequest(
                userId, item.getCartId(), item.getProduct(),
                item.getPack(), String.valueOf(newQty));

        apiService.updateCart(req).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> c, Response<CommonResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getStatus() == 1) {
                    item.setQuantity(String.valueOf(newQty));
                } else {
                    Toast.makeText(CartScreen.this,
                            "Update failed", Toast.LENGTH_SHORT).show();
                    // Revert
                    try { item.setUiQty(Integer.parseInt(item.getQuantity())); }
                    catch (Exception e) { item.setUiQty(1); }
                    adapter.notifyDataSetChanged();
                    updateSummary();
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> c, Throwable t) {
                Toast.makeText(CartScreen.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── CartChangeListener: delete via API ────────────────────────────────────
    @Override
    public void onDelete(CartModel item, int position) {
        showLoader("Removing...");
        DeleteCartRequest req = new DeleteCartRequest(item.getCartId(), userId);

        apiService.deleteCart(req).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> c, Response<CommonResponse> r) {
                hideLoader();
                if (r.isSuccessful() && r.body() != null && r.body().getStatus() == 1) {
                    cartItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, cartItems.size());
                    updateSummary();
                    showEmptyState(cartItems.isEmpty());
                    Toast.makeText(CartScreen.this,
                            "Removed from cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartScreen.this,
                            "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<CommonResponse> c, Throwable t) {
                hideLoader();
                Toast.makeText(CartScreen.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Update summary bar ────────────────────────────────────────────────────
    private void updateSummary() {
        CartManager cm    = CartManager.getInstance();
        double subtotal   = cm.getSubtotal();
        double mrpTotal   = cm.getMrpTotal();
        double savings    = cm.getSavings();
        double gst        = cm.getGstTotal();
        double grandTotal = cm.getGrandTotal();
        int    count      = cm.getSelectedCount();

        tvSubtotal.setText("₹" + String.format("%.0f", subtotal));
        btnProceed.setText("Proceed to Buy (" + count + " item"
                + (count == 1 ? "" : "s") + ")");

        if (tvItemCount  != null) tvItemCount.setText(count + " item" + (count == 1 ? "" : "s"));
        if (tvMrpTotal   != null) tvMrpTotal.setText("₹" + String.format("%.2f", mrpTotal));
        if (tvSavings    != null) tvSavings.setText("-₹" + String.format("%.2f", savings));
        if (tvGst        != null) tvGst.setText("+₹" + String.format("%.2f", gst));
        if (tvGrandTotal != null) tvGrandTotal.setText("₹" + String.format("%.2f", grandTotal));
    }

    private void showEmptyState(boolean empty) {
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ── Bottom nav ────────────────────────────────────────────────────────────
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_cart);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_cart)    return true;
            if (id == R.id.nav_home)  { startActivity(new Intent(this, MainActivity.class));  return true; }
            if (id == R.id.nav_orders)    { startActivity(new Intent(this, ProductListingScreen.class));     return true; }
            if (id == R.id.nav_account) { startActivity(new Intent(this, AccountScreen.class));  return true; }
            return false;
        });
    }

    // ── Loader ────────────────────────────────────────────────────────────────
    private void showLoader(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing()) progressDialog.show();
    }
    private void hideLoader() {
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