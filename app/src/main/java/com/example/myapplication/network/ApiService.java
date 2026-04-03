package com.example.myapplication.network;


import com.example.myapplication.model.AddCartRequest;
import com.example.myapplication.model.AddWishlistRequest;
import com.example.myapplication.model.AddWishlistResponse;
import com.example.myapplication.model.CityListRequest;
import com.example.myapplication.model.CityListResponse;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteCartRequest;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.model.FiltersRequest;
import com.example.myapplication.model.FiltersResponse;
import com.example.myapplication.model.HomeProductsRequest;
import com.example.myapplication.model.HomeProductsResponse;
import com.example.myapplication.model.ListCartRequest;
import com.example.myapplication.model.ListCartResponse;
import com.example.myapplication.model.ListCategoryRequest;
import com.example.myapplication.model.ListCategoryResponse;
import com.example.myapplication.model.ListItemsRequest;
import com.example.myapplication.model.ListItemsResponse;
import com.example.myapplication.model.ListWishlistRequest;
import com.example.myapplication.model.ListWishlistResponse;
import com.example.myapplication.model.ProductDetailRequest;
import com.example.myapplication.model.ProductDetailResponse;
import com.example.myapplication.model.RefreshTokenRequest;
import com.example.myapplication.model.RefreshTokenResponse;
import com.example.myapplication.model.RegisterDetailsRequest;
import com.example.myapplication.model.RegisterDetailsResponse;
import com.example.myapplication.model.RegisterInsertRequest;
import com.example.myapplication.model.RegisterInsertResponse;
import com.example.myapplication.model.SendOtpRequest;
import com.example.myapplication.model.SendOtpResponse;
import com.example.myapplication.model.StateListResponse;
import com.example.myapplication.model.UpdateCartRequest;
import com.example.myapplication.model.VerifyOtpRequest;
import com.example.myapplication.model.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("sendotp")
    Call<SendOtpResponse> sendOtp(@Body SendOtpRequest request);

    @POST("verifyotp")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("refreshtoken")
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("list-category")
    Call<ListCategoryResponse> getCategoryList(@Body ListCategoryRequest request);

    @POST("list-home-products")
    Call<HomeProductsResponse> getHomeProducts(@Body HomeProductsRequest request);


    @POST("list-items")
    Call<ListItemsResponse> getItems(@Body ListItemsRequest request);

    @POST("items-filters")
    Call<FiltersResponse> getFilters(@Body FiltersRequest request);


    @POST("items-details")
    Call<ProductDetailResponse> getProductDetail(@Body ProductDetailRequest request);

    @POST("add-cart")
    Call<CommonResponse> addCart(@Body AddCartRequest request);

    @POST("list-cart")
    Call<ListCartResponse> listCart(@Body ListCartRequest request);

    @POST("delete-cart")
    Call<CommonResponse> deleteCart(@Body DeleteCartRequest request);

    @POST("update-cart")
    Call<CommonResponse> updateCart(@Body UpdateCartRequest request);

    @POST("add-wishlist")
    Call<AddWishlistResponse> addWishlist(@Body AddWishlistRequest request);
    @POST("delete-wishlist")
    Call<CommonResponse> deleteWishlist(@Body DeleteWishlistRequest request);

    @POST("list-wishlist")
    Call<ListWishlistResponse> listWishlist(@Body ListWishlistRequest request);

    @POST("register-details")
    Call<RegisterDetailsResponse> getRegisterDetails(
            @Body RegisterDetailsRequest request);

    // ── Registration: save each step ─────────────────────────────────────────
    @POST("register-insert")
    Call<RegisterInsertResponse> registerInsert(
            @Body RegisterInsertRequest request);

    // ── Location: state list ──────────────────────────────────────────────────
    @GET("state-list")
    Call<StateListResponse> getStateList();

    // ── Location: city list for a state ──────────────────────────────────────
    @POST("city-list")
    Call<CityListResponse> getCityList(@Body CityListRequest request);
}

