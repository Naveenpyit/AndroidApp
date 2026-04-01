package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class AddWishlistRequest {
    @SerializedName("n_category") private String n_category;
    @SerializedName("n_product")  private String n_product;
    @SerializedName("n_pack")     private String n_pack;
    @SerializedName("n_user")     private String n_user;  // ← MUST be "n_user"

    public AddWishlistRequest(String category, String product, String pack, String userId) {
        this.n_category = category;
        this.n_product  = product;
        this.n_pack     = pack;
        this.n_user     = userId;
    }
}