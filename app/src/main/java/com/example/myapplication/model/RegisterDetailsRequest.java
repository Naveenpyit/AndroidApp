package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class RegisterDetailsRequest {

    @SerializedName("n_mobile")
    private String nMobile;

    public RegisterDetailsRequest(String nMobile) {
        this.nMobile = nMobile;
    }

    public String getNMobile() { return nMobile; }
    public void setNMobile(String nMobile) { this.nMobile = nMobile; }
}