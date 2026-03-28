package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class HomeProductsRequest {

    @SerializedName("n_user")
    private String userId;

    public HomeProductsRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}