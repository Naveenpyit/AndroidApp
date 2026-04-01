package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CartModel {

    @SerializedName("n_id")          private String cartId;
    @SerializedName("user_id")       private String userId;
    @SerializedName("n_category")    private String category;
    @SerializedName("n_product")     private String product;
    @SerializedName("n_pack")        private String pack;
    @SerializedName("n_mrp")         private String mrp;
    @SerializedName("category_name") private String categoryName;
    @SerializedName("n_price")       private String price;
    @SerializedName("n_quantity")    private String quantity;
    @SerializedName("n_total")       private String total;
    @SerializedName("c_random")      private String random;
    @SerializedName("c_item_name")   private String name;
    @SerializedName("n_gst")         private String gst;
    @SerializedName("dt_created")    private String createdAt;
    @SerializedName("c_item_code")   private String itemCode;
    @SerializedName("c_pack_name")   private String packName;
    @SerializedName("c_image")       private String imageUrl;
    @SerializedName("n_gst_value")   private String gstValue;

    // UI-only state (not from API)
    private boolean selected  = true;
    private int     uiQty     = 1; // live qty shown in UI before confirm

    // ── Getters ───────────────────────────────────────────────────────────────
    public String  getCartId()      { return cartId     != null ? cartId     : ""; }
    public String  getUserId()      { return userId     != null ? userId     : ""; }
    public String  getCategory()    { return category   != null ? category   : ""; }
    public String  getProduct()     { return product    != null ? product    : ""; }
    public String  getPack()        { return pack       != null ? pack       : ""; }
    public String  getMrp()         { return mrp        != null ? mrp        : "0"; }
    public String  getCategoryName(){ return categoryName != null ? categoryName : ""; }
    public String  getPrice()       { return price      != null ? price      : "0"; }
    public String  getQuantity()    { return quantity   != null ? quantity   : "1"; }
    public String  getTotal()       { return total      != null ? total      : "0"; }
    public String  getRandom()      { return random     != null ? random     : ""; }
    public String  getName()        { return name       != null ? name       : ""; }
    public String  getGst()         { return gst        != null ? gst        : "0"; }
    public String  getItemCode()    { return itemCode   != null ? itemCode   : ""; }
    public String  getPackName()    { return packName   != null ? packName   : ""; }
    public String  getImageUrl()    { return imageUrl   != null ? imageUrl   : ""; }
    public String  getGstValue()    { return gstValue   != null ? gstValue   : "0"; }
    public boolean isSelected()     { return selected; }
    public int     getUiQty()       {
        if (uiQty <= 0) {
            try { uiQty = Integer.parseInt(getQuantity()); }
            catch (Exception e) { uiQty = 1; }
        }
        return uiQty;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setSelected(boolean v) { selected = v; }
    public void setUiQty(int v)        { uiQty = Math.max(1, v); }
    public void setQuantity(String v)  { quantity = v; }

    // ── Calculated helpers ────────────────────────────────────────────────────
    public double getPriceDouble()    {
        try { return Double.parseDouble(price); }   catch (Exception e) { return 0; }
    }
    public double getMrpDouble()      {
        try { return Double.parseDouble(mrp); }     catch (Exception e) { return 0; }
    }
    public double getGstDouble()      {
        try { return Double.parseDouble(gstValue); }catch (Exception e) { return 0; }
    }
    public double getGstAmount()      { return getGstDouble(); }

    /** price × qty */
    public double getLineTotal()   { return getPriceDouble() * getUiQty(); }

    /** (price + gst) × qty */
    public double getGrandTotal()  { return (getPriceDouble() + getGstDouble()) * getUiQty(); }

    /** MRP × qty */
    public double getLineMrp()     { return getMrpDouble() * getUiQty(); }

    /** Savings vs MRP */
    public double getSavings()     { return getLineMrp() - getLineTotal(); }

    public String getSku()         { return itemCode != null ? itemCode : ""; }
}