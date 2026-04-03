package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.adapter.CategoryAdapter;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.adapter.SectionAdapter;
import com.example.myapplication.model.BannerModel;
import com.example.myapplication.model.CategoryModel;
import com.example.myapplication.model.DataModel;
import com.example.myapplication.model.HomeProductsRequest;
import com.example.myapplication.model.HomeProductsResponse;
import com.example.myapplication.model.ListCategoryRequest;
import com.example.myapplication.model.ListCategoryResponse;
import com.example.myapplication.model.ProductModel;
import com.example.myapplication.model.SectionModel;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.SearchUtils;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView catogery_cycle, banner_cycle, trending_cycle;
    private RecyclerView mens_collection, women_collection;
    private RecyclerView rv_products;

    private TextView tab_all, tab_new_arrivals, tab_best_selling, tab_five_star;
    private EditText search_input;

    private final ArrayList<CategoryModel> categoryList     = new ArrayList<>();
    private final ArrayList<BannerModel>   bannerList       = new ArrayList<>();
    private final ArrayList<SectionModel>  sectionList      = new ArrayList<>();
    private final ArrayList<SectionModel>  mensList         = new ArrayList<>();
    private final ArrayList<SectionModel>  womensList       = new ArrayList<>();
    private final ArrayList<ProductModel>  allProductsList  = new ArrayList<>();
    private final ArrayList<ProductModel>  newArrivalsList  = new ArrayList<>();
    private final ArrayList<ProductModel>  bestSellingList  = new ArrayList<>();
    private final ArrayList<ProductModel>  masterProductList = new ArrayList<>();
    private final ArrayList<ProductModel>  displayList       = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private BannerAdapter   bannerAdapter;
    private SectionAdapter  sectionAdapter, mensAdapter, womensAdapter;
    private ProductAdapter  productAdapter;

    private ApiService apiService;
    private ProgressDialog progressDialog;
    private int currentTab = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        catogery_cycle   = findViewById(R.id.catogery_cycle);
        banner_cycle     = findViewById(R.id.banner_cycle);
        trending_cycle   = findViewById(R.id.trending_cycle);
        mens_collection  = findViewById(R.id.mens_collection);
        women_collection = findViewById(R.id.women_collection);
        rv_products      = findViewById(R.id.rv_products);
        search_input     = findViewById(R.id.search_input);
        tab_all          = findViewById(R.id.tab_all);
        tab_new_arrivals = findViewById(R.id.tab_new_arrivals);
        tab_best_selling = findViewById(R.id.tab_best_selling);
        tab_five_star    = findViewById(R.id.tab_five_star);

        setupStatusBar();
        setupBottomNav();
        apiService = RetrofitClient.getClient(this);

        // Category — click inside adapter navigates to ProductListingActivity
        catogery_cycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(this, categoryList);
        catogery_cycle.setAdapter(categoryAdapter);

        banner_cycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bannerAdapter = new BannerAdapter(this, bannerList);
        banner_cycle.setAdapter(bannerAdapter);

        trending_cycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sectionAdapter = new SectionAdapter(this, sectionList, "1");
        trending_cycle.setAdapter(sectionAdapter);

        // Men sections — parentCategoryId = "1"
        mens_collection.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mensAdapter = new SectionAdapter(this, mensList, "1");
        mens_collection.setAdapter(mensAdapter);

        // Women sections — parentCategoryId = "7"
        women_collection.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        womensAdapter = new SectionAdapter(this, womensList, "7");
        women_collection.setAdapter(womensAdapter);



        rv_products.setLayoutManager(new GridLayoutManager(this, 2));
        TokenManager tokenManager = new TokenManager(this);
        productAdapter = new ProductAdapter(this, displayList, tokenManager);
        rv_products.setAdapter(productAdapter);
        rv_products.setNestedScrollingEnabled(false);

        tab_all.setOnClickListener(v -> showTab(0));
        tab_new_arrivals.setOnClickListener(v -> showTab(1));
        tab_five_star.setOnClickListener(v -> showTab(2));
        tab_best_selling.setOnClickListener(v -> showTab(3));

        findViewById(R.id.tv_mens_label).setOnClickListener(v ->
                openListing("1", "", "Men's Collections"));
        findViewById(R.id.tv_womens_label).setOnClickListener(v ->
                openListing("7", "", "Women's Collections"));

        setupSearch();
        DashboardApi();
        HomeProductsApi();
    }

    private void openListing(String categoryId, String sectionId, String title) {
        Intent i = new Intent(this, ProductListingScreen.class);
        i.putExtra("categoryId", categoryId);
        i.putExtra("sectionId",  sectionId);
        i.putExtra("title",      title);
        startActivity(i);
    }

    private void setupSearch() {
        search_input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<ProductModel> result = s.toString().trim().isEmpty()
                        ? new ArrayList<>(masterProductList)
                        : SearchUtils.filterAndSort(masterProductList, s.toString());
                displayList.clear();
                displayList.addAll(result);
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showTab(int idx) {
        currentTab = idx;
        resetTabStyles();
        search_input.setText("");
        switch (idx) {
            case 0: activateTab(tab_all);          masterProductList.clear(); masterProductList.addAll(allProductsList);  break;
            case 1: activateTab(tab_new_arrivals); masterProductList.clear(); masterProductList.addAll(newArrivalsList);  break;
            case 2: activateTab(tab_five_star);    masterProductList.clear(); masterProductList.addAll(allProductsList);  break;
            case 3: activateTab(tab_best_selling); masterProductList.clear(); masterProductList.addAll(bestSellingList);  break;
        }
        displayList.clear();
        displayList.addAll(masterProductList);
        productAdapter.notifyDataSetChanged();
    }

    private void resetTabStyles() {
        for (TextView t : new TextView[]{tab_all, tab_new_arrivals, tab_five_star, tab_best_selling}) {
            t.setTextColor(getResources().getColor(android.R.color.darker_gray));
            t.setBackgroundResource(R.drawable.tab_inactive_bg);
        }
    }

    private void activateTab(TextView t) {
        t.setTextColor(getResources().getColor(R.color.red_primary));
        t.setBackgroundResource(R.drawable.tab_active_bg);
    }

    private void DashboardApi() {
        showLoader();
        apiService.getCategoryList(new ListCategoryRequest("28")).enqueue(new Callback<ListCategoryResponse>() {
            @Override
            public void onResponse(Call<ListCategoryResponse> call, Response<ListCategoryResponse> response) {
                dismissLoader();
                if (response.isSuccessful() && response.body() != null) {
                    List<DataModel> dataList = response.body().getData();
                    if (dataList != null && !dataList.isEmpty()) {
                        DataModel data = dataList.get(0);
                        bannerList.clear(); categoryList.clear();
                        sectionList.clear(); mensList.clear(); womensList.clear();

                        if (data.getBanners() != null) bannerList.add(data.getBanners());
                        if (data.getCategories() != null) {
                            categoryList.addAll(data.getCategories());
                            for (CategoryModel cat : data.getCategories()) {
                                if (cat.getSections() == null || cat.getSections().isEmpty()) continue;
                                sectionList.addAll(cat.getSections());
                                String n = cat.getName();
                                if (n != null) {
                                    n = n.toLowerCase().trim();
                                    if (n.equals("men"))        mensList.addAll(cat.getSections());
                                    else if (n.equals("women")) womensList.addAll(cat.getSections());
                                }
                            }
                        }
                        bannerAdapter.notifyDataSetChanged();
                        categoryAdapter.notifyDataSetChanged();
                        sectionAdapter.notifyDataSetChanged();
                        mensAdapter.notifyDataSetChanged();
                        womensAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override public void onFailure(Call<ListCategoryResponse> call, Throwable t) { dismissLoader(); }
        });
    }

    private void HomeProductsApi() {
        showLoader();
        apiService.getHomeProducts(new HomeProductsRequest("28")).enqueue(new Callback<HomeProductsResponse>() {
            @Override
            public void onResponse(Call<HomeProductsResponse> call, Response<HomeProductsResponse> response) {
                dismissLoader();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    HomeProductsResponse.HomeProductsData data = response.body().getData();
                    allProductsList.clear(); newArrivalsList.clear(); bestSellingList.clear();
                    if (data.getAllProducts() != null) allProductsList.addAll(data.getAllProducts());
                    if (data.getNewArrivals() != null) newArrivalsList.addAll(data.getNewArrivals());
                    if (data.getBestSelling() != null) bestSellingList.addAll(data.getBestSelling());
                    showTab(0);
                }
            }
            @Override public void onFailure(Call<HomeProductsResponse> call, Throwable t) { dismissLoader(); }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)    return true;
            if (id == R.id.nav_orders)  { startActivity(new Intent(this, ProductListingScreen.class));  return true; }
            if (id == R.id.nav_cart)    { startActivity(new Intent(this, CartScreen.class));     return true; }
            if (id == R.id.nav_account) { startActivity(new Intent(this, AccountScreen.class));  return true; }
            return false;
        });
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
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary));
        }
    }
}