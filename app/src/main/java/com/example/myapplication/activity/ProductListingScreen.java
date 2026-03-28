package com.example.myapplication.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.CartScreen;
import com.example.myapplication.R;
import com.example.myapplication.adapter.FilterOptionAdapter;
import com.example.myapplication.adapter.ProductListingAdapter;
import com.example.myapplication.model.FilterModel;
import com.example.myapplication.model.FiltersRequest;
import com.example.myapplication.model.FiltersResponse;
import com.example.myapplication.model.ListItemsRequest;
import com.example.myapplication.model.ListItemsResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListingScreen extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvProductCount;

    private RecyclerView rvProducts;
    private ProductListingAdapter productAdapter;
    private final ArrayList<ProductModel> productList = new ArrayList<>();

    private LinearLayout chipCategory, chipPrice, chipFit, chipTag, chipSection, chipPack;
    private TextView tvChipCategory, tvChipPrice, tvChipFit, tvChipTag;

    private BottomNavigationView bottomNav;
    private ApiService apiService;
    private ProgressDialog progressDialog;

    private int currentPage = 1;
    private int totalRecords = 0;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private static final int PAGE_LIMIT = 20;

    // categoryId is null when opened directly (show all), non-null when opened from a category
    private String categoryId = null;
    private String sectionId = "";
    private String screenTitle = "Product Listing";

    private FiltersResponse.FilterData filterData = null;

    // selectedFilters only holds USER-chosen filter values (never auto-injected categoryId)
    private final HashMap<String, List<String>> selectedFilters = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_listing_screen);

        categoryId  = getIntent().getStringExtra("categoryId");   // null = open directly
        sectionId   = getIntent().getStringExtra("sectionId");
        screenTitle = getIntent().getStringExtra("title");

        if (sectionId   == null) sectionId   = "";
        if (screenTitle == null) screenTitle  = "Product Listing";

        setupStatusBar();
        initViews();
        setupRecycler();
        setupChips();
        setupBottomNav();

        // Fetch filters using categoryId (or empty-string for "all" when opened directly)
        fetchFilters();
        fetchProducts(true);
    }

    // ─── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
        btnBack        = findViewById(R.id.btn_back);
        tvTitle        = findViewById(R.id.tv_title);
        tvProductCount = findViewById(R.id.tv_product_count);
        rvProducts     = findViewById(R.id.rv_products);

        chipCategory = findViewById(R.id.chip_sort);
        chipPrice    = findViewById(R.id.chip_price);
        chipFit      = findViewById(R.id.chip_fabric);
        chipTag      = findViewById(R.id.chip_style);
        chipSection  = findViewById(R.id.ll_section_options);
        chipPack     = findViewById(R.id.ll_pack_options);

        tvChipCategory = findViewById(R.id.tv_sort_text);
        tvChipPrice    = findViewById(R.id.tv_price_text);
        tvChipFit      = findViewById(R.id.tv_fabric_text);
        tvChipTag      = findViewById(R.id.tv_style_text);

        apiService = RetrofitClient.getClient(this);
        tvTitle.setText(screenTitle);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void setupRecycler() {
        GridLayoutManager lm = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(lm);
        productAdapter = new ProductListingAdapter(this, productList);
        rvProducts.setAdapter(productAdapter);

        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (!isLoading && hasMorePages && dy > 0) {
                    int visible = lm.getChildCount();
                    int total   = lm.getItemCount();
                    int first   = lm.findFirstVisibleItemPosition();
                    if ((visible + first) >= total - 4) {
                        currentPage++;
                        fetchProducts(false);
                    }
                }
            }
        });
    }

    // ─── Chips ─────────────────────────────────────────────────────────────────

    private void setupChips() {
        chipCategory.setOnClickListener(v -> showFilterDialog("Category", convertCategory(), "category", tvChipCategory, "Category"));
        chipPrice   .setOnClickListener(v -> showFilterDialog("Price",    convertPrice(),    "price",    tvChipPrice,    "Price"));
        chipFit     .setOnClickListener(v -> showFilterDialog("Fit",      convertFit(),      "fit",      tvChipFit,      "Fit"));
        chipTag     .setOnClickListener(v -> showFilterDialog("Tags",     convertTag(),      "tag",      tvChipTag,      "Tags"));
        chipSection .setOnClickListener(v -> showFilterDialogNoLabel("Section", convertSection(), "section"));
        chipPack    .setOnClickListener(v -> showFilterDialogNoLabel("Pack",    convertPack(),    "pack"));
    }

    // ─── Convert filter data ───────────────────────────────────────────────────

    private List<FilterModel.FilterOption> convertCategory() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getCategoryFilters() != null) {
            List<String> sel = selectedFilters.containsKey("category") ? selectedFilters.get("category") : new ArrayList<>();
            for (FiltersResponse.CategoryFilter f : filterData.getCategoryFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getId());
                list.add(new FilterModel.FilterOption(f.getId(), f.getName(), f.getProductCount(), isSelected));
            }
        }
        return list;
    }

    private List<FilterModel.FilterOption> convertPrice() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getPriceFilters() != null) {
            List<String> sel = selectedFilters.containsKey("price") ? selectedFilters.get("price") : new ArrayList<>();
            for (FiltersResponse.PriceFilter f : filterData.getPriceFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getValue());
                list.add(new FilterModel.FilterOption(f.getValue(), f.getPriceRange(), 0, isSelected));
            }
        }
        return list;
    }

    private List<FilterModel.FilterOption> convertFit() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getFitFilters() != null) {
            List<String> sel = selectedFilters.containsKey("fit") ? selectedFilters.get("fit") : new ArrayList<>();
            for (FiltersResponse.FitFilter f : filterData.getFitFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getId());
                list.add(new FilterModel.FilterOption(f.getId(), f.getFit(), f.getFitCount(), isSelected));
            }
        }
        return list;
    }

    private List<FilterModel.FilterOption> convertTag() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getTagFilters() != null) {
            List<String> sel = selectedFilters.containsKey("tag") ? selectedFilters.get("tag") : new ArrayList<>();
            for (FiltersResponse.TagFilter f : filterData.getTagFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getId());
                list.add(new FilterModel.FilterOption(f.getId(), f.getTag(), 0, isSelected));
            }
        }
        return list;
    }

    private List<FilterModel.FilterOption> convertSection() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getSectionFilters() != null) {
            List<String> sel = selectedFilters.containsKey("section") ? selectedFilters.get("section") : new ArrayList<>();
            for (FiltersResponse.SectionFilter f : filterData.getSectionFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getId());
                list.add(new FilterModel.FilterOption(f.getId(), f.getName(), 0, isSelected));
            }
        }
        return list;
    }

    private List<FilterModel.FilterOption> convertPack() {
        List<FilterModel.FilterOption> list = new ArrayList<>();
        if (filterData != null && filterData.getPacksFilters() != null) {
            List<String> sel = selectedFilters.containsKey("pack") ? selectedFilters.get("pack") : new ArrayList<>();
            for (FiltersResponse.PackFilter f : filterData.getPacksFilters()) {
                boolean isSelected = sel != null && sel.contains(f.getId());
                list.add(new FilterModel.FilterOption(f.getId(), f.getName(), 0, isSelected));
            }
        }
        return list;
    }

    // ─── Beautiful Bottom-Sheet Filter Dialog ──────────────────────────────────

    private void showFilterDialog(String title,
                                  List<FilterModel.FilterOption> options,
                                  String filterKey,
                                  TextView chipLabel,
                                  String defaultLabel) {

        if (options == null || options.isEmpty()) {
            Toast.makeText(this, "No options available", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter_bottom_sheet);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        }

        // Views inside dialog
        TextView tvDialogTitle = dialog.findViewById(R.id.tv_filter_title);
        RecyclerView rvOptions = dialog.findViewById(R.id.rv_filter_options);
        MaterialButton btnClear = dialog.findViewById(R.id.btn_filter_clear);
        MaterialButton btnApply = dialog.findViewById(R.id.btn_filter_apply);
        ImageView btnClose      = dialog.findViewById(R.id.btn_filter_close);

        tvDialogTitle.setText(title);

        // Make a working copy so user can cancel without affecting state
        List<FilterModel.FilterOption> workingCopy = new ArrayList<>();
        for (FilterModel.FilterOption opt : options) {
            workingCopy.add(new FilterModel.FilterOption(
                    opt.getOptionId(), opt.getOptionName(), opt.getCount(), opt.isSelected()));
        }

        FilterOptionAdapter adapter = new FilterOptionAdapter(this, workingCopy);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));
        rvOptions.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnClear.setOnClickListener(v -> {
            for (FilterModel.FilterOption opt : workingCopy) opt.setSelected(false);
            adapter.notifyDataSetChanged();
        });

        btnApply.setOnClickListener(v -> {
            List<String> selectedIds   = new ArrayList<>();
            List<String> selectedNames = new ArrayList<>();

            for (FilterModel.FilterOption opt : workingCopy) {
                if (opt.isSelected()) {
                    selectedIds.add(opt.getOptionId());
                    selectedNames.add(opt.getOptionName());
                }
            }

            if (selectedIds.isEmpty()) {
                selectedFilters.remove(filterKey);
                chipLabel.setText(defaultLabel);
                updateChipActive(chipLabel, false);
            } else {
                selectedFilters.put(filterKey, selectedIds);
                String labelText = selectedNames.size() == 1
                        ? selectedNames.get(0)
                        : selectedNames.get(0) + " +" + (selectedNames.size() - 1);
                chipLabel.setText(labelText);
                updateChipActive(chipLabel, true);
            }

            dialog.dismiss();
            reloadProducts();
        });

        dialog.show();
    }

    /** Same dialog but without updating a chip label TextView */
    private void showFilterDialogNoLabel(String title,
                                         List<FilterModel.FilterOption> options,
                                         String filterKey) {
        showFilterDialog(title, options, filterKey, new TextView(this), title);
    }

    private void updateChipActive(TextView chipLabel, boolean active) {
        // Highlight parent chip when a filter is active
        if (chipLabel.getParent() instanceof LinearLayout) {
            LinearLayout parent = (LinearLayout) chipLabel.getParent();
            parent.setBackgroundResource(active ? R.drawable.chip_red_bg : R.drawable.chip_outline_bg);
            chipLabel.setTextColor(active
                    ? getResources().getColor(R.color.white)
                    : getResources().getColor(android.R.color.darker_gray));
        }
    }

    // ─── Reload ────────────────────────────────────────────────────────────────

    private void reloadProducts() {
        currentPage = 1;
        productList.clear();
        productAdapter.notifyDataSetChanged();
        fetchProducts(true);
    }

    // ─── API: Filters ──────────────────────────────────────────────────────────

    private void fetchFilters() {
        // When opened directly (no categoryId), pass empty string → API returns ALL filter options
        String catParam = (categoryId != null && !categoryId.isEmpty()) ? categoryId : "";
        apiService.getFilters(new FiltersRequest(catParam, "1"))
                .enqueue(new Callback<FiltersResponse>() {
                    @Override
                    public void onResponse(Call<FiltersResponse> call, Response<FiltersResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            filterData = response.body().getData();
                        }
                    }
                    @Override
                    public void onFailure(Call<FiltersResponse> call, Throwable t) { /* silent */ }
                });
    }

    // ─── API: Products ─────────────────────────────────────────────────────────

    private void fetchProducts(boolean isFirstPage) {
        if (isLoading) return;
        isLoading = true;

        if (isFirstPage) showFullLoader();
        else if (productAdapter != null) productAdapter.showLoading();

        List<Map<String, List<String>>> filters = new ArrayList<>();

        // ── CATEGORY ──────────────────────────────────────────────────────────
        // Rule:
        //   • User has chosen categories via chip  → use those IDs
        //   • Opened FROM a category (categoryId != null), no chip selection → use categoryId
        //   • Opened DIRECTLY (categoryId == null), no chip selection         → empty list = ALL
        Map<String, List<String>> categoryMap = new HashMap<>();
        if (selectedFilters.containsKey("category") && !selectedFilters.get("category").isEmpty()) {
            categoryMap.put("category", new ArrayList<>(selectedFilters.get("category")));
        } else if (categoryId != null && !categoryId.isEmpty()) {
            categoryMap.put("category", Collections.singletonList(categoryId));
        } else {
            categoryMap.put("category", new ArrayList<>()); // ALL
        }
        filters.add(categoryMap);

        // ── PACK ──────────────────────────────────────────────────────────────
        Map<String, List<String>> packMap = new HashMap<>();
        packMap.put("pack", selectedFilters.containsKey("pack")
                ? new ArrayList<>(selectedFilters.get("pack")) : new ArrayList<>());
        filters.add(packMap);

        // ── FIT ───────────────────────────────────────────────────────────────
        Map<String, List<String>> fitMap = new HashMap<>();
        fitMap.put("fit", selectedFilters.containsKey("fit")
                ? new ArrayList<>(selectedFilters.get("fit")) : new ArrayList<>());
        filters.add(fitMap);

        // ── MAIN ──────────────────────────────────────────────────────────────
        Map<String, List<String>> mainMap = new HashMap<>();
        mainMap.put("main", new ArrayList<>());
        filters.add(mainMap);

        // ── SECTION ───────────────────────────────────────────────────────────
        Map<String, List<String>> sectionMap = new HashMap<>();
        List<String> sectionList = selectedFilters.containsKey("section")
                ? new ArrayList<>(selectedFilters.get("section")) : new ArrayList<>();
        if (sectionId != null && !sectionId.isEmpty() && !sectionList.contains(sectionId)) {
            sectionList.add(sectionId);
        }
        sectionMap.put("section", sectionList);
        filters.add(sectionMap);

        // ── PRICE ─────────────────────────────────────────────────────────────
        Map<String, List<String>> priceMap = new HashMap<>();
        priceMap.put("price", selectedFilters.containsKey("price")
                ? new ArrayList<>(selectedFilters.get("price")) : new ArrayList<>());
        filters.add(priceMap);

        // ── TAG ───────────────────────────────────────────────────────────────
        Map<String, List<String>> tagMap = new HashMap<>();
        tagMap.put("tag", selectedFilters.containsKey("tag")
                ? new ArrayList<>(selectedFilters.get("tag")) : new ArrayList<>());
        filters.add(tagMap);

        ListItemsRequest request = new ListItemsRequest(
                "10",
                String.valueOf(currentPage),
                String.valueOf(PAGE_LIMIT),
                "",
                filters
        );

        apiService.getItems(request).enqueue(new Callback<ListItemsResponse>() {
            @Override
            public void onResponse(Call<ListItemsResponse> call, Response<ListItemsResponse> response) {
                isLoading = false;
                hideFullLoader();
                if (productAdapter != null) productAdapter.hideLoading();

                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {

                    ListItemsResponse.ItemsData data = response.body().getData();
                    totalRecords = data.getTotalRecord();
                    tvProductCount.setText(totalRecords + " Products");

                    List<ProductModel> newItems = data.getResults();
                    if (newItems != null && !newItems.isEmpty()) {
                        int start = productList.size();
                        productList.addAll(newItems);
                        if (productAdapter != null) {
                            productAdapter.notifyItemRangeInserted(start, newItems.size());
                        }
                    } else if (isFirstPage) {
                        Toast.makeText(ProductListingScreen.this,
                                "No products found", Toast.LENGTH_SHORT).show();
                    }
                    hasMorePages = productList.size() < totalRecords;

                } else {
                    Toast.makeText(ProductListingScreen.this,
                            "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListItemsResponse> call, Throwable t) {
                isLoading = false;
                hideFullLoader();
                if (productAdapter != null) productAdapter.hideLoading();
                Toast.makeText(ProductListingScreen.this,
                        "Network error", Toast.LENGTH_LONG).show();
                if (currentPage > 1) currentPage--;
            }
        });
    }

    // ─── Loader ────────────────────────────────────────────────────────────────

    private void showFullLoader() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading products...");
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    private void hideFullLoader() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    // ─── Bottom Nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_cart) {
                startActivity(new Intent(this, CartScreen.class));
                return true;
            }
            return false;
        });
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        }
    }
}