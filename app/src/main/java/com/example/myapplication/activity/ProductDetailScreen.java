package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageSliderAdapter;
import com.example.myapplication.adapter.PackAdapter;
import com.example.myapplication.adapter.RelatedProductAdapter;
import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.ProductDetailModel;
import com.example.myapplication.model.ProductDetailRequest;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailScreen extends AppCompatActivity {

    private ImageView  btnBack, btnSearch, btnNotification, btnCart;

    private ViewPager2        viewPager;
    private LinearLayout      dotsContainer;
    private ImageSliderAdapter sliderAdapter;
    private final List<String> imageList = new ArrayList<>();

    private ImageView ivWishlist, ivShare;
    private boolean   isWishlisted = false;

    private LinearLayout colorContainer;

    private RecyclerView  rvPacks;
    private PackAdapter   packAdapter;
    private final List<ProductDetailModel.PackModel> packList = new ArrayList<>();
    private ProductDetailModel.PackModel selectedPack = null;

    private TextView tvQty, tvStockStatus;
    private ImageView btnMinus, btnPlus;
    private LinearLayout qtyStepperLayout;
    private int quantity = 1;
    private int cartQty = 0;  // ✅ Track qty added to cart

    private TextView tvSku, tvCategory;

    private TextView tvTotalPrice, tvGstNote, tvMoq;
    private TextView tvBuyPrice, tvMinOrder;

    private ImageView ivThumbUp, ivThumbDown;

    private TextView tvDescription;

    private TextView tvSpecStyle, tvSpecCategory, tvSpecMaterial, tvSpecFabric;
    private TextView tvSpecNeck, tvSpecSeason, tvSpecColor, tvSpecSupplier, tvSpecFit;

    private RecyclerView         rvRelated;
    private RelatedProductAdapter relatedAdapter;
    private final List<ProductModel> relatedList = new ArrayList<>();

    private MaterialButton btnAddWishlist, btnAddCart;

    private ApiService     apiService;
    private TokenManager   tokenManager;
    private ProgressDialog progressDialog;
    private String         cRandom = "";

    // ✅ Store product info for cart requests
    private String productId = "";
    private String categoryId = "1";
    private String packId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_screen);

        cRandom = getIntent().getStringExtra("c_random");
        if (cRandom == null) cRandom = "";

        setupStatusBar();
        initViews();
        setupImageSlider();
        setupPackRecycler();
        setupRelatedRecycler();
        setupQuantityControls();
        setupBottomButtons();

        apiService = RetrofitClient.getClient(this);
        tokenManager = new TokenManager(this);
        fetchProductDetail();
    }

    private void initViews() {
        btnBack         = findViewById(R.id.btn_back);
        btnSearch       = findViewById(R.id.btn_search);
        btnNotification = findViewById(R.id.btn_notification);
        btnCart         = findViewById(R.id.btn_cart);
        ivShare    = findViewById(R.id.iv_share_icon);
        viewPager      = findViewById(R.id.view_pager_images);
        dotsContainer  = findViewById(R.id.dots_container);

        ivWishlist = findViewById(R.id.iv_wishlist_icon);
        ivShare    = findViewById(R.id.iv_share_icon);

        colorContainer = findViewById(R.id.color_container);

        rvPacks = findViewById(R.id.rv_packs);

        tvQty         = findViewById(R.id.tv_quantity);
        tvStockStatus = findViewById(R.id.tv_stock_status);
        btnMinus      = findViewById(R.id.btn_minus);
        btnPlus       = findViewById(R.id.btn_plus);
        qtyStepperLayout = findViewById(R.id.qty_stepper_layout);

        tvSku      = findViewById(R.id.tv_sku_value);
        tvCategory = findViewById(R.id.tv_category_value);

        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvGstNote    = findViewById(R.id.tv_gst_note);
        tvMoq        = findViewById(R.id.tv_moq_value);
        tvBuyPrice   = findViewById(R.id.tv_buy_price);
        tvMinOrder   = findViewById(R.id.tv_min_order);

        ivThumbUp   = findViewById(R.id.iv_thumb_up);
        ivThumbDown = findViewById(R.id.iv_thumb_down);

        tvDescription = findViewById(R.id.tv_description);

        tvSpecStyle    = findViewById(R.id.tv_spec_style);
        tvSpecCategory = findViewById(R.id.tv_spec_category);
        tvSpecMaterial = findViewById(R.id.tv_spec_material);
        tvSpecFabric   = findViewById(R.id.tv_spec_fabric);
        tvSpecNeck     = findViewById(R.id.tv_spec_neck);
        tvSpecSeason   = findViewById(R.id.tv_spec_season);
        tvSpecColor    = findViewById(R.id.tv_spec_color);
        tvSpecSupplier = findViewById(R.id.tv_spec_supplier);
        tvSpecFit      = findViewById(R.id.tv_spec_fit);

        rvRelated = findViewById(R.id.rv_related_products);

        btnAddWishlist = findViewById(R.id.btn_add_wishlist);
        btnAddCart     = findViewById(R.id.btn_add_cart);
        LinearLayout searchBarClick = findViewById(R.id.search_bar_click);
        searchBarClick.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailScreen.this, ProductListingScreen.class);
            intent.putExtra("openSearch", true);   // signal to open keyboard immediately
            intent.putExtra("title", "Search Products");
            startActivity(intent);
        });
        // ── Click Listeners ───────────────────────────────────────────────────
        btnBack.setOnClickListener(v -> onBackPressed());
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartScreen.class)));

        ivWishlist.setOnClickListener(v -> toggleWishlist());
        ivThumbUp.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());
        ivThumbDown.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());

        ivShare.setOnClickListener(v -> shareProduct());
    }


    private void shareProduct() {
        // Build share text from whatever product data is already loaded
        String productName = tvSku.getText().toString();       // or store name separately
        String category    = tvCategory.getText().toString();
        String price       = tvTotalPrice.getText().toString();

        String shareText =
                "🛍️ Check out this product!\n\n" +
                        "📦 Item Code : " + productName + "\n" +
                        "🏷️ Category  : " + category   + "\n" +
                        "💰 Price     : " + price       + "\n\n" +
                        "Shop now on our app!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // This shows the system share sheet — WhatsApp, Instagram, Gmail, etc.
        startActivity(Intent.createChooser(shareIntent, "Share product via"));
    }

    // ── Wishlist Toggle ───────────────────────────────────────────────────────
    private void toggleWishlist() {
        isWishlisted = !isWishlisted;
        ivWishlist.setImageResource(isWishlisted
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);
    }

    // ── Image Slider Setup ────────────────────────────────────────────────────
    private void setupImageSlider() {
        sliderAdapter = new ImageSliderAdapter(this, imageList);
        viewPager.setAdapter(sliderAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void setupDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == 0
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(16, 16);
            lp.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(lp);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int selectedIndex) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsContainer.getChildAt(i);
            dot.setImageResource(i == selectedIndex
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
        }
    }

    // ── Pack Recycler Setup ───────────────────────────────────────────────────
    private void setupPackRecycler() {
        rvPacks.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        packAdapter = new PackAdapter(this, packList, pack -> {
            selectedPack = pack;
            packId = String.valueOf(selectedPack.getPackId());

            updatePriceBlock(pack);
        });
        rvPacks.setAdapter(packAdapter);
    }


    private void setupRelatedRecycler() {
        rvRelated.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedAdapter = new RelatedProductAdapter(this, relatedList);
        rvRelated.setAdapter(relatedAdapter);
    }

    // ── Quantity Controls ─────────────────────────────────────────────────────
    private void setupQuantityControls() {
        tvQty.setText(String.valueOf(quantity));

        btnMinus.setOnClickListener(v -> {
            if (cartQty > 0) {
                // Decrease cart quantity
                if (cartQty == 1) {
                    // Remove from cart
                    removeFromCart();
                } else {
                    // Update cart with decremented qty
                    updateCartQuantity(cartQty - 1);
                }
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (cartQty > 0) {
                // Increase cart quantity
                updateCartQuantity(cartQty + 1);
            }
        });
    }

    // ── Bottom Buttons ────────────────────────────────────────────────────────
    private void setupBottomButtons() {
        btnAddWishlist.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            ivWishlist.setImageResource(isWishlisted
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite_border);
            Toast.makeText(this,
                    isWishlisted ? "Added to Wishlist" : "Removed from Wishlist",
                    Toast.LENGTH_SHORT).show();
        });

        btnAddCart.setOnClickListener(v -> {
            if (cartQty == 0) {
                // First time adding to cart
                addToCart(1);
            }
        });
    }

    // ── Add to Cart (First Time) ──────────────────────────────────────────────
    private void addToCart(int qtyToAdd) {
        if (selectedPack == null || packId.isEmpty()) {
            Toast.makeText(this, "Please select a pack", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) userId = "10";

        // ✅ Use correct variable names
        AddCartRequest request = new AddCartRequest(
                categoryId, packId, productId, String.valueOf(qtyToAdd), userId);

        showLoader("Adding to cart...");
        // ✅ Use 'addCart' not 'addToCart'
        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                dismissLoader();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    cartQty = qtyToAdd;
                    updateCartButtonUI();
                    Toast.makeText(ProductDetailScreen.this, "Added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailScreen.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                dismissLoader();
                Toast.makeText(ProductDetailScreen.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Update Cart Quantity (+ / -) ──────────────────────────────────────────
    private void updateCartQuantity(int newQty) {
        if (selectedPack == null || packId.isEmpty()) return;

        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) userId = "10";

        AddCartRequest request = new AddCartRequest(
                categoryId, packId, productId, String.valueOf(newQty), userId);

        showLoader("Updating cart...");
        // ✅ Use 'addCart' not 'addToCart'
        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                dismissLoader();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    cartQty = newQty;
                    tvQty.setText(String.valueOf(cartQty));
                    Toast.makeText(ProductDetailScreen.this, "Cart updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailScreen.this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                dismissLoader();
                Toast.makeText(ProductDetailScreen.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Remove from Cart ──────────────────────────────────────────────────────
    private void removeFromCart() {
        cartQty = 0;
        updateCartButtonUI();
        Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show();
    }

    // ── Update UI: Show Button OR Stepper ─────────────────────────────────────
    private void updateCartButtonUI() {
        if (cartQty > 0) {
            // Hide "Add to Cart" button, show qty stepper
            btnAddCart.setVisibility(View.GONE);
            qtyStepperLayout.setVisibility(View.VISIBLE);
            tvQty.setText(String.valueOf(cartQty));
        } else {
            // Show "Add to Cart" button, hide qty stepper
            btnAddCart.setVisibility(View.VISIBLE);
            qtyStepperLayout.setVisibility(View.GONE);
        }
    }

    // ── Fetch Product Detail ──────────────────────────────────────────────────
    private void fetchProductDetail() {
        showLoader();
        apiService.getProductDetail(new ProductDetailRequest(cRandom, "1"))
                .enqueue(new Callback<ProductDetailResponse>() {
                    @Override
                    public void onResponse(Call<ProductDetailResponse> call,
                                           Response<ProductDetailResponse> response) {
                        dismissLoader();
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            ProductDetailResponse.DetailData data = response.body().getData();

                            // Item details
                            if (data.getItemDetails() != null && !data.getItemDetails().isEmpty()) {
                                bindProductDetail(data.getItemDetails().get(0));
                            }

                            // Related items
                            if (data.getRelatedItems() != null) {
                                relatedList.clear();
                                relatedList.addAll(data.getRelatedItems());
                                relatedAdapter.notifyDataSetChanged();
                            }

                        } else {
                            Toast.makeText(ProductDetailScreen.this,
                                    "Failed to load product", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductDetailResponse> call, Throwable t) {
                        dismissLoader();
                        Toast.makeText(ProductDetailScreen.this,
                                "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Bind Product Detail to Views ──────────────────────────────────────────
    private void bindProductDetail(ProductDetailModel p) {

        // ✅ Extract product ID for cart
        productId = p.getItemId() != null ? p.getItemId() : "";

        // Images
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            imageList.clear();
            imageList.addAll(p.getImages());
            sliderAdapter.notifyDataSetChanged();
            setupDots(imageList.size());
        }

        // Packs
        if (p.getPacks() != null && !p.getPacks().isEmpty()) {
            packList.clear();
            packList.addAll(p.getPacks());
            packAdapter.notifyDataSetChanged();
            selectedPack = packList.get(0);
            packId = String.valueOf(selectedPack.getPackId());
            packAdapter.setSelectedIndex(0);
            updatePriceBlock(selectedPack);
        }

        // SKU & Category
        tvSku.setText((p.getItemCode() != null ? p.getItemCode() : "") +
                (p.getItemId() != null ? "-" + p.getItemId() : ""));
        tvCategory.setText(p.getCategoryName() != null ? p.getCategoryName() : "");

        // MOQ
        tvMoq.setText(p.getMoq() != null ? p.getMoq() : "");

        // Description
        String desc = "Stay effortlessly stylish and comfortable with this casual " +
                (p.getNeck() != null ? p.getNeck().toLowerCase() : "") +
                "-neck tee made from " +
                (p.getMaterial() != null ? p.getMaterial() : "100% premium cotton") +
                ". Crafted in a single jersey " +
                (p.getFabric() != null ? p.getFabric() : "") +
                " fabric, it offers a soft, breathable feel that's perfect for all-day wear.";
        tvDescription.setText(desc);

        // Specifications
        tvSpecStyle.setText(p.getStyle() != null ? p.getStyle() : "-");
        tvSpecCategory.setText(p.getCategoryName() != null ? p.getCategoryName() : "-");
        tvSpecMaterial.setText(p.getMaterial() != null ? "• " + p.getMaterial() : "-");
        tvSpecFabric.setText(p.getFabric() != null ? "• " + p.getFabric() : "-");
        tvSpecNeck.setText(p.getNeck() != null ? p.getNeck() : "-");
        tvSpecSeason.setText(p.getSeason() != null ? p.getSeason() : "-");
        tvSpecColor.setText(p.getColor() != null ? p.getColor() : "-");
        tvSpecSupplier.setText(p.getSupplier() != null ? p.getSupplier() : "-");
        tvSpecFit.setText(p.getFit() != null ? p.getFit() : "-");
    }

    // ── Update Price Block ────────────────────────────────────────────────────
    // ── Update Price Block ────────────────────────────────────────────────────
    private void updatePriceBlock(ProductDetailModel.PackModel pack) {
        // ✅ Safe null checks for double values
        double gst = 0;
        double base = 0;
        double mrp = 0;

        try {
            // ✅ Direct assignment
            gst = pack.getGstAmount();
            base = pack.getSellingPrice();

            // ✅ Check if getMrp() returns String or double
            Object mrpObj = pack.getMrp();

            if (mrpObj != null) {
                if (mrpObj instanceof String) {
                    // If it's a String, parse it
                    String mrpStr = (String) mrpObj;
                    if (!mrpStr.isEmpty()) {
                        mrp = Double.parseDouble(mrpStr);
                    }
                } else if (mrpObj instanceof Double) {
                    // If it's already a Double, just cast it
                    mrp = (Double) mrpObj;
                } else if (mrpObj instanceof Integer) {
                    // If it's an Integer, convert it
                    mrp = ((Integer) mrpObj).doubleValue();
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mrp = 0;
        }

        tvTotalPrice.setText("₹" + String.format("%.2f", base));
        tvGstNote.setText(String.format("( ₹%.2f + %.2f GST )", base, gst));
        tvBuyPrice.setText("Buy for ₹" + String.format("%.0f", base));
        tvMinOrder.setText("Minimum order value : ₹" + String.format("%.0f", mrp));
    }

    // ── Loader ────────────────────────────────────────────────────────────────
    private void showLoader() {
        showLoader("Loading...");
    }

    private void showLoader(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    private void dismissLoader() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
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