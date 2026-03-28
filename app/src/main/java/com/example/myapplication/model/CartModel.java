package com.example.myapplication.model;

public class CartModel {

    private String itemId;
    private String name;
    private String pack;
    private String category;
    private String sku;
    private String imageUrl;
    private double price;
    private double gst;
    private double gstAmount;
    private int quantity;
    private boolean selected;

    public CartModel() {
    }

    public CartModel(ProductModel product) {
        this.itemId = product.getItemId();
        this.name = product.getName();
        this.pack = product.getPack();
        this.category = product.getCategoryName();
        this.sku = product.getRandom();
        this.imageUrl = product.getImageUrl();
        this.price = Double.parseDouble(product.getSellingPrice() != null ? product.getSellingPrice() : "0");
        this.gst = Double.parseDouble(product.getGst() != null ? product.getGst() : "0");
        this.gstAmount = Double.parseDouble(product.getGstAmount() != null ? product.getGstAmount() : "0");
        this.quantity = 1;
        this.selected = true;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getGst() {
        return gst;
    }

    public void setGst(double gst) {
        this.gst = gst;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public double getLineTotal() {
        return price * quantity;
    }

    public double getGrandTotal() {
        return (price * quantity) + (gstAmount * quantity);
    }

    public double getSubtotal() {
        return price * quantity;
    }
}