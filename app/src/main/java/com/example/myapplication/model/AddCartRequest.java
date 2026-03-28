package com.example.myapplication.model;
import com.google.gson.annotations.SerializedName;
public class AddCartRequest {
    @SerializedName("n_category")
    private String category;
    @SerializedName("n_pack")
    private String pack;
    @SerializedName("n_product")
    private String product;
    @SerializedName("n_qty")
    private String qty;
    @SerializedName("n_user")
    private String user;

    public AddCartRequest(String category, String pack, String product, String qty, String user) {
        this.category = category;
        this.pack = pack;
        this.product = product;
        this.qty = qty;
        this.user = user;
    }
}