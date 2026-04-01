package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ProductModel {

    @SerializedName("c_item_name")
    private String name;

    @SerializedName("n_pack_id")
    private String packid;

    @SerializedName("c_random")
    private String random;

    @SerializedName("id")
    private String itemId;

    @SerializedName("n_category")
    private String category;

    @SerializedName("c_category")
    private String categoryName;

    @SerializedName("n_selling_price")
    private String sellingPrice;

    @SerializedName("n_mrp")
    private String mrp;

    @SerializedName("n_discount")
    private String discount;

    @SerializedName("n_gst")
    private String gstno;

    @SerializedName("n_gst_amount")
    private String gstAmount;

    @SerializedName("n_total_price")
    private String totalPrice;

    @SerializedName("n_moq")
    private String moq;

    @SerializedName("n_size_count")
    private String sizeCount;

    @SerializedName("n_cart_qty")
    private String cartQty;

    @SerializedName("n_card_id")
    private String cardid;

    @SerializedName("n_wishlist_id")
    private String wishlistId;

    @SerializedName("c_image_url")
    private String imageUrl;

    // ── Filter Fields ─────────────────────────────────────────────────────────

    @SerializedName("c_fabric")
    private String fabric;

    @SerializedName("c_style")
    private String style;

    @SerializedName("c_pack")
    private String pack;

    @SerializedName("c_fit")
    private String fit;

    @SerializedName("c_main")
    private String main;

    @SerializedName("n_section")
    private String section;

    @SerializedName("c_tag")
    private String tag;

    @SerializedName("n_price_range")
    private String priceRange;

    // Non-serialized fields
    private boolean wishlisted = false;

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getName()         { return name; }
    public String getRandom()       { return random; }
    public String getItemId()       { return itemId; }
    public String getCategory()     { return category; }
    public String getCategoryName() { return categoryName; }
    public String getSellingPrice() { return sellingPrice; }
    public String getMrp()          { return mrp; }
    public String getDiscount()     { return discount; }
    public String getGst()          { return gstno; }
    public String getGstAmount()    { return gstAmount; }
    public String getTotalPrice()   { return totalPrice; }
    public String getMoq()          { return moq; }
    public String getSizeCount()    { return sizeCount; }
    public String getCartQty()      { return cartQty; }
    public String getWishlistId()   { return wishlistId; }
    public String getImageUrl()     { return imageUrl; }
    public boolean isWishlisted()   { return wishlisted; }
    public String getPackId()       { return packid; }
    public String getCardid()       { return cardid; }

    // ── Filter Getters ────────────────────────────────────────────────────────

    public String getFabric()       { return fabric; }
    public String getStyle()        { return style; }
    public String getPack()         { return pack; }
    public String getFit()          { return fit; }
    public String getMain()         { return main; }
    public String getSection()      { return section; }
    public String getTag()          { return tag; }
    public String getPriceRange()   { return priceRange; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setWishlisted(boolean wishlisted) { this.wishlisted = wishlisted; }
    public void setWishlistId(String wishlistId) { this.wishlistId = wishlistId; }
    public void setName(String name) { this.name = name; }
    public void setSellingPrice(String sellingPrice) { this.sellingPrice = sellingPrice; }
    public void setMrp(String mrp) { this.mrp = mrp; }
    public void setDiscount(String discount) { this.discount = discount; }
    public void setCartQty(String cartQty) { this.cartQty = cartQty; }

    // ── Filter Setters ────────────────────────────────────────────────────────

    public void setFabric(String fabric) { this.fabric = fabric; }
    public void setStyle(String style) { this.style = style; }
    public void setPack(String pack) { this.pack = pack; }
    public void setFit(String fit) { this.fit = fit; }
    public void setMain(String main) { this.main = main; }
    public void setSection(String section) { this.section = section; }
    public void setTag(String tag) { this.tag = tag; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }

    // ── Helper Methods ────────────────────────────────────────────────────────

    public String getSubtitle()  { return categoryName != null ? categoryName : ""; }
    public String getBuyPrice()  { return sellingPrice != null ? sellingPrice : "0"; }
    public String getMargin()    { return discount != null ? discount : "0"; }
}