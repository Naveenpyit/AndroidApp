package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WishlistManager {
    private static final String PREF_NAME = "WISHLIST_PREF";
    private static final String WISHLIST_KEY = "wishlist_map";

    private static WishlistManager instance;
    private final SharedPreferences pref;
    private final Gson gson;
    private Map<String, String> wishlistMap; // productId -> wishlistId

    private WishlistManager(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadWishlist();
    }

    public static synchronized WishlistManager getInstance(Context context) {
        if (instance == null) {
            instance = new WishlistManager(context);
        }
        return instance;
    }

    private void loadWishlist() {
        String json = pref.getString(WISHLIST_KEY, "{}");
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        wishlistMap = gson.fromJson(json, type);
        if (wishlistMap == null) wishlistMap = new HashMap<>();
    }
    private void saveWishlist() {
        String json = gson.toJson(wishlistMap);
        pref.edit().putString(WISHLIST_KEY, json).apply();
    }
    public void addWishlist(String productId, String wishlistId) {
        wishlistMap.put(productId, wishlistId);
        saveWishlist();
    }

    public void removeWishlist(String productId) {
        wishlistMap.remove(productId);
        saveWishlist();
    }

    public boolean isWishlisted(String productId) {
        return wishlistMap.containsKey(productId);
    }

    public String getWishlistId(String productId) {
        return wishlistMap.getOrDefault(productId, "");
    }
    public int getWishlistCount() {
        return wishlistMap.size();
    }


    public void clearAll() {
        wishlistMap.clear();
        saveWishlist();
    }
}