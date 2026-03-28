package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeProductsResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("j_data")
    private HomeProductsData data;

    public int getStatus()            { return status; }
    public String getMessage()        { return message; }
    public HomeProductsData getData() { return data; }

    public static class HomeProductsData {

        @SerializedName("j_all_products")
        private List<ProductModel> allProducts;

        @SerializedName("j_new_arrivals")
        private List<ProductModel> newArrivals;

        @SerializedName("j_best_selling")
        private List<ProductModel> bestSelling;

        public List<ProductModel> getAllProducts() { return allProducts; }
        public List<ProductModel> getNewArrivals() { return newArrivals; }
        public List<ProductModel> getBestSelling() { return bestSelling; }
    }
}