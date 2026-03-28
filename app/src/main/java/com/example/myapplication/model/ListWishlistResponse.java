package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ListWishlistResponse {
    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("j_data")
    private List<WishlistItem> jData;

    public int getNStatus() { return nStatus; }
    public String getCMessage() { return cMessage; }
    public List<WishlistItem> getJData() { return jData; }

    public static class WishlistItem {
        @SerializedName("n_id")
        private String nId;

        @SerializedName("n_customer")
        private String nCustomer;

        @SerializedName("n_category")
        private String nCategory;

        @SerializedName("n_product")
        private String nProduct;

        @SerializedName("n_status")
        private String nStatus;

        @SerializedName("d_created")
        private String dCreated;

        @SerializedName("n_gst")
        private String nGst;

        @SerializedName("c_pack_name")
        private String cPackName;

        @SerializedName("n_mrp")
        private String nMrp;

        @SerializedName("n_selling_price")
        private String nSellingPrice;

        @SerializedName("category_name")
        private String categoryName;

        @SerializedName("c_item_code")
        private String cItemCode;

        @SerializedName("c_fabric")
        private String cFabric;

        @SerializedName("c_image")
        private String cImage;

        @SerializedName("n_gst_value")
        private String nGstValue;

        // Getters
        public String getNId() { return nId; }
        public String getNCustomer() { return nCustomer; }
        public String getNCategory() { return nCategory; }
        public String getNProduct() { return nProduct; }
        public String getNStatus() { return nStatus; }
        public String getDCreated() { return dCreated; }
        public String getNGst() { return nGst; }
        public String getCPackName() { return cPackName; }
        public String getNMrp() { return nMrp; }
        public String getNSellingPrice() { return nSellingPrice; }
        public String getCategoryName() { return categoryName; }
        public String getCItemCode() { return cItemCode; }
        public String getCFabric() { return cFabric; }
        public String getCImage() { return cImage; }
        public String getNGstValue() { return nGstValue; }
    }
}