package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageSliderAdapter;
import com.example.myapplication.adapter.PackAdapter;
import com.example.myapplication.adapter.RelatedProductAdapter;
import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.AddWishlistRequest;
import com.example.myapplication.model.AddWishlistResponse;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.model.ListWishlistRequest;
import com.example.myapplication.model.ListWishlistResponse;
import com.example.myapplication.model.ProductDetailModel;
import com.example.myapplication.model.ProductDetailRequest;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.example.myapplication.utils.WishlistManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailScreen extends AppCompatActivity {

    private static final String TAG = "ProductDetailScreen";


    private ImageView btnBack, btnSearch, btnNotification, btnCart;


    private ViewPager2         viewPager;
    private LinearLayout       dotsContainer;
    private ImageSliderAdapter sliderAdapter;
    private final List<String> imageList = new ArrayList<>();


    private ImageView ivWishlist, ivShare;


    private LinearLayout colorContainer;
    private RecyclerView rvPacks;
    private PackAdapter  packAdapter;
    private final List<ProductDetailModel.PackModel> packList = new ArrayList<>();
    private ProductDetailModel.PackModel selectedPack = null;

    private TextView     tvQty, tvStockStatus;
    private ImageView    btnMinus, btnPlus;
    private LinearLayout qtyStepperLayout;
    private MaterialButton btnAddCart;
    private int cartQty = 0;


    private TextView tvSku, tvCategory;
    private TextView tvTotalPrice, tvGstNote, tvMoq;
    private TextView tvBuyPrice, tvMinOrder;


    private ImageView ivThumbUp, ivThumbDown;


    private TextView tvDescription;
    private TextView tvSpecStyle, tvSpecCategory, tvSpecMaterial, tvSpecFabric;
    private TextView tvSpecNeck, tvSpecSeason, tvSpecColor, tvSpecSupplier, tvSpecFit;


    private RecyclerView          rvRelated;
    private RelatedProductAdapter relatedAdapter;
    private final List<ProductModel> relatedList = new ArrayList<>();


    private MaterialButton btnAddWishlist;


    private ApiService     apiService;
    private TokenManager   tokenManager;
    private WishlistManager wishlistManager;
    private ProgressDialog progressDialog;
    private String         cRandom    = "";


    private String productId  = "";
    private String categoryId = "1";
    private String packId     = "";


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

        apiService      = RetrofitClient.getClient(this);
        tokenManager    = new TokenManager(this);
        wishlistManager = WishlistManager.getInstance(this);

        fetchProductDetail();
    }


    private void initViews() {
        btnBack         = findViewById(R.id.btn_back);
        btnSearch       = findViewById(R.id.btn_search);
        btnNotification = findViewById(R.id.btn_notification);
        btnCart         = findViewById(R.id.btn_cart);

        viewPager     = findViewById(R.id.view_pager_images);
        dotsContainer = findViewById(R.id.dots_container);

        ivWishlist = findViewById(R.id.iv_wishlist_icon);
        ivShare    = findViewById(R.id.iv_share_icon);

        colorContainer = findViewById(R.id.color_container);
        rvPacks        = findViewById(R.id.rv_packs);


        qtyStepperLayout = findViewById(R.id.layout_qty);
        tvQty            = findViewById(R.id.tv_quantity);
        btnMinus         = findViewById(R.id.btn_minus);
        btnPlus          = findViewById(R.id.btn_plus);
        btnAddCart       = findViewById(R.id.btn_add_cart);

        tvStockStatus = findViewById(R.id.tv_stock_status);
        tvSku         = findViewById(R.id.tv_sku_value);
        tvCategory    = findViewById(R.id.tv_category_value);

        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvGstNote    = findViewById(R.id.tv_gst_note);
        tvMoq        = findViewById(R.id.tv_moq_value);
        tvBuyPrice   = findViewById(R.id.tv_buy_price);
        tvMinOrder   = findViewById(R.id.tv_min_order);

        ivThumbUp   = findViewById(R.id.iv_thumb_up);
        ivThumbDown = findViewById(R.id.iv_thumb_down);

        tvDescription  = findViewById(R.id.tv_description);
        tvSpecStyle    = findViewById(R.id.tv_spec_style);
        tvSpecCategory = findViewById(R.id.tv_spec_category);
        tvSpecMaterial = findViewById(R.id.tv_spec_material);
        tvSpecFabric   = findViewById(R.id.tv_spec_fabric);
        tvSpecNeck     = findViewById(R.id.tv_spec_neck);
        tvSpecSeason   = findViewById(R.id.tv_spec_season);
        tvSpecColor    = findViewById(R.id.tv_spec_color);
        tvSpecSupplier = findViewById(R.id.tv_spec_supplier);
        tvSpecFit      = findViewById(R.id.tv_spec_fit);

        rvRelated     = findViewById(R.id.rv_related_products);
        btnAddWishlist = findViewById(R.id.btn_add_wishlist);


        LinearLayout searchBarClick = findViewById(R.id.search_bar_click);
        searchBarClick.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListingScreen.class);
            intent.putExtra("openSearch", true);
            intent.putExtra("title", "Search Products");
            startActivity(intent);
        });


        btnBack.setOnClickListener(v -> onBackPressed());
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartScreen.class)));

        ivShare.setOnClickListener(v -> shareProduct());
        ivThumbUp.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());
        ivThumbDown.setOnClickListener(v ->
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show());


        ivWishlist.setOnClickListener(v -> handleWishlistToggle());


        btnAddWishlist.setOnClickListener(v -> handleWishlistToggle());


        btnAddCart.setOnClickListener(v -> {
            if (isEmpty(productId)) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedPack == null || isEmpty(packId)) {
                Toast.makeText(this, "Please select a pack", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAddCart.setVisibility(View.GONE);
            qtyStepperLayout.setVisibility(View.VISIBLE);
            tvQty.setText("1");
            callCartApi(1);
        });


        btnPlus.setOnClickListener(v -> {
            int newQty = safeInt(tvQty.getText().toString()) + 1;
            tvQty.setText(String.valueOf(newQty));
            callCartApi(newQty);
        });


        btnMinus.setOnClickListener(v -> {
            int current = safeInt(tvQty.getText().toString());
            if (current > 1) {
                int newQty = current - 1;
                tvQty.setText(String.valueOf(newQty));
                callCartApi(newQty);
            } else {

                qtyStepperLayout.setVisibility(View.GONE);
                btnAddCart.setVisibility(View.VISIBLE);
                cartQty = 0;
                callCartApi(0);
            }
        });
    }


    private void callCartApi(int quantity) {
        String userId = tokenManager.getUserId();

        if (isEmpty(categoryId) || isEmpty(packId) || isEmpty(productId)) {
            Toast.makeText(this, "Missing product info", Toast.LENGTH_SHORT).show();
            resetToAddButton();
            return;
        }
        if (isEmpty(userId)) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            resetToAddButton();
            return;
        }

        AddCartRequest request = new AddCartRequest(
                categoryId, packId, productId, String.valueOf(quantity), userId);

        apiService.addCart(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse body = response.body();
                    if (body.getStatus() == 1) {
                        cartQty = quantity;
                        Toast.makeText(ProductDetailScreen.this,
                                quantity > 0 ? "Added to cart ✓" : "Removed from cart",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailScreen.this,
                                body.getMessage() != null ? body.getMessage() : "Cart failed",
                                Toast.LENGTH_SHORT).show();
                        resetToAddButton();
                    }
                } else {
                    Toast.makeText(ProductDetailScreen.this,
                            "Request failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    resetToAddButton();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(ProductDetailScreen.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "callCartApi onFailure: " + t.getMessage());
                resetToAddButton();
            }
        });
    }


    private void resetToAddButton() {
        cartQty = 0;
        qtyStepperLayout.setVisibility(View.GONE);
        btnAddCart.setVisibility(View.VISIBLE);
    }


    private void handleWishlistToggle() {
        if (isEmpty(productId)) {
            Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmpty(categoryId) || isEmpty(packId)) {
            Toast.makeText(this, "Missing product info", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = tokenManager.getUserId();

        if (wishlistManager.isWishlisted(productId)) {
            removeFromWishlist(userId);
        } else {
            addToWishlist(userId);
        }
    }


    private void addToWishlist(String userId) {
        Log.d(TAG, "addToWishlist → category=" + categoryId
                + " product=" + productId + " pack=" + packId + " user=" + userId);

        AddWishlistRequest request = new AddWishlistRequest(
                categoryId, productId, packId, userId);

        ivWishlist.setEnabled(false);
        btnAddWishlist.setEnabled(false);

        apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {
            @Override
            public void onResponse(Call<AddWishlistResponse> call,
                                   Response<AddWishlistResponse> response) {

                ivWishlist.setEnabled(true);
                btnAddWishlist.setEnabled(true);

                Log.d(TAG, "addWishlist HTTP=" + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProductDetailScreen.this,
                            "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                AddWishlistResponse body = response.body();
                Log.d(TAG, "n_status=" + body.getNStatus()
                        + " n_wishlist_count=" + body.getNWishlistCount());

                if (body.getNStatus() != 1) {
                    Toast.makeText(ProductDetailScreen.this,
                            "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tempId = body.getNWishlistCount();

                wishlistManager.addWishlist(productId, tempId, packId);


                updateWishlistIcons(true);

                Toast.makeText(ProductDetailScreen.this,
                        "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show();

                fetchAndStoreWishlistId(userId);
            }

            @Override
            public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                ivWishlist.setEnabled(true);
                btnAddWishlist.setEnabled(true);
                Toast.makeText(ProductDetailScreen.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "addWishlist onFailure: " + t.getMessage());
            }
        });
    }

    // ── Wishlist: Fetch real n_id after add ───────────────────────────────────
    private void fetchAndStoreWishlistId(String userId) {
        ListWishlistRequest req = new ListWishlistRequest(userId);

        apiService.listWishlist(req).enqueue(new Callback<ListWishlistResponse>() {
            @Override
            public void onResponse(Call<ListWishlistResponse> call,
                                   Response<ListWishlistResponse> response) {

                if (!response.isSuccessful() || response.body() == null
                        || response.body().getNStatus() != 1
                        || response.body().getJData() == null) return;

                for (ListWishlistResponse.WishlistItem w : response.body().getJData()) {
                    if (productId.equals(w.getNProduct())) {
                        String realId = w.getNId();
                        wishlistManager.addWishlist(productId, realId, packId);
                        Log.d(TAG, "Stored real wishlistId=" + realId
                                + " for product=" + productId);
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

    // ── Wishlist: Remove ──────────────────────────────────────────────────────
    private void removeFromWishlist(String userId) {
        String wishlistId = wishlistManager.getWishlistId(productId);

        Log.d(TAG, "removeFromWishlist → wishlistId=" + wishlistId
                + " product=" + productId);

        if (isEmpty(wishlistId)) {
            Toast.makeText(this, "Cannot remove: ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ivWishlist.setEnabled(false);
        btnAddWishlist.setEnabled(false);

        DeleteWishlistRequest request = new DeleteWishlistRequest(userId, wishlistId);

        apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                ivWishlist.setEnabled(true);
                btnAddWishlist.setEnabled(true);

                if (response.isSuccessful() && response.body() != null
                        && response.body().getStatus() == 1) {

                    wishlistManager.removeWishlist(productId);
                    updateWishlistIcons(false);

                    Toast.makeText(ProductDetailScreen.this,
                            "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailScreen.this,
                            "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                ivWishlist.setEnabled(true);
                btnAddWishlist.setEnabled(true);
                Toast.makeText(ProductDetailScreen.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "removeFromWishlist onFailure: " + t.getMessage());
            }
        });
    }


    private void updateWishlistIcons(boolean isWishlisted) {

        ivWishlist.setImageResource(isWishlisted
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);


        btnAddWishlist.setIconResource(isWishlisted
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border);
        btnAddWishlist.setText(isWishlisted ? "Wishlisted ❤️" : "Add to Wishlist");
    }


    private void fetchProductDetail() {
        showLoader();
        apiService.getProductDetail(new ProductDetailRequest(cRandom, tokenManager.getUserId()))
                .enqueue(new Callback<ProductDetailResponse>() {
                    @Override
                    public void onResponse(Call<ProductDetailResponse> call,
                                           Response<ProductDetailResponse> response) {
                        dismissLoader();
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            ProductDetailResponse.DetailData data = response.body().getData();

                            if (data.getItemDetails() != null && !data.getItemDetails().isEmpty()) {
                                bindProductDetail(data.getItemDetails().get(0));
                            }

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

        productId  = p.getItemId()   != null ? p.getItemId()   : "";
        categoryId = p.getCategoryId() != null ? p.getCategoryId() : "1";


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
            packId = String.valueOf(selectedPack.getPackId());
            packAdapter.setSelectedIndex(0);
            updatePriceBlock(selectedPack);
        }


        boolean isWishlisted = wishlistManager.isWishlisted(productId);
        updateWishlistIcons(isWishlisted);

        cartQty = 0;
        qtyStepperLayout.setVisibility(View.GONE);
        btnAddCart.setVisibility(View.VISIBLE);


        tvSku.setText((p.getItemCode() != null ? p.getItemCode() : "")
                + (p.getItemId() != null ? "-" + p.getItemId() : ""));
        tvCategory.setText(p.getCategoryName() != null ? p.getCategoryName() : "");


        tvMoq.setText(p.getMoq() != null ? p.getMoq() : "");


        String desc = "Stay effortlessly stylish and comfortable with this casual "
                + (p.getNeck()     != null ? p.getNeck().toLowerCase()  : "")
                + "-neck tee made from "
                + (p.getMaterial() != null ? p.getMaterial()            : "100% premium cotton")
                + ". Crafted in a single jersey "
                + (p.getFabric()   != null ? p.getFabric()              : "")
                + " fabric, it offers a soft, breathable feel for all-day wear.";
        tvDescription.setText(desc);


        tvSpecStyle.setText(   p.getStyle()        != null ? p.getStyle()        : "-");
        tvSpecCategory.setText(p.getCategoryName() != null ? p.getCategoryName() : "-");
        tvSpecMaterial.setText(p.getMaterial()     != null ? "• " + p.getMaterial() : "-");
        tvSpecFabric.setText(  p.getFabric()       != null ? "• " + p.getFabric()   : "-");
        tvSpecNeck.setText(    p.getNeck()          != null ? p.getNeck()         : "-");
        tvSpecSeason.setText(  p.getSeason()        != null ? p.getSeason()       : "-");
        tvSpecColor.setText(   p.getColor()         != null ? p.getColor()        : "-");
        tvSpecSupplier.setText(p.getSupplier()      != null ? p.getSupplier()     : "-");
        tvSpecFit.setText(     p.getFit()           != null ? p.getFit()          : "-");
    }


    private void updatePriceBlock(ProductDetailModel.PackModel pack) {
        double gst  = pack.getGstAmount();
        double base = pack.getSellingPrice();
        double mrp  = 0;

        try {
            Object mrpObj = pack.getMrp();
            if (mrpObj instanceof String && !((String) mrpObj).isEmpty())
                mrp = Double.parseDouble((String) mrpObj);
            else if (mrpObj instanceof Double)
                mrp = (Double) mrpObj;
            else if (mrpObj instanceof Integer)
                mrp = ((Integer) mrpObj).doubleValue();
        } catch (NumberFormatException e) {
            mrp = 0;
        }

        tvTotalPrice.setText("₹" + String.format("%.2f", base));
        tvGstNote.setText(String.format("( ₹%.2f + %.2f GST )", base, gst));
        tvBuyPrice.setText("Buy for ₹" + String.format("%.0f", base));
        tvMinOrder.setText("Minimum order value : ₹" + String.format("%.0f", mrp));
    }


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


    private void setupImageSlider() {
        sliderAdapter = new ImageSliderAdapter(this, imageList);
        viewPager.setAdapter(sliderAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { updateDots(position); }
        });
    }

    private void setupDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(16, 16);
            lp.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(lp);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int selectedIndex) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsContainer.getChildAt(i);
            dot.setImageResource(i == selectedIndex
                    ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    // ── Share ─────────────────────────────────────────────────────────────────
    private void shareProduct() {
        String shareText = "🛍️ Check out this product!\n\n"
                + "📦 Item Code : " + tvSku.getText()      + "\n"
                + "🏷️ Category  : " + tvCategory.getText() + "\n"
                + "💰 Price     : " + tvTotalPrice.getText()+ "\n\n"
                + "Shop now on our app!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share product via"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }

    private int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 1; }
    }

    // ── Loader ────────────────────────────────────────────────────────────────
    private void showLoader() { showLoader("Loading..."); }

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