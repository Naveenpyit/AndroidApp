package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class DeleteCartRequest {

    @SerializedName("n_cart")
    private String n_cart;

    @SerializedName("n_user")
    private String n_user;

    public DeleteCartRequest(String n_cart, String n_user) {
        this.n_cart = n_cart;
        this.n_user = n_user;
    }

    public String getN_cart() {
        return n_cart;
    }

    public String getN_user() {
        return n_user;
    }

    public void setN_cart(String n_cart) {
        this.n_cart = n_cart;
    }

    public void setN_user(String n_user) {
        this.n_user = n_user;
    }

    @Override
    public String toString() {
        return "DeleteCartRequest{" +
                "n_cart='" + n_cart + '\'' +
                ", n_user='" + n_user + '\'' +
                '}';
    }
}