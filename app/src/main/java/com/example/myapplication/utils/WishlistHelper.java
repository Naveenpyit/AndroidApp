package com.example.myapplication.utils;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.AddWishlistRequest;
import com.example.myapplication.model.AddWishlistResponse;
import com.example.myapplication.model.CommonResponse;
import com.example.myapplication.model.DeleteWishlistRequest;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistHelper {

    public interface WishlistCallback {
        void onSuccess(boolean isAdded, String wishlistId);
        void onFailure(String error);
    }

    public static void toggleWishlist(
            Context context,
            String category,
            String product,
            String pack,
            String userId,
            boolean isAlreadyWishlisted,
            String wishlistId,
            WishlistCallback callback
    ) {

        ApiService apiService = RetrofitClient.getClient(context);

        if (isAlreadyWishlisted) {

            // 🔴 REMOVE
            DeleteWishlistRequest request =
                    new DeleteWishlistRequest(userId, wishlistId);

            apiService.deleteWishlist(request).enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                    if (response.isSuccessful() && response.body() != null
                            && response.body().getStatus() == 1) {

                        callback.onSuccess(false, null);
                    } else {
                        callback.onFailure("Remove failed");
                    }
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    callback.onFailure(t.getMessage());
                }
            });

        } else {

            // 🟢 ADD
            // ✅ FIX: ONLY 3 params
            AddWishlistRequest request =
                    new AddWishlistRequest(category, product, pack,"10");


            apiService.addWishlist(request).enqueue(new Callback<AddWishlistResponse>() {

                @Override
                public void onResponse(Call<AddWishlistResponse> call,
                                       Response<AddWishlistResponse> response) {

                    if (response.isSuccessful() && response.body() != null
                            && response.body().getNStatus() == 1) {

                        String newWishlistId = response.body().getNWishlistCount(); // use actual field

                        callback.onSuccess(true, newWishlistId);

                    } else {
                        callback.onFailure("Add failed");
                    }
                }

                @Override
                public void onFailure(Call<AddWishlistResponse> call, Throwable t) {
                    callback.onFailure(t.getMessage());
                }
            });
        }
    }

}