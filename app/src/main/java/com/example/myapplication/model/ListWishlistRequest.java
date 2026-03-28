package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ListWishlistRequest {
    @SerializedName("n_user")
    private String nUser;

    public ListWishlistRequest(String nUser) {
        this.nUser = nUser;
    }
}