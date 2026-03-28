package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class UpdateCartRequest {

    @SerializedName("n_user")
    private String n_user;

    @SerializedName("n_cart")
    private String n_cart;

    @SerializedName("n_product")
    private String n_product;

    @SerializedName("n_pack")
    private String n_pack;

    @SerializedName("n_qty")
    private String n_qty;

    public UpdateCartRequest(String n_user, String n_cart, String n_product,
                             String n_pack, String n_qty) {
        this.n_user = n_user;
        this.n_cart = n_cart;
        this.n_product = n_product;
        this.n_pack = n_pack;
        this.n_qty = n_qty;
    }

    public String getN_user() {
        return n_user;
    }

    public String getN_cart() {
        return n_cart;
    }

    public String getN_product() {
        return n_product;
    }

    public String getN_pack() {
        return n_pack;
    }

    public String getN_qty() {
        return n_qty;
    }

    public void setN_user(String n_user) {
        this.n_user = n_user;
    }

    public void setN_cart(String n_cart) {
        this.n_cart = n_cart;
    }

    public void setN_product(String n_product) {
        this.n_product = n_product;
    }

    public void setN_pack(String n_pack) {
        this.n_pack = n_pack;
    }

    public void setN_qty(String n_qty) {
        this.n_qty = n_qty;
    }

    @Override
    public String toString() {
        return "UpdateCartRequest{" +
                "n_user='" + n_user + '\'' +
                ", n_cart='" + n_cart + '\'' +
                ", n_product='" + n_product + '\'' +
                ", n_pack='" + n_pack + '\'' +
                ", n_qty='" + n_qty + '\'' +
                '}';
    }
}