package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataModel {

    @SerializedName("banners")
    private BannerModel banners;

    @SerializedName("categories")
    private List<CategoryModel> categories;

    public BannerModel getBanners() {
        return banners;
    }

    public List<CategoryModel> getCategories() {
        return categories;
    }
}
