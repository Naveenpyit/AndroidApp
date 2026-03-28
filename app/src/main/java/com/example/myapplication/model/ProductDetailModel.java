package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductDetailModel {

    @SerializedName("c_random")        private String random;
    @SerializedName("n_pack_id")       private String packId;
    @SerializedName("n_item_id")       private String itemId;
    @SerializedName("c_item_code")     private String itemCode;
    @SerializedName("c_item_name")     private String itemName;
    @SerializedName("c_shade")         private String shade;
    @SerializedName("c_description")   private String description;
    @SerializedName("c_fashion")       private String fashion;
    @SerializedName("c_style")         private String style;
    @SerializedName("c_uom")           private String uom;
    @SerializedName("n_hsn")           private String hsn;
    @SerializedName("c_material")      private String material;
    @SerializedName("c_fabric")        private String fabric;
    @SerializedName("c_neck")          private String neck;
    @SerializedName("c_sleeve")        private String sleeve;
    @SerializedName("c_season")        private String season;
    @SerializedName("c_color")         private String color;
    @SerializedName("c_supplier")      private String supplier;
    @SerializedName("c_fit")           private String fit;
    @SerializedName("c_description2")  private String description2;
    @SerializedName("n_moq")           private String moq;
    @SerializedName("n_category_id")   private String categoryId;
    @SerializedName("c_category_name") private String categoryName;
    @SerializedName("c_subcategory_name") private String subCategoryName;
    @SerializedName("n_gst")           private String gst;
    @SerializedName("packs")           private List<PackModel> packs;
    @SerializedName("tags")            private List<String> tags;
    @SerializedName("j_images")        private List<String> images;

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getRandom()          { return random; }
    public String getPackId()          { return packId; }
    public String getItemId()          { return itemId; }
    public String getItemCode()        { return itemCode; }
    public String getItemName()        { return itemName; }
    public String getShade()           { return shade; }
    public String getDescription()     { return description; }
    public String getFashion()         { return fashion; }
    public String getStyle()           { return style; }
    public String getUom()             { return uom; }
    public String getHsn()             { return hsn; }
    public String getMaterial()        { return material; }
    public String getFabric()          { return fabric; }
    public String getNeck()            { return neck; }
    public String getSleeve()          { return sleeve; }
    public String getSeason()          { return season; }
    public String getColor()           { return color; }
    public String getSupplier()        { return supplier; }
    public String getFit()             { return fit; }
    public String getDescription2()    { return description2; }
    public String getMoq()             { return moq; }
    public String getCategoryId()      { return categoryId; }
    public String getCategoryName()    { return categoryName; }
    public String getSubCategoryName() { return subCategoryName; }
    public String getGst()             { return gst; }
    public List<PackModel> getPacks()  { return packs; }
    public List<String> getTags()      { return tags; }
    public List<String> getImages()    { return images; }

    // ── Nested: PackModel ─────────────────────────────────────────────────────
    public static class PackModel {
        @SerializedName("n_pack_id")       private int packId;
        @SerializedName("c_pack_name")     private String packName;
        @SerializedName("n_mrp")           private double mrp;
        @SerializedName("n_selling_price") private double sellingPrice;
        @SerializedName("n_gst_amount")    private double gstAmount;
        @SerializedName("n_total_price")   private double totalPrice;
        @SerializedName("n_gst")           private double gst;
        @SerializedName("n_discount")      private int discount;

        public int    getPackId()       { return packId; }
        public String getPackName()     { return packName; }
        public double getMrp()          { return mrp; }
        public double getSellingPrice() { return sellingPrice; }
        public double getGstAmount()    { return gstAmount; }
        public double getTotalPrice()   { return totalPrice; }
        public double getGst()          { return gst; }
        public int    getDiscount()     { return discount; }
    }
}