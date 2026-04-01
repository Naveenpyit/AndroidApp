package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ListWishlistResponse {
    @SerializedName("n_status") private int nStatus;
    @SerializedName("c_message") private String cMessage;
    @SerializedName("j_data") private ArrayList<WishlistItem> jData;

    public int getNStatus() { return nStatus; }
    public ArrayList<WishlistItem> getJData() { return jData; }

    public static class WishlistItem {
        @SerializedName("n_id")           private String nId;
        @SerializedName("n_category")     private String nCategory;
        @SerializedName("n_product")      private String nProduct;
        @SerializedName("n_pack")         private String nPack;  // may be null from API
        @SerializedName("c_pack_name")    private String cPackName;
        @SerializedName("n_mrp")          private String nMrp;
        @SerializedName("n_selling_price")private String nSellingPrice;
        @SerializedName("c_item_code")    private String cItemCode;
        @SerializedName("c_fabric")       private String cFabric;
        @SerializedName("c_image")        private String cImage;
        @SerializedName("n_customer")     private String nCustomer;

        // getters + setters for all fields
        public String getNId()           { return nId; }
        public String getNCategory()     { return nCategory; }
        public String getNProduct()      { return nProduct; }
        public String getNPack()         { return nPack; }
        public void   setNPack(String p) { this.nPack = p; }
        public String getCPackName()     { return cPackName; }
        public String getNMrp()          { return nMrp; }
        public String getNSellingPrice() { return nSellingPrice; }
        public String getCItemCode()     { return cItemCode; }
        public String getCFabric()       { return cFabric; }
        public String getCImage()        { return cImage; }
    }
}