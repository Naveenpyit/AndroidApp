package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountScreen extends AppCompatActivity {

    private TextView tvUserName, tvUserPhone;
    private ImageView ivAvatar, btnEditProfile;
    private TextView btnLogout;
    private BottomNavigationView bottomNav;
    private TokenManager tokenManager;

    // ── Menu row root views ───────────────────────────────────────────────────
    private View rowSetup, rowOrders, rowBuyAgain, rowFavourites, rowReturn;
    private View rowNotifications, rowWallet, rowCoupons;
    private View rowVerification, rowAddress, rowLanguage, rowSettings;
    private View rowShareApp, rowComplaint, rowAbout, rowTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setupStatusBar();
        tokenManager = new TokenManager(this);
        initViews();
        bindUserInfo();
        setupMenuRows();
        setupBottomNav();
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    private void initViews() {
        tvUserName    = findViewById(R.id.tv_user_name);
        tvUserPhone   = findViewById(R.id.tv_user_phone);
        ivAvatar      = findViewById(R.id.iv_avatar);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout     = findViewById(R.id.btn_logout);
        bottomNav     = findViewById(R.id.bottom_nav);

        // Menu rows
        rowSetup         = findViewById(R.id.row_setup);
        rowOrders        = findViewById(R.id.row_orders);
        rowBuyAgain      = findViewById(R.id.row_buy_again);
        rowFavourites    = findViewById(R.id.row_favourites);
        rowReturn        = findViewById(R.id.row_return);
        rowNotifications = findViewById(R.id.row_notifications);
        rowWallet        = findViewById(R.id.row_wallet);
        rowCoupons       = findViewById(R.id.row_coupons);
        rowVerification  = findViewById(R.id.row_verification);
        rowAddress       = findViewById(R.id.row_address);
        rowLanguage      = findViewById(R.id.row_language);
        rowSettings      = findViewById(R.id.row_settings);
        rowShareApp      = findViewById(R.id.row_share_app);
        rowComplaint     = findViewById(R.id.row_complaint);
        rowAbout         = findViewById(R.id.row_about);
        rowTerms         = findViewById(R.id.row_terms);
    }

    // ── Bind user info from TokenManager ─────────────────────────────────────

    private void bindUserInfo() {
//        String name  = tokenManager.getUserName();
//        String phone = tokenManager.getUserPhone();
                String name  = "Nivetha";
        String phone = "4867984699";

        tvUserName.setText((name  != null && !name.isEmpty())  ? name  : "User");
        tvUserPhone.setText((phone != null && !phone.isEmpty()) ? phone : "");

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show());
    }

    // ── Configure each menu row: icon, title, subtitle, click ────────────────

    private void setupMenuRows() {
        configRow(rowSetup,         R.drawable.ic_person,          "Setup your account",
                "0 items pending orders", true,
                v -> Toast.makeText(this, "Setup Account", Toast.LENGTH_SHORT).show());

        configRow(rowOrders,        R.drawable.ic_orders,          "Orders",         null, false,
                v -> startActivity(new Intent(this, ProductListingScreen.class)));

        configRow(rowBuyAgain,      R.drawable.ic_replay,          "Buy Again",      null, false,
                v -> Toast.makeText(this, "Buy Again", Toast.LENGTH_SHORT).show());

        configRow(rowFavourites,    R.drawable.ic_favorite_border, "Favourites",     null, false,
                v -> startActivity(new Intent(this, WishlistScreen.class)));

        configRow(rowReturn,        R.drawable.ic_return,          "Return",         null, false,
                v -> Toast.makeText(this, "Return", Toast.LENGTH_SHORT).show());

        configRow(rowNotifications, R.drawable.notification,       "Notifications",  null, false,
                v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        configRow(rowWallet,        R.drawable.ic_wallet,          "Wallet",         null, false,
                v -> Toast.makeText(this, "Wallet", Toast.LENGTH_SHORT).show());

        configRow(rowCoupons,       R.drawable.ic_coupon,          "Coupons",        null, false,
                v -> Toast.makeText(this, "Coupons", Toast.LENGTH_SHORT).show());

        configRow(rowVerification,  R.drawable.ic_verified,        "Verification Details", null, false,
                v -> Toast.makeText(this, "Verification Details", Toast.LENGTH_SHORT).show());

        configRow(rowAddress,       R.drawable.ic_location,        "Address",        null, false,
                v -> Toast.makeText(this, "Address", Toast.LENGTH_SHORT).show());

        configRow(rowLanguage,      R.drawable.ic_language,        "Language",       null, false,
                v -> Toast.makeText(this, "Language", Toast.LENGTH_SHORT).show());

        configRow(rowSettings,      R.drawable.ic_settings,        "Settings",       null, false,
                v -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());

        configRow(rowShareApp,      R.drawable.ic_share,           "Share App with Your Friends", null, false,
                v -> shareApp());

        configRow(rowComplaint,     R.drawable.ic_complaint,       "Raise a complaint", null, false,
                v -> Toast.makeText(this, "Raise a complaint", Toast.LENGTH_SHORT).show());

        configRow(rowAbout,         R.drawable.ic_info,            "About Us",       null, false,
                v -> Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show());

        configRow(rowTerms,         R.drawable.ic_document,        "Terms & Conditions", null, false,
                v -> Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Helper to configure an included menu row.
     *
     * @param row         The root View of the included item_account_menu_row layout
     * @param iconRes     Drawable resource for the left icon
     * @param title       Primary label text
     * @param subtitle    Secondary label (null = hide)
     * @param showSubtitle Whether to show the subtitle TextView
     * @param clickListener Click action for the whole row
     */
    private void configRow(View row, int iconRes, String title,
                           String subtitle, boolean showSubtitle,
                           View.OnClickListener clickListener) {
        if (row == null) return;

        ImageView icon     = row.findViewById(R.id.iv_menu_icon);
        TextView  tvTitle  = row.findViewById(R.id.tv_menu_title);
        TextView  tvSub    = row.findViewById(R.id.tv_menu_subtitle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);

        if (showSubtitle && subtitle != null && !subtitle.isEmpty()) {
            tvSub.setText(subtitle);
            tvSub.setVisibility(View.VISIBLE);
        } else {
            tvSub.setVisibility(View.GONE);
        }

        row.setOnClickListener(clickListener);
    }

    // ── Share App ─────────────────────────────────────────────────────────────

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🛍️ Shop wholesale fashion at amazing prices!\n\nDownload our app now.");
        startActivity(Intent.createChooser(shareIntent, "Share app via"));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        // Mark Account tab as selected
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, ProductListingScreen.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartScreen.class));
                return true;
            } else if (id == R.id.nav_account) {
                return true; // already here
            }
            return false;
        });
    }

    // ── Status Bar ────────────────────────────────────────────────────────────

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