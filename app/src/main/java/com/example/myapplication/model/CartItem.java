package com.example.myapplication.model;

public class CartItem {

    private String itemId;
    private String name;
    private String imageUrl;
    private double price;
    private int quantity;
    private boolean selected;

    public CartItem(ProductModel product) {
        this.itemId = product.getItemId();
        this.name = product.getName();
        this.imageUrl = product.getImageUrl();
        this.price = Double.parseDouble(product.getSellingPrice() != null ? product.getSellingPrice() : "0");
        this.quantity = 1;
        this.selected = true;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public double getLineTotal() {
        return price * quantity;
    }
}