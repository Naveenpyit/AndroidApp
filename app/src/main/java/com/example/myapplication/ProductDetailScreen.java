package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.ImageSliderAdapter;
import com.example.myapplication.adapter.PackAdapter;
import com.example.myapplication.adapter.RelatedProductAdapter;
import com.example.myapplication.model.ProductDetailModel;
import com.example.myapplication.model.ProductDetailRequest;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
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
    private int quantity = 1;


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
    private ProgressDialog progressDialog;
    private String         cRandom = "";

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
        fetchProductDetail();
    }

    private void initViews() {
        btnBack         = findViewById(R.id.btn_back);
        btnSearch       = findViewById(R.id.btn_search);
        btnNotification = findViewById(R.id.btn_notification);
        btnCart         = findViewById(R.id.btn_cart);

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

        btnBack.setOnClickListener(v -> onBackPressed());
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartScreen.class)));

        ivWishlist.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            ivWishlist.setImageResource(isWishlisted
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite_border);
        });

        ivThumbUp.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());
        ivThumbDown.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());
    }


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

    private void setupPackRecycler() {
        rvPacks.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        packAdapter = new PackAdapter(this, packList, pack -> {
            selectedPack = pack;
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

    private void setupQuantityControls() {
        tvQty.setText(String.valueOf(quantity));

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQty.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQty.setText(String.valueOf(quantity));
        });
    }

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

        btnAddCart.setOnClickListener(v ->
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show());
    }

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

    private void bindProductDetail(ProductDetailModel p) {


        if (p.getImages() != null && !p.getImages().isEmpty()) {
            imageList.clear();
            imageList.addAll(p.getImages());
            sliderAdapter.notifyDataSetChanged();
            setupDots(imageList.size());
        }


        if (p.getPacks() != null && !p.getPacks().isEmpty()) {
            packList.clear();
            packList.addAll(p.getPacks());
            packAdapter.notifyDataSetChanged();
            selectedPack = packList.get(0);
            packAdapter.setSelectedIndex(0);
            updatePriceBlock(selectedPack);
        }


        tvSku.setText((p.getItemCode() != null ? p.getItemCode() : "") +
                (p.getItemId() != null ? "-" + p.getItemId() : ""));
        tvCategory.setText(p.getCategoryName() != null ? p.getCategoryName() : "");


        tvMoq.setText(p.getMoq() != null ? p.getMoq() : "");


        String desc = "Stay effortlessly stylish and comfortable with this casual " +
                (p.getNeck() != null ? p.getNeck().toLowerCase() : "") +
                "-neck tee made from " +
                (p.getMaterial() != null ? p.getMaterial() : "100% premium cotton") +
                ". Crafted in a single jersey " +
                (p.getFabric() != null ? p.getFabric() : "") +
                " fabric, it offers a soft, breathable feel that's perfect for all-day wear.";
        tvDescription.setText(desc);

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


    private void updatePriceBlock(ProductDetailModel.PackModel pack) {
        double gst   = pack.getGstAmount();
        double base  = pack.getSellingPrice();
        double total = pack.getTotalPrice();
        int    disc  = pack.getDiscount();

        tvTotalPrice.setText("₹" + String.format("%.2f", base));
        tvGstNote.setText(String.format("( ₹%.2f + %.2f GST )", base, gst));
        tvBuyPrice.setText("Buy for ₹" + String.format("%.0f", base));
        tvMinOrder.setText("Minimum order value : ₹" +
                String.format("%.0f", pack.getMrp()));
    }
    private void showLoader() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
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
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}