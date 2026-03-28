package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ListCategoryRequest {

    @SerializedName("n_user")
    private String nUser;

    public ListCategoryRequest(String nUser) {
        this.nUser = nUser;
    }
}