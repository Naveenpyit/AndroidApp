package com.example.myapplication.utils;

import com.example.myapplication.model.CartModel;
import com.example.myapplication.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartModel> items = new ArrayList<>();

    private CartManager() {
    }

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void addToCart(ProductModel product) {
        for (CartModel item : items) {
            if (item.getItemId() != null && item.getItemId().equals(product.getItemId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        items.add(new CartModel(product));
    }

    public void addOrUpdate(ProductModel product) {
        addToCart(product);
    }

    public void remove(CartModel item) {
        items.remove(item);
    }

    public void removeById(String itemId) {
        for (CartModel item : items) {
            if (item.getItemId() != null && item.getItemId().equals(itemId)) {
                items.remove(item);
                return;
            }
        }
    }

    public List<CartModel> getItems() {
        return items;
    }

    public ArrayList<CartModel> getCartItems() {
        return new ArrayList<>(items);
    }

    public int getTotalCount() {
        int count = 0;
        for (CartModel i : items) count += i.getQuantity();
        return count;
    }

    public int getQtyForItem(String itemId) {
        if (itemId == null) return 0;
        for (CartModel item : items) {
            if (itemId.equals(item.getItemId())) return item.getQuantity();
        }
        return 0;
    }

    public double getSubtotal() {
        double total = 0;
        for (CartModel i : items) {
            if (i.isSelected()) {
                total += i.getPrice() * i.getQuantity();
            }
        }
        return total;
    }

    public double getGstTotal() {
        double total = 0;
        for (CartModel i : items) {
            if (i.isSelected()) {
                total += i.getGstAmount() * i.getQuantity();
            }
        }
        return total;
    }

    public double getSelectedSubtotal() {
        return getSubtotal();
    }

    public double getShipping() {
        return 0;
    }

    public double getGrandTotal() {
        return getSubtotal() + getGstTotal() + getShipping();
    }

    public int getSelectedCount() {
        int c = 0;
        for (CartModel i : items) {
            if (i.isSelected()) c++;
        }
        return c;
    }

    public void selectAll(boolean select) {
        for (CartModel i : items) {
            i.setSelected(select);
        }
    }

    public boolean isAllSelected() {
        for (CartModel i : items) {
            if (!i.isSelected()) return false;
        }
        return !items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public void clearCart() {
        items.clear();
    }
}