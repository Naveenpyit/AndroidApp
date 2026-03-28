package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
public class BannerModel {

    @SerializedName("c_image")
    private String image;

    public BannerModel(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}