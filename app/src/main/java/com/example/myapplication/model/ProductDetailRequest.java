package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ProductDetailRequest {

    @SerializedName("c_random")
    private String random;

    @SerializedName("n_user")
    private String userId;

    public ProductDetailRequest(String random, String userId) {
        this.random = random;
        this.userId = userId;
    }
}