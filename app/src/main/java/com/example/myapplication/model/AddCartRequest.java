package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class AddCartRequest {

    @SerializedName("n_category")
    private String nCategory;

    @SerializedName("n_pack")
    private String nPack;

    @SerializedName("n_product")
    private String nProduct;

    @SerializedName("n_qty")
    private String nQty;

    @SerializedName("n_user")
    private String nUser;

    public AddCartRequest(String nCategory, String nPack, String nProduct,
                          String nQty, String nUser) {
        this.nCategory = nCategory;
        this.nPack     = nPack;
        this.nProduct  = nProduct;
        this.nQty      = nQty;
        this.nUser     = nUser;
    }


    public String getNCategory() { return nCategory; }
    public String getNPack()     { return nPack; }
    public String getNProduct()  { return nProduct; }
    public String getNQty()      { return nQty; }
    public String getNUser()     { return nUser; }

    @Override
    public String toString() {
        return "{n_category=" + nCategory
                + ", n_pack=" + nPack
                + ", n_product=" + nProduct
                + ", n_qty=" + nQty
                + ", n_user=" + nUser + "}";
    }
}