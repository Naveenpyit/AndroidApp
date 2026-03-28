package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ProductOrdermodel {

    @SerializedName("c_item_name")   private String name;
    @SerializedName("c_random")      private String random;
    @SerializedName("id")            private String itemId;
    @SerializedName("n_item_id")     private String nItemId;
    @SerializedName("n_category")    private String category;
    @SerializedName("c_category")    private String categoryName;
    @SerializedName("n_selling_price") private String sellingPrice;
    @SerializedName("n_mrp")         private String mrp;
    @SerializedName("n_discount")    private String discount;
    @SerializedName("n_gst")         private String gst;
    @SerializedName("n_gst_amount")  private String gstAmount;
    @SerializedName("n_total_price") private String totalPrice;
    @SerializedName("n_moq")         private String moq;
    @SerializedName("n_size_count")  private String sizeCount;
    @SerializedName("n_pack_id")     private String packId;
    @SerializedName("n_cart_qty")    private String cartQty;
    @SerializedName("n_cart_id")     private String cartId;
    @SerializedName("n_wishlist_id") private String wishlistId;
    @SerializedName("c_image_url")   private String imageUrl;

    private boolean wishlisted = false;

    public String getName()          { return name; }
    public String getRandom()        { return random; }
    public String getItemId()        { return itemId != null ? itemId : nItemId; }
    public String getCategory()      { return category; }
    public String getCategoryName()  { return categoryName; }
    public String getSellingPrice()  { return sellingPrice; }
    public String getMrp()           { return mrp; }
    public String getDiscount()      { return discount; }
    public String getGst()           { return gst; }
    public String getMoq()           { return moq; }
    public String getSizeCount()     { return sizeCount; }
    public String getPackId()        { return packId; }
    public String getCartQty()       { return cartQty; }
    public String getCartId()        { return cartId; }
    public String getWishlistId()    { return wishlistId; }
    public String getImageUrl()      { return imageUrl; }
    public boolean isWishlisted()    { return wishlistId != null || wishlisted; }
    public void setWishlisted(boolean w) { this.wishlisted = w; }
    public String getSubtitle()      { return categoryName != null ? categoryName : ""; }
    public String getMargin()        { return discount != null ? discount : "0"; }
}