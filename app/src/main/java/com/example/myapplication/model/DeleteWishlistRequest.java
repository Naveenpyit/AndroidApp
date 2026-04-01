package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class DeleteWishlistRequest {
    @SerializedName("n_user")     private String nUser;
    @SerializedName("n_wishlist") private String nWishlist;

    public DeleteWishlistRequest(String userId, String wishlistId) {
        this.nUser     = userId;
        this.nWishlist = wishlistId;
    }
}