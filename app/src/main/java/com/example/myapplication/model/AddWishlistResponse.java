package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class AddWishlistResponse {

    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("n_wishlist_count")
    private String nWishlistCount;

    @SerializedName("n_cart_count")
    private String nCartCount;

    public int    getNStatus()        { return nStatus; }
    public String getCMessage()       { return cMessage; }
    public String getNWishlistCount() { return nWishlistCount; }
    public String getNCartCount()     { return nCartCount; }
}