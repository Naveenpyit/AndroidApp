package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class AddWishlistRequest {
    @SerializedName("n_category")
    private String nCategory;

    @SerializedName("n_product")
    private String nProduct;

    @SerializedName("n_pack")
    private String nPack;

    @SerializedName("n_user")
    private String nUser;

    public AddWishlistRequest(String nCategory, String nProduct, String nPack, String nUser) {
        this.nCategory = nCategory;
        this.nProduct = nProduct;
        this.nPack = nPack;
        this.nUser = nUser;
    }
}