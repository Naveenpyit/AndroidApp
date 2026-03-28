package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class FiltersRequest {

    @SerializedName("n_category")
    private String categoryId;

    @SerializedName("n_user")
    private String userId;

    public FiltersRequest(String categoryId, String userId) {
        this.categoryId = categoryId;
        this.userId     = userId;
    }
}