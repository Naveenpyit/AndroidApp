package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.activity.AccountActivity;
import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.activity.ProductListingScreen;
import com.example.myapplication.adapter.CartAdapter;
import com.example.myapplication.model.CartModel;
import com.example.myapplication.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class CartScreen extends AppCompatActivity implements CartAdapter.CartChangeListener {

    private RecyclerView rvCart;
    private TextView tvSubtotal, btnProceed;
    private CheckBox cbSelectAll;
    private ImageView btnBack;

    private CartAdapter adapter;
    private BottomNavigationView bottomNav;

    private ArrayList<CartModel> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_screen);

        setupStatusBar();

        btnBack = findViewById(R.id.btn_back);
        rvCart = findViewById(R.id.rv_card);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        btnProceed = findViewById(R.id.btn_proceed);
        cbSelectAll = findViewById(R.id.cb_select_all);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        cartItems = CartManager.getInstance().getCartItems();

        CartManager.getInstance().selectAll(true);
        cbSelectAll.setChecked(true);

        adapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        updateSummary();

        cbSelectAll.setOnCheckedChangeListener((btn, checked) -> {
            CartManager.getInstance().selectAll(checked);
            adapter.notifyDataSetChanged();
            updateSummary();
        });

        btnProceed.setOnClickListener(v -> {
            if (CartManager.getInstance().getSelectedCount() == 0) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CheckoutScreen.class));
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_cart);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_cart) {
                return true;
            }

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_orders) {
                startActivity(new Intent(this, ProductListingScreen.class));
                finish();
                return true;
            }

            if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    public void onCartChanged() {
        updateSummary();
    }

    private void updateSummary() {
        double subtotal = CartManager.getInstance().getSubtotal();
        int count = CartManager.getInstance().getSelectedCount();

        tvSubtotal.setText("₹" + String.format("%.0f", subtotal));
        btnProceed.setText("Proceed to Buy ( " + count + " Items )");
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