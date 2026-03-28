package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class DeleteWishlistRequest {
    @SerializedName("n_user")
    private String nUser;

    @SerializedName("n_wishlist")
    private String nWishlist;

    public DeleteWishlistRequest(String nUser, String nWishlist) {
        this.nUser = nUser;
        this.nWishlist = nWishlist;
    }
}