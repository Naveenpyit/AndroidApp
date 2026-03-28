package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddWishlistResponse {
    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("n_wishlist_count")
    private String nWishlistCount;

    @SerializedName("n_cart_count")
    private String nCartCount;

    @SerializedName("j_data")
    private List<Object> jData;

    public int getNStatus() { return nStatus; }
    public String getCMessage() { return cMessage; }
    public String getNWishlistCount() { return nWishlistCount; }
    public String getNCartCount() { return nCartCount; }
    public List<Object> getJData() { return jData; }
}