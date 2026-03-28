package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CartItemModel {

    @SerializedName("n_id")
    private String cartId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("n_category")
    private String category;

    @SerializedName("n_product")
    private String productId;

    @SerializedName("n_pack")
    private String pack;

    @SerializedName("n_mrp")
    private String mrp;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("n_price")
    private String price;

    @SerializedName("n_quantity")
    private String quantity;

    @SerializedName("n_total")
    private String total;

    @SerializedName("c_random")
    private String random;

    @SerializedName("c_item_name")
    private String itemName;

    @SerializedName("n_gst")
    private String gst;

    @SerializedName("dt_created")
    private String created;

    @SerializedName("c_item_code")
    private String itemCode;

    @SerializedName("c_pack_name")
    private String packName;

    @SerializedName("c_image")
    private String image;

    @SerializedName("n_gst_value")
    private String gstValue;

    // Local override for quantity updates
    private int localQtyOverride = -1;

    // ── Getters ──
    public String getCartId() {
        return cartId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public String getProductId() {
        return productId;
    }

    public String getPack() {
        return pack;
    }

    public String getMrp() {
        return mrp;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public int getQuantityInt() {
        if (localQtyOverride > -1) {
            return localQtyOverride;
        }
        try {
            return Integer.parseInt(quantity);
        } catch (Exception e) {
            return 1;
        }
    }

    public String getTotal() {
        return total;
    }

    public double getLineTotal() {
        try {
            return Double.parseDouble(total);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getRandom() {
        return random;
    }

    public String getItemName() {
        return itemName;
    }

    public String getGst() {
        return gst;
    }

    public String getCreated() {
        return created;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getPackName() {
        return packName;
    }

    public String getImage() {
        return image;
    }

    public String getGstValue() {
        return gstValue;
    }

    public double getGstValueDouble() {
        try {
            return Double.parseDouble(gstValue);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getGrandTotal() {
        try {
            double lineTotal = Double.parseDouble(total);
            double gstAmount = getGstValueDouble() * getQuantityInt();
            return lineTotal + gstAmount;
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Setters ──
    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setLocalQty(int qty) {
        this.localQtyOverride = qty;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setGstValue(String gstValue) {
        this.gstValue = gstValue;
    }

    @Override
    public String toString() {
        return "CartItemModel{" +
                "cartId='" + cartId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity='" + quantity + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}