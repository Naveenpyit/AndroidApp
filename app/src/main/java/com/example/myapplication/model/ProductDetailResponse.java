package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductDetailResponse {

    @SerializedName("n_status")  private int status;
    @SerializedName("c_message") private String message;
    @SerializedName("j_data")    private DetailData data;

    public int       getStatus()  { return status; }
    public String    getMessage() { return message; }
    public DetailData getData()   { return data; }

    public static class DetailData {
        @SerializedName("j_item_details")  private List<ProductDetailModel> itemDetails;
        @SerializedName("j_related_items") private List<ProductModel>       relatedItems;

        public List<ProductDetailModel> getItemDetails()  { return itemDetails; }
        public List<ProductModel>       getRelatedItems() { return relatedItems; }
    }
}