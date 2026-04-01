package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class WishlistManager {

    private static final String PREF_NAME     = "WishlistPrefs";
    private static final String KEY_IDS       = "wishlist_ids";
    private static final String PREFIX_WISHID = "wish_";
    private static final String PREFIX_PACKID = "pack_";

    private static volatile WishlistManager instance;
    private final SharedPreferences prefs;

    private WishlistManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static WishlistManager getInstance(Context context) {
        if (instance == null) {
            synchronized (WishlistManager.class) {
                if (instance == null) {
                    instance = new WishlistManager(context);
                }
            }
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────
    // ADD
    // ─────────────────────────────────────────────────────────────
    public void addWishlist(String productId, String wishlistId) {
        addWishlist(productId, wishlistId, null);
    }

    public void addWishlist(String productId, String wishlistId, String packId) {
        Set<String> ids = new HashSet<>(getProductIds()); // ✅ COPY

        ids.add(productId);

        prefs.edit()
                .putStringSet(KEY_IDS, ids)
                .putString(PREFIX_WISHID + productId, wishlistId)
                .apply();

        if (packId != null) {
            prefs.edit()
                    .putString(PREFIX_PACKID + productId, packId)
                    .apply();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // REMOVE
    // ─────────────────────────────────────────────────────────────
    public void removeWishlist(String productId) {
        Set<String> ids = new HashSet<>(getProductIds()); // ✅ COPY

        ids.remove(productId);

        prefs.edit()
                .putStringSet(KEY_IDS, ids)
                .remove(PREFIX_WISHID + productId)
                .remove(PREFIX_PACKID + productId)
                .apply();
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────────────
    public boolean isWishlisted(String productId) {
        return getProductIds().contains(productId);
    }

    public String getWishlistId(String productId) {
        return prefs.getString(PREFIX_WISHID + productId, null);
    }

    public String getPackId(String productId) {
        return prefs.getString(PREFIX_PACKID + productId, null);
    }

    public int getWishlistCount() {
        return getProductIds().size();
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL SAFE FETCH
    // ─────────────────────────────────────────────────────────────
    private Set<String> getProductIds() {
        return new HashSet<>(prefs.getStringSet(KEY_IDS, new HashSet<>())); // ✅ SAFE COPY
    }

    // ─────────────────────────────────────────────────────────────
    // CLEAR (Optional)
    // ─────────────────────────────────────────────────────────────
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}