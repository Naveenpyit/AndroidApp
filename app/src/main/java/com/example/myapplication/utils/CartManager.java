package com.example.myapplication.utils;

import com.example.myapplication.model.CartModel;

import java.util.ArrayList;

public class CartManager {

    private static CartManager instance;
    private final ArrayList<CartModel> items = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    /** Replace list with fresh API data */
    public void setCartItems(ArrayList<CartModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
            for (CartModel m : items) {
                m.setSelected(true);
                // sync uiQty from API quantity
                try { m.setUiQty(Integer.parseInt(m.getQuantity())); }
                catch (Exception e) { m.setUiQty(1); }
            }
        }
    }

    public ArrayList<CartModel> getCartItems() { return items; }

    public int getTotalCount() { return items.size(); }

    public void selectAll(boolean select) {
        for (CartModel m : items) m.setSelected(select);
    }

    public boolean isAllSelected() {
        if (items.isEmpty()) return false;
        for (CartModel m : items) if (!m.isSelected()) return false;
        return true;
    }

    public int getSelectedCount() {
        int c = 0;
        for (CartModel m : items) if (m.isSelected()) c++;
        return c;
    }

    public double getSubtotal() {
        double t = 0;
        for (CartModel m : items) if (m.isSelected()) t += m.getLineTotal();
        return t;
    }

    public double getGstTotal() {
        double t = 0;
        for (CartModel m : items) if (m.isSelected()) t += m.getGstDouble() * m.getUiQty();
        return t;
    }

    public double getGrandTotal()  { return getSubtotal() + getGstTotal(); }
    public double getMrpTotal()    {
        double t = 0;
        for (CartModel m : items) if (m.isSelected()) t += m.getLineMrp();
        return t;
    }
    public double getSavings()     { return getMrpTotal() - getSubtotal(); }
    public double getShipping()    { return 0; }

    public void clearCart() { items.clear(); }
}