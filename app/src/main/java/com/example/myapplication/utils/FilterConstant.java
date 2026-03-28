package com.example.myapplication.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterConstant {

    // ── Price Ranges ──────────────────────────────────────────────────────────

    public static final Map<String, String> PRICE_RANGES = new HashMap<>();

    static {
        PRICE_RANGES.put("price_0_500", "Under ₹500");
        PRICE_RANGES.put("price_500_1000", "₹500 - ₹1000");
        PRICE_RANGES.put("price_1000_2000", "₹1000 - ₹2000");
        PRICE_RANGES.put("price_2000_5000", "₹2000 - ₹5000");
        PRICE_RANGES.put("price_5000_10000", "₹5000 - ₹10000");
        PRICE_RANGES.put("price_10000_plus", "Above ₹10000");
    }

    // ── Sort Options ──────────────────────────────────────────────────────────

    public static final List<String> SORT_OPTIONS = Arrays.asList(
            "Relevance",
            "Price: Low to High",
            "Price: High to Low",
            "Newest First",
            "Most Popular",
            "Best Selling"
    );

    public static final Map<String, String> SORT_MAP = new HashMap<>();

    static {
        SORT_MAP.put("Relevance", "relevance");
        SORT_MAP.put("Price: Low to High", "price_asc");
        SORT_MAP.put("Price: High to Low", "price_desc");
        SORT_MAP.put("Newest First", "newest");
        SORT_MAP.put("Most Popular", "popular");
        SORT_MAP.put("Best Selling", "best_selling");
    }

    // ── Fabric Types ──────────────────────────────────────────────────────────

    public static final Map<String, String> FABRIC_TYPES = new HashMap<>();

    static {
        FABRIC_TYPES.put("fabric_cotton", "Cotton");
        FABRIC_TYPES.put("fabric_polyester", "Polyester");
        FABRIC_TYPES.put("fabric_silk", "Silk");
        FABRIC_TYPES.put("fabric_wool", "Wool");
        FABRIC_TYPES.put("fabric_linen", "Linen");
        FABRIC_TYPES.put("fabric_denim", "Denim");
        FABRIC_TYPES.put("fabric_velvet", "Velvet");
        FABRIC_TYPES.put("fabric_leather", "Leather");
        FABRIC_TYPES.put("fabric_rayon", "Rayon");
        FABRIC_TYPES.put("fabric_spandex", "Spandex");
    }

    // ── Style Types ───────────────────────────────────────────────────────────

    public static final Map<String, String> STYLE_TYPES = new HashMap<>();

    static {
        STYLE_TYPES.put("style_casual", "Casual");
        STYLE_TYPES.put("style_formal", "Formal");
        STYLE_TYPES.put("style_semiformal", "Semi-Formal");
        STYLE_TYPES.put("style_sports", "Sports");
        STYLE_TYPES.put("style_partyware", "Party Wear");
        STYLE_TYPES.put("style_ethnic", "Ethnic");
        STYLE_TYPES.put("style_traditional", "Traditional");
        STYLE_TYPES.put("style_western", "Western");
        STYLE_TYPES.put("style_vintage", "Vintage");
        STYLE_TYPES.put("style_contemporary", "Contemporary");
    }

    // ── Fit Types ─────────────────────────────────────────────────────────────

    public static final Map<String, String> FIT_TYPES = new HashMap<>();

    static {
        FIT_TYPES.put("fit_slim", "Slim Fit");
        FIT_TYPES.put("fit_regular", "Regular Fit");
        FIT_TYPES.put("fit_loose", "Loose Fit");
        FIT_TYPES.put("fit_tight", "Tight Fit");
        FIT_TYPES.put("fit_oversized", "Oversized");
    }

    // ── Pack Types ────────────────────────────────────────────────────────────

    public static final Map<String, String> PACK_TYPES = new HashMap<>();

    static {
        PACK_TYPES.put("pack_single", "Single");
        PACK_TYPES.put("pack_double", "Double");
        PACK_TYPES.put("pack_triple", "Triple");
        PACK_TYPES.put("pack_bulk", "Bulk");
    }

    // ── Get Display Values ────────────────────────────────────────────────────

    public static String getPriceDisplay(String id) {
        return PRICE_RANGES.getOrDefault(id, id);
    }

    public static String getFabricDisplay(String id) {
        return FABRIC_TYPES.getOrDefault(id, id);
    }

    public static String getStyleDisplay(String id) {
        return STYLE_TYPES.getOrDefault(id, id);
    }

    public static String getFitDisplay(String id) {
        return FIT_TYPES.getOrDefault(id, id);
    }

    public static String getPackDisplay(String id) {
        return PACK_TYPES.getOrDefault(id, id);
    }
}