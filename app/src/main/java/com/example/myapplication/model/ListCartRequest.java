package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ListCartRequest {

    @SerializedName("n_user")
    private String n_user;

    public ListCartRequest(String n_user) {
        this.n_user = n_user;
    }

    public String getN_user() {
        return n_user;
    }

    public void setN_user(String n_user) {
        this.n_user = n_user;
    }

    @Override
    public String toString() {
        return "ListCartRequest{" +
                "n_user='" + n_user + '\'' +
                '}';
    }
}